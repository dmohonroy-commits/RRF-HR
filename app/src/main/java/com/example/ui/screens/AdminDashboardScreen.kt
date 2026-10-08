package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgreementEntity
import com.example.ui.MainViewModel
import com.example.ui.components.ShareSheetDialog
import com.example.ui.theme.*
import com.example.util.BanglaTextValidator
import com.example.util.BranchReportExporter
import com.example.util.BranchReportPdfGenerator
import com.example.util.ExcelExporter
import com.example.util.HtmlExporter
import com.example.util.JsonDataBackupManager
import com.example.util.PdfGenerator
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    agreements: List<AgreementEntity>,
    searchQuery: String,
    selectedDesignationFilter: String,
    selectedDateFilter: String,
    selectedDocumentCategoryFilter: String = "সকল",
    onLogout: () -> Unit,
    onViewAgreement: (AgreementEntity) -> Unit,
    onEditAgreement: (AgreementEntity) -> Unit,
    onNewAgreement: () -> Unit
) {
    val context = LocalContext.current
    var agreementToDelete by remember { mutableStateOf<AgreementEntity?>(null) }
    var agreementToShare by remember { mutableStateOf<AgreementEntity?>(null) }
    var agreementToDispatch by remember { mutableStateOf<AgreementEntity?>(null) }

    val isBackupExporting by viewModel.isBackupExporting.collectAsState()
    val lastExportResult by viewModel.lastExportResult.collectAsState()
    val importSummary by viewModel.importSummary.collectAsState()
    val isRestoring by viewModel.isRestoring.collectAsState()
    val restoreResult by viewModel.restoreResult.collectAsState()

    var selectedRestoreMode by remember { mutableStateOf("MERGE") }
    var shouldRestoreDrafts by remember { mutableStateOf(true) }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.inspectBackupUri(context, uri)
        }
    }

    val importGetContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.inspectBackupUri(context, uri)
        }
    }

    // Filtered Agreements
    val filteredAgreements = remember(
        agreements, searchQuery, selectedDesignationFilter, selectedDateFilter, selectedDocumentCategoryFilter
    ) {
        agreements.filter { item ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.employeeName.contains(searchQuery, ignoreCase = true) ||
                        item.serialNo.contains(searchQuery, ignoreCase = true) ||
                        item.guarantorNid.contains(searchQuery, ignoreCase = true) ||
                        item.employeeDistrict.contains(searchQuery, ignoreCase = true) ||
                        item.guarantorName.contains(searchQuery, ignoreCase = true) ||
                        item.assignedBranch.contains(searchQuery, ignoreCase = true)
            }
            val matchesDesig = if (selectedDesignationFilter == "সকল") true else {
                item.designation == selectedDesignationFilter
            }
            val matchesDate = if (selectedDateFilter == "সকল") true else {
                item.submissionDate == selectedDateFilter
            }
            val matchesCategory = when (selectedDocumentCategoryFilter) {
                "১০০ টাকার স্ট্যাম্প" -> true
                "২৫ টাকার প্রত্যয়ন" -> true
                "তথ্য যাচাই" -> true
                else -> true
            }
            matchesSearch && matchesDesig && matchesDate && matchesCategory
        }
    }

    // Date-wise breakdown calculation adapted to document category
    val dateWiseSubmissions = remember(agreements, selectedDocumentCategoryFilter) {
        agreements.groupBy { it.submissionDate }
            .mapValues { entry -> entry.value.size }
            .toList()
            .sortedByDescending { it.first }
    }

    // Branch summaries calculation
    val branchSummaries = remember(agreements) {
        BranchReportExporter.calculateBranchSummaries(agreements)
    }

    // Unique dates for filter
    val availableDates = remember(agreements) {
        listOf("সকল") + agreements.map { it.submissionDate }.distinct()
    }

    // Stats
    val totalCount = agreements.size
    val accountsCount = agreements.count { it.designation == "অফিসার (অ্যাকাউন্টস)" }
    val loanCount = agreements.count { it.designation == "অফিসার (ঋণ)" }
    val staffCount = agreements.count { it.designation == "সার্ভিস স্টাফ" }
    val printedCount = agreements.count { it.isPrinted }
    val pendingCount = totalCount - printedCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RRF HR অ্যাডমিন প্যানেল",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF059669)
                        )
                        Text(
                            text = "মোট সংরক্ষিত এগ্রিমেন্ট: $totalCount টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF065F46)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.exportLocalDataToJson(context)
                        },
                        modifier = Modifier.testTag("admin_top_export_json_btn")
                    ) {
                        if (isBackupExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF059669)
                            )
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = "JSON Backup Export", tint = Color(0xFF059669))
                        }
                    }
                    IconButton(
                        onClick = {
                            try {
                                importFileLauncher.launch(arrayOf("application/json", "application/*", "text/*", "*/*"))
                            } catch (_: Exception) {
                                importGetContentLauncher.launch("*/*")
                            }
                        },
                        modifier = Modifier.testTag("admin_top_import_json_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "JSON Backup Import/Restore", tint = Color(0xFF059669))
                    }
                    IconButton(
                        onClick = {
                            if (agreements.isEmpty()) {
                                Toast.makeText(context, "কোনো এগ্রিমেন্ট ডাটা নেই", Toast.LENGTH_SHORT).show()
                            } else {
                                val excelFile = ExcelExporter.exportToExcelCsv(context, agreements)
                                ShareHelper.openFile(context, excelFile)
                            }
                        },
                        modifier = Modifier.testTag("admin_export_excel_btn")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "Excel Export", tint = Color(0xFF059669))
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color(0xFF059669))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F5E9),
                    titleContentColor = Color(0xFF059669)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewAgreement,
                containerColor = LightGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("admin_fab_new_agreement")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Agreement")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Section
            item {
                Text(
                    "ড্যাশবোর্ড সামারি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "মোট এগ্রিমেন্ট",
                        value = totalCount.toString(),
                        color = NavyPrimary,
                        containerColor = NavyContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "স্ট্যাম্পে প্রিন্ট সম্পন্ন",
                        value = printedCount.toString(),
                        color = LightGreenDark,
                        containerColor = LightGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "প্রিন্ট অপেক্ষমান",
                        value = pendingCount.toString(),
                        color = Color(0xFFB78103),
                        containerColor = Color(0xFFFFF3CD),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatChip("অ্যাকাউন্টস: $accountsCount", modifier = Modifier.weight(1f))
                    MiniStatChip("ঋণ অফিসার: $loanCount", modifier = Modifier.weight(1f))
                    MiniStatChip("সার্ভিস স্টাফ: $staffCount", modifier = Modifier.weight(1f))
                }
            }

            // =========================================================
            // DATE-WISE SUBMISSIONS SUMMARY
            // =========================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_date_wise_summary_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE3F2FD),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = NavyPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "তারিখ ভিত্তিক ফরম পূরণের হিসাব",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "কোন তারিখে কতটি এগ্রিমেন্ট ফরম পূরণ হয়েছে",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        if (dateWiseSubmissions.isEmpty()) {
                            Text("এখনো কোনো তারিখের ডাটা নেই", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dateWiseSubmissions.forEach { (dateStr, count) ->
                                    val isSelected = selectedDateFilter == dateStr
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            if (isSelected) {
                                                viewModel.onDateFilterChange("সকল")
                                            } else {
                                                viewModel.onDateFilterChange(dateStr)
                                            }
                                        }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                dateStr,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE8F5E9)
                                            ) {
                                                Text(
                                                    "${BanglaTextValidator.toBanglaDigits(count.toString())} টি ফরম",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF059669),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================
            // BRANCH DISPATCH & VERIFICATION SUMMARY & REPORTS
            // =========================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_branch_reports_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF3E0),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Apartment,
                                            contentDescription = null,
                                            tint = Color(0xFFE65100),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "শাখা ভিত্তিক ডিসপ্যাচ ও তথ্য যাচাই",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "কোন শাখায় কতটি ফরম পাঠানো ও তথ্য যাচাই করা হয়েছে",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        if (branchSummaries.isEmpty()) {
                            Text(
                                "এখনো কোনো শাখা অফিসে ফরম পাঠানো হয়নি। নিচের এগ্রিমেন্ট কার্ড থেকে 'শাখায় পাঠান' অপশনে ক্লিক করুন।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                branchSummaries.forEach { b ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(b.branchName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("প্রেশনের তারিখ: ${b.dispatchDates}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(shape = RoundedCornerShape(6.dp), color = NavyContainer) {
                                                    Text("মোট: ${b.totalDispatched}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                                }
                                                Surface(shape = RoundedCornerShape(6.dp), color = LightGreenContainer) {
                                                    Text("যাচাই: ${b.verifiedCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LightGreenDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                                }
                                                if (b.pendingCount > 0) {
                                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFF3CD)) {
                                                        Text("বাকী: ${b.pendingCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF856404), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Download Branch Report Buttons (Excel & PDF)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val csvFile = BranchReportExporter.exportBranchReportToCsv(context, agreements)
                                    ShareHelper.openFile(context, csvFile)
                                },
                                modifier = Modifier.weight(1f).testTag("admin_export_branch_excel_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("শাখা এক্সেল রিপোর্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val pdfFile = BranchReportPdfGenerator.generateBranchReportPdf(context, agreements)
                                    ShareHelper.openFile(context, pdfFile)
                                },
                                modifier = Modifier.weight(1f).testTag("admin_export_branch_pdf_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("শাখা পিডিএফ রিপোর্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // =========================================================
            // DATA PORTABILITY & FULL LOCAL BACKUP (JSON EXPORT/IMPORT)
            // =========================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_data_portability_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "ডাটা পোর্টেবিলিটি ও সম্পূর্ণ ব্যাকআপ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "সকল এগ্রিমেন্ট, ঠিকানা ও ড্রাফট JSON ফাইলে এক্সপোর্ট বা রিস্টোর করুন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Primary Action Buttons (JSON Export & Import)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // JSON Export Button
                            Button(
                                onClick = {
                                    viewModel.exportLocalDataToJson(context)
                                },
                                enabled = !isBackupExporting,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_export_json_card_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                if (isBackupExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("তৈরি হচ্ছে...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("JSON এক্সপোর্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // JSON Import / Restore Button
                            Button(
                                onClick = {
                                    try {
                                        importFileLauncher.launch(arrayOf("application/json", "application/*", "text/*", "*/*"))
                                    } catch (_: Exception) {
                                        importGetContentLauncher.launch("*/*")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_import_json_card_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("JSON রিস্টোর", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Secondary Options (Excel CSV and Email/Drive Backup)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (agreements.isEmpty()) {
                                        Toast.makeText(context, "কোনো এগ্রিমেন্ট ডাটা নেই", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val excelFile = ExcelExporter.exportToExcelCsv(context, agreements)
                                        ShareHelper.fallbackShare(context, excelFile, "RRF HR এগ্রিমেন্ট তালিকা (Excel CSV)")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, Modifier.size(14.dp), tint = Color(0xFF059669))
                                Spacer(Modifier.width(4.dp))
                                Text("এক্সেল ডাউনলোড", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (agreements.isEmpty()) {
                                        Toast.makeText(context, "কোনো এগ্রিমেন্ট ডাটা নেই", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val excelFile = ExcelExporter.exportToExcelCsv(context, agreements)
                                        ShareHelper.shareViaEmail(
                                            context = context,
                                            file = excelFile,
                                            recipientEmail = ShareHelper.DEFAULT_BACKUP_EMAIL,
                                            subject = "RRF HR এগ্রিমেন্ট ডাটা ব্যাকআপ",
                                            body = "সম্মানিত কর্তৃপক্ষ,\nসকল এগ্রিমেন্টের এক্সেল ব্যাকআপ ফাইল সংযুক্ত করা হলো।"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Mail, contentDescription = null, Modifier.size(14.dp), tint = NavyPrimary)
                                Spacer(Modifier.width(4.dp))
                                Text("মেইল ব্যাকআপ", fontSize = 11.sp)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val htmlFile = HtmlExporter.exportFullWebAppPortal(context, agreements)
                                ShareHelper.openFile(context, htmlFile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, Modifier.size(16.dp), tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("🌐 অ্যাপের ফুল ওয়েব ভার্সন পোর্টাল (HTML) তৈরি ও অপেন করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = {
                                if (agreements.isEmpty()) {
                                    Toast.makeText(context, "কোনো এগ্রিমেন্ট ডাটা নেই", Toast.LENGTH_SHORT).show()
                                } else {
                                    val htmlFile = HtmlExporter.exportAllAgreementsToHtml(context, agreements)
                                    ShareHelper.openFile(context, htmlFile)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, Modifier.size(14.dp), tint = Color(0xFF059669))
                            Spacer(Modifier.width(6.dp))
                            Text("রেসপন্সিভ HTML ওয়েব রিপোর্ট (ফোন ও ল্যাপটপে ব্রাউজারে দেখুন)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        }
                    }
                }
            }

            // Search and Filters
            item {
                // Category Filter Tabs (100Tk Stamp, 25Tk Attestation, Information Verification)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_document_category_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = LightGreenDark, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("নথিপত্র ও তথ্য যাচাই ক্যাটাগরি:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categoryOptions = listOf(
                                "সকল" to "সকল ফরম",
                                "১০০ টাকার স্ট্যাম্প" to "১০০টাকা স্ট্যাম্প",
                                "২৫ টাকার প্রত্যয়ন" to "২৫টাকা প্রত্যয়ন",
                                "তথ্য যাচাই" to "তথ্য যাচাই (শাখায় প্রেরণ)"
                            )
                            categoryOptions.forEach { (catValue, catLabel) ->
                                val isSelected = selectedDocumentCategoryFilter == catValue
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onDocumentCategoryFilterChange(catValue) },
                                    label = {
                                        Text(
                                            catLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (catValue == "তথ্য যাচাই") NavyPrimary else LightGreenDark,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (selectedDocumentCategoryFilter == "১০০ টাকার স্ট্যাম্প" || selectedDocumentCategoryFilter == "২৫ টাকার প্রত্যয়ন") {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFF3CD),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "বিজ্ঞপ্তি: ১০০ টাকা বা ২৫ টাকার স্ট্যাম্প অপশনে সরাসরি শাখায় প্রেরণ স্থগিত থাকবে। শাখায় পাঠাতে 'তথ্য যাচাই' ক্যাটাগরি ব্যবহার করুন।",
                                        fontSize = 11.sp,
                                        color = Color(0xFF856404)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("নাম, সিরিয়াল, NID বা জেলা দিয়ে সার্চ...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_search_input")
                )

                Spacer(Modifier.height(8.dp))

                // Designation Filter Chips
                Text("পদবী অনুযায়ী ফিল্টার:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf("সকল", "অফিসার (অ্যাকাউন্টস)", "অফিসার (ঋণ)", "সার্ভিস স্টাফ")
                    filterOptions.forEach { option ->
                        FilterChip(
                            selected = selectedDesignationFilter == option,
                            onClick = { viewModel.onDesignationFilterChange(option) },
                            label = { Text(option, fontSize = 12.sp) }
                        )
                    }
                }

                // Date Filter Chips
                if (availableDates.size > 2) {
                    Spacer(Modifier.height(4.dp))
                    Text("তারিখ অনুযায়ী ফিল্টার:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableDates.forEach { date ->
                            FilterChip(
                                selected = selectedDateFilter == date,
                                onClick = { viewModel.onDateFilterChange(date) },
                                label = { Text(date, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // List of Agreements
            if (filteredAgreements.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isBlank()) "এখনো কোনো এগ্রিমেন্ট ফরম পূরণ করা হয়নি।" else "কোনো মিল খুঁজে পাওয়া যায়নি।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredAgreements, key = { it.id }) { agreement ->
                    AdminAgreementItemCard(
                        agreement = agreement,
                        selectedCategory = selectedDocumentCategoryFilter,
                        onView = { onViewAgreement(agreement) },
                        onEdit = { onEditAgreement(agreement) },
                        onDelete = { agreementToDelete = agreement },
                        onTogglePrint = { viewModel.togglePrintStatus(agreement) },
                        onShare = { agreementToShare = agreement },
                        onDispatch = { agreementToDispatch = agreement }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (agreementToDelete != null) {
        val item = agreementToDelete!!
        AlertDialog(
            onDismissRequest = { agreementToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = AlertRed) },
            title = { Text("এগ্রিমেন্ট ডিলিট করবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "আপনি কি নিশ্চিত যে কর্মীর (${item.employeeName}, সিরিয়াল: ${item.serialNo}) এগ্রিমেন্ট ডিলিট করতে চান? ডিলিট করলে কর্মী পুনরায় নতুন ফরম পূরণ করার সুযোগ পাবেন।"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAgreement(item)
                        agreementToDelete = null
                        Toast.makeText(context, "এগ্রিমেন্ট ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { agreementToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Share Sheet
    if (agreementToShare != null) {
        ShareSheetDialog(
            agreement = agreementToShare!!,
            useStampMargin = true,
            onDismiss = { agreementToShare = null }
        )
    }

    // =========================================================
    // JSON EXPORT SUCCESS DIALOG
    // =========================================================
    if (lastExportResult != null) {
        val result = lastExportResult!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportResultDialog() },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    "JSON ব্যাকআপ সফল হয়েছে!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF059669)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "সকল লোকাল ডাটা সফলভাবে JSON ফরম্যাটে এক্সপোর্ট করা হয়েছে।",
                        fontSize = 13.sp
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ফাইলের নাম: ${result.fileName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("মোট এগ্রিমেন্ট: ${result.agreementsCount} টি", fontSize = 12.sp)
                            Text("সংরক্ষিত ঠিকানা: ${result.addressCount} টি", fontSize = 12.sp)
                            Text("খসড়া ফরম ডাটা: ${if (result.hasDrafts) "অন্তর্ভুক্ত রয়েছে" else "নেই"}", fontSize = 12.sp)
                            Text("সময়: ${result.timestamp}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "ফাইলটি ডিভাইসের Downloads/RRF_HR_Forms ফোল্ডারে সেভ রয়েছে।",
                                fontSize = 11.sp,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ShareHelper.shareFile(context, result.file, "RRF HR সম্পূর্ণ JSON ব্যাকআপ")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ফাইল শেয়ার করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExportResultDialog() }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // =========================================================
    // JSON IMPORT & RESTORE OPTIONS DIALOG
    // =========================================================
    if (importSummary != null) {
        val summary = importSummary!!
        if (!summary.isValid) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissImportDialog() },
                icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AlertRed) },
                title = { Text("অকার্যকর ব্যাকআপ ফাইল", fontWeight = FontWeight.Bold) },
                text = {
                    Text(summary.errorMessage ?: "নির্বাচিত JSON ফাইলটি সঠিক ফরম্যাটের নয় বা পার্স করা যায়নি।")
                },
                confirmButton = {
                    Button(onClick = { viewModel.dismissImportDialog() }) {
                        Text("ঠিক আছে")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    if (!isRestoring) viewModel.dismissImportDialog()
                },
                icon = {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text("ডাটা রিস্টোর নিশ্চিতকরণ", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("অ্যাপ: ${summary.appName ?: "RRF HR Management"}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("ব্যাকআপ তৈরির তারিখ: ${summary.exportDate ?: "অজানা"}", fontSize = 12.sp)
                                Text("মোট এগ্রিমেন্ট: ${summary.agreementsCount} টি", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("মোট ঠিকানা ডাটা: ${summary.addressItemsCount} টি", fontSize = 12.sp)
                                Text("খসড়া ফরম: ${if (summary.hasDrafts) "অন্তর্ভুক্ত রয়েছে" else "নেই"}", fontSize = 12.sp)
                            }
                        }

                        Text("রিস্টোর মোড নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        // Radio Option: MERGE
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedRestoreMode == "MERGE") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRestoreMode = "MERGE" }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRestoreMode == "MERGE",
                                    onClick = { selectedRestoreMode = "MERGE" }
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("মার্জ করুন (Merge - প্রস্তাবিত)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("বর্তমান ডাটা বহাল থাকবে এবং ব্যাকআপের নতুন ডাটা যুক্ত হবে।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Radio Option: REPLACE
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedRestoreMode == "REPLACE") Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRestoreMode = "REPLACE" }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRestoreMode == "REPLACE",
                                    onClick = { selectedRestoreMode = "REPLACE" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AlertRed)
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("সম্পূর্ণ প্রতিস্থাপন (Replace All)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedRestoreMode == "REPLACE") AlertRed else Color.Unspecified)
                                    Text("বর্তমান সকল ডাটা মুছে ফেলে ব্যাকআপের ডাটা প্রতিস্থাপন করা হবে।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (summary.hasDrafts) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { shouldRestoreDrafts = !shouldRestoreDrafts }
                            ) {
                                Checkbox(
                                    checked = shouldRestoreDrafts,
                                    onCheckedChange = { shouldRestoreDrafts = it }
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("খসড়া ফরম ও সেটিংস রিস্টোর করুন", fontSize = 12.sp)
                            }
                        }

                        if (isRestoring) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("ডাটা রিস্টোর হচ্ছে, অপেক্ষা করুন...", fontSize = 12.sp, color = NavyPrimary)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.executeRestore(
                                context = context,
                                replaceExisting = (selectedRestoreMode == "REPLACE"),
                                restoreDrafts = shouldRestoreDrafts
                            )
                        },
                        enabled = !isRestoring,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRestoreMode == "REPLACE") AlertRed else NavyPrimary
                        )
                    ) {
                        Text(if (isRestoring) "রিস্টোর হচ্ছে..." else "রিস্টোর শুরু করুন")
                    }
                },
                dismissButton = {
                    if (!isRestoring) {
                        TextButton(onClick = { viewModel.dismissImportDialog() }) {
                            Text("বাতিল")
                        }
                    }
                }
            )
        }
    }

    // =========================================================
    // RESTORE RESULT NOTIFICATION DIALOG
    // =========================================================
    if (restoreResult != null) {
        val result = restoreResult!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestoreResultDialog() },
            icon = {
                Icon(
                    if (result.success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = if (result.success) Color(0xFF059669) else AlertRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    if (result.success) "ডাটা রিস্টোর সফল হয়েছে" else "রিস্টোরে সমস্যা হয়েছে",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (result.success) Color(0xFF059669) else AlertRed
                )
            },
            text = {
                Text(result.message, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissRestoreResultDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (result.success) Color(0xFF059669) else AlertRed
                    )
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // =========================================================
    // DISPATCH TO BRANCH OFFICE DIALOG
    // =========================================================
    if (agreementToDispatch != null) {
        val item = agreementToDispatch!!
        var selectedBranch by remember { mutableStateOf(item.assignedBranch.ifBlank { "যশোর প্রধান শাখা" }) }
        var customBranch by remember { mutableStateOf("") }
        var selectedStatus by remember { mutableStateOf(item.verificationStatus) }
        var notes by remember { mutableStateOf(item.branchNotes) }

        val branchOptions = listOf(
            "যশোর প্রধান শাখা",
            "খুলনা অঞ্চল শাখা",
            "কুষ্টিয়া জেলা শাখা",
            "ঝিনাইদহ শাখা",
            "মাগুরা শাখা",
            "সাতক্ষীরা শাখা",
            "নড়াইল শাখা",
            "রাজশাহী শাখা",
            "বরিশাল শাখা",
            "অন্যান্য"
        )

        AlertDialog(
            onDismissRequest = { agreementToDispatch = null },
            icon = { Icon(Icons.Default.Apartment, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("শাখা অফিসে প্রেরণ ও তথ্য যাচাই", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("কর্মী: ${item.employeeName} (সিরিয়াল: ${item.serialNo})", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Text("শাখা অফিস নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        branchOptions.chunked(2).forEach { rowBranches ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowBranches.forEach { b ->
                                    FilterChip(
                                        selected = selectedBranch == b,
                                        onClick = { selectedBranch = b },
                                        label = { Text(b, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedBranch == "অন্যান্য") {
                        OutlinedTextField(
                            value = customBranch,
                            onValueChange = { customBranch = it },
                            label = { Text("শাখা অফিসের নাম লিখুন") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text("তথ্য যাচাইকরণ স্ট্যাটাস:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("পাঠানো হয়েছে", "যাচাই সম্পন্ন", "প্রত্যাখ্যাত").forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("শাখার মন্তব্য / পর্যবেক্ষণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalBranch = if (selectedBranch == "অন্যান্য") customBranch.ifBlank { "অন্যান্য শাখা" } else selectedBranch
                        viewModel.dispatchAgreementToBranch(item.id, finalBranch)
                        viewModel.updateBranchVerification(item.id, selectedStatus, notes)
                        agreementToDispatch = null
                        Toast.makeText(context, "$finalBranch শাখা অফিসে প্রেরণ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("সংরক্ষণ ও প্রেরণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { agreementToDispatch = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun AdminAgreementItemCard(
    agreement: AgreementEntity,
    selectedCategory: String = "সকল",
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePrint: () -> Unit,
    onShare: () -> Unit,
    onDispatch: () -> Unit
) {
    val context = LocalContext.current
    val isDispatchAllowed = (selectedCategory == "তথ্য যাচাই" || selectedCategory == "সকল")

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Serial + Edit Badge + Stamp Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NavyContainer
                    ) {
                        Text(
                            agreement.serialNo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (agreement.editCount > 0) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFEBAA)
                        ) {
                            Text(
                                agreement.editLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (agreement.isPrinted) LightGreenContainer else Color(0xFFFFF3CD),
                    modifier = Modifier.clickable { onTogglePrint() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (agreement.isPrinted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (agreement.isPrinted) LightGreenDark else Color(0xFFB78103),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (agreement.isPrinted) "প্রিন্ট সম্পন্ন" else "অপেক্ষমান",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (agreement.isPrinted) LightGreenDark else Color(0xFF856404)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Employee and Guarantor info
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        agreement.employeeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "ফাইল: ${agreement.baseFileName}.pdf",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF059669)
                    )
                    Text(
                        "পদবী: ${agreement.designation}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "পিতা: ${agreement.employeeFatherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "ঠিকানা: ${agreement.employeeVillage}, ${agreement.employeePostOffice}, ${agreement.employeeUpazila}, ${agreement.employeeDistrict}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (agreement.assignedBranch.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                "শাখা: ${agreement.assignedBranch} (${agreement.verificationStatus})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "তারিখ: ${agreement.submissionDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "জামিনদার: ${agreement.guarantorName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "(${agreement.effectiveGuarantorRelationship})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (isDispatchAllowed) {
                            onDispatch()
                        } else {
                            Toast.makeText(
                                context,
                                "১০০ টাকা বা ২৫ টাকার স্ট্যাম্প সরাসরি শাখায় পাঠানো যাবে না। শুধুমাত্র 'তথ্য যাচাই' ক্যাটাগরির ফরম শাখা অফিসে পাঠানো যাবে।",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (isDispatchAllowed) {
                        ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                    } else {
                        ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Apartment,
                        contentDescription = null,
                        Modifier.size(14.dp),
                        tint = if (isDispatchAllowed) NavyPrimary else Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isDispatchAllowed) "শাখায় পাঠান" else "শাখায় পাঠান (শুধুমাত্র তথ্য যাচাই)",
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed)
                }

                Spacer(Modifier.width(6.dp))

                Button(
                    onClick = onView,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("দেখুন ও প্রিন্ট", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color, maxLines = 1)
        }
    }
}

@Composable
fun MiniStatChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

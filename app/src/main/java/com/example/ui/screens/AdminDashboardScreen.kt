package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.util.ExcelExporter
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
    onLogout: () -> Unit,
    onViewAgreement: (AgreementEntity) -> Unit,
    onEditAgreement: (AgreementEntity) -> Unit,
    onNewAgreement: () -> Unit
) {
    val context = LocalContext.current
    var agreementToDelete by remember { mutableStateOf<AgreementEntity?>(null) }
    var agreementToShare by remember { mutableStateOf<AgreementEntity?>(null) }

    // Filtered Agreements
    val filteredAgreements = remember(agreements, searchQuery, selectedDesignationFilter, selectedDateFilter) {
        agreements.filter { item ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.employeeName.contains(searchQuery, ignoreCase = true) ||
                        item.serialNo.contains(searchQuery, ignoreCase = true) ||
                        item.guarantorNid.contains(searchQuery, ignoreCase = true) ||
                        item.employeeDistrict.contains(searchQuery, ignoreCase = true) ||
                        item.guarantorName.contains(searchQuery, ignoreCase = true)
            }
            val matchesDesig = if (selectedDesignationFilter == "সকল") true else {
                item.designation == selectedDesignationFilter
            }
            val matchesDate = if (selectedDateFilter == "সকল") true else {
                item.submissionDate == selectedDateFilter
            }
            matchesSearch && matchesDesig && matchesDate
        }
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
                        Text("RRF HR অ্যাডমিন প্যানেল", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "মোট সংরক্ষিত এগ্রিমেন্ট: $totalCount টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
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
                        Icon(Icons.Default.TableChart, contentDescription = "Excel Export", tint = Color.White)
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
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

            // Quick Backup & Export Bar
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (agreements.isEmpty()) {
                                    Toast.makeText(context, "কোনো এগ্রিমেন্ট ডাটা নেই", Toast.LENGTH_SHORT).show()
                                } else {
                                    val excelFile = ExcelExporter.exportToExcelCsv(context, agreements)
                                    ShareHelper.fallbackShare(context, excelFile, "RRF HR এগ্রিমেন্ট তালিকা (Excel CSV)")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = LightGreenDark),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("এক্সেল ডাউনলোড", fontSize = 12.sp)
                        }

                        Button(
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("মেইল ও ড্রাইভ ব্যাকআপ", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Search and Filters
            item {
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
                        onView = { onViewAgreement(agreement) },
                        onEdit = { onEditAgreement(agreement) },
                        onDelete = { agreementToDelete = agreement },
                        onTogglePrint = { viewModel.togglePrintStatus(agreement) },
                        onShare = { agreementToShare = agreement }
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
}

@Composable
fun AdminAgreementItemCard(
    agreement: AgreementEntity,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePrint: () -> Unit,
    onShare: () -> Unit
) {
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
                    if (agreement.editCount > 0) {
                        Text(
                            "ফাইল: ${agreement.baseFileName}.pdf",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LightGreenDark
                        )
                    }
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
                        "জেলা: ${agreement.employeeDistrict}, ${agreement.employeeUpazila}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

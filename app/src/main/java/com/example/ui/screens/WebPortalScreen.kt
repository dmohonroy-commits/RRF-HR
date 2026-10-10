package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AgreementEntity
import com.example.ui.MainViewModel
import com.example.util.HtmlExporter
import com.example.util.ShareHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPortalScreen(
    viewModel: MainViewModel,
    agreements: List<AgreementEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var htmlFileState by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Generate HTML file and content
    val htmlContent = remember(agreements) {
        val file = HtmlExporter.exportFullWebAppPortal(context, agreements)
        htmlFileState = file
        if (file.exists()) file.readText() else "<h3>এইচটিএমএল ফাইল পাওয়া যায়নি</h3>"
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "আরআরএফ ওয়েব ভার্সন পোর্টাল (HTML)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "লাইভ ডিজিটাল অনলাইন ওয়েব এ্যাপ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_web_portal_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Refresh Web View
                    IconButton(
                        onClick = {
                            isLoading = true
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.testTag("btn_web_portal_refresh")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "রিফ্রেশ",
                            tint = Color.White
                        )
                    }

                    // Open in Chrome / External Browser
                    IconButton(
                        onClick = {
                            htmlFileState?.let { file ->
                                ShareHelper.openHtmlInBrowser(context, file)
                            } ?: Toast.makeText(context, "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("btn_web_portal_browser")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "ব্রাউজারে অপেন",
                            tint = Color.White
                        )
                    }

                    // Save / Download File
                    IconButton(
                        onClick = {
                            htmlFileState?.let { file ->
                                val success = ShareHelper.saveFileToDownloads(context, file, "RRF_HR_Web_Portal.html")
                                if (success) {
                                    Toast.makeText(context, "ডাউনলোড ফোল্ডারে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("btn_web_portal_download")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "ডাউনলোড",
                            tint = Color.White
                        )
                    }

                    // Share File
                    IconButton(
                        onClick = {
                            htmlFileState?.let { file ->
                                ShareHelper.shareFile(context, file, "আরআরএফ ওয়েব ভার্সন পোর্টাল (HTML)")
                            }
                        },
                        modifier = Modifier.testTag("btn_web_portal_share")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "শেয়ার",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0033A0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Banner bar explaining options
                Surface(
                    color = Color(0xFF0038B8),
                    contentColor = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💡 টিপস: উপরের 🌐 আইকনে ক্লিক করে সরাসরি ক্রোম বা অন্য ব্রাউজারে খুলতে পারেন।",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                htmlFileState?.let { file ->
                                    ShareHelper.openHtmlInBrowser(context, file)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "ব্রাউজারে খুলুন",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0033A0)
                            )
                        }
                    }
                }

                // Embedded Full Web App WebView
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    setSupportZoom(true)
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        return false
                                    }
                                }
                                webChromeClient = WebChromeClient()
                                loadDataWithBaseURL("https://rrf-hr-portal.local/", htmlContent, "text/html", "UTF-8", null)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            webViewInstance = webView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF0033A0)
                        )
                    }
                }
            }
        }
    }
}

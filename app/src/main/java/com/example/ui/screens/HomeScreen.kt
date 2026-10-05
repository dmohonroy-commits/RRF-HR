package com.example.ui.screens

import android.net.Uri
import kotlin.math.absoluteValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.components.RrfOfficialLogoVideoPlayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgreementEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.Attestation25TkPdfGenerator
import com.example.util.PdfGenerator
import com.example.util.ShareHelper
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

data class SliderItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

data class LeadershipPhotoItem(
    val title: String,
    val designation: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    localAgreement: AgreementEntity?,
    onOpenMenu: () -> Unit,
    onOpenAdminLogin: () -> Unit,
    onNavigateToWorkerForm: () -> Unit,
    onNavigateToAgreements: () -> Unit,
    onViewAgreement: (AgreementEntity) -> Unit,
    onOpenStampInfo: () -> Unit,
    onOpenOrgInfo: () -> Unit,
    onOpenTermsInfo: () -> Unit,
    onOpenContactInfo: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    var showStampPrintDialog by remember { mutableStateOf(false) }
    var selectedStampOption by remember { mutableIntStateOf(0) } // 0: 100 Tk Stamp, 1: 25 Tk Stamp
    var showAgreementPrintDialog by remember { mutableStateOf(false) }

    val allAgreementForms by viewModel.allAgreementForms.collectAsState()
    val formState by viewModel.formState.collectAsState()

    // Infinite pulse animation for banner/icons
    val infiniteTransition = rememberInfiniteTransition(label = "home_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val customUris by viewModel.customLeadershipUris.collectAsState()
    


    val sliderItems = remember {
        listOf(
            SliderItem(
                title = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন",
                subtitle = "মানবিক উন্নয়ন ও টেকসই সমৃদ্ধির অঙ্গীকার",
                icon = Icons.Default.Apartment,
                gradientColors = listOf(Color(0xFF0F766E), Color(0xFF0D9488), Color(0xFF14B8A6))
            ),
            SliderItem(
                title = "১০০ টাকার স্ট্যাম্প চুক্তিপত্র",
                subtitle = "কর্মী জামানতনামা ও লিগাল সাইজ প্রিন্ট পোর্টাল",
                icon = Icons.Default.Description,
                gradientColors = listOf(Color(0xFF065F46), Color(0xFF059669), Color(0xFF10B981))
            ),
            SliderItem(
                title = "এগ্রিমেন্ট ও তথ্যানুসন্ধান ফরম",
                subtitle = "৫টি অফিসিয়াল A4 সাইজ ফরম ও অটোমেশন",
                icon = Icons.Default.FolderShared,
                gradientColors = listOf(Color(0xFF047857), Color(0xFF059669), Color(0xFF34D399))
            ),
            SliderItem(
                title = "প্রশাসন ও মানবসম্পদ বিভাগ",
                subtitle = "দক্ষ ও নিষ্ঠাবান কর্মী বাহিনী গঠনে প্রতিশ্রুতিবদ্ধ",
                icon = Icons.Default.Groups,
                gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669))
            )
        )
    }

    val totalSlideCount = sliderItems.size
    val pagerState = rememberPagerState(pageCount = { totalSlideCount })

    // Auto-scroll banner smoothly every 7 seconds
    LaunchedEffect(totalSlideCount) {
        while (true) {
            delay(7000L)
            if (totalSlideCount > 1) {
                val nextPage = (pagerState.currentPage + 1) % totalSlideCount
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 800)
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(
                    elevation = 6.dp,
                    spotColor = Color.White.copy(alpha = 0.15f)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.testTag("btn_menu_drawer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "মেনু বার",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "RRF HR",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(onClick = onOpenAdminLogin),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "প্রোফাইল",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1B365D).copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1B365D)) // Solid professional lighter navy blue background
        ) {
            // Subtle ambient lighting for real glass refraction
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 80.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-70).dp, y = 70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0EA5E9).copy(alpha = 0.08f))
            )

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(700)) + slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = tween(700, easing = FastOutSlowInEasing)
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // =========================================================
                    // 7-SECOND AUTO-SLIDING REAL PREMIUM GLASS BANNER
                    // =========================================================
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f)))),
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(pulseScale)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Color.White.copy(alpha = 0.15f)
                            )
                    ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.14f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                )
                            )
                    ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(215.dp)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            ) { page ->
                                val item = sliderItems[page]
                                val pageOffset = kotlin.math.abs((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                val scaleFactor = 0.85f + (0.15f * (1f - pageOffset.coerceIn(0f, 1f)))
                                val alphaFactor = 0.4f + (0.6f * (1f - pageOffset.coerceIn(0f, 1f)))

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = scaleFactor
                                            scaleY = scaleFactor
                                            alpha = alphaFactor
                                            rotationY = pageOffset * 15f
                                        }
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // Official 8-second 3D RRF Logo Video Animation (Dark Background Removed & Zero Cropping)
                                        RrfOfficialLogoVideoPlayer(
                                            size = 72.dp,
                                            showSubLabel = false
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = item.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )

                                        Spacer(Modifier.height(2.dp))

                                        Text(
                                            text = item.subtitle,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.85f),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            // Dots Indicator (Bottom Center)
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(totalSlideCount) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 16.dp else 6.dp, 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                  }
                }



                // =========================================================
                // 4 REAL PREMIUM GLASS CARDS (2x2 Grid) WITH DIAMOND SPARKLE ANIMATION
                // =========================================================
                val diamondTransition = rememberInfiniteTransition(label = "diamond_sparkle")
                val diamondGlow by diamondTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "diamond_glow"
                )

                val cardBorder = BorderStroke(
                    1.8.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = diamondGlow),
                            Color(0xFF7DD3FC).copy(alpha = diamondGlow * 0.8f),
                            Color.White.copy(alpha = 0.3f)
                        )
                    )
                )

                val cardElevation = CardDefaults.cardElevation(
                    defaultElevation = (8 + (diamondGlow * 6)).dp
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Stamp Print Card
                        Card(
                            modifier = Modifier.weight(1f).aspectRatio(1.1f).clickable { showStampPrintDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            border = cardBorder,
                            elevation = cardElevation
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("স্ট্যাম্প প্রিন্ট", fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        // 2. Agreements Form Print Card
                        Card(
                            modifier = Modifier.weight(1f).aspectRatio(1.1f).clickable { showAgreementPrintDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            border = cardBorder,
                            elevation = cardElevation
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("এগ্রিমেন্ট ফরম প্রিন্ট", fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 3. Organization Info Card
                        Card(
                            modifier = Modifier.weight(1f).aspectRatio(1.1f).clickable(onClick = onOpenOrgInfo),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            border = cardBorder,
                            elevation = cardElevation
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Apartment, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("প্রতিষ্ঠান পরিচিতি", fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        // 4. Agreement Rules Card
                        Card(
                            modifier = Modifier.weight(1f).aspectRatio(1.1f).clickable(onClick = onOpenTermsInfo),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            border = cardBorder,
                            elevation = cardElevation
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("চুক্তিপত্রের নিয়মাবলী", fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                // =========================================================
                // CONTACT & HELPLINE REAL GLASS CARD
                // =========================================================
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f)))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenContactInfo)
                        .testTag("btn_home_contact_info")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SupportAgent,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "যোগাযোগ ও হেল্পলাইন",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "আরআরএফ মানবসম্পদ বিভাগ ও জরুরি সহায়তা",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Guidance box for filling forms via 3-line menu
                Surface(
                    onClick = onOpenMenu,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f)))),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "লাইনে ক্লিক করে ফরম পূরণ করুন",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Copyright Footer
                Text(
                    text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ)",
                    fontSize = 10.5.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))
            }
        }
      }
    }

    // 1. Stamp Print & Share Dialog (100 Tk Stamp & 25 Tk Stamp Options)
    if (showStampPrintDialog) {
        val stampAgr = viewModel.getStampAgreementForPreview()
        AlertDialog(
            onDismissRequest = { showStampPrintDialog = false },
            icon = {
                Surface(shape = CircleShape, color = Color(0xFF059669).copy(alpha = 0.12f), modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(28.dp))
                    }
                }
            },
            title = {
                Text(
                    text = "স্ট্যাম্প প্রিন্ট অপশন নির্বাচন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF059669),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 2 Stamp Selection Buttons / Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { selectedStampOption = 0 },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedStampOption == 0) Color(0xFF059669) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.2.dp, if (selectedStampOption == 0) Color(0xFF059669) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                                Text(
                                    text = "১০০ টাকার স্ট্যাম্প",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (selectedStampOption == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            onClick = { selectedStampOption = 1 },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedStampOption == 1) Color(0xFF059669) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.2.dp, if (selectedStampOption == 1) Color(0xFF059669) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                                Text(
                                    text = "২৫ টাকার স্ট্যাম্প",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (selectedStampOption == 1) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (selectedStampOption == 0) {
                        // --- 100-Taka Stamp Agreement Section ---
                        Text(
                            text = if (stampAgr.employeeName.isNotBlank())
                                "কর্মী: ${stampAgr.employeeName} (${stampAgr.designation})\n১০০ টাকার ৩টি নন-জুডিশিয়াল স্ট্যাম্প চুক্তিপত্র (অটোমেটিক পূরণকৃত)।"
                            else
                                "১০০ টাকার ৩টি নন-জুডিশিয়াল স্ট্যাম্প পেপারের জন্য চুক্তিপত্র PDF প্রিন্ট ও শেয়ার অপশন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Visible Red Note for 100 Taka Stamp
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.5.dp, Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "নোট: প্রিন্ট করুন এবং প্রধান কার্যালয়ে গিয়ে জামিনদারের স্বাক্ষর করবে।",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Open & Print PDF
                        Button(
                            onClick = {
                                val pdf = PdfGenerator.generateAgreementPdf(context, stampAgr, forStampPaper = true)
                                ShareHelper.openFile(context, pdf)
                                showStampPrintDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("১০০ টাকার ৩টি PDF ওপেন ও প্রিন্ট", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Share PDF
                        OutlinedButton(
                            onClick = {
                                val pdf = PdfGenerator.generateAgreementPdf(context, stampAgr, forStampPaper = true)
                                val caption = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) - ১০০ টাকার স্ট্যাম্প চুক্তিপত্র\nকর্মী: ${stampAgr.employeeName}"
                                ShareHelper.shareFile(context, pdf, caption)
                                showStampPrintDialog = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
                            border = BorderStroke(1.2.dp, Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("শেয়ার করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Direct WhatsApp & Download
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val pdf = PdfGenerator.generateAgreementPdf(context, stampAgr, forStampPaper = true)
                                    val caption = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) - ১০০ টাকার স্ট্যাম্প চুক্তিপত্র\nকর্মী: ${stampAgr.employeeName}"
                                    ShareHelper.shareToWhatsApp(context, pdf, null, caption)
                                    showStampPrintDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val pdf = PdfGenerator.generateAgreementPdf(context, stampAgr, forStampPaper = true)
                                    ShareHelper.saveFileToDownloads(context, pdf, "${stampAgr.baseFileName}.pdf")
                                    showStampPrintDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("ডাউনলোড", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        TextButton(
                            onClick = {
                                showStampPrintDialog = false
                                onNavigateToWorkerForm()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("স্ট্যাম্প ফরম পূরণ / এডিট করুন ➔", fontSize = 12.5.sp)
                        }

                    } else {
                        // --- 25-Taka Stamp Attestation Section ---
                        val empName = allAgreementForms.personalInfoForm.employeeNameBangla.ifBlank { stampAgr.employeeName }
                        Text(
                            text = if (empName.isNotBlank())
                                "কর্মী: $empName\nকর্মীর প্রতিবেশী ২ জনের তথ্য নিয়ে ২৫ টাকার প্রত্যয়ন পত্র (অটোমেটিক পূরণকৃত)।"
                            else
                                "কর্মীর প্রতিবেশী ২ জনের তথ্য নিয়ে ২৫ টাকার প্রত্যয়ন পত্র PDF প্রিন্ট ও শেয়ার অপশন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Visible Red Note for 25 Taka Stamp
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.5.dp, Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "নোট: প্রিন্ট করুন এবং প্রত্যয়নকারীদের স্বাক্ষর নিয়ে আসবেন।",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Open & Print 25 Tk PDF
                        Button(
                            onClick = {
                                val pdf = Attestation25TkPdfGenerator.generate25TkAttestationPdf(context, allAgreementForms, stampAgr)
                                ShareHelper.openFile(context, pdf)
                                showStampPrintDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("প্রত্যয়ন ওপেন ও প্রিন্ট", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, maxLines = 1)
                        }

                        // Share PDF
                        OutlinedButton(
                            onClick = {
                                val pdf = Attestation25TkPdfGenerator.generate25TkAttestationPdf(context, allAgreementForms, stampAgr)
                                val caption = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) - ২৫ টাকার প্রত্যয়ন পত্র\nকর্মী: $empName"
                                ShareHelper.shareFile(context, pdf, caption)
                                showStampPrintDialog = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
                            border = BorderStroke(1.2.dp, Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("শেয়ার করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Direct WhatsApp & Download
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val pdf = Attestation25TkPdfGenerator.generate25TkAttestationPdf(context, allAgreementForms, stampAgr)
                                    val caption = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) - ২৫ টাকার প্রত্যয়ন পত্র\nকর্মী: $empName"
                                    ShareHelper.shareToWhatsApp(context, pdf, null, caption)
                                    showStampPrintDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val pdf = Attestation25TkPdfGenerator.generate25TkAttestationPdf(context, allAgreementForms, stampAgr)
                                    ShareHelper.saveFileToDownloads(context, pdf, "RRF_25_Taka_Stamp_Attestation.pdf")
                                    showStampPrintDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("ডাউনলোড", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        TextButton(
                            onClick = {
                                showStampPrintDialog = false
                                viewModel.updateScreen(AppScreen.FORM_VERIFICATION)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("তথ্য যাচাই ফরম (প্রতিবেশী তথ্য) এডিট করুন ➔", fontSize = 12.5.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStampPrintDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // 2. Agreement Form Merged Print & Share Dialog
    if (showAgreementPrintDialog) {
        AlertDialog(
            onDismissRequest = { showAgreementPrintDialog = false },
            icon = {
                Surface(shape = CircleShape, color = Color(0xFF059669).copy(alpha = 0.12f), modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(28.dp))
                    }
                }
            },
            title = {
                Text(
                    text = "এগ্রিমেন্ট ফরম প্রিন্ট ও শেয়ার",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF059669),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "সকল ৬টি এগ্রিমেন্ট ফরম এক সাথে (মার্জ আকারে ৯ পাতার A4 PDF) প্রিন্ট অথবা শেয়ার করুন।",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // 1. Open & Print Merged PDF
                    Button(
                        onClick = {
                            val pdf = AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(context, allAgreementForms)
                            ShareHelper.openFile(context, pdf)
                            showAgreementPrintDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("পিডিএফ ওপেন ও প্রিন্ট", fontWeight = FontWeight.Bold)
                    }

                    // 2. Share Merged PDF
                    OutlinedButton(
                        onClick = {
                            val pdf = AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(context, allAgreementForms)
                            ShareHelper.shareFile(context, pdf, "আরআরএফ সকল এগ্রিমেন্ট ফরম (মার্জকৃত A4 PDF)")
                            showAgreementPrintDialog = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
                        border = BorderStroke(1.5.dp, Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("শেয়ার করুন", fontWeight = FontWeight.Bold)
                    }

                    // 3. Direct WhatsApp & Download
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val pdf = AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(context, allAgreementForms)
                                ShareHelper.shareToWhatsApp(context, pdf, null, "আরআরএফ সকল এগ্রিমেন্ট ফরম (মার্জকৃত A4 PDF)")
                                showAgreementPrintDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                val pdf = AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(context, allAgreementForms)
                                ShareHelper.saveFileToDownloads(context, pdf, "RRF_All_Agreement_Forms_Merged.pdf")
                                showAgreementPrintDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("ডাউনলোড", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    TextButton(
                        onClick = {
                            showAgreementPrintDialog = false
                            onNavigateToAgreements()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("সকল এগ্রিমেন্ট ফরম তালিকা ও হাব ➔", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAgreementPrintDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen

@Composable
fun AppDrawerContent(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onOpenAdminLogin: () -> Unit,
    onOpenStampInfo: () -> Unit,
    onOpenOrgInfo: () -> Unit,
    onOpenTermsInfo: () -> Unit,
    onOpenGuidelines: () -> Unit = {},
    onOpenCloudFunctions: () -> Unit = {},
    onOpenContactInfo: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = Color(0xFF1B365D)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B365D))
                .verticalScroll(rememberScrollState())
        ) {
            // Header (Real Glass Header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF244873),
                                Color(0xFF1B365D)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Text(
                        text = "RRF HR",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Navigation Items
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {

                // 1. Home Item
                DrawerItemButton(
                    icon = Icons.Default.Home,
                    title = "হোম পেজ",
                    selected = currentScreen == AppScreen.HOME,
                    testTag = "drawer_item_home",
                    onClick = {
                        onCloseDrawer()
                        onNavigate(AppScreen.HOME)
                    }
                )

                Spacer(Modifier.height(8.dp))

                // 2. Agreements Form
                val isFormScreen = currentScreen in listOf(
                    AppScreen.FORM_ID_CARD,
                    AppScreen.FORM_TRAINING,
                    AppScreen.FORM_RELATIONSHIP,
                    AppScreen.FORM_NOMINEE,
                    AppScreen.FORM_PERSONAL_INFO,
                    AppScreen.FORM_VERIFICATION,
                    AppScreen.WORKER_PANEL,
                    AppScreen.AGREEMENTS_HUB
                )
                DrawerItemButton(
                    icon = Icons.Default.FolderShared,
                    title = "এগ্রিমেন্ট ফরম",
                    selected = isFormScreen,
                    testTag = "drawer_item_agreement_forms_unified",
                    onClick = {
                        onCloseDrawer()
                        onNavigate(AppScreen.FORM_ID_CARD)
                    }
                )

                Spacer(Modifier.height(8.dp))

                // 3. Organization Info
                DrawerItemButton(
                    icon = Icons.Default.Apartment,
                    title = "প্রতিষ্ঠান পরিচিতি",
                    selected = false,
                    testTag = "drawer_item_org_info",
                    onClick = {
                        onCloseDrawer()
                        onOpenOrgInfo()
                    }
                )

                Spacer(Modifier.height(8.dp))

                // 4. Agreement Terms
                DrawerItemButton(
                    icon = Icons.Default.Gavel,
                    title = "চুক্তিপত্রের নিয়মাবলী",
                    selected = currentScreen == AppScreen.AGREEMENT_CONDITIONS,
                    testTag = "drawer_item_terms_info",
                    onClick = {
                        onCloseDrawer()
                        onOpenTermsInfo()
                    }
                )

                Spacer(Modifier.height(8.dp))

                // 5. App Guidelines & Voice Tutorial (চুক্তিপত্রের নিয়মাবলীর নিচে ও যোগাযোগ ও হেল্পলাইনের উপরে)
                DrawerItemButton(
                    icon = Icons.Default.PlayCircle,
                    title = "অ্যাপ ব্যবহারের গাইডলাইন",
                    selected = currentScreen == AppScreen.APP_GUIDELINES,
                    testTag = "drawer_item_app_guidelines",
                    onClick = {
                        onCloseDrawer()
                        onOpenGuidelines()
                    }
                )

                Spacer(Modifier.height(8.dp))

                // 6. Firebase Cloud Functions
                DrawerItemButton(
                    icon = Icons.Default.CloudSync,
                    title = "Firebase Cloud Functions",
                    selected = currentScreen == AppScreen.CLOUD_FUNCTIONS,
                    testTag = "drawer_item_cloud_functions",
                    onClick = {
                        onCloseDrawer()
                        onOpenCloudFunctions()
                    }
                )

                Spacer(Modifier.height(8.dp))

                // 7. Contact Info
                DrawerItemButton(
                    icon = Icons.Default.SupportAgent,
                    title = "যোগাযোগ ও হেল্পলাইন",
                    selected = false,
                    testTag = "drawer_item_contact_info",
                    onClick = {
                        onCloseDrawer()
                        onOpenContactInfo()
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Footer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.1f)))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.1f),
                                        Color.White.copy(alpha = 0.02f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (RRF)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DrawerItemButton(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "drawer_item_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = glowAlpha),
                    Color.White.copy(alpha = (glowAlpha * 0.3f).coerceIn(0.1f, 1f))
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .shadow(if (selected) 12.dp else 6.dp, RoundedCornerShape(14.dp), spotColor = Color.White)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (selected) 0.25f else 0.12f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

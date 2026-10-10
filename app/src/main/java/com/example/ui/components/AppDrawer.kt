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
import androidx.compose.ui.text.style.TextAlign
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
    onOpenWebPortal: () -> Unit = {},
    onOpenContactInfo: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = Color(0xFFE0F2FE)
    ) {
        PastelAmbientBackground(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                // Header (Real Glass Header)
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerShape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassOrb3DIcon(
                            icon = Icons.Default.Apartment,
                            size = 52.dp,
                            iconSize = 26.dp,
                            iconTint = Color(0xFF0F766E)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "RRF HR",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F2942)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Navigation Items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

                    // 5. App Guidelines & Voice Tutorial
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

                    Spacer(Modifier.height(10.dp))

                    // Footer
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerShape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F2942),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                }
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
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color.White.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.68f)
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.5.dp,
            if (selected) {
                Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
            } else {
                Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.4f)))
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 10.dp else 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            GlassOrb3DIcon(
                icon = icon,
                size = 40.dp,
                iconSize = 20.dp,
                iconTint = if (selected) Color(0xFF0284C7) else Color(0xFF0F2942)
            )

            Spacer(Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                color = Color(0xFF0F2942),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (selected) Color(0xFF0284C7) else Color(0xFF0F2942).copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

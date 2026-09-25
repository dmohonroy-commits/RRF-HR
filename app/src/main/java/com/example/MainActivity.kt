package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.AppShareQrDialog
import com.example.ui.components.ShareSheetDialog
import com.example.ui.components.SubmissionConfirmationDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminEditScreen
import com.example.ui.screens.AgreementPreviewScreen
import com.example.ui.screens.WorkerFormScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RrfApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RrfApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val allAgreements by viewModel.allAgreements.collectAsState()
    val localWorkerAgreement by viewModel.localWorkerAgreement.collectAsState()
    val selectedAgreement by viewModel.selectedAgreement.collectAsState()
    val savedConfirmation by viewModel.savedConfirmation.collectAsState()
    val showAdminLoginDialog by viewModel.showAdminLoginDialog.collectAsState()
    val adminPasswordInput by viewModel.adminPasswordInput.collectAsState()
    val adminLoginError by viewModel.adminLoginError.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDesignationFilter by viewModel.selectedDesignationFilter.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    val showQrDialog by viewModel.showQrDialog.collectAsState()
    val showShareDialog by viewModel.showShareDialog.collectAsState()
    val use100TkStampMargin by viewModel.use100TkStampMargin.collectAsState()

    // Back handling
    BackHandler(enabled = currentScreen != AppScreen.WORKER_PANEL) {
        when (currentScreen) {
            AppScreen.ADMIN_PANEL -> {
                // If leaving admin panel, reset login so re-entry requires password
                viewModel.adminLogout()
            }
            AppScreen.ADMIN_EDIT_FORM -> {
                viewModel.updateScreen(AppScreen.ADMIN_PANEL)
            }
            AppScreen.AGREEMENT_PREVIEW -> {
                if (isAdminLoggedIn) {
                    viewModel.updateScreen(AppScreen.ADMIN_PANEL)
                } else {
                    viewModel.updateScreen(AppScreen.WORKER_PANEL)
                }
            }
            AppScreen.WORKER_PANEL -> {
                // Stay or system back
            }
        }
    }

    when (currentScreen) {
        AppScreen.WORKER_PANEL -> {
            WorkerFormScreen(
                viewModel = viewModel,
                formState = formState,
                localAgreement = localWorkerAgreement,
                onOpenAdminLogin = { viewModel.openAdminLoginDialog() },
                onOpenQrDialog = { viewModel.setShowQrDialog(true) },
                onViewAgreement = { agreement ->
                    viewModel.selectAgreementForPreview(agreement)
                }
            )
        }

        AppScreen.ADMIN_PANEL -> {
            AdminDashboardScreen(
                viewModel = viewModel,
                agreements = allAgreements,
                searchQuery = searchQuery,
                selectedDesignationFilter = selectedDesignationFilter,
                selectedDateFilter = selectedDateFilter,
                onLogout = { viewModel.adminLogout() },
                onViewAgreement = { agreement ->
                    viewModel.selectAgreementForPreview(agreement)
                },
                onEditAgreement = { agreement ->
                    viewModel.prepareEditForAdmin(agreement)
                },
                onNewAgreement = {
                    viewModel.prepareNewFormForAdmin()
                }
            )
        }

        AppScreen.ADMIN_EDIT_FORM -> {
            AdminEditScreen(
                viewModel = viewModel,
                formState = formState,
                onBack = { viewModel.updateScreen(AppScreen.ADMIN_PANEL) }
            )
        }

        AppScreen.AGREEMENT_PREVIEW -> {
            if (selectedAgreement != null) {
                AgreementPreviewScreen(
                    agreement = selectedAgreement!!,
                    isAdmin = isAdminLoggedIn,
                    onBack = {
                        if (isAdminLoggedIn) {
                            viewModel.updateScreen(AppScreen.ADMIN_PANEL)
                        } else {
                            viewModel.updateScreen(AppScreen.WORKER_PANEL)
                        }
                    },
                    onTogglePrintStatus = { agreement ->
                        viewModel.togglePrintStatus(agreement)
                    },
                    onEditAgreement = { agreement ->
                        if (isAdminLoggedIn) {
                            viewModel.prepareEditForAdmin(agreement)
                        } else {
                            viewModel.prepareEditForWorker(agreement)
                            viewModel.updateScreen(AppScreen.WORKER_PANEL)
                        }
                    }
                )
            } else {
                viewModel.updateScreen(AppScreen.WORKER_PANEL)
            }
        }
    }

    // Admin Login Dialog
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            passwordInput = adminPasswordInput,
            onPasswordChange = { viewModel.onAdminPasswordChange(it) },
            hasError = adminLoginError,
            onDismiss = { viewModel.closeAdminLoginDialog() },
            onSubmit = { viewModel.verifyAdminPassword() }
        )
    }

    // Save Confirmation Dialog (Displays Serial No & Employee Name)
    if (savedConfirmation != null) {
        SubmissionConfirmationDialog(
            agreement = savedConfirmation!!,
            onDismiss = { viewModel.closeConfirmationDialog() },
            onViewAgreement = {
                val item = savedConfirmation!!
                viewModel.closeConfirmationDialog()
                viewModel.selectAgreementForPreview(item)
            },
            onShare = {
                val item = savedConfirmation!!
                viewModel.closeConfirmationDialog()
                viewModel.selectAgreementForPreview(item)
                viewModel.setShowShareDialog(true)
            }
        )
    }

    // App Share / Download QR Dialog
    if (showQrDialog) {
        AppShareQrDialog(
            onDismiss = { viewModel.setShowQrDialog(false) }
        )
    }

    // Global Share Sheet Dialog
    if (showShareDialog && selectedAgreement != null) {
        ShareSheetDialog(
            agreement = selectedAgreement!!,
            useStampMargin = use100TkStampMargin,
            onDismiss = { viewModel.setShowShareDialog(false) }
        )
    }
}

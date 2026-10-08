package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppShareQrDialog
import com.example.ui.components.ContactInfoDialog
import com.example.ui.components.OrgInfoDialog
import com.example.ui.components.ShareSheetDialog
import com.example.ui.components.StampInfoDialog
import com.example.ui.components.SubmissionConfirmationDialog
import com.example.ui.components.TermsInfoDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminEditScreen
import com.example.ui.screens.AgreementConditionsViewerScreen
import com.example.ui.screens.AgreementFormsHubScreen
import com.example.ui.screens.AgreementPreviewScreen
import com.example.ui.screens.AppGuidelinesScreen
import com.example.ui.screens.CloudFunctionsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.WorkerFormScreen
import com.example.ui.screens.forms.IdCardFormScreen
import com.example.ui.screens.forms.NomineeFormScreen
import com.example.ui.screens.forms.PersonalInfoFormScreen
import com.example.ui.screens.forms.RelationshipFormScreen
import com.example.ui.screens.forms.TrainingFormScreen
import com.example.ui.screens.forms.VerificationFormScreen

import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

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

    override fun onPause() {
        super.onPause()
        viewModel.saveAllDrafts()
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveAllDrafts()
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
    val selectedDocumentCategoryFilter by viewModel.selectedDocumentCategoryFilter.collectAsState()
    val showQrDialog by viewModel.showQrDialog.collectAsState()
    val showShareDialog by viewModel.showShareDialog.collectAsState()
    val use100TkStampMargin by viewModel.use100TkStampMargin.collectAsState()
    val allAgreementForms by viewModel.allAgreementForms.collectAsState()

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showStampInfoDialog by remember { mutableStateOf(false) }
    var showOrgInfoDialog by remember { mutableStateOf(false) }
    var showTermsInfoDialog by remember { mutableStateOf(false) }
    var showContactInfoDialog by remember { mutableStateOf(false) }

    // Back handling
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
            return@BackHandler
        }
        when (currentScreen) {
            AppScreen.ADMIN_PANEL -> {
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
                viewModel.updateScreen(AppScreen.HOME)
            }
            AppScreen.AGREEMENT_CONDITIONS,
            AppScreen.APP_GUIDELINES,
            AppScreen.CLOUD_FUNCTIONS -> {
                viewModel.updateScreen(AppScreen.HOME)
            }
            AppScreen.AGREEMENTS_HUB -> {
                viewModel.updateScreen(AppScreen.HOME)
            }
            AppScreen.FORM_ID_CARD,
            AppScreen.FORM_TRAINING,
            AppScreen.FORM_RELATIONSHIP,
            AppScreen.FORM_NOMINEE,
            AppScreen.FORM_PERSONAL_INFO,
            AppScreen.FORM_VERIFICATION -> {
                viewModel.updateScreen(AppScreen.AGREEMENTS_HUB)
            }
            AppScreen.HOME -> {
                // System default back exits
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    viewModel.updateScreen(screen)
                },
                onOpenAdminLogin = {
                    viewModel.openAdminLoginDialog()
                },
                onOpenStampInfo = { showStampInfoDialog = true },
                onOpenOrgInfo = { showOrgInfoDialog = true },
                onOpenTermsInfo = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.updateScreen(AppScreen.AGREEMENT_CONDITIONS)
                },
                onOpenGuidelines = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.updateScreen(AppScreen.APP_GUIDELINES)
                },
                onOpenCloudFunctions = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.updateScreen(AppScreen.CLOUD_FUNCTIONS)
                },
                onOpenWebPortal = {
                    coroutineScope.launch { drawerState.close() }
                    val htmlFile = com.example.util.HtmlExporter.exportFullWebAppPortal(context, allAgreements)
                    com.example.util.ShareHelper.openFile(context, htmlFile)
                },
                onOpenContactInfo = { showContactInfoDialog = true },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    viewModel = viewModel,
                    localAgreement = localWorkerAgreement,
                    onOpenMenu = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onOpenAdminLogin = { viewModel.openAdminLoginDialog() },
                    onNavigateToWorkerForm = { viewModel.updateScreen(AppScreen.WORKER_PANEL) },
                    onNavigateToAgreements = { viewModel.updateScreen(AppScreen.AGREEMENTS_HUB) },
                    onViewAgreement = { agreement ->
                        viewModel.selectAgreementForPreview(agreement)
                    },
                    onOpenStampInfo = { showStampInfoDialog = true },
                    onOpenOrgInfo = { showOrgInfoDialog = true },
                    onOpenTermsInfo = { viewModel.updateScreen(AppScreen.AGREEMENT_CONDITIONS) },
                    onOpenContactInfo = { showContactInfoDialog = true }
                )
            }

            AppScreen.WORKER_PANEL -> {
                WorkerFormScreen(
                    viewModel = viewModel,
                    formState = formState,
                    localAgreement = localWorkerAgreement,
                    onOpenMenu = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onNavigateHome = {
                        viewModel.updateScreen(AppScreen.HOME)
                    },
                    onOpenAdminLogin = { viewModel.openAdminLoginDialog() },
                    onOpenQrDialog = { viewModel.setShowQrDialog(true) },
                    onViewAgreement = { agreement ->
                        viewModel.selectAgreementForPreview(agreement)
                    }
                )
            }

            AppScreen.AGREEMENTS_HUB -> {
                AgreementFormsHubScreen(
                    viewModel = viewModel,
                    allForms = allAgreementForms,
                    onBack = { viewModel.updateScreen(AppScreen.HOME) },
                    onNavigateToForm = { formScreen -> viewModel.updateScreen(formScreen) }
                )
            }

            AppScreen.FORM_ID_CARD -> {
                IdCardFormScreen(
                    viewModel = viewModel,
                    formState = allAgreementForms.idCardForm,
                    onBack = { viewModel.updateScreen(AppScreen.AGREEMENTS_HUB) }
                )
            }

            AppScreen.FORM_TRAINING -> {
                TrainingFormScreen(
                    viewModel = viewModel,
                    formState = allAgreementForms.trainingForm,
                    onBack = { viewModel.updateScreen(AppScreen.AGREEMENTS_HUB) }
                )
            }

            AppScreen.FORM_RELATIONSHIP -> {
                RelationshipFormScreen(
                    viewModel = viewModel,
                    formState = allAgreementForms.relationshipForm,
                    onBack = { viewModel.updateScreen(AppScreen.AGREEMENTS_HUB) }
                )
            }

            AppScreen.FORM_NOMINEE -> {
                NomineeFormScreen(
                    viewModel = viewModel,
                    formState = allAgreementForms.nomineeForm,
                    onBack = { viewModel.updateScreen(AppScreen.AGREEMENTS_HUB) }
                )
            }

            AppScreen.FORM_PERSONAL_INFO -> {
                PersonalInfoFormScreen(
                    viewModel = viewModel,
                    formState = allAgreementForms.personalInfoForm,
                    onBack = { viewModel.updateScreen(AppScreen.AGREEMENTS_HUB) }
                )
            }

            AppScreen.FORM_VERIFICATION -> {
                VerificationFormScreen(
                    viewModel = viewModel,
                    formState = allAgreementForms.verificationForm,
                    onBack = { viewModel.updateScreen(AppScreen.AGREEMENTS_HUB) }
                )
            }



            AppScreen.ADMIN_PANEL -> {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    agreements = allAgreements,
                    searchQuery = searchQuery,
                    selectedDesignationFilter = selectedDesignationFilter,
                    selectedDateFilter = selectedDateFilter,
                    selectedDocumentCategoryFilter = selectedDocumentCategoryFilter,
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
                    viewModel.updateScreen(AppScreen.HOME)
                }
            }

            AppScreen.AGREEMENT_CONDITIONS -> {
                AgreementConditionsViewerScreen(
                    onBack = { viewModel.updateScreen(AppScreen.HOME) }
                )
            }

            AppScreen.APP_GUIDELINES -> {
                AppGuidelinesScreen(
                    onBack = { viewModel.updateScreen(AppScreen.HOME) }
                )
            }

            AppScreen.CLOUD_FUNCTIONS -> {
                CloudFunctionsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.updateScreen(AppScreen.HOME) }
                )
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
            },
            onGoToHome = {
                viewModel.closeConfirmationDialog()
                viewModel.updateScreen(AppScreen.HOME)
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

    // Information Dialogs
    if (showStampInfoDialog) {
        StampInfoDialog(onDismiss = { showStampInfoDialog = false })
    }
    if (showOrgInfoDialog) {
        OrgInfoDialog(onDismiss = { showOrgInfoDialog = false })
    }
    if (showTermsInfoDialog) {
        TermsInfoDialog(
            onDismiss = { showTermsInfoDialog = false },
            onViewFullScreen = { viewModel.updateScreen(AppScreen.AGREEMENT_CONDITIONS) }
        )
    }
    if (showContactInfoDialog) {
        ContactInfoDialog(onDismiss = { showContactInfoDialog = false })
    }
}

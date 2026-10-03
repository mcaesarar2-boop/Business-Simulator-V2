package com.example

import com.example.viewmodel.GameViewModel

import com.example.data.*

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.SplashScreen
import com.example.ma.ui.MaOfferTermSheetDialog
import com.example.ma.ui.MaNegotiationDialog
import com.example.ma.ui.MaInboxDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import com.example.localization.LanguageManager
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.stringResource
import android.content.res.Configuration
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.init(this)
        enableEdgeToEdge()
        setContent {
            val currentLocaleCode by LanguageManager.currentLocaleCode.collectAsState()
            val isRtl = LanguageManager.isCurrentLocaleRtl()
            val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            val localizedConfiguration = remember(currentLocaleCode) {
                Configuration(resources.configuration).apply {
                    val locale = Locale(currentLocaleCode)
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }

            LaunchedEffect(currentLocaleCode) {
                val locale = Locale(currentLocaleCode)
                Locale.setDefault(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(localizedConfiguration, resources.displayMetrics)
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalConfiguration provides localizedConfiguration
            ) {
                MyApplicationTheme {
                    MainApp()
                }
            }
        }
    }
}

// ==========================================

// ==========================================
// 2. NAVIGASI BOTTOM TABS (Gaya "Instagram")
// ==========================================
sealed class BottomNavItem(var title: String, var icon: ImageVector, var screen_route: String, @StringRes val titleRes: Int) {
    object Investing : BottomNavItem("Investasi", Icons.Filled.TrendingUp, "investing", R.string.nav_investing)
    object Business : BottomNavItem("Bisnis", Icons.Filled.BusinessCenter, "business", R.string.nav_business)
    object Earnings : BottomNavItem("Pendapatan", Icons.Filled.AttachMoney, "earnings", R.string.nav_earnings)
    object Items : BottomNavItem("Aset", Icons.Filled.ShoppingCart, "items", R.string.nav_assets)
    object Profile : BottomNavItem("Profil", Icons.Filled.Person, "profile", R.string.nav_profile)
}

@Composable
fun MainApp(viewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "splash"

    // Corporate M&A State
    val activeMaOffer by viewModel.maRepository.activePopupOffer.collectAsState()
    val showNegotiationDialog by viewModel.maRepository.showNegotiationDialog.collectAsState()
    val showMaInboxDialog by viewModel.maRepository.showInboxDialog.collectAsState()
    val stalledOffers by viewModel.maRepository.pendingStalledOffers.collectAsState()
    val dealHistory by viewModel.maRepository.dealHistory.collectAsState()
    val lastNegResult by viewModel.maRepository.lastNegotiationResult.collectAsState()

    Scaffold(
        bottomBar = { 
            if (showBottomBar) {
                BottomNavigationApp(navController = navController) 
            }
        }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController, 
            viewModel = viewModel, 
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
        )
    }

    // Unsolicited Corporate M&A Letter of Intent (LOI) Dialog
    if (activeMaOffer != null && !showNegotiationDialog) {
        MaOfferTermSheetDialog(
            offer = activeMaOffer!!,
            onAccept = { viewModel.acceptMaOffer(activeMaOffer!!) },
            onCounter = { viewModel.maRepository.openNegotiationDialog() },
            onStall = { viewModel.stallMaOffer(activeMaOffer!!) },
            onReject = { viewModel.rejectMaOffer(activeMaOffer!!) },
            onDismiss = { viewModel.maRepository.dismissPopup() }
        )
    }

    // Interactive Negotiation Dialog
    if (showNegotiationDialog && activeMaOffer != null) {
        MaNegotiationDialog(
            offer = activeMaOffer!!,
            lastResult = lastNegResult,
            onSubmitCounter = { mult, stake ->
                viewModel.submitMaCounterOffer(activeMaOffer!!, mult, stake)
            },
            onAcceptDeal = { viewModel.acceptMaOffer(activeMaOffer!!) },
            onDismiss = { viewModel.maRepository.closeNegotiationDialog() }
        )
    }

    // M&A Board Inbox Dialog
    if (showMaInboxDialog) {
        MaInboxDialog(
            stalledOffers = stalledOffers,
            dealHistory = dealHistory,
            onOpenOffer = { offer -> viewModel.maRepository.openOfferFromInbox(offer) },
            onDismiss = { viewModel.maRepository.closeInboxDialog() }
        )
    }
}

@Composable
fun BottomNavigationApp(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Investing,
        BottomNavItem.Business,
        BottomNavItem.Earnings,
        BottomNavItem.Items,
        BottomNavItem.Profile
    )
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val labelText = stringResource(item.titleRes)
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = labelText) },
                label = { androidx.compose.material3.Text(text = labelText, maxLines = 1, softWrap = false, fontSize = 11.sp, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                selected = currentRoute == item.screen_route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD700),
                    selectedTextColor = Color(0xFFFFD700),
                    indicatorColor = Color.White.copy(alpha = 0.05f),
                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                ),
                onClick = {
                    navController.navigate(item.screen_route) {
                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, viewModel: GameViewModel, modifier: Modifier) {
    NavHost(navController, startDestination = "splash", modifier = modifier) {
        composable("splash") { SplashScreen(navController) }
        composable(BottomNavItem.Investing.screen_route) { InvestingScreen(navController, viewModel) }
        composable(
            route = "global_stock_market?ticker={ticker}",
            arguments = listOf(navArgument("ticker") { 
                nullable = true
                defaultValue = null
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker")
            GlobalStockMarketScreen(navController, viewModel, initialTicker = ticker)
        }
        composable(
            route = "private_stock_market?ticker={ticker}",
            arguments = listOf(navArgument("ticker") { 
                nullable = true
                defaultValue = null
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker")
            com.example.ui.PrivateStockMarketScreen(navController, viewModel, initialTicker = ticker)
        }
        composable("my_portfolio_detail") { com.example.ui.MyPortfolioScreen(navController, viewModel) }
        composable("my_private_portfolio") { com.example.ui.PrivatePortfolioScreen(navController, viewModel) }
        composable("bank_savings") { com.example.ui.BankScreen(navController, viewModel) }
        composable("asset_management_hub") { com.example.ui.AssetManagementScreen(navController, viewModel) }
        composable(
            route = "ip_history/{instanceId}",
            arguments = listOf(navArgument("instanceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: return@composable
            IPLibraryHistoryScreen(navController, viewModel, instanceId)
        }
        composable("tv_ip_library") { TvIpLibraryScreen(navController, viewModel) }
        composable(BottomNavItem.Business.screen_route) { BusinessDashboardScreen(navController, viewModel) }
        composable(
            route = "business_catalog?holdingId={holdingId}&targetParentId={targetParentId}",
            arguments = listOf(
                navArgument("holdingId") {
                    nullable = true
                    defaultValue = null
                    type = NavType.StringType
                },
                navArgument("targetParentId") {
                    nullable = true
                    defaultValue = null
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val holdingId = backStackEntry.arguments?.getString("holdingId")
            val targetParentId = backStackEntry.arguments?.getString("targetParentId")
            BusinessCatalogScreen(navController, viewModel, holdingId, targetParentId)
        }
        composable("business_detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            BusinessDetailScreen(navController, viewModel, id)
        }
        composable("theme_park_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.ThemeParkDashboardScreen(navController, viewModel, id)
        }
        composable("theme_park_branch_detail/{instanceId}/{branchId}") { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: ""
            val branchId = backStackEntry.arguments?.getString("branchId") ?: ""
            com.example.ui.ThemeParkDetailScreen(navController, viewModel, instanceId, branchId)
        }
        composable("theme_park_marketing/{instanceId}/{branchId}") { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: ""
            val branchId = backStackEntry.arguments?.getString("branchId") ?: ""
            com.example.ui.MarketingAgencyScreen(navController, viewModel, instanceId, branchId)
        }
        composable("theme_park_facilities/{instanceId}/{branchId}") { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: ""
            val branchId = backStackEntry.arguments?.getString("branchId") ?: ""
            com.example.ui.FacilityCatalogScreen(navController, viewModel, instanceId, branchId)
        }
        composable("aviation_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.AviationDashboardScreen(navController, viewModel, id)
        }
        composable("cruise_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.CruiseDashboardUI(navController, viewModel, id)
        }
        composable("cruise_shipyard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.ShipyardUI(navController, viewModel, id)
        }
        composable("cruise_route_manager/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.RouteManagerUI(navController, viewModel, id)
        }
        composable("aviation_catalog_screen/{businessId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            com.example.ui.AviationCatalogScreen(navController, viewModel, businessId)
        }
        composable("aviation_hub_catalog/{businessId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            com.example.ui.HubCatalogScreen(navController, viewModel, businessId)
        }
        composable("hospitality_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.HospitalityDashboardScreen(navController, viewModel, id)
        }
        composable("build_hotel_property/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.BuildHotelPropertyScreen(navController, viewModel, id)
        }
        composable("hotel_detail/{businessId}/{hotelId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
            com.example.ui.HotelDetailScreen(navController, viewModel, businessId, hotelId)
        }
        composable("room_management/{businessId}/{hotelId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
            com.example.ui.RoomManagementScreen(navController, viewModel, businessId, hotelId)
        }
        composable("holding_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.HoldingDashboardScreen(navController, viewModel, id)
        }
        composable("mega_holding_detail") { 
            com.example.ui.MegaHoldingDetailScreen(navController, viewModel) 
        }
        composable("content_creator_screen") {
            com.example.ui.ContentCreatorScreen(
                navController = navController,
                gameViewModel = viewModel
            )
        }
        composable("content_creator_screen/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.ContentCreatorScreen(
                navController = navController,
                gameViewModel = viewModel,
                businessInstanceId = id
            )
        }
        composable("streaming_service_screen") {
            com.example.ui.StreamingServiceScreen(
                navController = navController,
                gameViewModel = viewModel
            )
        }
        composable("streaming_service_screen/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.StreamingServiceScreen(
                navController = navController,
                gameViewModel = viewModel,
                businessInstanceId = id
            )
        }
        composable("logistics_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.LogisticsScreen(navController, viewModel, id)
        }
        composable("apartment_property/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.ApartmentPropertyScreen(navController, viewModel, id)
        }
        composable("software_house_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.SoftwareHouseDashboardScreen(navController, viewModel, id)
        }
        composable("banking_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.BankingDashboardScreen(navController, viewModel, id)
        }
        composable("football_club_dashboard") {
            com.example.ui.FootballClubScreen(navController, viewModel)
        }
        composable("football_club_dashboard/{id}") {
            com.example.ui.FootballClubScreen(navController, viewModel)
        }
        composable("football_club_acquisition") {
            com.example.ui.FootballClubAcquisitionScreen(navController, viewModel)
        }
        composable("football_club_acquisition/{holdingId}") { backStackEntry ->
            val holdingId = backStackEntry.arguments?.getString("holdingId")
            com.example.ui.FootballClubAcquisitionScreen(navController, viewModel, holdingId)
        }
        composable("football_manager_market") {
            com.example.ui.FootballManagerMarketScreen(navController, viewModel)
        }
        composable("football_scouting_market") {
            com.example.ui.FootballScoutingMarketScreen(navController, viewModel)
        }
        composable("game_publisher_hub") {
            com.example.publisher.ui.GamePublisherHubScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("game_publisher_hub/{id}") {
            com.example.publisher.ui.GamePublisherHubScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("ai_cloud_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.aicloud.ui.AiCloudDashboardScreen(
                navController = navController,
                gameViewModel = viewModel,
                businessInstanceId = id
            )
        }
        composable(BottomNavItem.Earnings.screen_route) { EarningsScreen(viewModel) }
        composable(BottomNavItem.Items.screen_route) { com.example.ui.CollectionScreen(navController, viewModel) }
        composable("garage") { com.example.ui.GarageScreen(navController, viewModel) }
        composable("housing") { com.example.ui.HousingScreen(navController, viewModel) }
        composable("tax_legal") { com.example.ui.TaxLegalScreen(navController, viewModel) }
        composable("private_equity") { com.example.privateequity.ui.PrivateEquityScreen(navController, viewModel) }
        composable("global_tycoon_index") { com.example.ui.GlobalTycoonIndexScreen(navController, viewModel) }
        composable(BottomNavItem.Profile.screen_route) { ProfileScreen(navController, viewModel) }
        composable("family_office") { com.example.ui.FamilyOfficeScreen(navController, viewModel) }
        composable("private_ledger") { com.example.ui.PrivateLedgerScreen(navController, viewModel) }
        composable("tax_and_audit") { com.example.ui.TaxAndAuditScreen(navController, viewModel) }
        composable("private_lifestyle") { com.example.ui.lifestyle.LifestyleDashboardScreen(navController, viewModel) }
        composable("private_travel_concierge") { com.example.ui.lifestyle.TravelConciergeScreen(navController, viewModel) }
        composable("private_foundation_dashboard") { com.example.ui.FoundationDashboardScreen(navController, viewModel) }
        composable("private_foundation_detail/{foundationId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            com.example.ui.FoundationDetailScreen(navController, viewModel, foundationId)
        }
        composable("foundation_pre_built/{foundationId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            com.example.ui.FoundationPreBuiltScreen(navController, viewModel, foundationId)
        }
        composable("kindergarten_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.KindergartenDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("kindergarten_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.KindergartenFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("primary_school_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.JuniorSchoolDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("junior_school_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.JuniorSchoolFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("high_school_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.HighSchoolDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("high_school_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.HighSchoolFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("university_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.UniversityDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("university_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.UniversityFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("clinic_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.ClinicDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("clinic_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.ClinicFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("general_hospital_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.GeneralHospitalDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("general_hospital_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.GeneralHospitalFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("specialized_hospital_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.SpecializedHospitalDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("specialized_hospital_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.SpecializedHospitalFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("international_hospital_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.InternationalHospitalDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("international_hospital_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.InternationalHospitalFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }

        // --- CHARITY ROUTES ---
        composable("humanitarian_aid_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.HumanitarianAidDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("humanitarian_aid_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.HumanitarianAidFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }

        composable("social_care_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.SocialCareDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("social_care_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.SocialCareFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }

        composable("disaster_relief_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.DisasterReliefDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("disaster_relief_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.DisasterReliefFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }

        composable("community_empowerment_dashboard/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.CommunityEmpowermentDashboardScreen(navController, viewModel, foundationId, institutionId)
        }
        composable("community_empowerment_facility_catalogue/{foundationId}/{institutionId}") { backStackEntry ->
            val foundationId = backStackEntry.arguments?.getString("foundationId") ?: ""
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.example.ui.charity.CommunityEmpowermentFacilityCatalogueScreen(navController, viewModel, foundationId, institutionId)
        }

        // --- INDIE GAME PUBLISHER & INCUBATOR ROUTES ---
        composable("game_publisher_hub") {
            val playerState by viewModel.playerState.collectAsState()
            val publisherBusiness = playerState.ownedBusinesses.find { it.catalogId == "indie_game_publisher" }
                ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "indie_game_publisher" }
            val holding = playerState.holdingCompanies.find { h -> h.subsidiaries.any { it.catalogId == "indie_game_publisher" } }
            
            com.example.publisher.ui.GamePublisherHubScreen(
                onNavigateBack = { navController.popBackStack() },
                onLiquidateBusiness = {
                    publisherBusiness?.let { biz ->
                        viewModel.liquidateBusiness(biz.instanceId)
                    }
                },
                onInjectCash = { amount ->
                    publisherBusiness?.let { biz ->
                        viewModel.injectCapitalToBusiness(biz.instanceId, amount)
                    }
                },
                onWithdrawCash = { amount ->
                    publisherBusiness?.let { biz ->
                        viewModel.withdrawCapitalFromBusiness(biz.instanceId, amount)
                    }
                },
                playerCash = playerState.cash,
                holdingCash = (holding?.holdingCash ?: playerState.holdingCompanies.firstOrNull()?.holdingCash ?: 0.0).toLong()
            )
        }
        composable("game_publisher_hub/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val playerState by viewModel.playerState.collectAsState()
            val holding = playerState.holdingCompanies.find { h -> h.subsidiaries.any { it.instanceId == id } }

            com.example.publisher.ui.GamePublisherHubScreen(
                onNavigateBack = { navController.popBackStack() },
                onLiquidateBusiness = {
                    viewModel.liquidateBusiness(id)
                },
                onInjectCash = { amount ->
                    viewModel.injectCapitalToBusiness(id, amount)
                },
                onWithdrawCash = { amount ->
                    viewModel.withdrawCapitalFromBusiness(id, amount)
                },
                playerCash = playerState.cash,
                holdingCash = (holding?.holdingCash ?: playerState.holdingCompanies.firstOrNull()?.holdingCash ?: 0.0).toLong()
            )
        }
    }
}

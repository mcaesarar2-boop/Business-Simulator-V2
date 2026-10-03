package com.example.privateequity.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.totalMonthlyDebtObligation
import com.example.data.totalOutstandingDebt
import com.example.privateequity.data.PrivateEquityRepository
import com.example.privateequity.engine.PrivateEquityEngine
import com.example.privateequity.model.FundingType
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateEquityScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val playerState by viewModel.playerState.collectAsState()

    // Dynamic valuation & loan limit
    val totalBusinessValuation = remember(playerState) {
        PrivateEquityRepository.getBusinessValuation(playerState)
    }

    val maxLoanDynamic = remember(totalBusinessValuation) {
        PrivateEquityRepository.getMaxLoanLimit(totalBusinessValuation)
    }

    val sectors = remember { PrivateEquityRepository.defaultSectors }
    var selectedSectorIndex by remember { mutableStateOf(0) }
    val selectedSector = sectors[selectedSectorIndex]

    var loanSliderValue by remember { mutableStateOf(100_000.0) }
    val loanAmount = loanSliderValue.toLong().coerceIn(10_000L, maxLoanDynamic)

    var selectedFundingType by remember { mutableStateOf(FundingType.DEBT) }

    val estimation = remember(loanAmount, selectedSector, totalBusinessValuation, selectedFundingType, playerState.playerEquityShare) {
        PrivateEquityEngine.calculateEstimation(
            loanAmount = loanAmount,
            sector = selectedSector,
            valuation = totalBusinessValuation,
            fundingType = selectedFundingType,
            currentEquity = playerState.playerEquityShare
        )
    }

    var showSuccessDialog by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Private Equity & Investors",
                        fontWeight = FontWeight.Bold,
                        color = PrivateEquityTheme.TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = PrivateEquityTheme.TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrivateEquityTheme.BgDark)
            )
        },
        containerColor = PrivateEquityTheme.BgDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. STATS OVERVIEW PANEL
            item {
                PrivateEquityPortfolioCard(
                    playerEquityShare = playerState.playerEquityShare,
                    totalBusinessValuation = totalBusinessValuation,
                    totalOutstandingDebt = playerState.totalOutstandingDebt,
                    totalMonthlyPayment = playerState.totalMonthlyDebtObligation
                )
            }

            // 2. SECTOR SELECTOR & LOAN APPLICATION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrivateEquityTheme.CardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AJUKAN PEMBIAYAAN INVESTOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrivateEquityTheme.AccentCyan,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SectorSelectorSection(
                            sectors = sectors,
                            selectedIndex = selectedSectorIndex,
                            onSelectIndex = { selectedSectorIndex = it }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        LoanSliderSection(
                            loanAmount = loanAmount,
                            sliderValue = loanSliderValue,
                            maxLimit = maxLoanDynamic,
                            onSliderChange = { loanSliderValue = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        FundingTypeSelector(
                            selectedType = selectedFundingType,
                            baseMonthlyPayment = estimation.baseMonthlyPayment,
                            baseEquityGiven = estimation.baseEquityGiven,
                            onSelectType = { selectedFundingType = it }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        EstimationBreakdownCard(estimation = estimation)

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val err = viewModel.applyForInvestorsLoan(
                                    sectorName = selectedSector.name,
                                    loanAmount = loanAmount,
                                    tenorMonths = selectedSector.tenor,
                                    interestRate = selectedSector.interestRate,
                                    dilutionMultiplier = selectedSector.dilutionMultiplier,
                                    fundingType = selectedFundingType
                                )
                                if (err != null) {
                                    showErrorDialog = err
                                } else {
                                    showSuccessDialog = "Pengajuan dana disetujui! Dana pembiayaan langsung dicairkan ke Kas Utama Perusahaan (Company Cash) Anda."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (estimation.isControlViolation) Color.Gray else PrivateEquityTheme.AccentCyan,
                                contentColor = PrivateEquityTheme.BgDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !estimation.isControlViolation,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (estimation.isControlViolation) "DITOLAK (SAHAM < 51%)" else "Ajukan Pembiayaan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 3. OUTSTANDING DEBTS PANEL
            activeLoansListItems(loans = playerState.activeInvestorsLoans)

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Alerts and Dialogs
    showSuccessDialog?.let { msg ->
        AlertDialog(
            onDismissRequest = { showSuccessDialog = null },
            title = { Text("Transaksi Berhasil", fontWeight = FontWeight.Bold, color = PrivateEquityTheme.NeonGreen) },
            text = { Text(msg, color = PrivateEquityTheme.TextWhite) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = null }) {
                    Text("Ok", color = PrivateEquityTheme.AccentCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = PrivateEquityTheme.CardDark
        )
    }

    showErrorDialog?.let { msg ->
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Transaksi Ditolak", fontWeight = FontWeight.Bold, color = PrivateEquityTheme.ErrorRed) },
            text = { Text(msg, color = PrivateEquityTheme.TextWhite) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text("Tutup", color = PrivateEquityTheme.AccentCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = PrivateEquityTheme.CardDark
        )
    }
}

package com.example.viewmodel

import com.example.data.*

data class AdPackage(
    val name: String,
    val durationMonths: Int,
    val boostPercentage: Int,
    val boostMultiplier: Double,
    val totalPrice: Long
)

object ThemeParkEngine {

    /**
     * Proses kalkulasi bulanan untuk satu cabang taman hiburan.
     */
    fun processMonthlyTick(branch: ThemeParkBranch, seasonMultiplier: Double): ThemeParkBranch {
        var b = branch
        
        // Kurangi sisa masa bidding jika ada
        if (b.remainingBiddingMonths > 0) {
            b = b.copy(remainingBiddingMonths = b.remainingBiddingMonths - 1)
        }
        
        // Update status konstruksi & perbaikan wahana
        val updatedRides = b.rides.map { ride ->
            var r = ride
            if (r.isConstructing && r.constructionMonthsLeft > 0) {
                val newLeft = r.constructionMonthsLeft - 1
                r = r.copy(
                    constructionMonthsLeft = newLeft,
                    isConstructing = newLeft > 0
                )
            } else if (r.isUnderMaintenance && r.maintenanceMonthsLeft > 0) {
                val maintLeft = r.maintenanceMonthsLeft - 1
                r = r.copy(
                    maintenanceMonthsLeft = maintLeft,
                    isUnderMaintenance = maintLeft > 0
                )
            }
            r
        }.toMutableList()
        
        if (b.isLaunched) {
            // Update ad status & decrement duration
            if (b.adMonthsLeft > 0) {
                b = b.copy(adMonthsLeft = b.adMonthsLeft - 1)
            } else if (b.activeAdName != null) {
                b = b.copy(activeAdName = null, adBoostMultiplier = 1.0, adMonthsLeft = 0)
            }

            val maxTheoreticalCapacity = b.landType.maxSlots * 3000
            var activeRidesCount = 0
            var totalRideAppeal = 0.0
            var ridesMaintenance = 0L

            for (ride in updatedRides) {
                if (!ride.isConstructing) {
                    activeRidesCount++
                    val isPaused = ride.isPaused
                    val isMaint = ride.isUnderMaintenance
                    
                    if (!isPaused && !isMaint) {
                        var rideTier = (ride.baseMonthlyVisitors / 100).toDouble()
                        
                        // --- LOGIKA SINERGI IP FILM ---
                        if (ride.ipThemeScore != null) {
                            val synergyMultiplier = 1.0 + (ride.ipThemeScore!! / 100.0)
                            rideTier *= synergyMultiplier
                        }
                        
                        totalRideAppeal += rideTier
                        ridesMaintenance += ride.maintenanceCost
                    } else if (isMaint) {
                        ridesMaintenance += ride.maintenanceCost
                    } else if (isPaused) {
                        ridesMaintenance += (ride.maintenanceCost * 0.5).toLong()
                    }
                }
            }
            
            val facilitiesMaintenance = b.facilities.sumOf { it.maintenanceCost }
            val facilitiesAppeal = b.facilities.sumOf { it.appealBoost }
            val totalRideAppealWithFacilities = totalRideAppeal + facilitiesAppeal
            val totalMaintenanceWithFacilities = ridesMaintenance + facilitiesMaintenance

            val activeCapacity = 5000 + (activeRidesCount * 3000)
            val finalCapacityCap = minOf(activeCapacity, maxTheoreticalCapacity)

            val isDisaster = (1..100).random() <= 2
            if (isDisaster) {
                val disasters = listOf("Badai Merusak Fasilitas", "Insiden Wahana", "Pemogokan Karyawan")
                b = b.copy(
                    activeDisaster = disasters.random(), 
                    lastMonthVisitors = 0,
                    lastMonthProfit = -totalMaintenanceWithFacilities * 2,
                    lastMonthRevenue = 0L,
                    lastMonthExpense = totalMaintenanceWithFacilities * 2
                )
            } else {
                b = b.copy(activeDisaster = null)
                
                var rawDemand = 10000 + (totalRideAppealWithFacilities * 500)

                val avgTicketPrice = (b.priceRegular + b.priceTerusan + (b.priceFamily / 4.0)) / 3.0
                val baseIdealPrice = calculateIdealPrice(totalRideAppealWithFacilities)
                val idealPrice = baseIdealPrice * b.adBoostMultiplier

                val priceMultiplier = if (avgTicketPrice > idealPrice && idealPrice > 0) {
                    idealPrice / avgTicketPrice
                } else if (idealPrice > 0) {
                    1.0 + ((idealPrice - avgTicketPrice) / idealPrice) * 0.5
                } else 1.0

                // Terapkan multiplier ke demand, termasuk adBoostMultiplier jika ada promosi aktif
                rawDemand = (rawDemand * priceMultiplier * seasonMultiplier * b.adBoostMultiplier)

                var finalVisitorCount = minOf(rawDemand.toInt(), finalCapacityCap)

                // Nerf Penalti Elastisitas Harga:
                // Jika harga tiket Reguler atau Terusan berada di sekitar idealPrice (+/- 10%),
                // jumlah pengunjung hanya boleh turun maksimal 15% dari kapasitas maksimal (finalCapacityCap).
                if (idealPrice > 0) {
                    val regPrice = b.priceRegular.toDouble()
                    val terPrice = b.priceTerusan.toDouble()
                    val isRegNearIdeal = regPrice >= (idealPrice * 0.9) && regPrice <= (idealPrice * 1.1)
                    val isTerNearIdeal = terPrice >= (idealPrice * 0.9) && terPrice <= (idealPrice * 1.1)
                    
                    if (isRegNearIdeal || isTerNearIdeal) {
                        val minAllowedVisitors = (finalCapacityCap * 0.85).toInt()
                        if (finalVisitorCount < minAllowedVisitors) {
                            finalVisitorCount = minAllowedVisitors
                        }
                    }
                }

                val vipGuests = (finalVisitorCount * 0.10).toInt()
                val familyGroups = ((finalVisitorCount * 0.25) / 4).toInt()
                val terusanGuests = (finalVisitorCount * 0.30).toInt()
                val regularGuests = finalVisitorCount - vipGuests - (familyGroups * 4) - terusanGuests

                val ticketRevenue = (vipGuests * b.priceVIP) + (familyGroups * b.priceFamily) + (terusanGuests * b.priceTerusan) + (regularGuests * b.priceRegular)
                
                // Buff F&B Revenue:
                val totalFnbBoost = 1.0 + (b.facilities.sumOf { it.fnbBoostPercent } / 100.0)
                // Base jajan pengunjung $45 dikalikan buff fasilitas
                val avgSpendPerVisitor = 45.0 * totalFnbBoost 
                val baseFnBRevenue = (finalVisitorCount * 0.8 * avgSpendPerVisitor).toLong()
                val fnBRevenue = (baseFnBRevenue * b.adBoostMultiplier).toLong()

                var totalIncome = ticketRevenue + fnBRevenue

                if (b.hypeMonthsLeft > 0) {
                    finalVisitorCount += (finalVisitorCount * 0.5).toInt()
                    totalIncome = (totalIncome * 2.0).toLong()
                    b = b.copy(hypeMonthsLeft = b.hypeMonthsLeft - 1, hasHypeMarketing = b.hypeMonthsLeft - 1 > 0)
                }

                val staffCost = (finalVisitorCount / 100) * 1500L
                val totalExpense = staffCost + totalMaintenanceWithFacilities
                val netProfit = totalIncome - totalExpense

                b = b.copy(
                    lastMonthVisitors = finalVisitorCount,
                    lastMonthProfit = netProfit,
                    lastMonthRevenue = totalIncome,
                    lastMonthExpense = totalExpense
                )
            }
        } else {
            b = b.copy(lastMonthVisitors = 0, lastMonthProfit = 0L, lastMonthRevenue = 0L, lastMonthExpense = 0L)
        }
        
        return b.copy(rides = updatedRides, remainingBiddingMonths = b.remainingBiddingMonths)
    }

    /**
     * Hitung Harga Ideal berdasarkan total daya tarik wahana (totalRideAppeal).
     */
    fun calculateIdealPrice(totalRideAppeal: Double): Double {
        return 10.0 + (totalRideAppeal * 5.0)
    }

    /**
     * Hitung Iklan Dinamis berbasis Nilai Dasar Taman.
     */
    fun calculateAdPackages(branch: ThemeParkBranch, idealPrice: Double): List<AdPackage> {
        val baseValue = idealPrice * (branch.landType.maxSlots * 3000.0)
        return listOf(
            AdPackage(
                name = "Bronze",
                durationMonths = 12,
                boostPercentage = 30,
                boostMultiplier = 1.30,
                totalPrice = (baseValue * 0.40).toLong().coerceAtLeast(10_000L)
            ),
            AdPackage(
                name = "Silver",
                durationMonths = 24,
                boostPercentage = 45,
                boostMultiplier = 1.45,
                totalPrice = (baseValue * 0.70).toLong().coerceAtLeast(20_000L)
            ),
            AdPackage(
                name = "Gold",
                durationMonths = 48,
                boostPercentage = 65,
                boostMultiplier = 1.65,
                totalPrice = (baseValue * 1.10).toLong().coerceAtLeast(40_000L)
            ),
            AdPackage(
                name = "Platinum",
                durationMonths = 60,
                boostPercentage = 75,
                boostMultiplier = 1.75,
                totalPrice = (baseValue * 1.25).toLong().coerceAtLeast(50_000L)
            ),
            AdPackage(
                name = "Diamond Monopoly",
                durationMonths = 120,
                boostPercentage = 100,
                boostMultiplier = 2.00,
                totalPrice = (baseValue * 2.00).toLong().coerceAtLeast(100_000L)
            )
        )
    }

    data class ThemeParkFacilityCatalogEntry(
        val catalogId: String,
        val name: String,
        val buildCost: Long,
        val maintenanceCost: Long,
        val fnbBoostPercent: Double,
        val appealBoost: Double,
        val icon: String,
        val description: String
    )

    val facilitiesCatalog = listOf(
        ThemeParkFacilityCatalogEntry("toilet_standar", "Toilet Standar", 500_000L, 15_000L, 2.0, 0.5, "🚽", "Fasilitas sanitasi pokok yang menjaga kenyamanan minimal bagi seluruh pengunjung."),
        ThemeParkFacilityCatalogEntry("restroom_premium", "Restroom Premium & Lounge", 2_500_000L, 60_000L, 8.0, 2.0, "🛋️", "Toilet ber-AC premium lengkap dengan ruang rias dan lounge tunggu mewah."),
        ThemeParkFacilityCatalogEntry("klinik_p3k", "Klinik P3K & Medis", 1_000_000L, 40_000L, 1.0, 1.5, "🏥", "Pusat medis darurat siaga cepat untuk melayani cedera atau kelelahan di taman."),
        ThemeParkFacilityCatalogEntry("mushola_kecil", "Mushola Kecil", 800_000L, 10_000L, 3.0, 1.0, "🕌", "Tempat ibadah minimalis yang tenang dan sejuk untuk para pengunjung Muslim."),
        ThemeParkFacilityCatalogEntry("masjid_taman", "Masjid Taman", 3_000_000L, 50_000L, 7.0, 2.5, "🕌", "Masjid berkapasitas sedang lengkap dengan fasilitas wudhu luas dan penyejuk udara."),
        ThemeParkFacilityCatalogEntry("masjid_raya", "Masjid Raya (Landmark Area)", 15_000_000L, 150_000L, 15.0, 8.0, "🕌", "Landmark megah bernilai arsitektur tinggi, menampung ribuan jamaah secara agung."),
        ThemeParkFacilityCatalogEntry("pusat_informasi", "Pusat Informasi & Loker", 1_200_000L, 25_000L, 4.0, 1.0, "ℹ️", "Pusat bantuan navigasi wisata, peta panduan fisik, ramah disabilitas, dan loker penitipan barang."),
        ThemeParkFacilityCatalogEntry("gazebo_rest_area", "Gazebo & Rest Area Taman", 2_000_000L, 30_000L, 10.0, 2.0, "🌳", "Area istirahat teduh rindang, dipenuhi gazebo-gazebo etnik untuk santap santai keluarga.")
    )
}

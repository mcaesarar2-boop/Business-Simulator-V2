package com.example.data

data class ActiveLawsuit(
    val id: String,
    val title: String,
    val description: String,
    val scaleFactor: Long // We will use this to scale damages & lawyer logic
)

data class TaxLegalReport(
    val hasNotary: Boolean = false,
    val unpaidTaxes: Long = 0,
    val activeLawsuits: List<ActiveLawsuit> = emptyList(),
    val isTaxHavenActive: Boolean = false,
    val frozenBusinessId: String? = null
)

fun generateRandomLawsuit(netWorth: Long): ActiveLawsuit {
    val isBillionaire = netWorth > 10_000_000L
    val cases = if (isBillionaire) {
        listOf(
            Pair("Kebocoran Data Server", "Database aplikasi Anda diretas, pengguna menuntut ganti rugi masif."),
            Pair("Pencemaran Nama Baik", "Konten digital Anda menyinggung tokoh penting, mereka melayangkan somasi miliaran."),
            Pair("Gugatan Monopoli", "Komisi pengawas persaingan usaha menyelidiki dugaan praktek monopoli bisnis Anda.")
        )
    } else {
        listOf(
            Pair("Gugatan Pelanggaran Hak Cipta", "Ada pihak yang mengklaim aset visual atau video di channel media Anda."),
            Pair("Kecelakaan Kerja Ringan", "Seseorang menuntut kompensasi akibat cedera di properti Anda."),
            Pair("Sengketa Kontrak Vendor", "Sebuah vendor menuntut pembayaran penalti atas dugaan pembatalan sepihak.")
        )
    }
    
    val randomCase = cases.random()
    // Dynamic scale based on netWorth
    val scale = (netWorth * 0.05).toLong().coerceAtLeast(1000L).coerceAtMost(250_000_000L)
    
    return ActiveLawsuit(
        id = "lawsuit_${System.currentTimeMillis()}",
        title = randomCase.first,
        description = randomCase.second,
        scaleFactor = scale
    )
}

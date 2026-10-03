package com.example.data

data class HousingItem(
    val id: String,
    val name: String,
    val location: String,
    val type: String,
    val buyPrice: Long,
    val rentPrice: Long,
    val imageUrl: String? = null
)

data class OwnedHousing(
    val instanceId: String = java.util.UUID.randomUUID().toString(),
    val housingId: String,
    val purchasedPrice: Long,
    val customImageUrl: String? = null
)

data class RentedHousing(
    val instanceId: String = java.util.UUID.randomUUID().toString(),
    val housingId: String,
    val monthlyRent: Long,
    val customImageUrl: String? = null
)

val REAL_ESTATE_MASTER_CATALOG = listOf(
    // --- JAKARTA AREA ---
    HousingItem("jkt_kalideres", "Rumah Minimalis Citra Garden, Kalideres, Jakarta Barat", "Jakarta Barat", "Rumah Subsidi/Entry Level", 65000L, 325L),
    HousingItem("jkt_jagakarsa", "Cluster Minimalis Type A Jagakarsa, Jakarta Selatan", "Jakarta Selatan", "Compact Townhouse", 115000L, 575L),
    HousingItem("jkt_cilangkap", "Townhouse Modern Cilangkap, Cipayung, Jakarta Timur", "Jakarta Timur", "Medium Townhouse", 140000L, 700L),
    HousingItem("jkt_kelapa_gading", "Rumah Renovasi Modern Kelapa Gading, Jakarta Utara", "Jakarta Utara", "Residential House", 280000L, 1400L),
    HousingItem("jkt_kemang_residence", "Premium Townhouse Kemang Timur, Mampang, Jakarta Selatan", "Jakarta Selatan", "Premium Townhouse", 550000L, 2750L),
    HousingItem("jkt_puri_indah", "Classic Mansion Blok A Puri Indah, Kembangan, Jakarta Barat", "Jakarta Barat", "Luxury Mansion", 1200000L, 6000L),
    HousingItem("jkt_menteng_mansion", "Mansion Kolonial Jl. Teuku Umar, Menteng, Jakarta Pusat", "Jakarta Pusat", "Ultra-Luxury Historic Mansion", 4500000L, 22500L),
    HousingItem("jkt_pondok_indah", "Modern Tropical Mansion Jl. Duta Permai, Pondok Indah, Jakarta Selatan", "Jakarta Selatan", "Ultra-Luxury Mansion", 5200000L, 26000L),

    // --- BOGOR & DEPOK AREA ---
    HousingItem("bgr_cileungsi", "Rumah Subsidi Grand Kahuripan, Cileungsi, Bogor", "Bogor", "Entry Level House", 15000L, 75L),
    HousingItem("dpk_sawangan", "Cluster Scandinavian Sawangan Baru, Depok", "Depok", "Suburban Townhouse", 55000L, 275L),
    HousingItem("bgr_cibinong", "Cluster Nirwana Estate, Cibinong, Bogor", "Bogor", "Medium Residential", 75000L, 375L),
    HousingItem("bgr_sentul_alpinia", "Cluster Alpinia Sentul City, Babakan Madang, Bogor", "Bogor", "Premium Cluster", 210000L, 1050L),
    HousingItem("bgr_sentul_mansion", "Hillside Villa Cluster Golf Highland, Sentul City, Bogor", "Bogor", "Luxury Villa House", 950000L, 4750L),

    // --- TANGERANG & BEKASI AREA (SATELLITE CITIES) ---
    HousingItem("bks_tambun", "Rumah Cluster Bekasi Timur Regensi, Tambun, Bekasi", "Bekasi", "Entry Level House", 35000L, 175L),
    HousingItem("tng_pasar_kemis", "Cluster Suvarna Sutera, Pasar Kemis, Tangerang", "Tangerang", "Medium Cluster", 90000L, 450L),
    HousingItem("bks_summarecon", "Cluster Burgundy Summarecon Bekasi, Bekasi Utara", "Bekasi Utara", "Premium Cluster", 185000L, 925L),
    HousingItem("tng_bsd_lyndon", "The Lyndon Cluster, Navapark BSD City, Tangerang", "Tangerang", "Luxury Mansion", 1300000L, 6500L),

    // --- BANDUNG AREA ---
    HousingItem("bdg_rancaekek", "Perumahan Subsidi Rancaekek Kencana, Bandung Kabupaten", "Bandung", "Entry Level House", 16000L, 80L),
    HousingItem("bdg_antapani", "Rumah Minimalis Jl. Puri Dago, Antapani, Bandung", "Bandung", "Medium Residential", 85000L, 425L),
    HousingItem("bdg_buah_batu", "Cluster Podomoro Park, Buah Batu, Bandung", "Bandung", "Premium Cluster", 160000L, 800L),
    HousingItem("bdg_dago_pakar", "Modern Hillside Villa Dago Pakar, Cimenyan, Bandung", "Bandung", "Luxury Villa", 480000L, 2400L),
    HousingItem("bdg_setiabudi", "Mansion Cluster Setiabudi Regency, Sukasari, Bandung", "Bandung", "Luxury Mansion", 1100000L, 5500L),

    // --- SURABAYA AREA ---
    HousingItem("sby_rungkut", "Rumah Minimalis Gunung Anyar, Rungkut, Surabaya", "Surabaya", "Compact House", 60000L, 300L),
    HousingItem("sby_wiyung", "Cluster Taman Pondok Indah, Wiyung, Surabaya", "Surabaya", "Medium Residential", 110000L, 550L),
    HousingItem("sby_pakuwon_city", "Cluster San Diego, Pakuwon City, Mulyorejo, Surabaya", "Surabaya", "Premium Cluster", 350000L, 1750L),
    HousingItem("sby_citraland", "Modern Tropical House Cluster Golf Avenue, Citraland, Surabaya", "Surabaya", "Luxury Residential", 850000L, 4250L),
    HousingItem("sby_graha_famili", "Golf View Mansion Blok M, Graha Famili, Wiyung, Surabaya", "Surabaya", "Luxury Mansion", 2400000L, 12000L),

    // --- BALI AREA ---
    HousingItem("bali_singaraja", "Rumah Subsidi BTN Tukadmungga, Buleleng, Singaraja", "Singaraja", "Entry Level House", 14000L, 70L),
    HousingItem("bali_negara", "Perumahan Sederhana Melaya, Jembrana", "Bali", "Entry Level House", 18000L, 90L),
    HousingItem("bali_tabanan_kost", "Kos-kosan Mahasiswa/Pekerja Kediri, Tabanan", "Tabanan", "Commercial / Boarding House", 35000L, 175L),
    HousingItem("bali_gianyar_house", "Rumah Minimalis Blahbatuh, Gianyar", "Gianyar", "Compact House", 45000L, 225L),
    HousingItem("bali_denpasar_utara", "Townhouse Ergonomis Peguyangan, Denpasar Utara", "Denpasar", "Compact Townhouse", 65000L, 325L),
    HousingItem("bali_denpasar", "Cluster Minimalis Jl. Tukad Badung, Renon, Denpasar", "Denpasar", "Medium Townhouse", 95000L, 475L),
    HousingItem("bali_kuta_apartment", "Apartemen Studio Central Kuta, Badung", "Kuta", "Medium Apartment", 85000L, 425L),
    HousingItem("bali_seminyak_shophouse", "Ruko Komersial Jl. Raya Seminyak, Kuta", "Seminyak", "Commercial Property", 350000L, 1750L),
    HousingItem("bali_jimbaran_town", "Jimbaran Hijau Townhouse, Kuta Selatan, Badung", "Badung", "Premium Residential", 220000L, 1100L),
    HousingItem("bali_nusa_dua_cluster", "Cluster Eksklusif Kampial, Nusa Dua, Badung", "Nusa Dua", "Premium Cluster", 180000L, 900L),
    HousingItem("bali_sanur_villa", "Private Villa Jl. Danau Tamblingan, Sanur, Denpasar", "Sanur", "Boutique Villa", 450000L, 2250L),
    HousingItem("bali_canggu_loft", "Mezzanine Loft Pererenan, Canggu", "Canggu", "Premium Loft", 320000L, 1600L),
    HousingItem("bali_ubud_jungle", "River View Villa Sayan, Ubud, Gianyar", "Ubud", "Luxury Eco-Villa", 850000L, 4250L),
    HousingItem("bali_canggu_beach", "Modern Loft Villa Jl. Batu Bolong, Canggu, Badung", "Canggu", "Premium Beachside Villa", 1200000L, 6000L),
    HousingItem("bali_uluwatu_cliff", "Cliffedge Oceanfront Villa, Pecatu, Uluwatu, Bali", "Uluwatu", "Ultra-Luxury Cliff Villa", 3800000L, 19000L),
    HousingItem("bali_nusa_penida", "Luxury Resort Villa Nusa Penida, Klungkung", "Nusa Penida", "Ultra-Luxury Resort Villa", 2500000L, 12500L),

    // --- OTHER MAJOR CITIES (MEDAN, SEMARANG, JOGJA, MAKASSAR) ---
    HousingItem("mdn_johor", "Rumah Cluster Medan Johor, Kota Medan", "Medan", "Medium Residential", 55000L, 275L),
    HousingItem("smg_candi", "Hillside House Lokasi Candi Golf, Semarang Candi", "Semarang", "Premium Cluster", 250000L, 1250L),
    HousingItem("jog_sleman", "Heritage Villa Cluster Jl. Kaliurang KM 10, Sleman, Yogyakarta", "Yogyakarta", "Premium Residential", 180000L, 900L),
    HousingItem("mks_panakkukang", "Cluster Orchid Vida View, Panakkukang, Makassar", "Makassar", "Medium Residential", 95000L, 475L),
    HousingItem("mdn_citraland", "Mansion Cluster Bagya City, Deli Serdang, Medan", "Medan", "Luxury Mansion", 750000L, 3750L),
    HousingItem("jog_merapi", "Exclusive Private Villa View Gunung Merapi, Pakem, Yogyakarta", "Yogyakarta", "Luxury Villa", 600000L, 3000L),

    // --- BINTARO JAYA AREA (SECTOR 1-9) ---
    HousingItem("bintaro_s1", "Rumah Tua Hitung Tanah Sektor 1, Bintaro Jaya, Jakarta Selatan", "Bintaro", "Old House / Land Investment", 250000L, 1250L),
    HousingItem("bintaro_s2_townhouse", "Townhouse Modern Bintaro Sektor 2, Ciputat Timur", "Bintaro", "Compact Townhouse", 135000L, 675L),
    HousingItem("bintaro_s3_classic", "Rumah Klasik Asri Sektor 3, Bintaro Jaya", "Bintaro", "Medium Residential", 180000L, 900L),
    HousingItem("bintaro_s4_renovated", "Rumah Renovasi Siap Huni Bintaro Sektor 4", "Bintaro", "Medium Residential", 195000L, 975L),
    HousingItem("bintaro_s5_stan", "Kos-kosan Mahasiswa Dekat STAN Sektor 5, Bintaro", "Bintaro", "Commercial / Boarding House", 320000L, 1600L),
    HousingItem("bintaro_s6_corner", "Rumah Hoek Premium Bintaro Sektor 6, Pondok Aren", "Bintaro", "Premium Residential", 380000L, 1900L),
    HousingItem("bintaro_s7_menteng", "Cluster Menteng Bintaro, Sektor 7, Bintaro Jaya", "Bintaro", "Premium Cluster", 450000L, 2250L),
    HousingItem("bintaro_s8_discovery", "Discovery Residences Sektor 8, Bintaro Jaya", "Bintaro", "Premium Cluster", 350000L, 1750L),
    HousingItem("bintaro_s9_emeraldi", "Emerald Bintaro Sektor 9, Pondok Aren, Tangerang Selatan", "Bintaro", "Luxury Residential", 650000L, 3250L),
    HousingItem("bintaro_s9_kebanyoran", "Cluster Kebayoran Residences Sektor 9, Bintaro Jaya", "Bintaro", "Luxury Residential", 720000L, 3600L),

    // --- PREMIUM APARTMENTS & TOWNHOUSES (MENTENG, THAMRIN, CBD) ---
    HousingItem("jkt_keraton_plaza", "Keraton at The Plaza, Thamrin, Jakarta Pusat", "Jakarta Pusat", "Ultra-Luxury Apartment", 3500000L, 17500L),
    HousingItem("jkt_kempinski_hi", "Kempinski Private Residences, Bundaran HI, Jakarta Pusat", "Jakarta Pusat", "Luxury Apartment", 1500000L, 7500L),
    HousingItem("jkt_le_parc_thamrin", "Le Parc at Thamrin Nine, Thamrin, Jakarta Pusat", "Jakarta Pusat", "Luxury Low-rise Apartment", 2800000L, 14000L),
    HousingItem("jkt_menteng_park", "Menteng Park Apartment, Cikini, Menteng, Jakarta Pusat", "Jakarta Pusat", "Premium Apartment", 350000L, 1750L),
    HousingItem("jkt_menteng_townhouse", "Townhouse Eksklusif Jl. Cik Ditiro, Menteng, Jakarta Pusat", "Jakarta Pusat", "Premium Townhouse", 1800000L, 9000L),
    HousingItem("jkt_langham_scbd", "The Langham Residences, SCBD, Jakarta Selatan", "Jakarta Selatan", "Ultra-Luxury Branded Residence", 4200000L, 21000L),
    HousingItem("jkt_st_regis_kuningan", "The St. Regis Residences, Kuningan, Jakarta Selatan", "Jakarta Selatan", "Luxury Branded Residence", 2500000L, 12500L),
    HousingItem("jkt_anandamaya_sudirman", "Anandamaya Residences, Sudirman, Jakarta Pusat", "Jakarta Pusat", "Luxury Apartment", 1200000L, 6000L),
    HousingItem("jkt_thamrin_penthouse", "Thamrin Residence Executive Penthouse, Thamrin, Jakarta Pusat", "Jakarta Pusat", "Penthouse Apartment", 950000L, 4750L),
    HousingItem("jkt_casa_domaine", "Casa Domaine Tower 1, Tanah Abang, Jakarta Pusat", "Jakarta Pusat", "Premium Apartment", 850000L, 4250L),

    // --- KALIMANTAN AREA (BALIKPAPAN, BANJARMASIN, PONTIANAK, IKN) ---
    HousingItem("bpn_batakan", "Cluster Batakan Village, Balikpapan Timur", "Balikpapan", "Medium Residential", 70000L, 350L),
    HousingItem("bpn_grand_city", "Grand City Balikpapan Cluster Pineville", "Balikpapan", "Premium Cluster", 220000L, 1100L),
    HousingItem("bpn_bsb_penthouse", "Penthouse Balikpapan Superblock (BSB), Balikpapan Selatan", "Balikpapan", "Luxury Apartment", 450000L, 2250L),
    HousingItem("bpn_kemala_ocean", "Ocean View Villa Pantai Kemala, Balikpapan", "Balikpapan", "Luxury Villa", 600000L, 3000L),
    HousingItem("bjm_sungai_andai", "Perumahan Sungai Andai, Banjarmasin Utara", "Banjarmasin", "Entry Level House", 20000L, 100L),
    HousingItem("bjm_citraland", "Cluster The Mansion, CitraLand Banjarmasin", "Banjarmasin", "Luxury Residential", 180000L, 900L),
    HousingItem("bjm_gatot_subroto", "Rumah Mewah Eksklusif Jl. Gatot Subroto, Banjarmasin", "Banjarmasin", "Premium House", 350000L, 1750L),
    HousingItem("ptk_kubu_raya", "Rumah Subsidi Sungai Raya Dalam, Kubu Raya, Pontianak", "Pontianak", "Entry Level House", 15000L, 75L),
    HousingItem("ptk_perdana", "Townhouse Modern Jl. Perdana, Pontianak Selatan", "Pontianak", "Compact Townhouse", 65000L, 325L),
    HousingItem("ptk_ayani_mansion", "Mansion Klasik Jl. Ahmad Yani, Pontianak", "Pontianak", "Luxury Mansion", 280000L, 1400L),
    HousingItem("smd_citraland", "Cluster Mutiara, CitraLand City Samarinda", "Samarinda", "Premium Cluster", 200000L, 1000L),
    HousingItem("smd_alaya", "Perumahan Alaya, Sungai Pinang, Samarinda", "Samarinda", "Medium Residential", 120000L, 600L),
    HousingItem("plk_tjilik_riwut", "Cluster Minimalis Jl. Tjilik Riwut, Palangkaraya", "Palangkaraya", "Medium Residential", 45000L, 225L),
    HousingItem("ikn_smart_eco", "Smart Eco-Home Nusantara Cluster, IKN (Nusantara)", "IKN (Nusantara)", "Premium Eco-Home", 320000L, 1600L),

    // --- USA: NEW YORK, WASHINGTON, & EAST COAST ---
    HousingItem("usa_nyc_penthouse", "Billionaires Row Penthouse, Manhattan, New York", "New York", "Ultra-Luxury Penthouse", 35000000L, 175000L),
    HousingItem("usa_nyc_brownstone", "Classic Brownstone, Brooklyn, New York", "New York", "Historic Townhouse", 4500000L, 22500L),
    HousingItem("usa_dc_georgetown", "Historic Colonial Estate, Georgetown, Washington D.C.", "Washington D.C.", "Luxury Estate", 6500000L, 32500L),
    HousingItem("usa_wa_seattle", "Lake Washington Waterfront Home, Seattle, Washington", "Seattle", "Luxury Waterfront House", 5200000L, 26000L),

    // --- USA: FLORIDA (MIAMI, ORLANDO, DISNEY WORLD AREA) ---
    HousingItem("usa_mia_star_island", "Oceanfront Mansion, Star Island, Miami, Florida", "Miami", "Ultra-Luxury Mansion", 45000000L, 225000L),
    HousingItem("usa_mia_brickell", "Luxury Ocean View Condo, Brickell, Miami, Florida", "Miami", "Premium Apartment", 2500000L, 12500L),
    HousingItem("usa_orl_golden_oak", "Disney Golden Oak Custom Estate, Walt Disney World Resort, Orlando", "Orlando", "Ultra-Luxury Resort Estate", 8500000L, 42500L),
    HousingItem("usa_orl_kissimmee", "Vacation Pool Villa near Disney, Kissimmee, Florida", "Orlando", "Holiday Villa", 650000L, 3250L),
    HousingItem("usa_fl_palm_beach", "Mediterranean Estate, Palm Beach, Florida", "Miami", "Ultra-Luxury Estate", 25000000L, 125000L),

    // --- USA: CALIFORNIA (LOS ANGELES & WEST COAST) ---
    HousingItem("usa_la_beverly_hills", "Modern Mega Mansion, Beverly Hills, Los Angeles, California", "Beverly Hills", "Ultra-Luxury Mansion", 55000000L, 275000L),
    HousingItem("usa_la_hollywood", "Mid-Century Modern Home, Hollywood Hills, California", "Los Angeles", "Premium Residential", 3800000L, 19000L),
    HousingItem("usa_la_malibu", "Beachfront Modern Villa, Malibu, California", "Los Angeles", "Ultra-Luxury Beach House", 22000000L, 110000L),

    // --- MIDDLE EAST: DUBAI, ABU DHABI, MECCA ---
    HousingItem("uae_dxb_palm", "Signature Villa Frond G, Palm Jumeirah, Dubai", "Dubai", "Ultra-Luxury Beachfront Villa", 15000000L, 75000L),
    HousingItem("uae_dxb_burj", "Luxury Residence Downtown, Burj Khalifa, Dubai", "Dubai", "Premium Apartment", 3500000L, 17500L),
    HousingItem("uae_auh_saadiyat", "Saadiyat Reserve Villa, Saadiyat Island, Abu Dhabi", "Abu Dhabi", "Luxury Villa", 4200000L, 21000L),
    HousingItem("ksa_mecca_haram", "Haram View Luxury Suite, Abraj Al Bait, Mecca, KSA", "Mecca, KSA", "Ultra-Luxury Suite/Apartment", 5500000L, 27500L),

    // --- EUROPE: PARIS, LONDON, & OTHERS ---
    HousingItem("eu_paris_eiffel", "Haussmannian Apartment with Eiffel View, 7th Arr., Paris, France", "Paris", "Luxury Classic Apartment", 4800000L, 24000L),
    HousingItem("eu_paris_marais", "Modern Loft in Le Marais, Paris, France", "Paris", "Premium Loft", 1800000L, 9000L),
    HousingItem("eu_lon_mayfair", "Historic Townhouse, Mayfair, London, UK", "London", "Ultra-Luxury Townhouse", 18500000L, 92500L),
    HousingItem("eu_lon_hyde", "Hyde Park Penthouse, Knightsbridge, London, UK", "London", "Ultra-Luxury Penthouse", 32000000L, 160000L),

    // --- AFRICA & ASIA: CASABLANCA, TOKYO, SINGAPORE ---
    HousingItem("mar_casablanca_anfa", "Oceanfront Modern Villa, Anfa Superieur, Casablanca, Morocco", "Casablanca", "Luxury Villa", 3200000L, 16000L),
    HousingItem("jp_tyo_roppongi", "Roppongi Hills Residence Penthouse, Tokyo, Japan", "Tokyo", "Ultra-Luxury Penthouse", 12000000L, 60000L),
    HousingItem("sg_sentosa_cove", "Oceanfront Bungalow, Sentosa Cove, Singapore", "Singapore", "Ultra-Luxury Bungalow", 22000000L, 110000L)
)

val initialHousingItems = REAL_ESTATE_MASTER_CATALOG

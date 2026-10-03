package com.example.data

data class FlightDestination(
    val id: String,
    val origin: String,
    val dest: String,
    val distance: Int,
    val type: String, // "domestik" or "internasional"
    val minPrice: Int,
    val maxPrice: Int,
    val flag: String,
    val airportCode: String = ""
) {
    val medianPrice: Int
        get() = (minPrice + maxPrice) / 2

    val distanceCategory: String
        get() = when {
            distance < 1500 -> "SHORT"
            distance < 4000 -> "MEDIUM"
            else -> "LONG"
        }
}

object FlightRouteCatalogue {

    fun calculateDynamicPricing(distance: Int, isDomestic: Boolean): Pair<Int, Int> {
        val basePrice = if (isDomestic) {
            20.0 + (distance * 0.05)
        } else {
            50.0 + (distance * 0.08)
        }
        val minP = (basePrice * 0.70).toInt().coerceAtLeast(15)
        val maxP = (basePrice * 1.50).toInt().coerceAtLeast(minP + 20)
        return Pair(minP, maxP)
    }

    fun calculateEstimatedDemand(baseDemand: Int, price: Int, minPrice: Int, maxPrice: Int): Int {
        if (maxPrice <= minPrice) return baseDemand
        val clampedPrice = price.coerceIn(minPrice, maxPrice)
        // Price ratio: 0.0 (cheapest -> 100% demand) to 1.0 (most expensive -> 30% demand)
        val priceRatio = (clampedPrice - minPrice).toFloat() / (maxPrice - minPrice)
        val demandMultiplier = 1.0f - (priceRatio * 0.70f) // 1.0 down to 0.30
        return (baseDemand * demandMultiplier).toInt().coerceAtLeast(100)
    }

    val ROUTES: List<FlightDestination> = listOf(
        // ================= DOMESTIK (INDONESIA 🇮🇩) =================
        FlightDestination("CGK-AAP", "Jakarta (CGK)", "Samarinda (AAP)", 1180, "domestik", 55, 180, "🇮🇩", "AAP"),
        FlightDestination("CGK-AMQ", "Jakarta (CGK)", "Ambon (AMQ)", 2300, "domestik", 120, 360, "🇮🇩", "AMQ"),
        FlightDestination("CGK-BDJ", "Jakarta (CGK)", "Banjarmasin (BDJ)", 575, "domestik", 30, 110, "🇮🇩", "BDJ"),
        FlightDestination("CGK-BEJ", "Jakarta (CGK)", "Berau (BEJ)", 1450, "domestik", 70, 230, "🇮🇩", "BEJ"),
        FlightDestination("CGK-BIK", "Jakarta (CGK)", "Biak (BIK)", 3000, "domestik", 160, 460, "🇮🇩", "BIK"),
        FlightDestination("CGK-BKS", "Jakarta (CGK)", "Bengkulu (BKS)", 590, "domestik", 30, 110, "🇮🇩", "BKS"),
        FlightDestination("CGK-BPN", "Jakarta (CGK)", "Balikpapan (BPN)", 1250, "domestik", 60, 200, "🇮🇩", "BPN"),
        FlightDestination("CGK-BTH", "Jakarta (CGK)", "Batam (BTH)", 850, "domestik", 40, 140, "🇮🇩", "BTH"),
        FlightDestination("CGK-BTJ", "Jakarta (CGK)", "Banda Aceh (BTJ)", 1690, "domestik", 80, 250, "🇮🇩", "BTJ"),
        FlightDestination("CGK-BUU", "Jakarta (CGK)", "Muara Bungo (BUU)", 620, "domestik", 35, 120, "🇮🇩", "BUU"),
        FlightDestination("CGK-DJB", "Jakarta (CGK)", "Jambi (DJB)", 520, "domestik", 30, 100, "🇮🇩", "DJB"),
        FlightDestination("CGK-DPS", "Jakarta (CGK)", "Bali (DPS)", 980, "domestik", 45, 150, "🇮🇩", "DPS"),
        FlightDestination("CGK-HLP", "Jakarta (CGK)", "Jakarta Halim (HLP)", 30, "domestik", 10, 30, "🇮🇩", "HLP"),
        FlightDestination("CGK-KBR", "Jakarta (CGK)", "Kaimana (KBR)", 2550, "domestik", 140, 400, "🇮🇩", "KBR"),
        FlightDestination("CGK-KDI", "Jakarta (CGK)", "Kendari (KDI)", 1900, "domestik", 90, 290, "🇮🇩", "KDI"),
        FlightDestination("CGK-KNO", "Jakarta (CGK)", "Medan (KNO)", 1416, "domestik", 60, 220, "🇮🇩", "KNO"),
        FlightDestination("CGK-KOE", "Jakarta (CGK)", "Kupang (KOE)", 1860, "domestik", 90, 280, "🇮🇩", "KOE"),
        FlightDestination("CGK-LBJ", "Jakarta (CGK)", "Labuan Bajo (LBJ)", 1470, "domestik", 70, 220, "🇮🇩", "LBJ"),
        FlightDestination("CGK-LOP", "Jakarta (CGK)", "Lombok (LOP)", 1070, "domestik", 50, 170, "🇮🇩", "LOP"),
        FlightDestination("CGK-MDC", "Jakarta (CGK)", "Manado (MDC)", 2200, "domestik", 110, 320, "🇮🇩", "MDC"),
        FlightDestination("CGK-MES", "Jakarta (CGK)", "Medan Polonia (MES)", 1410, "domestik", 60, 220, "🇮🇩", "MES"),
        FlightDestination("CGK-MTW", "Jakarta (CGK)", "Maumere (MTW)", 1670, "domestik", 90, 260, "🇮🇩", "MTW"),
        FlightDestination("CGK-PDG", "Jakarta (CGK)", "Padang (PDG)", 913, "domestik", 45, 170, "🇮🇩", "PDG"),
        FlightDestination("CGK-PGK", "Jakarta (CGK)", "Pangkal Pinang (PGK)", 420, "domestik", 25, 90, "🇮🇩", "PGK"),
        FlightDestination("CGK-PKN", "Jakarta (CGK)", "Pangkalan Bun (PKN)", 540, "domestik", 30, 110, "🇮🇩", "PKN"),
        FlightDestination("CGK-PLM", "Jakarta (CGK)", "Palembang (PLM)", 423, "domestik", 25, 90, "🇮🇩", "PLM"),
        FlightDestination("CGK-PKU", "Jakarta (CGK)", "Pekanbaru (PKU)", 930, "domestik", 45, 160, "🇮🇩", "PKU"),
        FlightDestination("CGK-PNK", "Jakarta (CGK)", "Pontianak (PNK)", 740, "domestik", 40, 140, "🇮🇩", "PNK"),
        FlightDestination("CGK-SOQ", "Jakarta (CGK)", "Sorong (SOQ)", 2750, "domestik", 140, 420, "🇮🇩", "SOQ"),
        FlightDestination("CGK-SRG", "Jakarta (CGK)", "Semarang (SRG)", 405, "domestik", 25, 95, "🇮🇩", "SRG"),
        FlightDestination("CGK-SUB", "Jakarta (CGK)", "Surabaya (SUB)", 690, "domestik", 35, 120, "🇮🇩", "SUB"),
        FlightDestination("CGK-TKG", "Jakarta (CGK)", "Bandar Lampung (TKG)", 195, "domestik", 20, 70, "🇮🇩", "TKG"),
        FlightDestination("CGK-TNJ", "Jakarta (CGK)", "Tanjung Pinang (TNJ)", 790, "domestik", 40, 130, "🇮🇩", "TNJ"),
        FlightDestination("CGK-TRK", "Jakarta (CGK)", "Tarakan (TRK)", 1500, "domestik", 75, 230, "🇮🇩", "TRK"),
        FlightDestination("CGK-TTE", "Jakarta (CGK)", "Ternate (TTE)", 2400, "domestik", 120, 360, "🇮🇩", "TTE"),
        FlightDestination("CGK-UPG", "Jakarta (CGK)", "Makassar (UPG)", 1408, "domestik", 65, 230, "🇮🇩", "UPG"),
        FlightDestination("CGK-YIA", "Jakarta (CGK)", "Yogyakarta (YIA)", 525, "domestik", 28, 100, "🇮🇩", "YIA"),

        // ================= ASIA TENGGARA (ASEAN) =================
        FlightDestination("CGK-SIN", "Jakarta (CGK)", "Singapore (SIN)", 878, "internasional", 80, 300, "🇸🇬", "SIN"),
        FlightDestination("CGK-KUL", "Jakarta (CGK)", "Kuala Lumpur (KUL)", 1124, "internasional", 70, 250, "🇲🇾", "KUL"),
        FlightDestination("CGK-JHB", "Jakarta (CGK)", "Johor Bahru (JHB)", 720, "internasional", 70, 220, "🇲🇾", "JHB"),
        FlightDestination("CGK-BKI", "Jakarta (CGK)", "Kota Kinabalu (BKI)", 1510, "internasional", 120, 340, "🇲🇾", "BKI"),
        FlightDestination("CGK-BTU", "Jakarta (CGK)", "Bintulu (BTU)", 960, "internasional", 90, 260, "🇲🇾", "BTU"),
        FlightDestination("CGK-DMK", "Jakarta (CGK)", "Bangkok Don Mueang (DMK)", 2310, "internasional", 120, 350, "🇹🇭", "DMK"),
        FlightDestination("CGK-CNX", "Jakarta (CGK)", "Chiang Mai (CNX)", 2400, "internasional", 150, 400, "🇹🇭", "CNX"),
        FlightDestination("CGK-HAN", "Jakarta (CGK)", "Hanoi (HAN)", 3050, "internasional", 180, 480, "🇻🇳", "HAN"),
        FlightDestination("CGK-MNL", "Jakarta (CGK)", "Manila (MNL)", 2790, "internasional", 180, 450, "🇵🇭", "MNL"),
        FlightDestination("CGK-RGX", "Jakarta (CGK)", "Yangon (RGN)", 1910, "internasional", 140, 360, "🇲🇲", "RGN"),

        // ================= ASIA TIMUR =================
        FlightDestination("CGK-HKG", "Jakarta (CGK)", "Hong Kong (HKG)", 3240, "internasional", 180, 500, "🇭🇰", "HKG"),
        FlightDestination("CGK-MFM", "Jakarta (CGK)", "Macau (MFM)", 3370, "internasional", 190, 520, "🇲🇴", "MFM"),
        FlightDestination("CGK-TPE", "Jakarta (CGK)", "Taipei (TPE)", 3800, "internasional", 220, 580, "🇹🇼", "TPE"),
        FlightDestination("CGK-KHH", "Jakarta (CGK)", "Kaohsiung (KHH)", 3550, "internasional", 210, 540, "🇹🇼", "KHH"),
        FlightDestination("CGK-CAN", "Jakarta (CGK)", "Guangzhou (CAN)", 3270, "internasional", 200, 550, "🇨🇳", "CAN"),
        FlightDestination("CGK-SZX", "Jakarta (CGK)", "Shenzhen (SZX)", 3270, "internasional", 200, 560, "🇨🇳", "SZX"),
        FlightDestination("CGK-CSX", "Jakarta (CGK)", "Changsha (CSX)", 3900, "internasional", 220, 620, "🇨🇳", "CSX"),
        FlightDestination("CGK-FOC", "Jakarta (CGK)", "Fuzhou (FOC)", 3900, "internasional", 230, 640, "🇨🇳", "FOC"),
        FlightDestination("CGK-PVG", "Jakarta (CGK)", "Shanghai Pudong (PVG)", 4470, "internasional", 250, 700, "🇨🇳", "PVG"),
        FlightDestination("CGK-PEK", "Jakarta (CGK)", "Beijing (PEK)", 5210, "internasional", 310, 820, "🇨🇳", "PEK"),
        FlightDestination("CGK-XMN", "Jakarta (CGK)", "Xiamen (XMN)", 3570, "internasional", 210, 580, "🇨🇳", "XMN"),
        FlightDestination("CGK-KMG", "Jakarta (CGK)", "Kunming (KMG)", 3070, "internasional", 190, 520, "🇨🇳", "KMG"),
        FlightDestination("CGK-CTU", "Jakarta (CGK)", "Chengdu (CTU)", 4080, "internasional", 240, 650, "🇨🇳", "CTU"),
        FlightDestination("CGK-HGH", "Jakarta (CGK)", "Hangzhou (HGH)", 4230, "internasional", 250, 660, "🇨🇳", "HGH"),
        FlightDestination("CGK-NKG", "Jakarta (CGK)", "Nanjing (NKG)", 4330, "internasional", 250, 670, "🇨🇳", "NKG"),
        FlightDestination("CGK-TAO", "Jakarta (CGK)", "Qingdao (TAO)", 4860, "internasional", 290, 760, "🇨🇳", "TAO"),
        FlightDestination("CGK-HND", "Jakarta (CGK)", "Tokyo Haneda (HND)", 5780, "internasional", 350, 900, "🇯🇵", "HND"),
        FlightDestination("CGK-NRT", "Jakarta (CGK)", "Tokyo Narita (NRT)", 5770, "internasional", 350, 900, "🇯🇵", "NRT"),
        FlightDestination("CGK-KIX", "Jakarta (CGK)", "Osaka (KIX)", 4930, "internasional", 300, 780, "🇯🇵", "KIX"),
        FlightDestination("CGK-NGO", "Jakarta (CGK)", "Nagoya (NGO)", 5220, "internasional", 320, 810, "🇯🇵", "NGO"),
        FlightDestination("CGK-FUK", "Jakarta (CGK)", "Fukuoka (FUK)", 4780, "internasional", 290, 750, "🇯🇵", "FUK"),
        FlightDestination("CGK-CTS", "Jakarta (CGK)", "Sapporo (CTS)", 6090, "internasional", 370, 950, "🇯🇵", "CTS"),
        FlightDestination("CGK-OKA", "Jakarta (CGK)", "Okinawa (OKA)", 4300, "internasional", 260, 700, "🇯🇵", "OKA"),
        FlightDestination("CGK-ICN", "Jakarta (CGK)", "Seoul Incheon (ICN)", 5240, "internasional", 320, 850, "🇰🇷", "ICN"),
        FlightDestination("CGK-PUS", "Jakarta (CGK)", "Busan (PUS)", 4930, "internasional", 300, 790, "🇰🇷", "PUS"),

        // ================= ASIA SELATAN =================
        FlightDestination("CGK-DEL", "Jakarta (CGK)", "New Delhi (DEL)", 4880, "internasional", 280, 720, "🇮🇳", "DEL"),
        FlightDestination("CGK-BOM", "Jakarta (CGK)", "Mumbai (BOM)", 4470, "internasional", 260, 680, "🇮🇳", "BOM"),
        FlightDestination("CGK-BLR", "Jakarta (CGK)", "Bengaluru (BLR)", 4300, "internasional", 250, 650, "🇮🇳", "BLR"),
        FlightDestination("CGK-MAA", "Jakarta (CGK)", "Chennai (MAA)", 3500, "internasional", 220, 560, "🇮🇳", "MAA"),
        FlightDestination("CGK-CCU", "Jakarta (CGK)", "Kolkata (CCU)", 3950, "internasional", 230, 590, "🇮🇳", "CCU"),
        FlightDestination("CGK-HYD", "Jakarta (CGK)", "Hyderabad (HYD)", 4700, "internasional", 280, 720, "🇮🇳", "HYD"),
        FlightDestination("CGK-KTM", "Jakarta (CGK)", "Kathmandu (KTM)", 4600, "internasional", 270, 700, "🇳🇵", "KTM"),
        FlightDestination("CGK-CMB", "Jakarta (CGK)", "Colombo (CMB)", 3350, "internasional", 210, 520, "🇱🇰", "CMB"),
        FlightDestination("CGK-MLE", "Jakarta (CGK)", "Malé (MLE)", 3910, "internasional", 240, 610, "🇲🇻", "MLE"),

        // ================= TIMUR TENGAH =================
        FlightDestination("CGK-DXB", "Jakarta (CGK)", "Dubai (DXB)", 6560, "internasional", 400, 980, "🇦🇪", "DXB"),
        FlightDestination("CGK-AUH", "Jakarta (CGK)", "Abu Dhabi (AUH)", 6550, "internasional", 390, 950, "🇦🇪", "AUH"),
        FlightDestination("CGK-DOH", "Jakarta (CGK)", "Doha (DOH)", 6900, "internasional", 420, 990, "🇶🇦", "DOH"),
        FlightDestination("CGK-KWI", "Jakarta (CGK)", "Kuwait City (KWI)", 7300, "internasional", 440, 1050, "🇰🇼", "KWI"),
        FlightDestination("CGK-MCT", "Jakarta (CGK)", "Muscat (MCT)", 6500, "internasional", 390, 950, "🇴🇲", "MCT"),
        FlightDestination("CGK-BAH", "Jakarta (CGK)", "Bahrain (BAH)", 7100, "internasional", 430, 1020, "🇧🇭", "BAH"),
        FlightDestination("CGK-RUH", "Jakarta (CGK)", "Riyadh (RUH)", 7900, "internasional", 470, 1150, "🇸🇦", "RUH"),
        FlightDestination("CGK-JED", "Jakarta (CGK)", "Jeddah (JED)", 7900, "internasional", 480, 1200, "🇸🇦", "JED"),
        FlightDestination("CGK-IST", "Jakarta (CGK)", "Istanbul (IST)", 9500, "internasional", 520, 1400, "🇹🇷", "IST"),

        // ================= OCEANIA & AUSTRALIA =================
        FlightDestination("CGK-SYD", "Jakarta (CGK)", "Sydney (SYD)", 5510, "internasional", 300, 850, "🇦🇺", "SYD"),
        FlightDestination("CGK-MEL", "Jakarta (CGK)", "Melbourne (MEL)", 5210, "internasional", 290, 820, "🇦🇺", "MEL"),
        FlightDestination("CGK-BNE", "Jakarta (CGK)", "Brisbane (BNE)", 4710, "internasional", 270, 760, "🇦🇺", "BNE"),
        FlightDestination("CGK-PER", "Jakarta (CGK)", "Perth (PER)", 3010, "internasional", 170, 450, "🇦🇺", "PER"),
        FlightDestination("CGK-ADL", "Jakarta (CGK)", "Adelaide (ADL)", 4670, "internasional", 270, 750, "🇦🇺", "ADL"),
        FlightDestination("CGK-AKL", "Jakarta (CGK)", "Auckland (AKL)", 8710, "internasional", 480, 1250, "🇳🇿", "AKL"),

        // ================= EROPA =================
        FlightDestination("CGK-LHR", "Jakarta (CGK)", "London Heathrow (LHR)", 11720, "internasional", 620, 1700, "🇬🇧", "LHR"),
        FlightDestination("CGK-LGW", "Jakarta (CGK)", "London Gatwick (LGW)", 11730, "internasional", 610, 1650, "🇬🇧", "LGW"),
        FlightDestination("CGK-CDG", "Jakarta (CGK)", "Paris Charles de Gaulle (CDG)", 11530, "internasional", 610, 1680, "🇫🇷", "CDG"),
        FlightDestination("CGK-FRA", "Jakarta (CGK)", "Frankfurt (FRA)", 11140, "internasional", 590, 1600, "🇩🇪", "FRA"),
        FlightDestination("CGK-MUC", "Jakarta (CGK)", "Munich (MUC)", 10960, "internasional", 580, 1580, "🇩🇪", "MUC"),
        FlightDestination("CGK-AMS", "Jakarta (CGK)", "Amsterdam Schiphol (AMS)", 11350, "internasional", 600, 1650, "🇳🇱", "AMS"),
        FlightDestination("CGK-BRU", "Jakarta (CGK)", "Brussels (BRU)", 11380, "internasional", 600, 1620, "🇧🇪", "BRU"),
        FlightDestination("CGK-ZRH", "Jakarta (CGK)", "Zurich (ZRH)", 10920, "internasional", 580, 1580, "🇨🇭", "ZRH"),
        FlightDestination("CGK-VIE", "Jakarta (CGK)", "Vienna (VIE)", 10140, "internasional", 540, 1500, "🇦🇹", "VIE"),
        FlightDestination("CGK-MAD", "Jakarta (CGK)", "Madrid (MAD)", 12100, "internasional", 630, 1750, "🇪🇸", "MAD"),
        FlightDestination("CGK-BCN", "Jakarta (CGK)", "Barcelona (BCN)", 11780, "internasional", 610, 1700, "🇪🇸", "BCN"),
        FlightDestination("CGK-FCO", "Jakarta (CGK)", "Rome Fiumicino (FCO)", 10320, "internasional", 550, 1520, "🇮🇹", "FCO"),
        FlightDestination("CGK-MXP", "Jakarta (CGK)", "Milan Malpensa (MXP)", 10750, "internasional", 570, 1550, "🇮🇹", "MXP"),
        FlightDestination("CGK-CPH", "Jakarta (CGK)", "Copenhagen (CPH)", 10480, "internasional", 560, 1540, "🇩🇰", "CPH"),
        FlightDestination("CGK-OSL", "Jakarta (CGK)", "Oslo (OSL)", 10650, "internasional", 570, 1560, "🇳🇴", "OSL"),
        FlightDestination("CGK-ARN", "Jakarta (CGK)", "Stockholm (ARN)", 10280, "internasional", 560, 1520, "🇸🇪", "ARN"),
        FlightDestination("CGK-HEL", "Jakarta (CGK)", "Helsinki (HEL)", 9540, "internasional", 520, 1450, "🇫🇮", "HEL"),
        FlightDestination("CGK-LIS", "Jakarta (CGK)", "Lisbon (LIS)", 12780, "internasional", 650, 1800, "🇵🇹", "LIS"),
        FlightDestination("CGK-DUB", "Jakarta (CGK)", "Dublin (DUB)", 12080, "internasional", 630, 1750, "🇮🇪", "DUB"),
        FlightDestination("CGK-ATH", "Jakarta (CGK)", "Athens (ATH)", 9360, "internasional", 500, 1400, "🇬🇷", "ATH"),
        FlightDestination("CGK-PRG", "Jakarta (CGK)", "Prague (PRG)", 10420, "internasional", 550, 1500, "🇨🇿", "PRG"),
        FlightDestination("CGK-WAW", "Jakarta (CGK)", "Warsaw (WAW)", 10050, "internasional", 530, 1480, "🇵🇱", "WAW"),
        FlightDestination("CGK-BUD", "Jakarta (CGK)", "Budapest (BUD)", 9980, "internasional", 530, 1460, "🇭🇺", "BUD"),
        FlightDestination("CGK-MSQ", "Jakarta (CGK)", "Minsk (MSQ)", 9300, "internasional", 600, 1500, "🇧🇾", "MSQ"),

        // ================= AMERIKA UTARA =================
        FlightDestination("CGK-LAX", "Jakarta (CGK)", "Los Angeles (LAX)", 14480, "internasional", 700, 2200, "🇺🇸", "LAX"),
        FlightDestination("CGK-SFO", "Jakarta (CGK)", "San Francisco (SFO)", 14120, "internasional", 690, 2100, "🇺🇸", "SFO"),
        FlightDestination("CGK-JFK", "Jakarta (CGK)", "New York (JFK)", 16170, "internasional", 820, 2600, "🇺🇸", "JFK"),
        FlightDestination("CGK-EWR", "Jakarta (CGK)", "Newark (EWR)", 16140, "internasional", 820, 2550, "🇺🇸", "EWR"),
        FlightDestination("CGK-BOS", "Jakarta (CGK)", "Boston (BOS)", 16290, "internasional", 830, 2600, "🇺🇸", "BOS"),
        FlightDestination("CGK-IAD", "Jakarta (CGK)", "Washington D.C. (IAD)", 16320, "internasional", 830, 2550, "🇺🇸", "IAD"),
        FlightDestination("CGK-MIA", "Jakarta (CGK)", "Miami (MIA)", 17500, "internasional", 900, 2800, "🇺🇸", "MIA"),
        FlightDestination("CGK-ORD", "Jakarta (CGK)", "Chicago (ORD)", 15620, "internasional", 790, 2450, "🇺🇸", "ORD"),
        FlightDestination("CGK-SEA", "Jakarta (CGK)", "Seattle (SEA)", 13120, "internasional", 650, 1950, "🇺🇸", "SEA"),
        FlightDestination("CGK-YVR", "Jakarta (CGK)", "Vancouver (YVR)", 12880, "internasional", 640, 1900, "🇨🇦", "YVR"),
        FlightDestination("CGK-YYZ", "Jakarta (CGK)", "Toronto (YYZ)", 15820, "internasional", 810, 2500, "🇨🇦", "YYZ"),
        FlightDestination("CGK-YUL", "Jakarta (CGK)", "Montreal (YUL)", 16070, "internasional", 820, 2550, "🇨🇦", "YUL"),

        // ================= AFRIKA =================
        FlightDestination("CGK-JNB", "Jakarta (CGK)", "Johannesburg (JNB)", 8630, "internasional", 480, 1300, "🇿🇦", "JNB"),
        FlightDestination("CGK-CPT", "Jakarta (CGK)", "Cape Town (CPT)", 9670, "internasional", 540, 1450, "🇿🇦", "CPT"),
        FlightDestination("CGK-NBO", "Jakarta (CGK)", "Nairobi (NBO)", 7600, "internasional", 450, 1100, "🇰🇪", "NBO"),
        FlightDestination("CGK-CAI", "Jakarta (CGK)", "Cairo (CAI)", 9010, "internasional", 500, 1350, "🇪🇬", "CAI"),
        FlightDestination("CGK-CAS", "Jakarta (CGK)", "Casablanca (CAS)", 12180, "internasional", 650, 1800, "🇲🇦", "CAS"),

        // ================= AMERIKA LATIN =================
        FlightDestination("CGK-GRU", "Jakarta (CGK)", "São Paulo (GRU)", 15950, "internasional", 820, 2600, "🇧🇷", "GRU"),
        FlightDestination("CGK-EZE", "Jakarta (CGK)", "Buenos Aires (EZE)", 15880, "internasional", 810, 2550, "🇦🇷", "EZE"),
        FlightDestination("CGK-SCL", "Jakarta (CGK)", "Santiago (SCL)", 16840, "internasional", 880, 2700, "🇨🇱", "SCL"),
        FlightDestination("CGK-LIM", "Jakarta (CGK)", "Lima (LIM)", 18110, "internasional", 950, 2900, "🇵🇪", "LIM")
    )
}

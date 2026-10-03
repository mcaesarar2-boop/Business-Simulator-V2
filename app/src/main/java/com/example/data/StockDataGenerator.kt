package com.example.data

import java.util.Locale

fun getDomainForTicker(ticker: String): String? {
    val base = ticker.substringBefore(".").uppercase(Locale.ROOT)
    return when (base) {
        "TM" -> "global.toyota"
        "RACE" -> "ferrari.com"
        "MC" -> "lvmh.com"
        "AAPL" -> "apple.com"
        "MSFT" -> "microsoft.com"
        "GOOGL", "GOOG" -> "google.com"
        "NVDA" -> "nvidia.com"
        "TSLA" -> "tesla.com"
        "META" -> "meta.com"
        "AMZN" -> "amazon.com"
        "JPM" -> "jpmorgan.com"
        "BAC" -> "bankofamerica.com"
        "WMT" -> "walmart.com"
        "JNJ" -> "jnj.com"
        "V" -> "visa.com"
        "PG" -> "pg.com"
        "MA" -> "mastercard.com"
        "HD" -> "homedepot.com"
        "CVX" -> "chevron.com"
        "LLY" -> "lilly.com"
        "ABBV" -> "abbvie.com"
        "MRK" -> "merck.com"
        "PEP" -> "pepsico.com"
        "KO" -> "coca-cola.com"
        "AVGO" -> "broadcom.com"
        "ASML" -> "asml.com"
        "TTE" -> "totalenergies.com"
        "NVO" -> "novonordisk.com"
        "NVS" -> "novartis.com"
        "AZN" -> "astrazeneca.com"
        "SHEL" -> "shell.com"
        "TMUS" -> "t-mobile.com"
        "CMCSA" -> "comcast.com"
        "DIS" -> "disney.com"
        "NFLX" -> "netflix.com"
        "ADBE" -> "adobe.com"
        "NKE" -> "nike.com"
        "NTDOY" -> "nintendo.com"
        "SONY" -> "sony.com"
        "AMD" -> "amd.com"
        "ORCL" -> "oracle.com"
        "CRM" -> "salesforce.com"
        "INTC" -> "intel.com"
        "QCOM" -> "qualcomm.com"
        "TXN" -> "ti.com"
        "COST" -> "costco.com"
        "SBUX" -> "starbucks.com"
        "MCD" -> "mcdonalds.com"
        
        "BBCA" -> "bca.co.id"
        "BBRI" -> "bri.co.id"
        "GOTO" -> "gotocompany.com"
        "TLKM" -> "telkom.co.id"
        "BMRI" -> "bankmandiri.co.id"
        "BBNI" -> "bni.co.id"
        "ASII" -> "astra.co.id"
        "UNVR" -> "unilever.co.id"
        "KLBF" -> "kalbe.co.id"
        "ADRO" -> "adaro.com"
        "PTBA" -> "bukitasam.co.id"
        "INDF" -> "indofood.com"
        "ICBP" -> "indofoodcbp.com"
        "AMRT" -> "alfamart.co.id"
        "BRIS" -> "bankbsi.co.id"
        "PGAS" -> "pgas.co.id"
        "ANTM" -> "antam.com"
        "TINS" -> "timah.com"
        "HRUM" -> "harumenergy.com"
        "UNTR" -> "unitedtractors.com"
        "JSMR" -> "jasamarga.com"
        "ADHI" -> "adhi.co.id"
        "WIKA" -> "wijayakarya.com"
        "PTPP" -> "ptpp.co.id"
        "SMGR" -> "sig.id"
        "INCO" -> "vale.com"
        "BDMN" -> "danamon.co.id"
        "BNGA" -> "cimbniaga.co.id"
        "CPIN" -> "cp.co.id"
        "EXCL" -> "xl.co.id"
        "ISAT" -> "indosatooredoo.com"
        "JPFA" -> "japfacomfeed.co.id"
        "MEDC" -> "medcoenergi.com"
        "MAPI" -> "map.co.id"
        "MYOR" -> "mayora.com"
        "SCMA" -> "suryacitramedia.com"
        "TOWR" -> "sarana-menara.com"
        "TBIG" -> "tbbtower.com"
        "HEAL" -> "medikalokahermina.com"
        "MIKA" -> "mitra-keluarga.com"
        
        // New Additions
        "YAMCY" -> "yamaha.com"
        "DBT" -> "dbaudio.com"
        "LACO" -> "l-acoustics.com"
        "MEYER" -> "meyersound.com"
        "HARMAN" -> "harman.com"
        "ATX" -> "audiotonix.com"
        "SENN" -> "sennheiser.com"
        "MTRIBE" -> "musictribe.com"
        "CLAY" -> "claypaky.it"
        "SHURE" -> "shure.com"
        "PDJ" -> "pioneerdj.com"
        "BDANCE" -> "bytedance.com"
        "FOXA" -> "foxcorporation.com"
        "WBD" -> "wbd.com"
        "PARA" -> "paramount.com"
        "MNCN" -> "mnc.co.id"
        "EMTK" -> "emtek.co.id"
        "KG" -> "kompasgramedia.com"
        "VIVA" -> "vivagroup.co.id"
        "CT" -> "ctcorpora.com"
        "TSM" -> "tsmc.com"
        "CSCO" -> "cisco.com"
        "BRK-B" -> "berkshirehathaway.com"
        "XOM" -> "exxonmobil.com"
        "BYAN" -> "bayan.com.sg"
        "AMMN" -> "amman.co.id"
        "BREN" -> "barito-renewables.co.id"
        "EA" -> "ea.com"
        "TTWO" -> "take2games.com"
        "NTES" -> "netease.com"
        "TCEHY" -> "tencent.com"
        "RBLX" -> "roblox.com"
        "NCBDY" -> "bandainamco.co.jp"
        "CCOEY" -> "capcom.com"
        "259960" -> "krafton.com"
        "UBI" -> "ubisoft.com"
        "SQNXF" -> "square-enix.com"
        "OTGLY" -> "cdprojektred.com"
        "SSNLF" -> "samsung.com"
        "CAT" -> "caterpillar.com"
        "BA" -> "boeing.com"
        "EADSY" -> "airbus.com"
        "RYCEY" -> "rolls-royce.com"
        "BYDDY" -> "bydglobal.com"
        "HYMTF" -> "hyundai.com"
        "GM" -> "gm.com"
        "F" -> "ford.com"
        "MBGYY" -> "mercedes-benz.com"
        "VWAGY" -> "volkswagen.com"
        "BMWYY" -> "bmwgroup.com"
        "KIMTF" -> "kia.com"
        "HMC" -> "honda.com"
        "SZKMY" -> "globalsuzuki.com"
        "TTM" -> "tatamotors.com"
        "NSANY" -> "nissan-global.com"
        "MZDAY" -> "mazda.com"
        "MMTOF" -> "mitsubishi-motors.com"
        "DAL" -> "delta.com"
        "UAL" -> "united.com"
        "RYAAY" -> "ryanair.com"
        "ICAGY" -> "iairgroup.com"
        "LUV" -> "southwest.com"
        "INDIGO" -> "goindigo.in"
        "AIRYY" -> "airchina.id"
        "SINGY" -> "singaporeair.com"
        "LTMAY" -> "latamairlines.com"
        "CPCAY" -> "cathaypacific.com"
        "QUBSF" -> "qantas.com"
        "AAL" -> "aa.com"
        "ALNPY" -> "ana.co.jp"
        "CAPITALA" -> "airasia.com"
        "GIAA" -> "garuda-indonesia.com"
        else -> null
    }
}

private data class StockDataRaw(
    val ticker: String,
    val companyName: String,
    val currentPrice: Double,
    val initialMarketCap: Double,
    val sector: String
)

fun generateStockData(): List<StockItem> {
    return listOf(
        StockDataRaw("AAPL", "Apple Inc.", 210.35, 3220000000000.0, "Technology"), //Done
        StockDataRaw("MSFT", "Microsoft Corp.", 450.15, 3170000000000.0, "Technology"),
        StockDataRaw("GOOGL", "Alphabet Inc.", 185.20, 4330000000000.0, "Technology"),
        StockDataRaw("NVDA", "NVIDIA Corp.", 214.50, 5200000000000.0, "Technology"),
        StockDataRaw("TSLA", "Tesla Inc.", 200.45, 1330000000000.0, "Automotive"),
        StockDataRaw("TM", "Toyota Motor", 200.10, 234500000000.0, "Automotive"),
        StockDataRaw("RACE", "Ferrari N.V.", 420.30, 60200000000.0, "Automotive"),
        StockDataRaw("MC.PA", "LVMH Moët Hennessy", 800.50, 264100000000.0, "Luxury"),
        StockDataRaw("META", "Meta Platforms", 600.80, 1580000000000.0, "Technology"),
        StockDataRaw("AMZN", "Amazon.com Inc.", 250.25, 2680000000000.0, "Consumer"),
        StockDataRaw("JPM", "JPMorgan Chase", 205.10, 806400000000.0, "Finance"),
        StockDataRaw("BAC", "Bank of America", 50.50, 372200000000.0, "Finance"),
        StockDataRaw("WMT", "Walmart Inc.", 65.80, 930100000000.0, "Consumer"),
        StockDataRaw("JNJ", "Johnson & Johnson", 223.20, 537500000000.0, "Healthcare"),
        StockDataRaw("V", "Visa Inc.", 312.15, 597800000000.0, "Finance"),
        StockDataRaw("PG", "Procter & Gamble", 140.30, 326400000000.0, "Consumer"), //done
        StockDataRaw("MA", "Mastercard Inc.", 479.26, 417010000000.0, "Finance"),
        StockDataRaw("HD", "Home Depot Inc.", 310.15, 312120000000.0, "Consumer"),
        StockDataRaw("CVX", "Chevron Corp.", 187.70, 378050000000.0, "Energy"),
        StockDataRaw("LLY", "Eli Lilly & Co.", 1081.24, 1020000000000.0, "Healthcare"),
        StockDataRaw("ABBV", "AbbVie Inc.", 218.03, 383520000000.0, "Healthcare"),
        StockDataRaw("MRK", "Merck & Co.", 115.00, 283540000000.0, "Healthcare"),
        StockDataRaw("PEP", "PepsiCo Inc.", 144.20, 194860000000.0, "Consumer"),
        StockDataRaw("KO", "Coca-Cola Co.", 78.72, 338970000000.0, "Consumer"),
        StockDataRaw("AVGO", "Broadcom Inc.", 492.31, 2270000000000.0, "Technology"),
        StockDataRaw("ASML", "ASML Holding", 1679.95, 668000000000.0, "Technology"),
        StockDataRaw("TTE", "TotalEnergies SE", 89.50, 203570000000.0, "Energy"), //done
        StockDataRaw("NVO", "Novo Nordisk", 42.87, 143374098000.0, "Healthcare"),
        StockDataRaw("NVS", "Novartis AG", 146.12, 234408113177.0, "Healthcare"),
        StockDataRaw("AZN", "AstraZeneca", 179.87, 206938264680.0, "Healthcare"),
        StockDataRaw("SHEL", "Shell plc", 86.47, 179400172805.0, "Energy"),
        StockDataRaw("TMUS", "T-Mobile US", 184.09, 196365912497.0, "Communication"),
        StockDataRaw("CMCSA", "Comcast Corp.", 23.91, 84018804195.0, "Communication"),
        StockDataRaw("DIS", "Walt Disney Co", 100.50, 172591827230.0, "Communication"),
        StockDataRaw("NFLX", "Netflix Inc.", 82.97, 343264238824.0, "Communication"),
        StockDataRaw("ADBE", "Adobe Inc.", 261.66, 103572229676.0, "Technology"),
        StockDataRaw("NKE", "Nike Inc.", 44.20, 64877661503.0, "Apparel"),
        StockDataRaw("NTDOY", "Nintendo Co. Ltd.", 11.43, 520954720000.0, "Gaming"),
        StockDataRaw("SONY", "Sony Group", 88.50, 109400000000.0, "Technology"),
        StockDataRaw("AMD", "Advanced Micro Devices", 165.20, 267300000000.0, "Technology"),
        StockDataRaw("ORCL", "Oracle Corp.", 128.75, 355600000000.0, "Technology"),
        StockDataRaw("CRM", "Salesforce Inc.", 265.40, 256100000000.0, "Technology"),
        StockDataRaw("INTC", "Intel Corp.", 31.80, 135200000000.0, "Technology"),
        StockDataRaw("QCOM", "Qualcomm Inc.", 195.60, 218500000000.0, "Technology"),
        StockDataRaw("TXN", "Texas Instruments", 170.15, 155300000000.0, "Technology"),
        StockDataRaw("COST", "Costco Wholesale", 815.30, 362400000000.0, "Consumer"),
        StockDataRaw("SBUX", "Starbucks Corp.", 78.50, 88700000000.0, "Consumer"),
        StockDataRaw("MCD", "McDonald's Corp.", 264.80, 188900000000.0, "Consumer"), //done
        StockDataRaw("BBCA.JK", "Bank Central Asia", 0.56, 68500000000.0, "Finance"),
        StockDataRaw("BBRI.JK", "Bank Rakyat Indonesia", 0.26, 38500000000.0, "Finance"),
        StockDataRaw("GOTO.JK", "GoTo Gojek Tokopedia", 0.0030, 3100000000.0, "Technology"),
        StockDataRaw("TLKM.JK", "Telkom Indonesia", 0.18, 16500000000.0, "Communication"),
        StockDataRaw("BMRI.JK", "Bank Mandiri (Persero)", 0.37, 34000000000.0, "Finance"),
        StockDataRaw("BBNI.JK", "Bank Negara Indonesia", 0.29, 10800000000.0, "Finance"),
        StockDataRaw("ASII.JK", "Astra International", 0.27, 10900000000.0, "Automotive"),
        StockDataRaw("UNVR.JK", "Unilever Indonesia", 0.13, 4900000000.0, "Consumer"),
        StockDataRaw("KLBF.JK", "Kalbe Farma", 0.09, 4100000000.0, "Healthcare"),
        StockDataRaw("ADRO.JK", "Adaro Energy", 0.17, 5300000000.0, "Energy"), //done
        StockDataRaw("PTBA.JK", "Bukit Asam", 0.15, 1699474767.0, "Energy"),
        StockDataRaw("INDF.JK", "Indofood Sukses", 0.35, 3043348283.0, "Consumer"),
        StockDataRaw("ICBP.JK", "Indofood CBP", 0.37, 4284609998.0, "Consumer"),
        StockDataRaw("AMRT.JK", "Sumber Alfaria Trijaya", 0.07, 3062754270.0, "Consumer"),
        StockDataRaw("BRIS.JK", "Bank Syariah Indonesia", 0.10, 4786616809.0, "Finance"),
        StockDataRaw("PGAS.JK", "Perusahaan Gas Negara", 0.10, 2312299245.0, "Energy"),
        StockDataRaw("ANTM.JK", "Aneka Tambang", 0.15, 3678177551.0, "Energy"),
        StockDataRaw("TINS.JK", "Timah Tbk", 0.17, 1263870958.0, "Energy"),
        StockDataRaw("HRUM.JK", "Harum Energy", 0.04, 547260742.0, "Energy"),
        StockDataRaw("UNTR.JK", "United Tractors", 1.22, 4550963287.0, "Heavy Industrials"), //done
        StockDataRaw("JSMR.JK", "Jasa Marga Tbk", 0.28, 2000000000.0, "Infrastructure"),
        StockDataRaw("ADHI.JK", "Adhi Karya (Persero)", 0.014, 110000000.0, "Infrastructure"),
        StockDataRaw("WIKA.JK", "Wijaya Karya", 0.006, 451000000.0, "Infrastructure"),
        StockDataRaw("PTPP.JK", "PP (Persero)", 0.022, 138000000.0, "Infrastructure"),
        StockDataRaw("SMGR.JK", "Semen Indonesia", 0.22, 1440000000.0, "Infrastructure"),
        StockDataRaw("INCO.JK", "Vale Indonesia Tbk", 0.25, 2440000000.0, "Energy"),
        StockDataRaw("BDMN.JK", "Bank Danamon Tbk", 0.14, 1380000000.0, "Finance"),
        StockDataRaw("BNGA.JK", "Bank CIMB Niaga", 0.10, 2500000000.0, "Finance"),
        StockDataRaw("CPIN.JK", "Charoen Pokphand Indonesia", 0.29, 4710000000.0, "Consumer"),
        StockDataRaw("EXCL.JK", "XL Axiata Tbk", 0.13, 1660000000.0, "Communication"),
        StockDataRaw("ISAT.JK", "Indosat Ooredoo Hutchison", 0.58, 4710000000.0, "Communication"),
        StockDataRaw("JPFA.JK", "Japfa Comfeed Indonesia", 0.08, 880000000.0, "Consumer"),
        StockDataRaw("MEDC.JK", "Medco Energi Internasional", 0.07, 1770000000.0, "Energy"),
        StockDataRaw("MAPI.JK", "Mitra Adiperkasa", 0.08, 1380000000.0, "Consumer"),
        StockDataRaw("MYOR.JK", "Mayora Indah Tbk", 0.14, 3050000000.0, "Consumer"),
        StockDataRaw("SCMA.JK", "Surya Citra Media", 0.007, 550000000.0, "Communication"),
        StockDataRaw("TOWR.JK", "Sarana Menara Nusantara", 0.04, 2210000000.0, "Communication"),
        StockDataRaw("TBIG.JK", "Tower Bersama Infrastructure", 0.11, 2500000000.0, "Communication"),
        StockDataRaw("HEAL.JK", "Medikaloka Hermina", 0.07, 1050000000.0, "Healthcare"),
        StockDataRaw("MIKA.JK", "Mitra Keluarga Karyasehat", 0.17, 2330000000.0, "Healthcare"), //done
        StockDataRaw("YAMCY", "Yamaha Corporation", 7.19, 3160000000.0, "Technology"),
        StockDataRaw("FOXA", "Fox Corporation", 64.28, 27010000000.0, "Media"),
        StockDataRaw("WBD", "Warner Bros. Discovery", 11.25, 27300000000.0, "Media"),
        StockDataRaw("PARA", "Paramount Global", 15.40, 10500000000.0, "Media"),
        StockDataRaw("MNCN.JK", "Media Nusantara Citra", 0.013, 167000000.0, "Media"),
        StockDataRaw("EMTK.JK", "Elang Mahkota Teknologi", 0.034, 2080000000.0, "Media"),
        StockDataRaw("VIVA.JK", "Visi Media Asia", 0.0022, 56000000.0, "Media"),
        StockDataRaw("TSM", "Taiwan Semiconductor", 248.60, 1290000000000.0, "Technology"),
        StockDataRaw("CSCO", "Cisco Systems", 126.50, 500000000000.0, "Technology"),
        StockDataRaw("BRK-B", "Berkshire Hathaway", 485.00, 1030000000000.0, "Finance"),//done
        StockDataRaw("XOM", "Exxon Mobil Corp.", 152.53, 619170000000.0, "Energy"),
        StockDataRaw("BYAN.JK", "Bayan Resources", 2.05, 68200000000.0, "Energy"),
        StockDataRaw("AMMN.JK", "Amman Mineral", 0.73, 53200000000.0, "Energy"),
        StockDataRaw("BREN.JK", "Barito Renewables Energy", 0.47, 62700000000.0, "Energy"),
        StockDataRaw("EA", "Electronic Arts", 171.40, 44800000000.0, "Gaming"),
        StockDataRaw("TTWO", "Take-Two Interactive", 248.60, 46200000000.0, "Gaming"),
        StockDataRaw("NTES", "NetEase, Inc.", 128.90, 82600000000.0, "Gaming"),
        StockDataRaw("TCEHY", "Tencent Holdings", 73.50, 678000000000.0, "Gaming"),
        StockDataRaw("RBLX", "Roblox Corporation", 92.80, 64100000000.0, "Gaming"),
        StockDataRaw("NCBDY", "Bandai Namco", 24.90, 16700000000.0, "Gaming"),
        StockDataRaw("CCOEY", "Capcom Co., Ltd.", 21.70, 12200000000.0, "Gaming"),
        StockDataRaw("259960.KS", "Krafton Inc.", 298.40, 13800000000.0, "Gaming"),
        StockDataRaw("UBI.PA", "Ubisoft Entertainment", 15.80, 2050000000.0, "Gaming"),
        StockDataRaw("SQNXF", "Square Enix", 47.20, 6200000000.0, "Gaming"),
        StockDataRaw("OTGLY", "CD Projekt S.A.", 16.40, 4200000000.0, "Gaming"),
        StockDataRaw("SSNLF", "Samsung Electronics", 69.80, 410000000000.0, "Technology"),
        StockDataRaw("CAT", "Caterpillar Inc.", 421.60, 198000000000.0, "Heavy Industrials"),
        StockDataRaw("BA", "Boeing Co.", 236.50, 181000000000.0, "Aviation"),
        StockDataRaw("EADSY", "Airbus SE", 53.70, 214000000000.0, "Aviation"),
        StockDataRaw("RYCEY", "Rolls-Royce Holdings", 14.60, 123000000000.0, "Aviation"),//done
        StockDataRaw("BYDDY", "BYD Company Ltd.", 11.47, 118500000000.0, "Automotive"),
        StockDataRaw("HYMTF", "Hyundai Motor Co.", 62.40, 43000000000.0, "Automotive"),
        StockDataRaw("GM", "General Motors Company", 82.70, 91000000000.0, "Automotive"),
        StockDataRaw("F", "Ford Motor Company", 16.63, 65000000000.0, "Automotive"),
        StockDataRaw("MBGYY", "Mercedes-Benz Group", 14.48, 54400000000.0, "Automotive"),
        StockDataRaw("VWAGY", "Volkswagen AG", 10.98, 54750000000.0, "Automotive"),
        StockDataRaw("BMWYY", "Bayerische Motoren Werke", 33.80, 56000000000.0, "Automotive"),
        StockDataRaw("KIMTF", "Kia Corporation", 92.50, 36000000000.0, "Automotive"),
        StockDataRaw("HMC", "Honda Motor Co.", 31.80, 47000000000.0, "Automotive"),
        StockDataRaw("SZKMY", "Suzuki Motor Corp.", 38.20, 32000000000.0, "Automotive"),
        StockDataRaw("TTM", "Tata Motors Limited", 28.40, 104000000000.0, "Automotive"),
        StockDataRaw("NSANY", "Nissan Motor Co.", 5.60, 13000000000.0, "Automotive"),
        StockDataRaw("MZDAY", "Mazda Motor Corp.", 2.85, 5600000000.0, "Automotive"),
        StockDataRaw("MMTOF", "Mitsubishi Motors", 2.90, 4300000000.0, "Automotive"),
        StockDataRaw("DAL", "Delta Air Lines", 78.78, 50000000000.0, "Aviation"),
        StockDataRaw("UAL", "United Airlines", 105.14, 35000000000.0, "Aviation"),
        StockDataRaw("RYAAY", "Ryanair Holdings", 63.20, 34000000000.0, "Aviation"),
        StockDataRaw("ICAGY", "International Airlines Group", 5.80, 18000000000.0, "Aviation"),
        StockDataRaw("LUV", "Southwest Airlines", 40.87, 20000000000.0, "Aviation"),
        StockDataRaw("INDIGO.NS", "InterGlobe Aviation (IndiGo)", 71.50, 33000000000.0, "Aviation"),//done
        StockDataRaw("AIRYY", "Air China Limited", 10.20, 13400000000.0, "Aviation"),
        StockDataRaw("SINGY", "Singapore Airlines", 8.30, 17000000000.0, "Aviation"),
        StockDataRaw("LTMAY", "LATAM Airlines Group", 37.50, 23000000000.0, "Aviation"),
        StockDataRaw("CPCAY", "Cathay Pacific Airways", 6.10, 8700000000.0, "Aviation"),
        StockDataRaw("QUBSF", "Qantas Airways Limited", 5.20, 9400000000.0, "Aviation"),
        StockDataRaw("AAL", "American Airlines Group", 14.80, 9800000000.0, "Aviation"),
        StockDataRaw("ALNPY", "ANA Holdings Inc.", 4.10, 12000000000.0, "Aviation"),
        StockDataRaw("CAPITALA.KL", "Capital A (AirAsia)", 0.15, 620000000.0, "Aviation"),
        StockDataRaw("GIAA.JK", "Garuda Indonesia", 0.0011, 29000000.0, "Aviation"),
        StockDataRaw("FILM.JK", "MD Entertainment", 0.19, 1750000000.0, "Entertainment"),
        StockDataRaw("BBTN.JK", "Bank Tabungan Negara", 0.082, 1350000000.0, "Finance"),
        StockDataRaw("BTPN.JK", "Bank BTPN", 0.19, 1480000000.0, "Finance"),
        StockDataRaw("JRPT.JK", "Jaya Real Property", 0.033, 842000000.0, "Real Estate"),
        StockDataRaw("ARTO.JK", "Bank Jago", 0.11, 1520000000.0, "Finance"),
        StockDataRaw("RCL", "Royal Caribbean Int.", 347.20, 97000000000.0, "Travel"),
        StockDataRaw("CCL", "Carnival Cruise Line", 32.60, 43000000000.0, "Travel"),
        StockDataRaw("BLK", "BlackRock", 1125.40, 176000000000.0, "Finance"),
        StockDataRaw("STT", "State Street Corp", 108.20, 31000000000.0, "Finance"),
        StockDataRaw("MS", "Morgan Stanley", 152.70, 243000000000.0, "Finance"),
        StockDataRaw("SCHW", "Charles Schwab", 102.80, 187000000000.0, "Finance"),
        StockDataRaw("C", "Citigroup", 96.30, 176000000000.0, "Finance"),
        StockDataRaw("COF", "Capital One", 232.40, 89000000000.0, "Finance")
    ).mapIndexed { index, data ->
        val initialMarketCap = data.initialMarketCap
        val currentPrice = data.currentPrice
        val sharesOutstanding = (initialMarketCap / currentPrice).toLong()

        val volatility = currentPrice * 0.02
        val changeAbsolute = (Math.random() - 0.5) * volatility
        val changePercent = if (currentPrice > 0) (changeAbsolute / currentPrice) * 100.0 else 0.0

        StockItem(
            id = (index + 1).toString(),
            ticker = data.ticker,
            name = data.companyName,
            currentPrice = currentPrice,
            changeAbsolute = changeAbsolute,
            changePercentage = changePercent,
            sector = data.sector,
            sharesOutstanding = sharesOutstanding,
            priceHistory = getSimulatedHistory(data.ticker, currentPrice, "1M", 40)
        )
    }
}

fun getMarketStats(stock: StockItem): StockStats {
    val hash = Math.abs(stock.ticker.hashCode()).coerceAtLeast(1)
    
    val divYield = when {
        stock.ticker.contains(".JK") -> 1.5 + (hash % 45) / 10.0
        else -> 0.1 + (hash % 35) / 10.0
    }
    
    val peRatio = when {
        stock.ticker.contains("AAPL") || stock.ticker.contains("MSFT") || stock.ticker.contains("NVDA") -> 28.0 + (hash % 30)
        stock.ticker.contains(".JK") -> 8.0 + (hash % 15)
        else -> 12.0 + (hash % 20)
    }

    val highDaily = stock.currentPrice * (1.0 + (hash % 15) / 400.0)
    val lowDaily = stock.currentPrice * (1.0 - (hash % 12) / 400.0)
    
    return StockStats(
        sharesOutstanding = stock.sharesOutstanding,
        dividendYield = divYield,
        peRatio = peRatio,
        highToday = highDaily,
        lowToday = lowDaily
    )
}

fun getSimulatedHistory(ticker: String, currentPrice: Double, interval: String, pointsCount: Int = 40): List<Double> {
    val intervalSeed = when (interval) {
        "1D" -> 101L
        "1W" -> 202L
        "1M" -> 303L
        "3M" -> 404L
        "1Y" -> 505L
        else -> 606L
    }
    val seed = Math.abs(ticker.hashCode()).toLong() * 31 + intervalSeed
    val random = java.util.Random(seed)
    val history = mutableListOf<Double>()
    
    val startFactor = when (interval) {
        "1D" -> 0.985 + (random.nextDouble() * 0.03)
        "1W" -> 0.96 + (random.nextDouble() * 0.06)
        "1M" -> 0.92 + (random.nextDouble() * 0.12)
        "3M" -> 0.85 + (random.nextDouble() * 0.25)
        "1Y" -> 0.70 + (random.nextDouble() * 0.50)
        else -> 0.95
    }
    
    var tempVal = currentPrice * startFactor
    
    for (i in 0 until pointsCount) {
        val fraction = i.toDouble() / (pointsCount - 1)
        val volt = when (interval) {
            "1D" -> 0.005
            "1W" -> 0.012
            "1M" -> 0.02
            "3M" -> 0.03
            "1Y" -> 0.05
            else -> 0.01
        }
        val fluctuation = (random.nextDouble() - 0.495) * volt
        tempVal = Math.max(0.01, tempVal * (1.0 + fluctuation))
        history.add(tempVal)
    }
    
    if (history.isNotEmpty()) {
        history[history.lastIndex] = currentPrice
    }
    return history
}


package com.example.ma.data

import com.example.ma.model.BidderType
import com.example.ma.model.MaBidder

/**
 * Catalog containing realistic global giants and Indonesian conglomerates
 * participating in the unsolicited M&A deal market.
 */
object MaBidderCatalog {

    val GLOBAL_BIDDERS = listOf(
        MaBidder(
            id = "softbank_vision",
            name = "SoftBank Vision Fund",
            type = BidderType.GLOBAL_GIANT,
            headquarters = "Tokyo, Japan & London, UK",
            motto = "Empowering the Next Tech Frontier with Aggressive Capital",
            avatarInitials = "SVF",
            primaryColorHex = 0xFFFF9800,
            aggressiveness = 0.85f,
            reputationScore = 96
        ),
        MaBidder(
            id = "berkshire_hathaway",
            name = "Berkshire Hathaway",
            type = BidderType.GLOBAL_GIANT,
            headquarters = "Omaha, Nebraska, USA",
            motto = "Patient Value Investing & Cash Flow Fortresses",
            avatarInitials = "BRK",
            primaryColorHex = 0xFF3F51B5,
            aggressiveness = 0.25f,
            reputationScore = 99
        ),
        MaBidder(
            id = "tencent_holdings",
            name = "Tencent Holdings",
            type = BidderType.GLOBAL_GIANT,
            headquarters = "Shenzhen, China",
            motto = "Strategic Gaming, Social & Cloud Integration",
            avatarInitials = "TCT",
            primaryColorHex = 0xFF00BCD4,
            aggressiveness = 0.70f,
            reputationScore = 95
        ),
        MaBidder(
            id = "sony_interactive",
            name = "Sony Interactive",
            type = BidderType.GLOBAL_GIANT,
            headquarters = "Tokyo, Japan & San Mateo, USA",
            motto = "Next-Gen Entertainment & Immersive Media Consolidation",
            avatarInitials = "SNE",
            primaryColorHex = 0xFF2196F3,
            aggressiveness = 0.60f,
            reputationScore = 97
        ),
        MaBidder(
            id = "microsoft_capital",
            name = "Microsoft Capital",
            type = BidderType.GLOBAL_GIANT,
            headquarters = "Redmond, Washington, USA",
            motto = "Enterprise Cloud, AI Infrastructure & Global Scaling",
            avatarInitials = "MSFT",
            primaryColorHex = 0xFF4CAF50,
            aggressiveness = 0.65f,
            reputationScore = 98
        ),
        MaBidder(
            id = "sequoia_capital",
            name = "Sequoia Capital",
            type = BidderType.GLOBAL_GIANT,
            headquarters = "Menlo Park, California, USA",
            motto = "Venture Elite & Hypergrowth Seed-to-IPO Scale",
            avatarInitials = "SEQ",
            primaryColorHex = 0xFFE91E63,
            aggressiveness = 0.80f,
            reputationScore = 97
        ),
        MaBidder(
            id = "blackrock_sovereign",
            name = "BlackRock Sovereign",
            type = BidderType.GLOBAL_GIANT,
            headquarters = "New York City, USA",
            motto = "Global Asset Management & Institutional Balance Sheet Stability",
            avatarInitials = "BLK",
            primaryColorHex = 0xFF212121,
            aggressiveness = 0.40f,
            reputationScore = 99
        )
    )

    val INDONESIAN_BIDDERS = listOf(
        MaBidder(
            id = "salim_group",
            name = "Salim Group",
            type = BidderType.INDONESIAN_CONGLOMERATE,
            headquarters = "Jakarta, Indonesia",
            motto = "Konglomerasi Konsumer, Infrastruktur & Ritel Terpadu",
            avatarInitials = "SLM",
            primaryColorHex = 0xFFD32F2F,
            aggressiveness = 0.65f,
            reputationScore = 96
        ),
        MaBidder(
            id = "saratoga_investama",
            name = "Saratoga Investama",
            type = BidderType.INDONESIAN_CONGLOMERATE,
            headquarters = "Jakarta, Indonesia",
            motto = "Private Equity Aktif Energi, Logistik & Infrastruktur Modern",
            avatarInitials = "SRTG",
            primaryColorHex = 0xFF1976D2,
            aggressiveness = 0.50f,
            reputationScore = 94
        ),
        MaBidder(
            id = "djarum_investment",
            name = "Djarum Investment Group",
            type = BidderType.INDONESIAN_CONGLOMERATE,
            headquarters = "Kudus & Jakarta, Indonesia",
            motto = "Digital Ecosystem, Perbankan & Properti Komersial",
            avatarInitials = "DJR",
            primaryColorHex = 0xFFC2185B,
            aggressiveness = 0.45f,
            reputationScore = 98
        ),
        MaBidder(
            id = "astra_international",
            name = "Astra International",
            type = BidderType.INDONESIAN_CONGLOMERATE,
            headquarters = "Jakarta, Indonesia",
            motto = "Otomotif, Finansial & Infrastruktur Unggulan Nusantara",
            avatarInitials = "ASII",
            primaryColorHex = 0xFF0D47A1,
            aggressiveness = 0.35f,
            reputationScore = 99
        ),
        MaBidder(
            id = "ct_corp",
            name = "CT Corp",
            type = BidderType.INDONESIAN_CONGLOMERATE,
            headquarters = "Jakarta, Indonesia",
            motto = "Media Terintegrasi, Ritel Gaya Hidup & Perbankan Konsumen",
            avatarInitials = "CTC",
            primaryColorHex = 0xFFF57C00,
            aggressiveness = 0.75f,
            reputationScore = 95
        )
    )

    val ALL_BIDDERS: List<MaBidder> = GLOBAL_BIDDERS + INDONESIAN_BIDDERS

    fun getRandomBidder(): MaBidder {
        return ALL_BIDDERS.random()
    }
}

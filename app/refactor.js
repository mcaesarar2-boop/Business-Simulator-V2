const fs = require("fs");

const content = fs.readFileSync("app/src/main/java/com/example/StockMarketScreen.kt", "utf8");

// Extract data models and data functions
const topSplit = content.indexOf("fun getDomainForTicker");
const bottomSplit = content.indexOf("@Composable\nfun StockLineChart");

const dataContent = content.substring(topSplit, bottomSplit);

let models = `package com.example.data

data class StockItem(
    val id: String,
    val ticker: String,
    val name: String,
    val currentPrice: Double,
    val changeAbsolute: Double,
    val changePercentage: Double,
    val sector: String
) {
    val logoDomain: String?
        get() = getDomainForTicker(ticker)
}

data class StockStats(
    val sharesOutstanding: Long,
    val dividendYield: Double,
    val peRatio: Double,
    val highToday: Double,
    val lowToday: Double
)

data class MarketNews(
    val id: String,
    val text: String,
    val type: String,
    val timestamp: String = "Now"
)
`;

let generators = `package com.example.data

import java.util.Locale

` + dataContent.replace(/data class StockItem[\s\S]*?\}\n\n/, "").replace(/data class StockStats[\s\S]*?\}\n\n/, "").replace(/data class MarketNews[\s\S]*?\}\n\n/, "");

fs.mkdirSync("app/src/main/java/com/example/data", { recursive: true });
fs.writeFileSync("app/src/main/java/com/example/data/StockModels.kt", models);
fs.writeFileSync("app/src/main/java/com/example/data/StockDataGenerator.kt", generators);

console.log("Extracted to data package");

package com.example.data

data class CollectionCategory(
    val id: String,
    val name: String,
    val hexColor: String
)

data class CollectionItem(
    val id: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val basePrice: Long,
    val imageUrl: String = "",
    val releaseYear: Int? = null,
    val type: String = ""
)

data class OwnedCollection(
    val instanceId: String = java.util.UUID.randomUUID().toString(),
    val itemId: String,
    val purchasedPrice: Long,
    val customImageUrl: String? = null
)

val collectionCategories = listOf(
    CollectionCategory("jewels", "Jewels", "#00BCD4"),
    CollectionCategory("fine_art", "Fine Art", "#FF9800"),
    CollectionCategory("nfts", "NFTs", "#9C27B0"),
    CollectionCategory("private_islands", "Private Islands", "#4CAF50"),
    CollectionCategory("sports_franchises", "Sports Franchises", "#FF5722"),
    CollectionCategory("artifacts", "Historical Artifacts", "#795548")
)

val initialCollectionItems = listOf(
    // Jewels
    CollectionItem("jw_1", "jewels", "Hope Diamond Replica", "Berlian biru legendaris dengan aura mistis.", 2000000),
    CollectionItem("jw_2", "jewels", "The Pink Star", "Berlian potong luar biasa dengan paduan merah muda langka.", 71000000),
    CollectionItem("jw_3", "jewels", "Ruby Choker of Queens", "Kalung ruby delima favorit permaisuri abad 18.", 15000000),
    CollectionItem("jw_4", "jewels", "Sapphire Tear", "Batu safir jernih berukuran 100-karat.", 8500000),

    // Fine Art
    CollectionItem("fa_1", "fine_art", "Les Femmes d'Alger", "Lukisan klasik agung mahakarya abad ke-20.", 179000000),
    CollectionItem("fa_2", "fine_art", "Salvator Mundi Replica", "Karya seni yang menceritakan era kebangkitan kembali.", 450000000),
    CollectionItem("fa_3", "fine_art", "The Scream (Draft)", "Sketsa awal yang mengekspresikan ketakutan eksistensial.", 120000000),
    CollectionItem("fa_4", "fine_art", "Water Lilies", "Sapuan kuas elegan dari masa impresionisme.", 80000000),

    // NFTs
    CollectionItem("nft_1", "nfts", "CryptoPunk #3100", "Avatar seni digital legendaris yang merintis era NFT.", 7500000),
    CollectionItem("nft_2", "nfts", "Bored Ape #131", "Kera digital langka dengan aksesoris laser eksklusif.", 3200000),
    CollectionItem("nft_3", "nfts", "Everydays: The First 5000 Days", "Evolusi kolase seni digital fantastis.", 69000000),

    // Private Islands
    CollectionItem("is_1", "private_islands", "Laucala Island", "Pulau vulkanik menakjubkan dengan pasir putih di Pasifik.", 85000000),
    CollectionItem("is_2", "private_islands", "Necker Island", "Surga tersembunyi langganan biliuner elit global.", 100000000),
    CollectionItem("is_3", "private_islands", "Bora Bora Atoll", "Cincin karang tropis surga bawah laut.", 15000000),
    
    // Sports Franchises
    CollectionItem("sf_1", "sports_franchises", "Madrid Legacy FC", "Klub raksasa sepak bola dengan trofi prestisius.", 6000000000),
    CollectionItem("sf_2", "sports_franchises", "New York Hoops", "Tim basket kebanggaan kota metropolis terbesar.", 5800000000),
    CollectionItem("sf_3", "sports_franchises", "Global F1 Team", "Tim Formula balap super cepat dengan rekayasa aerodinamika nomor satu.", 1200000000),

    // Historical Artifacts
    CollectionItem("ha_1", "artifacts", "Excalibur Sword (Myth)", "Pedang berlapis emas konon dari raja kuno Inggris.", 25000000),
    CollectionItem("ha_2", "artifacts", "Golden Pharaoh Mask", "Topeng penguasa mesir yang ditemukan di lembah para raja.", 350000000),
    CollectionItem("ha_3", "artifacts", "Enigma Machine", "Mesin sandi perang dunia paling rumit dan fenomenal.", 5000000)
)

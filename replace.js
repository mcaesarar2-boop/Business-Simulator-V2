const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/Screens.kt', 'utf8');

const t1 = `val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        cat != "retro_cars" && cat != "space_rockets"`;
const r1 = `val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        val isVehicle = listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
        cat != null && !isVehicle`;
code = code.replace(t1, r1);

const t2 = `val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        cat == "retro_cars" || cat == "space_rockets"`;
const r2 = `val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)`;
code = code.replace(t2, r2);

const t3 = `val carsCount = ownedByCategory["retro_cars"] ?: 0
                        val rocketsCount = ownedByCategory["space_rockets"] ?: 0`;
const r3 = `val carsCount = ownedByCategory["cars"] ?: 0
                        val motorcyclesCount = ownedByCategory["motorcycles"] ?: 0
                        val yachtsCount = ownedByCategory["yachts"] ?: 0
                        val airplanesCount = ownedByCategory["airplanes"] ?: 0`;
code = code.replace(t3, r3);

const t4 = `"Retro Cars / Vehicles" to "$carsCount",
                            "Spacecrafts & Rockets" to "$rocketsCount",`;
const r4 = `"Cars / Motorcycles" to "\\\${carsCount + motorcyclesCount}",
                            "Yachts & Airplanes" to "\\\${yachtsCount + airplanesCount}",`;
code = code.replace(t4, r4);

fs.writeFileSync('app/src/main/java/com/example/Screens.kt', code);

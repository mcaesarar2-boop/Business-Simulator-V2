package com.example.data

import kotlin.random.Random

object FootballDatabase {

    // ==========================================
    // 1. DAFTAR LIGA DARI DATASET LAMPIRAN
    // ==========================================
    val leagues: List<FootballLeague> = listOf(
        // England (Multi-Tier 1 - 4)
        FootballLeague(
            country = "England",
            name = "Premier League",
            level = 1,
            clubs = listOf(
                "Arsenal", "Aston Villa", "AFC Bournemouth", "Brentford", "Brighton & Hove Albion",
                "Chelsea", "Crystal Palace", "Everton", "Fulham", "Hull City",
                "Ipswich Town", "Leeds United", "Liverpool", "Manchester City", "Manchester United",
                "Newcastle United", "Nottingham Forest", "Sunderland", "Tottenham Hotspur", "Coventry City"
            ),
            relegationSlots = 3
        ),
        FootballLeague(
            country = "England",
            name = "EFL Championship",
            level = 2,
            clubs = listOf(
                "Birmingham City", "Blackburn Rovers", "Bolton Wanderers", "Bristol City", "Burnley",
                "Cardiff City", "Charlton Athletic", "Derby County", "Lincoln City", "Middlesbrough",
                "Millwall", "Norwich City", "Portsmouth", "Preston North End", "Queens Park Rangers",
                "Sheffield United", "Southampton", "Stoke City", "Swansea City", "Watford",
                "West Bromwich Albion", "West Ham United", "Wolverhampton Wanderers", "Wrexham"
            ),
            promotionSlots = 3,
            relegationSlots = 3
        ),
        FootballLeague(
            country = "England",
            name = "EFL League One",
            level = 3,
            clubs = listOf(
                "AFC Wimbledon", "Barnsley", "Blackpool", "Bradford City", "Bromley",
                "Burton Albion", "Cambridge United", "Doncaster Rovers", "Huddersfield Town", "Leicester City",
                "Leyton Orient", "Luton Town", "Mansfield Town", "MK Dons", "Notts County",
                "Oxford United", "Peterborough United", "Plymouth Argyle", "Reading", "Sheffield Wednesday",
                "Stevenage", "Stockport County", "Wigan Athletic", "Wycombe Wanderers"
            ),
            promotionSlots = 3,
            relegationSlots = 4
        ),
        FootballLeague(
            country = "England",
            name = "EFL League Two",
            level = 4,
            clubs = listOf(
                "Accrington Stanley", "Barnet", "Bristol Rovers", "Cheltenham Town", "Chesterfield",
                "Colchester United", "Crawley Town", "Crewe Alexandra", "Exeter City", "Fleetwood Town",
                "Gillingham", "Grimsby Town", "Newport County", "Northampton Town", "Oldham Athletic",
                "Port Vale", "Rochdale", "Rotherham United", "Salford City", "Shrewsbury Town",
                "Swindon Town", "Tranmere Rovers", "Walsall", "York City"
            ),
            promotionSlots = 4,
            relegationSlots = 2
        ),

        // Spain (Multi-Tier 1 - 2)
        FootballLeague(
            country = "Spain",
            name = "LALIGA EA SPORTS",
            level = 1,
            clubs = listOf(
                "Athletic Club", "Atlético de Madrid", "CA Osasuna", "Celta", "Deportivo Alavés",
                "Elche CF", "FC Barcelona", "Getafe CF", "Levante UD", "Málaga CF",
                "Rayo Vallecano", "RC Deportivo", "RCD Espanyol", "Real Betis", "Real Madrid",
                "Real Sociedad", "Racing Santander", "Sevilla FC", "Valencia CF", "Villarreal CF"
            ),
            relegationSlots = 3
        ),
        FootballLeague(
            country = "Spain",
            name = "LALIGA HYPERMOTION",
            level = 2,
            clubs = listOf(
                "AD Ceuta FC", "Albacete BP", "Burgos CF", "Cádiz CF", "CD Castellón",
                "CD Eldense", "CD Leganés", "CD Tenerife", "CE Sabadell", "Celta Fortuna",
                "Córdoba CF", "FC Andorra", "Girona FC", "Granada CF", "Real Sociedad B",
                "RCD Mallorca", "Real Oviedo", "Real Sporting", "Real Valladolid", "SD Eibar",
                "UD Almería", "UD Las Palmas"
            ),
            promotionSlots = 3,
            relegationSlots = 4
        ),

        // Germany (Tier 1 & 3 in dataset)
        FootballLeague(
            country = "Germany",
            name = "Bundesliga",
            level = 1,
            clubs = listOf(
                "1. FC Köln", "1. FC Union Berlin", "1. FSV Mainz 05", "Bayern Munich", "Bayer 04 Leverkusen",
                "Borussia Dortmund", "Borussia Mönchengladbach", "Eintracht Frankfurt", "FC Augsburg", "FC Schalke 04",
                "Hamburger SV", "SC Freiburg", "SC Paderborn 07", "RB Leipzig", "SV Elversberg",
                "SV Werder Bremen", "TSG Hoffenheim", "VfB Stuttgart"
            ),
            relegationSlots = 3
        ),
        FootballLeague(
            country = "Germany",
            name = "3. Liga",
            level = 3,
            clubs = listOf(
                "1. FC Saarbrücken", "Alemannia Aachen", "FC Ingolstadt 04", "Fortuna Düsseldorf", "Hansa Rostock",
                "MSV Duisburg", "Preußen Münster", "Rot-Weiss Essen", "SC Fortuna Köln", "SC Verl",
                "SG Sonnenhof Großaspach", "SSV Jahn Regensburg", "SV Meppen", "SV Waldhof Mannheim", "SV Wehen Wiesbaden",
                "TSG Hoffenheim II", "TSV Havelse", "VfB Stuttgart II", "Viktoria Köln", "Würzburger Kickers"
            ),
            promotionSlots = 3,
            relegationSlots = 4
        ),

        // Indonesia (Multi-Tier 1 - 3)
        FootballLeague(
            country = "Indonesia",
            name = "Liga 1 / Super League",
            level = 1,
            clubs = listOf(
                "Arema FC", "Bali United", "Bhayangkara Presisi Lampung FC", "Borneo FC Samarinda",
                "Dewa United Banten FC", "Garudayaksa FC", "Isenmulang Kalteng FC", "Java United FC",
                "Madura United", "Persebaya Surabaya", "Persib Bandung", "Persija Jakarta",
                "Persijap Jepara", "Persik Kediri", "Persita", "PSIM Yogyakarta", "PSM Makassar", "PSS Sleman"
            ),
            relegationSlots = 3
        ),
        FootballLeague(
            country = "Indonesia",
            name = "Liga 2 / Championship",
            level = 2,
            clubs = listOf(
                "Adhyaksa FC Bekasi", "Barito Putera", "Deltras FC", "Dejan FC", "Kendal Tornado FC",
                "Persela Lamongan", "Persiba Balikpapan", "Persikad Depok", "Persiku Kudus", "Persipura Jayapura",
                "Persiraja Banda Aceh", "PSIS Semarang", "PSMS Medan", "PSPS Pekanbaru", "PSGC Ciamis",
                "RANS Nusantara FC", "Semen Padang", "PSBS Biak", "Persis Solo", "Sumsel United"
            ),
            promotionSlots = 3,
            relegationSlots = 3
        ),
        FootballLeague(
            country = "Indonesia",
            name = "Liga 3 / Liga Nusantara",
            level = 3,
            clubs = listOf(
                "Persikota Tangerang", "Persibo Bojonegoro", "Persiba Bantul", "Dejan FC B",
                "Persipasi Bekasi", "PSBL Langsa", "NZR Sumbersari", "Tornado FC Pekanbaru"
            ),
            promotionSlots = 3,
            relegationSlots = 0
        ),

        // Italy
        FootballLeague(
            country = "Italy",
            name = "Serie A Enilive",
            level = 1,
            clubs = listOf(
                "Atalanta", "Bologna", "Cagliari", "Como", "Fiorentina",
                "Frosinone", "Genoa", "Inter", "Juventus", "Lazio",
                "Lecce", "AC Milan", "Monza", "Napoli", "Parma",
                "Roma", "Sassuolo", "Torino", "Udinese", "Venezia"
            ),
            relegationSlots = 3
        ),

        // France
        FootballLeague(
            country = "France",
            name = "Ligue 1 McDonald's",
            level = 1,
            clubs = listOf(
                "Angers SCO", "AJ Auxerre", "Stade Brestois 29", "Le Havre AC", "Le Mans FC",
                "RC Lens", "FC Lorient", "LOSC Lille", "Olympique Lyonnais", "Olympique de Marseille",
                "AS Monaco", "OGC Nice", "Paris FC", "Paris Saint-Germain", "Stade Rennais FC",
                "RC Strasbourg Alsace", "Toulouse FC", "ESTAC Troyes"
            ),
            relegationSlots = 3
        ),

        // Portugal
        FootballLeague(
            country = "Portugal",
            name = "Liga Portugal",
            level = 1,
            clubs = listOf(
                "Académico de Viseu", "FC Alverca", "FC Arouca", "SL Benfica", "SC Braga",
                "Casa Pia AC", "Estoril Praia", "Estrela da Amadora", "FC Famalicão", "FC Porto",
                "Gil Vicente FC", "CS Marítimo", "Moreirense FC", "CD Nacional", "Rio Ave FC",
                "Santa Clara", "Sporting CP", "Vitória SC"
            ),
            relegationSlots = 3
        ),

        // Netherlands
        FootballLeague(
            country = "Netherlands",
            name = "Eredivisie",
            level = 1,
            clubs = listOf(
                "ADO Den Haag", "Ajax", "AZ Alkmaar", "SC Cambuur", "Excelsior Rotterdam",
                "Feyenoord", "FC Groningen", "sc Heerenveen", "Heracles Almelo", "N.E.C. Nijmegen",
                "PEC Zwolle", "PSV Eindhoven", "Sparta Rotterdam", "FC Twente", "FC Utrecht",
                "Telstar", "Willem II", "Go Ahead Eagles"
            ),
            relegationSlots = 3
        ),

        // Saudi Arabia
        FootballLeague(
            country = "Saudi Arabia",
            name = "Roshn Saudi League",
            level = 1,
            clubs = listOf(
                "Al Ahli", "Al Faisaly", "Al Fateh", "Al Fayha", "Al Hazem",
                "Al Hilal", "Al Ittihad", "Al Kholood", "Al Nassr", "Al Qadsiah",
                "Al Riyadh", "Al Shabab", "Al Taawoun", "Al Ettifaq", "Al Khaleej",
                "NEOM SC", "Abha", "Al Diriyah"
            ),
            relegationSlots = 3
        ),

        // USA / Canada
        FootballLeague(
            country = "United States / Canada",
            name = "Major League Soccer (MLS)",
            level = 1,
            clubs = listOf(
                "Atlanta United", "Austin FC", "Charlotte FC", "Chicago Fire FC", "FC Cincinnati",
                "Colorado Rapids", "Columbus Crew", "D.C. United", "FC Dallas", "Houston Dynamo FC",
                "Inter Miami CF", "LA Galaxy", "Los Angeles FC", "Minnesota United FC", "CF Montréal",
                "Nashville SC", "New England Revolution", "New York City FC", "New York Red Bulls",
                "Orlando City SC", "Philadelphia Union", "Portland Timbers", "Real Salt Lake", "San Diego FC",
                "San Jose Earthquakes", "Seattle Sounders FC", "St. Louis CITY SC", "Sporting Kansas City", "Toronto FC", "Vancouver Whitecaps FC"
            ),
            relegationSlots = 0
        )
    )

    // ==========================================
    // 2. DAFTAR 40 MANAGER RESMI DARI DATASET PDF
    // ==========================================
    val managers: List<FootballManager> = listOf(
        FootballManager(
            name = "Pep Guardiola", country = "Spanyol", previousClubOrNation = "Manchester City",
            fameDescription = "Barcelona, Bayern, Man City, 3× UCL", rating = 94,
            favoriteTactic = "Tiki-Taka (4-3-3)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 1_850_000L, minClubReputationRequired = 88, minTransferBudgetRequired = 100_000_000L
        ),
        FootballManager(
            name = "Carlo Ancelotti", country = "Italia", previousClubOrNation = "Real Madrid",
            fameDescription = "Rekor UCL, AC Milan, Real Madrid, Bayern, Chelsea", rating = 93,
            favoriteTactic = "Fluid Diamond (4-3-1-2)", tacticalStyle = "FLUID",
            monthlySalary = 1_500_000L, minClubReputationRequired = 85, minTransferBudgetRequired = 80_000_000L
        ),
        FootballManager(
            name = "José Mourinho", country = "Portugal", previousClubOrNation = "Real Madrid",
            fameDescription = "Porto, Chelsea, Inter, Real Madrid, 2× UCL", rating = 88,
            favoriteTactic = "Park The Bus & Counter (4-2-3-1)", tacticalStyle = "PARK_BUS",
            monthlySalary = 1_100_000L, minClubReputationRequired = 78, minTransferBudgetRequired = 50_000_000L
        ),
        FootballManager(
            name = "Luis Enrique", country = "Spanyol", previousClubOrNation = "Paris Saint-Germain",
            fameDescription = "Barcelona & PSG, 2× UCL", rating = 89,
            favoriteTactic = "High-Intensity Positional (4-3-3)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 1_250_000L, minClubReputationRequired = 80, minTransferBudgetRequired = 60_000_000L
        ),
        FootballManager(
            name = "Jürgen Klopp", country = "Jerman", previousClubOrNation = "Jerman",
            fameDescription = "Liverpool, Dortmund, Gegenpressing pioneer", rating = 93,
            favoriteTactic = "Heavy Metal Gegenpressing (4-3-3)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 1_600_000L, minClubReputationRequired = 85, minTransferBudgetRequired = 70_000_000L
        ),
        FootballManager(
            name = "Diego Simeone", country = "Argentina", previousClubOrNation = "Atlético Madrid",
            fameDescription = "Atlético Madrid, La Liga champion, UCL finals", rating = 89,
            favoriteTactic = "Cholo Low Block (4-4-2)", tacticalStyle = "DEFENSIVE",
            monthlySalary = 1_300_000L, minClubReputationRequired = 80, minTransferBudgetRequired = 50_000_000L
        ),
        FootballManager(
            name = "Mikel Arteta", country = "Spanyol", previousClubOrNation = "Arsenal",
            fameDescription = "Arsenal, Premier League 2025/26 title chase", rating = 88,
            favoriteTactic = "Inverted Positional (4-3-3)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 1_100_000L, minClubReputationRequired = 78, minTransferBudgetRequired = 55_000_000L
        ),
        FootballManager(
            name = "Zinedine Zidane", country = "Prancis", previousClubOrNation = "Free Agent",
            fameDescription = "Real Madrid, 3× UCL berturut-turut", rating = 91,
            favoriteTactic = "Direct Fluid Dynamic (4-3-3)", tacticalStyle = "FLUID",
            monthlySalary = 1_400_000L, minClubReputationRequired = 84, minTransferBudgetRequired = 75_000_000L
        ),
        FootballManager(
            name = "Antonio Conte", country = "Italia", previousClubOrNation = "Free Agent",
            fameDescription = "Juventus, Chelsea, Inter, Tottenham", rating = 87,
            favoriteTactic = "Wing-Back Overload (3-5-2)", tacticalStyle = "COUNTER",
            monthlySalary = 950_000L, minClubReputationRequired = 76, minTransferBudgetRequired = 45_000_000L
        ),
        FootballManager(
            name = "Thomas Tuchel", country = "Jerman", previousClubOrNation = "Inggris",
            fameDescription = "Chelsea, Bayern, PSG, UCL 2021", rating = 88,
            favoriteTactic = "Tactical Flexibility (3-4-2-1)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 1_050_000L, minClubReputationRequired = 78, minTransferBudgetRequired = 50_000_000L
        ),
        FootballManager(
            name = "Xabi Alonso", country = "Spanyol", previousClubOrNation = "Chelsea",
            fameDescription = "Leverkusen Invincible, Bundesliga, Real Madrid", rating = 90,
            favoriteTactic = "Controlled Gegenpress (3-4-2-1)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 1_200_000L, minClubReputationRequired = 82, minTransferBudgetRequired = 65_000_000L
        ),
        FootballManager(
            name = "Unai Emery", country = "Spanyol", previousClubOrNation = "Aston Villa",
            fameDescription = "Sevilla, Villarreal, Arsenal, 4x Europa League", rating = 86,
            favoriteTactic = "Compact Midfield Block (4-4-2)", tacticalStyle = "COUNTER",
            monthlySalary = 800_000L, minClubReputationRequired = 74, minTransferBudgetRequired = 35_000_000L
        ),
        FootballManager(
            name = "Hansi Flick", country = "Jerman", previousClubOrNation = "Barcelona",
            fameDescription = "Bayern Munich sextuple, FC Barcelona high press", rating = 90,
            favoriteTactic = "Ultra High-Line Press (4-2-3-1)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 1_150_000L, minClubReputationRequired = 82, minTransferBudgetRequired = 60_000_000L
        ),
        FootballManager(
            name = "Massimiliano Allegri", country = "Italia", previousClubOrNation = "Free Agent",
            fameDescription = "Juventus, AC Milan, Short Pass Pragmatism", rating = 84,
            favoriteTactic = "Catenaccio Solid (3-5-2)", tacticalStyle = "DEFENSIVE",
            monthlySalary = 650_000L, minClubReputationRequired = 70, minTransferBudgetRequired = 25_000_000L
        ),
        FootballManager(
            name = "Arsène Wenger", country = "Prancis", previousClubOrNation = "Free Agent / Legend",
            fameDescription = "Arsenal Invincibles, Total Attacking Football", rating = 92,
            favoriteTactic = "Free Flowing Attacking (4-2-3-1)", tacticalStyle = "TOTAL_FOOTBALL",
            monthlySalary = 1_000_000L, minClubReputationRequired = 78, minTransferBudgetRequired = 40_000_000L
        ),
        FootballManager(
            name = "Sir Alex Ferguson", country = "Skotlandia", previousClubOrNation = "Free Agent / Legend",
            fameDescription = "Manchester United, 13× Premier League, 2x UCL", rating = 96,
            favoriteTactic = "Direct Winger Cross (4-4-2)", tacticalStyle = "DIRECT",
            monthlySalary = 1_700_000L, minClubReputationRequired = 86, minTransferBudgetRequired = 70_000_000L
        ),
        FootballManager(
            name = "Vicente del Bosque", country = "Spanyol", previousClubOrNation = "Free Agent / Legend",
            fameDescription = "Real Madrid, Spanyol World Cup & Euro winner", rating = 90,
            favoriteTactic = "Balanced Possession (4-3-3)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 750_000L, minClubReputationRequired = 75, minTransferBudgetRequired = 30_000_000L
        ),
        FootballManager(
            name = "Fabio Capello", country = "Italia", previousClubOrNation = "Free Agent",
            fameDescription = "AC Milan, Real Madrid, Inggris", rating = 87,
            favoriteTactic = "Strict Defensive Discipline (4-4-2)", tacticalStyle = "DEFENSIVE",
            monthlySalary = 680_000L, minClubReputationRequired = 72, minTransferBudgetRequired = 25_000_000L
        ),
        FootballManager(
            name = "Marcelo Bielsa", country = "Argentina", previousClubOrNation = "Free Agent",
            fameDescription = "Argentina, Chile, Leeds, Tactical Guru", rating = 85,
            favoriteTactic = "Man-to-Man Murderball (4-1-4-1)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 600_000L, minClubReputationRequired = 65, minTransferBudgetRequired = 20_000_000L
        ),
        FootballManager(
            name = "Mauricio Pochettino", country = "Argentina", previousClubOrNation = "Amerika Serikat",
            fameDescription = "Tottenham, PSG, Chelsea, USMNT", rating = 84,
            favoriteTactic = "High Press Transition (4-2-3-1)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 700_000L, minClubReputationRequired = 70, minTransferBudgetRequired = 30_000_000L
        ),
        FootballManager(
            name = "Roberto Mancini", country = "Italia", previousClubOrNation = "Free Agent",
            fameDescription = "Manchester City, Italia, Euro 2020 Champion", rating = 85,
            favoriteTactic = "Solid Italian Transition (4-3-3)", tacticalStyle = "COUNTER",
            monthlySalary = 650_000L, minClubReputationRequired = 68, minTransferBudgetRequired = 25_000_000L
        ),
        FootballManager(
            name = "Joachim Löw", country = "Jerman", previousClubOrNation = "Free Agent",
            fameDescription = "Jerman, World Cup 2014 Champion", rating = 86,
            favoriteTactic = "Technical Pass & Move (4-2-3-1)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 620_000L, minClubReputationRequired = 70, minTransferBudgetRequired = 28_000_000L
        ),
        FootballManager(
            name = "Didier Deschamps", country = "Prancis", previousClubOrNation = "Prancis",
            fameDescription = "World Cup 2018 Champion, Euro Finalist", rating = 89,
            favoriteTactic = "Pragmatic Compact (4-3-3)", tacticalStyle = "COUNTER",
            monthlySalary = 800_000L, minClubReputationRequired = 78, minTransferBudgetRequired = 45_000_000L
        ),
        FootballManager(
            name = "Rafael Benítez", country = "Spanyol", previousClubOrNation = "Free Agent",
            fameDescription = "Liverpool Miracle of Istanbul, Valencia, Napoli", rating = 83,
            favoriteTactic = "Rigid Zonal Defense (4-2-3-1)", tacticalStyle = "DEFENSIVE",
            monthlySalary = 500_000L, minClubReputationRequired = 62, minTransferBudgetRequired = 18_000_000L
        ),
        FootballManager(
            name = "Manuel Pellegrini", country = "Chile", previousClubOrNation = "Free Agent",
            fameDescription = "Man City, Villarreal, Real Madrid", rating = 83,
            favoriteTactic = "Fluid Double Pivot (4-2-2-2)", tacticalStyle = "FLUID",
            monthlySalary = 520_000L, minClubReputationRequired = 63, minTransferBudgetRequired = 20_000_000L
        ),
        FootballManager(
            name = "Laurent Blanc", country = "Prancis", previousClubOrNation = "Free Agent",
            fameDescription = "PSG, Prancis, Bordeaux", rating = 82,
            favoriteTactic = "Patient Build-Up (4-3-3)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 450_000L, minClubReputationRequired = 60, minTransferBudgetRequired = 15_000_000L
        ),
        FootballManager(
            name = "Luciano Spalletti", country = "Italia", previousClubOrNation = "Italia",
            fameDescription = "Napoli Scudetto historical run, Italia", rating = 87,
            favoriteTactic = "Dynamic Vertical Wingers (4-3-3)", tacticalStyle = "FLUID",
            monthlySalary = 750_000L, minClubReputationRequired = 74, minTransferBudgetRequired = 35_000_000L
        ),
        FootballManager(
            name = "Erik ten Hag", country = "Belanda", previousClubOrNation = "Free Agent",
            fameDescription = "Ajax UCL Semifinals, Manchester United", rating = 82,
            favoriteTactic = "Transition Direct Press (4-2-3-1)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 550_000L, minClubReputationRequired = 65, minTransferBudgetRequired = 25_000_000L
        ),
        FootballManager(
            name = "Roberto De Zerbi", country = "Italia", previousClubOrNation = "Free Agent",
            fameDescription = "Brighton, Sassuolo, Marseille, build from deep", rating = 86,
            favoriteTactic = "Bait Press & Vertical Break (4-2-3-1)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 680_000L, minClubReputationRequired = 72, minTransferBudgetRequired = 30_000_000L
        ),
        FootballManager(
            name = "Vincenzo Italiano", country = "Italia", previousClubOrNation = "Bologna",
            fameDescription = "Fiorentina, Bologna, Conference League Finals", rating = 81,
            favoriteTactic = "Aggressive High Press (4-3-3)", tacticalStyle = "GEGENPRESS",
            monthlySalary = 350_000L, minClubReputationRequired = 58, minTransferBudgetRequired = 12_000_000L
        ),
        FootballManager(
            name = "Vincent Kompany", country = "Belgia", previousClubOrNation = "Bayern Munich",
            fameDescription = "Burnley Championship winner, Bayern Munich", rating = 85,
            favoriteTactic = "Dominant Ball Control (4-2-3-1)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 800_000L, minClubReputationRequired = 75, minTransferBudgetRequired = 40_000_000L
        ),
        FootballManager(
            name = "Rúben Amorim", country = "Portugal", previousClubOrNation = "Manchester United",
            fameDescription = "Sporting CP league title, Manchester United", rating = 86,
            favoriteTactic = "High Flying Wing-Backs (3-4-3)", tacticalStyle = "COUNTER",
            monthlySalary = 750_000L, minClubReputationRequired = 73, minTransferBudgetRequired = 35_000_000L
        ),
        FootballManager(
            name = "Simone Inzaghi", country = "Italia", previousClubOrNation = "Free Agent",
            fameDescription = "Inter Milan Scudetto & Champions League Finalist", rating = 88,
            favoriteTactic = "Lethal Counter Vertical (3-5-2)", tacticalStyle = "COUNTER",
            monthlySalary = 950_000L, minClubReputationRequired = 77, minTransferBudgetRequired = 45_000_000L
        ),
        FootballManager(
            name = "Sérgio Conceição", country = "Portugal", previousClubOrNation = "Free Agent",
            fameDescription = "Porto title winner, AC Milan", rating = 84,
            favoriteTactic = "Fierce Grinta Press (4-4-2)", tacticalStyle = "DIRECT",
            monthlySalary = 500_000L, minClubReputationRequired = 65, minTransferBudgetRequired = 20_000_000L
        ),
        FootballManager(
            name = "Paulo Fonseca", country = "Portugal", previousClubOrNation = "Free Agent",
            fameDescription = "Lille, Milan, AS Roma", rating = 82,
            favoriteTactic = "Possession Dominance (4-2-3-1)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 420_000L, minClubReputationRequired = 60, minTransferBudgetRequired = 15_000_000L
        ),
        FootballManager(
            name = "Frank Lampard", country = "Inggris", previousClubOrNation = "Coventry City",
            fameDescription = "Chelsea, Everton, legenda Premier League", rating = 78,
            favoriteTactic = "Direct Attacking (4-3-3)", tacticalStyle = "DIRECT",
            monthlySalary = 250_000L, minClubReputationRequired = 50, minTransferBudgetRequired = 8_000_000L
        ),
        FootballManager(
            name = "Steven Gerrard", country = "Inggris", previousClubOrNation = "Free Agent",
            fameDescription = "Rangers Invincible title, Aston Villa, Al-Ettifaq", rating = 78,
            favoriteTactic = "Narrow Twin 10s (4-3-3)", tacticalStyle = "COUNTER",
            monthlySalary = 280_000L, minClubReputationRequired = 52, minTransferBudgetRequired = 8_000_000L
        ),
        FootballManager(
            name = "Frank Rijkaard", country = "Belanda", previousClubOrNation = "Free Agent",
            fameDescription = "Barcelona UCL 2006, Total Football pioneer", rating = 88,
            favoriteTactic = "Total Football Attacking (4-3-3)", tacticalStyle = "TOTAL_FOOTBALL",
            monthlySalary = 700_000L, minClubReputationRequired = 72, minTransferBudgetRequired = 30_000_000L
        ),
        FootballManager(
            name = "Louis van Gaal", country = "Belanda", previousClubOrNation = "Free Agent",
            fameDescription = "Ajax UCL, Barcelona, Bayern, Timnas Belanda", rating = 89,
            favoriteTactic = "Strict Systemic Philosophy (3-4-1-2)", tacticalStyle = "TOTAL_FOOTBALL",
            monthlySalary = 800_000L, minClubReputationRequired = 75, minTransferBudgetRequired = 35_000_000L
        ),
        FootballManager(
            name = "Rafa Márquez", country = "Meksiko", previousClubOrNation = "Free Agent",
            fameDescription = "Barcelona Atletic, Meksiko World Cup captain", rating = 76,
            favoriteTactic = "Positional Cruyffian (4-3-3)", tacticalStyle = "TIKI_TAKA",
            monthlySalary = 180_000L, minClubReputationRequired = 45, minTransferBudgetRequired = 5_000_000L
        )
    )

    // ==========================================
    // 3. DAFTAR PEMAIN RESMI DARI DATASET PDF
    // ==========================================
    val realPlayers: List<FootballPlayer> = listOf(
        // Superstars
        FootballPlayer(name = "Lamine Yamal", age = 19, currentClub = "FC Barcelona", nationality = "Spanyol", position = "FWD", rating = 92, marketValue = 200_000_000L, monthlyWage = 1_200_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Erling Haaland", age = 26, currentClub = "Manchester City", nationality = "Norwegia", position = "FWD", rating = 93, marketValue = 200_000_000L, monthlyWage = 1_800_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Kylian Mbappé", age = 27, currentClub = "Real Madrid", nationality = "Prancis", position = "FWD", rating = 93, marketValue = 180_000_000L, monthlyWage = 2_000_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Pedri", age = 23, currentClub = "FC Barcelona", nationality = "Spanyol", position = "MID", rating = 90, marketValue = 150_000_000L, monthlyWage = 950_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Michael Olise", age = 24, currentClub = "Bayern Munich", nationality = "Prancis", position = "FWD", rating = 89, marketValue = 150_000_000L, monthlyWage = 900_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Jude Bellingham", age = 23, currentClub = "Real Madrid", nationality = "Inggris", position = "MID", rating = 91, marketValue = 130_000_000L, monthlyWage = 1_300_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Florian Wirtz", age = 23, currentClub = "Liverpool", nationality = "Jerman", position = "MID", rating = 90, marketValue = 130_000_000L, monthlyWage = 1_100_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Vinícius Júnior", age = 26, currentClub = "Real Madrid", nationality = "Brasil", position = "FWD", rating = 92, marketValue = 130_000_000L, monthlyWage = 1_400_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Bukayo Saka", age = 25, currentClub = "Arsenal", nationality = "Inggris", position = "FWD", rating = 89, marketValue = 120_000_000L, monthlyWage = 1_000_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Jamal Musiala", age = 23, currentClub = "Bayern Munich", nationality = "Jerman", position = "MID", rating = 90, marketValue = 120_000_000L, monthlyWage = 1_050_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Désiré Doué", age = 21, currentClub = "Paris Saint-Germain", nationality = "Prancis", position = "MID", rating = 86, marketValue = 120_000_000L, monthlyWage = 700_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Rodri", age = 30, currentClub = "Manchester City", nationality = "Spanyol", position = "MID", rating = 92, marketValue = 110_000_000L, monthlyWage = 1_300_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Cole Palmer", age = 24, currentClub = "Chelsea", nationality = "Inggris", position = "MID", rating = 89, marketValue = 110_000_000L, monthlyWage = 900_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Federico Valverde", age = 27, currentClub = "Real Madrid", nationality = "Uruguay", position = "MID", rating = 90, marketValue = 110_000_000L, monthlyWage = 1_100_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Eduardo Camavinga", age = 23, currentClub = "Real Madrid", nationality = "Prancis", position = "MID", rating = 88, marketValue = 100_000_000L, monthlyWage = 850_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Julián Álvarez", age = 26, currentClub = "Atlético Madrid", nationality = "Argentina", position = "FWD", rating = 88, marketValue = 100_000_000L, monthlyWage = 900_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Declan Rice", age = 27, currentClub = "Arsenal", nationality = "Inggris", position = "MID", rating = 89, marketValue = 100_000_000L, monthlyWage = 1_000_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Alexander Isak", age = 26, currentClub = "Liverpool", nationality = "Swedia", position = "FWD", rating = 89, marketValue = 100_000_000L, monthlyWage = 950_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Martin Ødegaard", age = 27, currentClub = "Arsenal", nationality = "Norwegia", position = "MID", rating = 89, marketValue = 90_000_000L, monthlyWage = 950_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Rayan Cherki", age = 23, currentClub = "Manchester City", nationality = "Prancis", position = "MID", rating = 85, marketValue = 90_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Arda Güler", age = 21, currentClub = "Real Madrid", nationality = "Turki", position = "MID", rating = 85, marketValue = 90_000_000L, monthlyWage = 550_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Pau Cubarsí", age = 19, currentClub = "FC Barcelona", nationality = "Spanyol", position = "DEF", rating = 86, marketValue = 80_000_000L, monthlyWage = 500_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Nuno Mendes", age = 24, currentClub = "Paris Saint-Germain", nationality = "Portugal", position = "DEF", rating = 87, marketValue = 80_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Warren Zaïre-Emery", age = 20, currentClub = "Paris Saint-Germain", nationality = "Prancis", position = "MID", rating = 85, marketValue = 80_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Ryan Gravenberch", age = 24, currentClub = "Liverpool", nationality = "Belanda", position = "MID", rating = 86, marketValue = 80_000_000L, monthlyWage = 700_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Martín Zubimendi", age = 27, currentClub = "Arsenal", nationality = "Spanyol", position = "MID", rating = 87, marketValue = 75_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "William Saliba", age = 25, currentClub = "Arsenal", nationality = "Prancis", position = "DEF", rating = 89, marketValue = 75_000_000L, monthlyWage = 850_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Gabriel Magalhães", age = 28, currentClub = "Arsenal", nationality = "Brasil", position = "DEF", rating = 88, marketValue = 75_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "Ronald Araújo", age = 27, currentClub = "FC Barcelona", nationality = "Uruguay", position = "DEF", rating = 88, marketValue = 70_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "Aurélien Tchouaméni", age = 26, currentClub = "Real Madrid", nationality = "Prancis", position = "MID", rating = 87, marketValue = 70_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Khvicha Kvaratskhelia", age = 25, currentClub = "Paris Saint-Germain", nationality = "Georgia", position = "FWD", rating = 88, marketValue = 70_000_000L, monthlyWage = 850_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Rodrygo", age = 25, currentClub = "Real Madrid", nationality = "Brasil", position = "FWD", rating = 88, marketValue = 70_000_000L, monthlyWage = 900_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Phil Foden", age = 26, currentClub = "Manchester City", nationality = "Inggris", position = "FWD", rating = 89, marketValue = 70_000_000L, monthlyWage = 1_000_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "João Neves", age = 21, currentClub = "Paris Saint-Germain", nationality = "Portugal", position = "MID", rating = 85, marketValue = 70_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Jurriën Timber", age = 25, currentClub = "Arsenal", nationality = "Belanda", position = "DEF", rating = 85, marketValue = 70_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Vitinha", age = 26, currentClub = "Paris Saint-Germain", nationality = "Portugal", position = "MID", rating = 87, marketValue = 70_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Nico Williams", age = 24, currentClub = "FC Barcelona", nationality = "Spanyol", position = "FWD", rating = 87, marketValue = 70_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Moisés Caicedo", age = 24, currentClub = "Chelsea", nationality = "Ekuador", position = "MID", rating = 86, marketValue = 70_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Enzo Fernández", age = 25, currentClub = "Chelsea", nationality = "Argentina", position = "MID", rating = 86, marketValue = 70_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),

        // Goalkeepers
        FootballPlayer(name = "Marc-André ter Stegen", age = 34, currentClub = "FC Barcelona", nationality = "Jerman", position = "GK", rating = 88, marketValue = 15_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Mike Maignan", age = 31, currentClub = "AC Milan", nationality = "Prancis", position = "GK", rating = 89, marketValue = 30_000_000L, monthlyWage = 700_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Thibaut Courtois", age = 34, currentClub = "Real Madrid", nationality = "Belgia", position = "GK", rating = 90, marketValue = 18_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "Jan Oblak", age = 33, currentClub = "Atlético Madrid", nationality = "Slovenia", position = "GK", rating = 89, marketValue = 15_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "Alisson Becker", age = 33, currentClub = "Liverpool", nationality = "Brasil", position = "GK", rating = 89, marketValue = 25_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Ederson", age = 33, currentClub = "Fenerbahçe", nationality = "Brasil", position = "GK", rating = 87, marketValue = 15_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Emiliano Martínez", age = 33, currentClub = "Aston Villa", nationality = "Argentina", position = "GK", rating = 88, marketValue = 18_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "André Onana", age = 30, currentClub = "Manchester United", nationality = "Kamerun", position = "GK", rating = 83, marketValue = 10_000_000L, monthlyWage = 450_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Diogo Costa", age = 26, currentClub = "FC Porto", nationality = "Portugal", position = "GK", rating = 87, marketValue = 40_000_000L, monthlyWage = 400_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Bart Verbruggen", age = 24, currentClub = "Brighton & Hove Albion", nationality = "Belanda", position = "GK", rating = 83, marketValue = 35_000_000L, monthlyWage = 300_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Lucas Chevalier", age = 24, currentClub = "Paris Saint-Germain", nationality = "Prancis", position = "GK", rating = 84, marketValue = 35_000_000L, monthlyWage = 350_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Giorgi Mamardashvili", age = 26, currentClub = "Liverpool", nationality = "Georgia", position = "GK", rating = 86, marketValue = 35_000_000L, monthlyWage = 400_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "David Raya", age = 30, currentClub = "Arsenal", nationality = "Spanyol", position = "GK", rating = 87, marketValue = 30_000_000L, monthlyWage = 500_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),

        // Defenders
        FootballPlayer(name = "Marc Guéhi", age = 26, currentClub = "Manchester City", nationality = "Inggris", position = "DEF", rating = 86, marketValue = 50_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Ibrahima Konaté", age = 27, currentClub = "Liverpool", nationality = "Prancis", position = "DEF", rating = 86, marketValue = 50_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Alessandro Bastoni", age = 27, currentClub = "Inter", nationality = "Italia", position = "DEF", rating = 88, marketValue = 65_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Matthijs de Ligt", age = 27, currentClub = "Manchester United", nationality = "Belanda", position = "DEF", rating = 85, marketValue = 40_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "Joško Gvardiol", age = 24, currentClub = "Manchester City", nationality = "Kroasia", position = "DEF", rating = 88, marketValue = 70_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Levi Colwill", age = 23, currentClub = "Chelsea", nationality = "Inggris", position = "DEF", rating = 84, marketValue = 45_000_000L, monthlyWage = 450_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Pau Torres", age = 29, currentClub = "Aston Villa", nationality = "Spanyol", position = "DEF", rating = 84, marketValue = 30_000_000L, monthlyWage = 500_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Cristian Romero", age = 28, currentClub = "Tottenham Hotspur", nationality = "Argentina", position = "DEF", rating = 86, marketValue = 45_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Kim Min-jae", age = 29, currentClub = "Bayern Munich", nationality = "Korea Selatan", position = "DEF", rating = 85, marketValue = 35_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "Dayot Upamecano", age = 27, currentClub = "Bayern Munich", nationality = "Prancis", position = "DEF", rating = 85, marketValue = 50_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "William Pacho", age = 24, currentClub = "Paris Saint-Germain", nationality = "Ekuador", position = "DEF", rating = 84, marketValue = 50_000_000L, monthlyWage = 500_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Castello Lukeba", age = 23, currentClub = "RB Leipzig", nationality = "Prancis", position = "DEF", rating = 83, marketValue = 45_000_000L, monthlyWage = 400_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Marc Cucurella", age = 28, currentClub = "Chelsea", nationality = "Spanyol", position = "DEF", rating = 84, marketValue = 35_000_000L, monthlyWage = 500_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Reece James", age = 26, currentClub = "Chelsea", nationality = "Inggris", position = "DEF", rating = 85, marketValue = 35_000_000L, monthlyWage = 700_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Pedro Porro", age = 27, currentClub = "Tottenham Hotspur", nationality = "Spanyol", position = "DEF", rating = 84, marketValue = 45_000_000L, monthlyWage = 450_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Denzel Dumfries", age = 30, currentClub = "Inter", nationality = "Belanda", position = "DEF", rating = 83, marketValue = 25_000_000L, monthlyWage = 400_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Federico Dimarco", age = 28, currentClub = "Inter", nationality = "Italia", position = "DEF", rating = 86, marketValue = 50_000_000L, monthlyWage = 550_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Theo Hernández", age = 28, currentClub = "Al-Hilal", nationality = "Prancis", position = "DEF", rating = 86, marketValue = 60_000_000L, monthlyWage = 1_000_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Achraf Hakimi", age = 27, currentClub = "Paris Saint-Germain", nationality = "Maroko", position = "DEF", rating = 88, marketValue = 80_000_000L, monthlyWage = 900_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Jeremie Frimpong", age = 25, currentClub = "Liverpool", nationality = "Belanda", position = "DEF", rating = 86, marketValue = 55_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Alphonso Davies", age = 25, currentClub = "Bayern Munich", nationality = "Kanada", position = "DEF", rating = 87, marketValue = 55_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Alejandro Grimaldo", age = 30, currentClub = "Bayer 04 Leverkusen", nationality = "Spanyol", position = "DEF", rating = 86, marketValue = 35_000_000L, monthlyWage = 500_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Trent Alexander-Arnold", age = 27, currentClub = "Real Madrid", nationality = "Inggris", position = "DEF", rating = 88, marketValue = 60_000_000L, monthlyWage = 950_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Antonio Rüdiger", age = 33, currentClub = "Real Madrid", nationality = "Jerman", position = "DEF", rating = 87, marketValue = 15_000_000L, monthlyWage = 700_000L, isRealPlayer = true, tacticalFit = "Defensive"),
        FootballPlayer(name = "Éder Militão", age = 28, currentClub = "Real Madrid", nationality = "Brasil", position = "DEF", rating = 86, marketValue = 45_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Defensive"),

        // Strikers & Midfielders
        FootballPlayer(name = "Harry Kane", age = 33, currentClub = "Bayern Munich", nationality = "Inggris", position = "FWD", rating = 91, marketValue = 65_000_000L, monthlyWage = 1_400_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Lautaro Martínez", age = 29, currentClub = "Inter", nationality = "Argentina", position = "FWD", rating = 89, marketValue = 85_000_000L, monthlyWage = 950_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Victor Osimhen", age = 27, currentClub = "Galatasaray", nationality = "Nigeria", position = "FWD", rating = 88, marketValue = 75_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Viktor Gyökeres", age = 28, currentClub = "Arsenal", nationality = "Swedia", position = "FWD", rating = 88, marketValue = 65_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Benjamin Šeško", age = 23, currentClub = "Manchester United", nationality = "Slovenia", position = "FWD", rating = 85, marketValue = 70_000_000L, monthlyWage = 550_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Raphinha", age = 29, currentClub = "FC Barcelona", nationality = "Brasil", position = "FWD", rating = 89, marketValue = 90_000_000L, monthlyWage = 950_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Dani Olmo", age = 28, currentClub = "FC Barcelona", nationality = "Spanyol", position = "MID", rating = 87, marketValue = 60_000_000L, monthlyWage = 700_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Gavi", age = 22, currentClub = "FC Barcelona", nationality = "Spanyol", position = "MID", rating = 86, marketValue = 60_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Frenkie de Jong", age = 29, currentClub = "FC Barcelona", nationality = "Belanda", position = "MID", rating = 87, marketValue = 55_000_000L, monthlyWage = 900_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Alejandro Balde", age = 22, currentClub = "FC Barcelona", nationality = "Spanyol", position = "DEF", rating = 84, marketValue = 50_000_000L, monthlyWage = 400_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Jules Koundé", age = 27, currentClub = "FC Barcelona", nationality = "Prancis", position = "DEF", rating = 87, marketValue = 60_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Endrick", age = 20, currentClub = "Real Madrid", nationality = "Brasil", position = "FWD", rating = 82, marketValue = 50_000_000L, monthlyWage = 350_000L, isRealPlayer = true, tacticalFit = "Counter Attack"),
        FootballPlayer(name = "Brahim Díaz", age = 27, currentClub = "Real Madrid", nationality = "Maroko", position = "MID", rating = 83, marketValue = 40_000_000L, monthlyWage = 450_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Sandro Tonali", age = 26, currentClub = "Newcastle United", nationality = "Italia", position = "MID", rating = 86, marketValue = 70_000_000L, monthlyWage = 700_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Bruno Guimarães", age = 28, currentClub = "Newcastle United", nationality = "Brasil", position = "MID", rating = 87, marketValue = 70_000_000L, monthlyWage = 750_000L, isRealPlayer = true, tacticalFit = "All-Round"),
        FootballPlayer(name = "Anthony Gordon", age = 25, currentClub = "Newcastle United", nationality = "Inggris", position = "FWD", rating = 84, marketValue = 60_000_000L, monthlyWage = 550_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Bruno Fernandes", age = 31, currentClub = "Manchester United", nationality = "Portugal", position = "MID", rating = 87, marketValue = 50_000_000L, monthlyWage = 900_000L, isRealPlayer = true, tacticalFit = "Direct Attack"),
        FootballPlayer(name = "Nicolò Barella", age = 29, currentClub = "Inter", nationality = "Italia", position = "MID", rating = 88, marketValue = 60_000_000L, monthlyWage = 800_000L, isRealPlayer = true, tacticalFit = "Gegenpressing"),
        FootballPlayer(name = "Hakan Çalhanoğlu", age = 32, currentClub = "Inter", nationality = "Turki", position = "MID", rating = 86, marketValue = 30_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Tijjani Reijnders", age = 27, currentClub = "Manchester City", nationality = "Belanda", position = "MID", rating = 85, marketValue = 55_000_000L, monthlyWage = 600_000L, isRealPlayer = true, tacticalFit = "Tiki-Taka"),
        FootballPlayer(name = "Ousmane Dembélé", age = 29, currentClub = "Paris Saint-Germain", nationality = "Prancis", position = "FWD", rating = 87, marketValue = 60_000_000L, monthlyWage = 850_000L, isRealPlayer = true, tacticalFit = "Wing Play"),
        FootballPlayer(name = "Bradley Barcola", age = 23, currentClub = "Paris Saint-Germain", nationality = "Prancis", position = "FWD", rating = 86, marketValue = 90_000_000L, monthlyWage = 650_000L, isRealPlayer = true, tacticalFit = "Wing Play")
    )

    // ==========================================
    // 4. GENERATOR FALLBACK SKUAD DUMMY REALISTIS
    // ==========================================
    private val firstNamesByCountry = mapOf(
        "England" to listOf("Oliver", "George", "Harry", "Jack", "Jacob", "Noah", "Charlie", "Thomas", "James", "William", "Alfie", "Mason", "Ethan", "Lucas", "Liam", "Callum", "Ryan", "Jordan", "Ben", "Dan"),
        "Spain" to listOf("Alejandro", "Carlos", "David", "Javier", "Daniel", "Pablo", "Álvaro", "Adrián", "Marcos", "Sergio", "Gonzalo", "Diego", "Hugo", "Mario", "Manuel", "Raúl", "Iker", "Ignacio", "Guillermo"),
        "Germany" to listOf("Lukas", "Maximilian", "Paul", "Felix", "Leon", "Niklas", "Jonas", "Tim", "Jan", "Finn", "Florian", "Moritz", "Philipp", "Sebastian", "Tobias", "Marcel", "Kevin", "Christian"),
        "Indonesia" to listOf("Bagus", "Rizky", "Pratama", "Dimas", "Bagas", "Ilham", "Wahyu", "Bayu", "Arif", "Febri", "Fajar", "Dicky", "Taufik", "Reza", "Hendra", "Bambang", "Witan", "Egy", "Asnawi", "Ernando"),
        "Italy" to listOf("Lorenzo", "Leonardo", "Francesco", "Alessandro", "Mattia", "Andrea", "Gabriele", "Matteo", "Federico", "Riccardo", "Edoardo", "Marco", "Davide", "Giuseppe", "Antonio"),
        "France" to listOf("Gabriel", "Léo", "Raphaël", "Arthur", "Louis", "Lucas", "Adam", "Jules", "Hugo", "Maël", "Liam", "Noah", "Paul", "Nathan", "Sacha"),
        "Netherlands" to listOf("Daan", "Sem", "Lucas", "Levi", "Finn", "Milan", "Jesse", "Luuk", "Bram", "Lars", "Thijs", "Stijn", "Mats", "Thomas"),
        "Saudi Arabia" to listOf("Salem", "Fahad", "Mohammed", "Saud", "Ali", "Hassan", "Abdullah", "Nasser", "Sultan", "Yasser", "Abdulrahman", "Hattan"),
        "United States / Canada" to listOf("Christian", "Tyler", "Weston", "Walker", "Antonee", "Sergiño", "Brenden", "Tim", "Matthew", "Miles", "Caleb", "Jordan")
    )

    private val lastNamesByCountry = mapOf(
        "England" to listOf("Smith", "Jones", "Taylor", "Brown", "Williams", "Wilson", "Johnson", "Davies", "Robinson", "Wright", "Walker", "White", "Edwards", "Hughes", "Green", "Hall", "Lewis", "Harris", "Clarke"),
        "Spain" to listOf("García", "Rodríguez", "González", "Fernández", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández", "Díaz", "Moreno", "Muñoz", "Álvarez", "Romero"),
        "Germany" to listOf("Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Schulz", "Hoffmann", "Schäfer", "Koch", "Bauer", "Richter", "Klein", "Wolf", "Schröder", "Neumann"),
        "Indonesia" to listOf("Kusuma", "Prasetyo", "Saputra", "Santoso", "Wijaya", "Firmansyah", "Hidayat", "Nugroho", "Suryadi", "Kurniawan", "Setiawan", "Pradana", "Wibowo", "Ramadhan", "Siregar", "Maulana"),
        "Italy" to listOf("Rossi", "Russo", "Ferrari", "Esposito", "Bianchi", "Romano", "Colombo", "Ricci", "Marino", "Greco", "Bruno", "Gallo", "Conti", "De Luca", "Mancini", "Costa", "Giordano", "Rizzo"),
        "France" to listOf("Martin", "Bernard", "Thomas", "Petit", "Robert", "Richard", "Durand", "Dubois", "Moreau", "Laurent", "Simon", "Michel", "Lefebvre", "Leroy", "Roux", "David", "Bertrand", "Morel"),
        "Netherlands" to listOf("de Jong", "Jansen", "de Vries", "van de Berg", "van Dijk", "Bakker", "Janssen", "Visser", "Smit", "Meijer", "de Boer", "Mulder", "de Groot", "Bos", "Vos", "Peters"),
        "Saudi Arabia" to listOf("Al-Dawsari", "Al-Shahrani", "Al-Faraj", "Al-Brikan", "Al-Malki", "Al-Amri", "Al-Ghannam", "Al-Owais", "Kanno", "Al-Shehri", "Al-Hassan"),
        "United States / Canada" to listOf("Pulisic", "Adams", "McKennie", "Zimmerman", "Robinson", "Dest", "Aaronson", "Weah", "Turner", "Carter-Vickers", "Ferreira", "Reyna")
    )

    fun generateCompleteSquadForClub(clubName: String, league: FootballLeague): List<FootballPlayer> {
        // Ambil pemain nyata yang ada di dataset
        val existingReal = realPlayers.filter { it.currentClub.equals(clubName, ignoreCase = true) }.toMutableList()

        val targetCount = 22 // Kuota ideal skuad (16 - 24)
        val needed = (targetCount - existingReal.size).coerceAtLeast(0)

        val countryKey = if (firstNamesByCountry.containsKey(league.country)) league.country else "England"
        val firstNames = firstNamesByCountry[countryKey] ?: firstNamesByCountry["England"]!!
        val lastNames = lastNamesByCountry[countryKey] ?: lastNamesByCountry["England"]!!

        // Range OVR, Market Value, dan Gaji seimbang berdasarkan kasta / level liga
        val (baseOvrMin, baseOvrMax) = when (league.level) {
            1 -> {
                if (league.country in listOf("England", "Spain", "Germany", "Italy", "France")) Pair(76, 83)
                else if (league.country == "Indonesia") Pair(63, 72)
                else Pair(70, 78)
            }
            2 -> {
                if (league.country == "England") Pair(68, 75)
                else if (league.country == "Spain") Pair(66, 73)
                else if (league.country == "Indonesia") Pair(54, 63)
                else Pair(62, 70)
            }
            3 -> {
                if (league.country == "England") Pair(60, 67)
                else if (league.country == "Germany") Pair(62, 69)
                else if (league.country == "Indonesia") Pair(48, 56)
                else Pair(56, 64)
            }
            else -> Pair(52, 60) // Tier 4 (League Two)
        }

        val existingPositions = existingReal.map { it.position }.toMutableList()
        val neededPositions = mutableListOf<String>()

        // Kuota posisi standar sepak bola
        val gkNeeded = (2 - existingPositions.count { it == "GK" }).coerceAtLeast(0)
        val defNeeded = (7 - existingPositions.count { it == "DEF" }).coerceAtLeast(0)
        val midNeeded = (7 - existingPositions.count { it == "MID" }).coerceAtLeast(0)
        val fwdNeeded = (targetCount - existingReal.size - gkNeeded - defNeeded - midNeeded).coerceAtLeast(2)

        repeat(gkNeeded) { neededPositions.add("GK") }
        repeat(defNeeded) { neededPositions.add("DEF") }
        repeat(midNeeded) { neededPositions.add("MID") }
        repeat(fwdNeeded) { neededPositions.add("FWD") }

        while (neededPositions.size < needed) {
            neededPositions.add(listOf("DEF", "MID", "FWD").random())
        }

        val tactics = listOf("Tiki-Taka", "Gegenpressing", "Counter Attack", "Direct Attack", "All-Round")

        for (i in 0 until needed) {
            val pos = neededPositions.getOrElse(i) { listOf("DEF", "MID", "FWD").random() }
            val fName = firstNames.random()
            val lName = lastNames.random()
            val age = Random.nextInt(18, 34)
            val ovr = Random.nextInt(baseOvrMin, baseOvrMax + 1)

            // Hitung harga pasar dan gaji rasional
            val multiplier = (ovr - 50).coerceAtLeast(1)
            val baseVal = when (league.level) {
                1 -> if (league.country == "Indonesia") multiplier * 25_000L else multiplier * 800_000L
                2 -> if (league.country == "Indonesia") multiplier * 8_000L else multiplier * 250_000L
                3 -> if (league.country == "Indonesia") multiplier * 3_000L else multiplier * 80_000L
                else -> multiplier * 25_000L
            }
            val marketVal = (baseVal * (1.0 + (Random.nextDouble(-0.15, 0.25)))).toLong().coerceAtLeast(20_000L)
            val monthlyWage = (marketVal * 0.012).toLong().coerceAtLeast(if (league.country == "Indonesia") 1_200L else 5_000L)

            existingReal.add(
                FootballPlayer(
                    name = "$fName $lName",
                    age = age,
                    currentClub = clubName,
                    nationality = league.country,
                    position = pos,
                    rating = ovr,
                    marketValue = marketVal,
                    monthlyWage = monthlyWage,
                    isRealPlayer = false,
                    tacticalFit = tactics.random()
                )
            )
        }

        return existingReal
    }

    fun calculateInitialClubProfile(clubName: String, league: FootballLeague): Triple<Int, Long, Long> {
        // Return Triple(Reputation 1-100, TransferBudget, WageBudget)
        val eliteClubs = listOf("Real Madrid", "Manchester City", "FC Barcelona", "Bayern Munich", "Liverpool", "Paris Saint-Germain", "Arsenal", "Chelsea")
        val strongClubs = listOf("Inter", "Juventus", "AC Milan", "Atlético Madrid", "Tottenham Hotspur", "Newcastle United", "Manchester United", "Borussia Dortmund", "Bayer 04 Leverkusen")
        val indoTopClubs = listOf("Persib Bandung", "Persija Jakarta", "Persebaya Surabaya", "Arema FC", "Bali United", "Borneo FC Samarinda")

        val reputation = when {
            eliteClubs.any { it.equals(clubName, ignoreCase = true) } -> Random.nextInt(90, 96)
            strongClubs.any { it.equals(clubName, ignoreCase = true) } -> Random.nextInt(82, 89)
            indoTopClubs.any { it.equals(clubName, ignoreCase = true) } -> Random.nextInt(64, 72)
            league.level == 1 -> if (league.country == "Indonesia") Random.nextInt(52, 63) else Random.nextInt(70, 81)
            league.level == 2 -> if (league.country == "Indonesia") Random.nextInt(40, 51) else Random.nextInt(60, 69)
            league.level == 3 -> if (league.country == "Indonesia") Random.nextInt(30, 39) else Random.nextInt(50, 59)
            else -> Random.nextInt(42, 52)
        }

        val transferBudget = when {
            reputation >= 90 -> 150_000_000L
            reputation >= 82 -> 80_000_000L
            reputation >= 72 -> 35_000_000L
            reputation >= 60 -> 12_000_000L
            reputation >= 50 -> 4_000_000L
            else -> 1_000_000L
        }

        val monthlyWageBudget = when {
            reputation >= 90 -> 14_000_000L
            reputation >= 82 -> 8_000_000L
            reputation >= 72 -> 4_000_000L
            reputation >= 60 -> 1_500_000L
            reputation >= 50 -> 500_000L
            else -> 150_000L
        }

        return Triple(reputation, transferBudget, monthlyWageBudget)
    }

    fun generateSponsorOffers(clubReputation: Int, leagueLevel: Int): List<FootballSponsor> {
        val tierFactor = when (leagueLevel) {
            1 -> 1.0
            2 -> 0.35
            3 -> 0.12
            else -> 0.05
        }
        val repFactor = (clubReputation / 100.0)

        val jerseyBrands = listOf("Fly Emirates", "Spotify", "Qatar Airways", "Allianz", "Standard Chartered", "AIA", "Etihad Airways", "Indofood", "Bank Mandiri")
        val stadiumBrands = listOf("Etihad Arena", "Allianz Stadium", "Emirates Ground", "Civitas Park", "Signal Iduna Park", "Indomilk Arena", "Gelora Bung Karno")

        val offers = mutableListOf<FootballSponsor>()

        val baseJerseyAnnual = (60_000_000L * tierFactor * repFactor).toLong().coerceAtLeast(200_000L)
        val jBrand = jerseyBrands.random()
        offers.add(
            FootballSponsor(
                sponsorName = jBrand,
                category = "JERSEY",
                industry = "Global Brand",
                annualPayout = baseJerseyAnnual,
                monthlyPayout = baseJerseyAnnual / 12L,
                contractYears = 2,
                monthsRemaining = 24,
                bonusPerWin = (baseJerseyAnnual * 0.005).toLong()
            )
        )

        val baseStadiumAnnual = (45_000_000L * tierFactor * repFactor).toLong().coerceAtLeast(150_000L)
        val sBrand = stadiumBrands.random()
        offers.add(
            FootballSponsor(
                sponsorName = sBrand,
                category = "STADIUM",
                industry = "Naming Rights",
                annualPayout = baseStadiumAnnual,
                monthlyPayout = baseStadiumAnnual / 12L,
                contractYears = 3,
                monthsRemaining = 36,
                bonusPerWin = 0L
            )
        )

        return offers
    }

    fun getClubAcquisitionPriceUsd(clubName: String, league: FootballLeague): Long {
        when (clubName) {
            "Real Madrid" -> return 3_500_000_000L
            "Manchester City" -> return 3_200_000_000L
            "Manchester United" -> return 3_000_000_000L
            "Liverpool" -> return 2_800_000_000L
            "Arsenal" -> return 2_400_000_000L
            "Barcelona" -> return 2_500_000_000L
            "Bayern Munich" -> return 2_600_000_000L
            "Paris Saint-Germain" -> return 2_100_000_000L
            "Chelsea" -> return 1_900_000_000L
            "Tottenham Hotspur" -> return 1_700_000_000L
            "Juventus" -> return 1_200_000_000L
            "Inter Milan" -> return 1_100_000_000L
            "AC Milan" -> return 1_150_000_000L
            "Atletico Madrid" -> return 1_050_000_000L
            "Borussia Dortmund" -> return 950_000_000L
            "Newcastle United" -> return 850_000_000L
            "Aston Villa" -> return 650_000_000L
            "Al Hilal" -> return 750_000_000L
            "Al Nassr" -> return 680_000_000L
            "Inter Miami CF" -> return 600_000_000L
            "Persib Bandung", "Persija Jakarta" -> return 25_000_000L
            "Bali United", "Persebaya Surabaya", "Arema FC", "Borneo FC" -> return 18_000_000L
            "Wrexham" -> return 45_000_000L
        }

        val baseByCountryAndTier = when {
            league.country == "England" && league.level == 1 -> 350_000_000L
            league.country == "England" && league.level == 2 -> 65_000_000L
            league.country == "England" && league.level == 3 -> 15_000_000L
            league.country == "England" && league.level == 4 -> 4_500_000L
            league.country == "Spain" && league.level == 1 -> 180_000_000L
            league.country == "Spain" && league.level == 2 -> 35_000_000L
            league.country == "Italy" && league.level == 1 -> 160_000_000L
            league.country == "Germany" && league.level == 1 -> 220_000_000L
            league.country == "France" && league.level == 1 -> 120_000_000L
            league.country == "Saudi Arabia" -> 250_000_000L
            league.country == "United States" -> 300_000_000L
            league.country == "Indonesia" && league.level == 1 -> 8_000_000L
            league.country == "Indonesia" && league.level == 2 -> 2_500_000L
            league.country == "Netherlands" || league.country == "Portugal" -> 80_000_000L
            else -> 10_000_000L
        }

        val hashMod = (clubName.hashCode().let { if (it < 0) -it else it } % 30) - 10
        val price = baseByCountryAndTier + (baseByCountryAndTier * hashMod / 100L)
        return price.coerceAtLeast(1_500_000L)
    }

    fun generateManagerBudgetProposal(state: FootballClubState): ManagerBudgetProposal? {
        val manager = state.hiredManager ?: return null

        val targetBenchmarkOvr = when (state.leagueLevel) {
            1 -> 85
            2 -> 78
            3 -> 72
            else -> 66
        }
        val gap = (targetBenchmarkOvr - state.starting11Ovr).coerceAtLeast(0)

        val positions = listOf("GK", "DEF", "MID", "FWD")
        val avgByPos = positions.associateWith { pos ->
            val inPos = state.squad.filter { it.position == pos }
            if (inPos.isNotEmpty()) inPos.map { it.rating }.average() else 60.0
        }
        val weakestPos = avgByPos.minByOrNull { it.value }?.key ?: "DEF"
        val weakestAvg = avgByPos[weakestPos]?.toInt() ?: 70

        val baseTierMultiplier = when (state.leagueLevel) {
            1 -> 18_000_000L
            2 -> 5_000_000L
            3 -> 1_500_000L
            else -> 600_000L
        }
        val isIndo = state.country == "Indonesia"
        val countryFactor = if (isIndo) 0.15 else 1.0

        val requested = ((baseTierMultiplier + (gap * baseTierMultiplier / 2L) + (manager.rating * 100_000L)) * countryFactor).toLong().coerceAtLeast(500_000L)

        val targetPosIndo = when (weakestPos) {
            "GK" -> "Kiper Utama (GK)"
            "DEF" -> "Lini Pertahanan (DEF)"
            "MID" -> "Jantung Lini Tengah (MID)"
            "FWD" -> "Penyerang Utama (FWD)"
            else -> weakestPos
        }

        val explanation = "Rata-rata OVR $targetPosIndo saat ini adalah $weakestAvg OVR (Benchmark liga: $targetBenchmarkOvr OVR). Manajer menilai tim butuh tambahan amunisi pemain baru untuk bersaing."
        val quote = "Tuan Chairman, taktik ${manager.favoriteTactic} yang saya rancang membutuhkan stabilitas di $targetPosIndo. Saya mengajukan permohonan dana belanja sebesar USD ${String.format("%,d", requested)} agar kita bisa bersaing memperebutkan poin krusial!"
        val impact = "Proyeksi kedatangan 1-2 pemain pilar untuk mendongkrak OVR tim sebesar +2 hingga +4 poin."

        return ManagerBudgetProposal(
            requestedAmount = requested,
            targetPosition = targetPosIndo,
            reasonExplanation = explanation,
            managerQuote = quote,
            estimatedSquadImpact = impact
        )
    }
}

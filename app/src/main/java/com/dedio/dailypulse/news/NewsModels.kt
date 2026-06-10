package com.dedio.dailypulse.news

data class NewsItem(
    val title: String,
    val description: String,
    val pubDate: String,
    val source: String,
    val link: String
)

data class RssSource(
    val name: String,
    val url: String,
    val language: String = "IT" // "IT" or "EN"
)

val DEFAULT_RSS_SOURCES: List<RssSource> = listOf(
    RssSource("ANSA", "https://www.ansa.it/sito/ansait_rss.xml", "IT"),
    RssSource("Repubblica", "https://www.repubblica.it/rss/homepage/rss2.0.xml", "IT"),
    RssSource("Corriere", "https://xml2.corriereobjects.it/rss/homepage.xml", "IT"),
    RssSource("SkyTG24", "https://tg24.sky.it/rss/tg24_mondo.xml", "IT"),
    RssSource("Il Sole 24 Ore", "https://www.ilsole24ore.com/rss/italia.xml", "IT"),
    RssSource("La Stampa", "https://www.lastampa.it/rss/homepage.xml", "IT"),
    RssSource("Wired IT", "https://www.wired.it/rss/all/", "IT"),
    RssSource("HDBlog", "https://www.hdblog.it/feed/", "IT"),
    RssSource("Gazzetta", "https://services.gazzetta.it/rss/home.xml", "IT"),
    RssSource("Quattroruote", "https://www.quattroruote.it/rss/rss_tutto.xml", "IT"),
    RssSource("TuttoSport", "https://www.tuttosport.com/rss/home", "IT"),
    RssSource("Le Scienze", "https://www.lescienze.it/rss/tutte-le-notizie.xml", "IT"),
    
    // English Sources
    RssSource("BBC News", "https://feeds.bbci.co.uk/news/world/rss.xml", "EN"),
    RssSource("CNN", "http://rss.cnn.com/rss/edition.rss", "EN"),
    RssSource("Reuters", "https://www.reutersagency.com/feed/", "EN"),
    RssSource("Wired US", "https://www.wired.com/feed/rss", "EN"),
    RssSource("TechCrunch", "https://techcrunch.com/feed/", "EN"),
    RssSource("The Verge", "https://www.theverge.com/rss/index.xml", "EN"),
    RssSource("Nat Geo", "https://www.nationalgeographic.it/rss/tutte-le-notizie.xml", "EN"),
    RssSource("Ars Technica", "https://feeds.arstechnica.com/arstechnica/index", "EN"),
    RssSource("NY Times", "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml", "EN"),
    RssSource("The Guardian", "https://www.theguardian.com/world/rss", "EN")
)

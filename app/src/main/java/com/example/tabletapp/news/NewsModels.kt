package com.example.tabletapp.news

/**
 * Represents a single news article parsed from an RSS feed.
 */
data class NewsItem(
    val title: String,
    val description: String,
    val pubDate: String,
    val source: String,
    val link: String
)

/**
 * Represents an RSS feed source with a display name and feed URL.
 */
data class RssSource(
    val name: String,
    val url: String
)

/**
 * Default RSS feed sources for the news ticker.
 * Includes Italian and international news outlets.
 */
val DEFAULT_RSS_SOURCES: List<RssSource> = listOf(
    RssSource("ANSA", "https://www.ansa.it/sito/ansait_rss.xml"),
    RssSource("Repubblica", "https://www.repubblica.it/rss/homepage/rss2.0.xml"),
    RssSource("Corriere", "https://xml2.corriereobjects.it/rss/homepage.xml"),
    RssSource("SkyTG24", "https://tg24.sky.it/rss/tg24_mondo.xml"),
    // Tech & Geek
    RssSource("Wired", "https://www.wired.it/rss/all/"),
    RssSource("TechCrunch", "https://techcrunch.com/feed/"),
    // Sport & Motori
    RssSource("Gazzetta", "https://services.gazzetta.it/rss/home.xml"),
    RssSource("Quattroruote", "https://www.quattroruote.it/rss/rss_tutto.xml"),
    // World
    RssSource("BBC", "https://feeds.bbci.co.uk/news/world/rss.xml"),
    RssSource("CNN", "http://rss.cnn.com/rss/edition.rss")
)

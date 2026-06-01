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
    RssSource("BBC", "https://feeds.bbci.co.uk/news/world/rss.xml")
)

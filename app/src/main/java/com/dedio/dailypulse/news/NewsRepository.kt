package com.dedio.dailypulse.news

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository that fetches and parses RSS news feeds from multiple sources.
 * Handles HTTP connections, XML parsing, and error recovery.
 */
class NewsRepository {

    companion object {
        private const val CONNECTION_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000
        private const val MAX_ITEMS = 50
    }

    /**
     * Known RSS date formats for parsing pubDate fields.
     * RFC 822 variants commonly used in RSS feeds.
     */
    private val dateFormats: List<SimpleDateFormat> = listOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH),
        SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm Z", Locale.ENGLISH),
    )

    /**
     * Regex pattern to strip HTML tags from description text.
     */
    private val htmlTagRegex = Regex("<[^>]*>")

    /**
     * Regex pattern to strip CDATA wrappers.
     */
    private val cdataRegex = Regex("<!\\[CDATA\\[|]]>")

    /**
     * Fetches news from all provided RSS sources, combines them,
     * sorts by publication date (newest first), and limits to [MAX_ITEMS].
     *
     * Failed sources are silently skipped — partial results are returned.
     *
     * @param sources List of RSS sources to fetch from.
     * @return Combined and sorted list of news items.
     */
    suspend fun fetchNews(sources: List<RssSource> = DEFAULT_RSS_SOURCES): List<NewsItem> {
        return withContext(Dispatchers.IO) {
            val allItems = coroutineScope {
                sources.map { source ->
                    async {
                        try {
                            fetchFromSource(source)
                        } catch (_: Exception) {
                            // Skip failed sources, return empty list
                            emptyList()
                        }
                    }
                }.awaitAll().flatten()
            }

            // Sort by parsed date (newest first), fall back to original order
            allItems
                .asSequence()
                .sortedByDescending { item -> parseDate(item.pubDate)?.time ?: 0L }
                .take(MAX_ITEMS)
                .toList()
        }
    }

    /**
     * Fetches and parses a single RSS feed source.
     *
     * @param source The RSS source to fetch.
     * @return List of parsed news items from this source.
     */
    private fun fetchFromSource(source: RssSource): List<NewsItem> {
        val url = URL(source.url)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECTION_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        connection.setRequestProperty("User-Agent", "DailyPulse/1.0 RSS Reader")
        connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml")

        return try {
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                emptyList()
            } else {
                val inputStream: InputStream = connection.inputStream
                parseRssFeed(inputStream, source.name)
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Parses an RSS XML feed from the given input stream.
     *
     * @param inputStream The XML input stream.
     * @param sourceName The display name of the source for attribution.
     * @return List of parsed [NewsItem] objects.
     */
    private fun parseRssFeed(inputStream: InputStream, sourceName: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var insideItem = false
        var title = ""
        var description = ""
        var pubDate = ""
        var link = ""
        var currentTag = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name ?: ""
                    if (currentTag.equals("item", ignoreCase = true)) {
                        insideItem = true
                        title = ""
                        description = ""
                        pubDate = ""
                        link = ""
                    }
                }

                XmlPullParser.TEXT -> {
                    if (insideItem) {
                        val text = parser.text ?: ""
                        when {
                            currentTag.equals("title", ignoreCase = true) -> {
                                title += text
                            }
                            currentTag.equals("description", ignoreCase = true) -> {
                                description += text
                            }
                            currentTag.equals("pubDate", ignoreCase = true) ||
                                    currentTag.equals("pubdate", ignoreCase = true) -> {
                                pubDate += text
                            }
                            currentTag.equals("link", ignoreCase = true) -> {
                                link += text
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val endTag = parser.name ?: ""
                    if (endTag.equals("item", ignoreCase = true) && insideItem) {
                        insideItem = false
                        val cleanTitle = stripHtml(title).trim()
                        val cleanDescription = stripHtml(description).trim()
                        if (cleanTitle.isNotEmpty()) {
                            items.add(
                                NewsItem(
                                    title = cleanTitle,
                                    description = cleanDescription,
                                    pubDate = pubDate.trim(),
                                    source = sourceName,
                                    link = link.trim(),
                                )
                            )
                        }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        inputStream.close()
        return items
    }

    /**
     * Strips HTML tags and CDATA wrappers from a string.
     * Also decodes common HTML entities.
     *
     * @param html The input string potentially containing HTML.
     * @return Clean plain text string.
     */
    private fun stripHtml(html: String): String {
        var cleaned = html
        cleaned = cdataRegex.replace(cleaned, "")
        cleaned = htmlTagRegex.replace(cleaned, "")
        // Decode common HTML entities
        cleaned = cleaned
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
        // Collapse multiple spaces
        cleaned = cleaned.replace(Regex("\\s+"), " ")
        return cleaned
    }

    /**
     * Attempts to parse a date string using known RSS date formats.
     *
     * @param dateString The date string to parse.
     * @return Parsed [Date] or null if no format matched.
     */
    private fun parseDate(dateString: String): Date? {
        val trimmed = dateString.trim()
        if (trimmed.isEmpty()) return null
        for (format in dateFormats) {
            try {
                return format.parse(trimmed)
            } catch (_: ParseException) {
                // Try next format
            }
        }
        return null
    }
}

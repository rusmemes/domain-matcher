package domainmatcher

import domainmatcher.DomainMatcher.Companion.LowCaseString.Companion.lowerCased
import java.util.LinkedList
import java.util.TreeSet
import kotlin.math.min

private const val CHARS_MAP_SIZE = 25

/**
 * Tool to block/allow domains.
 * Works with domains written only with numbers or ASCII letters.
 * */
class DomainMatcher private constructor(
    private val levelMinKeySize: Int = 0,
    private val values: Map<LowCaseString, DomainMatcher> = emptyMap(),
    private val charsMap: Array<CharEntry?> = arrayOfNulls(CHARS_MAP_SIZE),
    private val cache: Cache? = null
) {
    infix fun matched(url: String): Boolean {
        return when (cache) {
            null -> matched(url.urlPartsReversed())
            else -> matched(cache[url] ?: url.urlPartsReversed().also { cache[url] = it })
        }
    }

    private fun matched(urlPartsReversed: List<LowCaseString>): Boolean {

        var matcher = this
        var index = 0

        while (true) when {
            matcher.values.isEmpty() -> return index > 0
            else -> {
                val part = urlPartsReversed[index]
                when (val nextLevelMatcher = matcher.values[part]) {
                    null -> return when {
                        part.length < matcher.levelMinKeySize -> false
                        else -> matched(part.length - 1, part, matcher.charsMap)
                    }

                    else -> when (val nextIndex = index + 1) {
                        urlPartsReversed.size -> return nextLevelMatcher.values.isEmpty()
                        else -> {
                            matcher = nextLevelMatcher
                            index = nextIndex
                        }
                    }
                }
            }
        }
    }

    private tailrec fun matched(index: Int, part: LowCaseString, charsMap: Array<CharEntry?>?): Boolean {

        if (charsMap === null) {
            return true
        }

        val c = part[index]
        val charEntry = charsMap[c.code - 'a'.code]
        if (charEntry === null) {
            return false
        }

        val nextLevel = index - 1
        if (nextLevel < 0) {
            return false
        }

        return matched(nextLevel, part, charEntry.arr)
    }

    interface Cache {
        operator fun get(url: String): List<LowCaseString>?
        operator fun set(url: String, value: List<LowCaseString>)
    }

    companion object {

        private val stringComparatorCaseInsensitive: Comparator<String> = Comparator { o1, o2 ->
            o1.compareTo(o2, ignoreCase = true)
        }

        private fun String.clear(): String {

            var index = when {
                startsWith("http://", ignoreCase = true) -> 7
                startsWith("https://", ignoreCase = true) -> 8
                else -> 0
            }

            if (get(index).isW() && get(index + 1).isW() && get(index + 2).isW() && get(index + 3) == '.') {
                index += 4
            }

            return drop(index)
                .dropLastWhile { !it.isLetterOrDigit() }
                .takeWhile { it != '/' }
                .apply {
                    if (any {
                            !(it in 'A'..'z' || it == '.' || it.isDigit())
                        }) throw IllegalArgumentException("wrong url $this")
                }
        }

        @JvmStatic
        @JvmOverloads
        fun create(rawPatterns: Collection<String>, cache: Cache? = null): DomainMatcher {

            val sortedPatterns = TreeSet(stringComparatorCaseInsensitive).apply {
                rawPatterns.forEach { add(it.clear()) }
            }

            var matcher = DomainMatcher(cache = cache)
            var prev: String? = null

            for (pattern in sortedPatterns) {
                val skip = prev?.let { pattern.endsWith(it, ignoreCase = true) } ?: false
                if (!skip) {
                    val parts = pattern.urlPartsReversed()
                    matcher = addPartToMatcher(0, parts, matcher)
                }
                prev = pattern
            }

            return matcher
        }

        private fun addPartToMatcher(index: Int, parts: List<LowCaseString>, matcher: DomainMatcher): DomainMatcher {
            val part = parts[index]
            return DomainMatcher(
                levelMinKeySize = when (val curr = matcher.levelMinKeySize) {
                    0 -> part.length
                    else -> min(part.length, curr)
                },
                values = matcher.values.toMutableMap().apply {
                    val nextIndex = index + 1

                    compute(part) { _, nextLevelMatcher ->
                        when {
                            nextIndex < parts.size -> addPartToMatcher(
                                index = nextIndex,
                                parts = parts,
                                matcher = nextLevelMatcher ?: DomainMatcher()
                            )

                            else -> nextLevelMatcher ?: DomainMatcher()
                        }
                    }
                },
                charsMap = addCharToCharsMap(part.length - 1, part, matcher.charsMap)
            )
        }

        private fun addCharToCharsMap(
            index: Int,
            part: LowCaseString,
            charsMap: Array<CharEntry?>
        ): Array<CharEntry?> {

            val c = part[index]
            val code = c.code - 'a'.code
            var charEntry = charsMap[code]

            if (charEntry == null) {
                charEntry = CharEntry()
                charsMap[code] = charEntry
            }

            val nextIndex = index - 1
            if (nextIndex >= 0) {
                charsMap[code] = CharEntry(
                    arr = addCharToCharsMap(
                        index = nextIndex,
                        part = part,
                        charsMap = charEntry.arr ?: arrayOfNulls(CHARS_MAP_SIZE)
                    )
                )
            }

            return charsMap
        }

        private fun Char.isW(): Boolean {
            return this == 'w' || this == 'W'
        }

        private fun String.urlPartsReversed(): List<LowCaseString> {

            var startIndex = when {
                startsWith("http", ignoreCase = true) -> when {
                    get(4) == ':' && get(5) == '/' && get(6) == '/' -> 7
                    get(4) == 's' && get(5) == ':' && get(6) == '/' && get(7) == '/' -> 8
                    else -> throw IllegalArgumentException("wrong url $this")
                }
                else -> 0
            }

            var c = get(startIndex)
            if (!c.isLetterOrDigit()) throw IllegalArgumentException("wrong url $this")

            if (c.isW() && get(startIndex + 1).isW() && get(startIndex + 2).isW() && get(startIndex + 3) == '.') {
                startIndex += 4
            }

            val parts = LinkedList<LowCaseString>()
            var index = startIndex
            while (c != '/') {
                if (c == '.') {

                    val lowerCased = substring(startIndex, index).lowerCased()

                    if (lowerCased.any { !(it in 'a'..'z' || it == '.') || it.isDigit() }) {
                        throw IllegalArgumentException("wrong url $this")
                    }

                    parts.push(lowerCased)

                    index++
                    startIndex = index
                }

                if (++index < length) c = get(index) else break
            }

            val lowerCased = substring(startIndex, index).lowerCased()

            if (lowerCased.any { !(it in 'a'..'z' || it == '.' || it.isDigit()) }) {
                throw IllegalArgumentException("wrong url $this")
            }

            parts.push(lowerCased)

            return parts
        }

        @JvmInline
        value class LowCaseString private constructor(
            private val lowerCaseValue: String
        ) : CharSequence by lowerCaseValue {

            override fun toString() = lowerCaseValue

            companion object {
                internal fun String.lowerCased() = LowCaseString(lowercase())
            }
        }

        private class CharEntry(val arr: Array<CharEntry?>? = null)
    }
}



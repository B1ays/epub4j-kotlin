package io.documentnode.epub4j.util

import java.util.*

/**
 * Various String utility functions.
 *
 * Most of the functions herein are re-implementations of the ones in apache
 * commons StringUtils. The reason for re-implementing this is that the
 * functions are fairly simple and using my own implementation saves the
 * inclusion of a 200Kb jar file.
 *
 * @author paul.siegmann
 */
object StringUtil {
    /**
     * Changes a path containing '..', '.' and empty dirs into a path that
     * doesn't. X/foo/../Y is changed into 'X/Y', etc. Does not handle invalid
     * paths like "../".
     *
     * @param path
     * @return the normalized path
     */
    @JvmStatic
    fun collapsePathDots(path: String): String {
        val parts = path.split('/')
            .dropLastWhile(String::isEmpty)
            .toMutableList()

        var i = 0
        while (i < parts.size - 1) {
            val currentDir = parts[i]
            if (currentDir.isEmpty() || currentDir == ".") {
                parts.removeAt(i)
                i--
            } else if (currentDir == "..") {
                parts.removeAt(i - 1)
                parts.removeAt(i - 1)
                i -= 2
            }
            i++
        }

        val initial = if (path.startsWith("/")) {
            "/"
        } else {
            ""
        }
        val result = parts.foldIndexed(initial) { index, acc, part ->
            if(index < parts.lastIndex) {
                "$acc$part/"
            } else {
                acc + part
            }
        }
        return result
    }

    /**
     * Whether the given source string ends with the given suffix, ignoring
     * case.
     *
     * @param source
     * @param suffix
     * @return Whether the given source string ends with the given suffix, ignoring case.
     */
    @JvmStatic
    fun endsWithIgnoreCase(source: String, suffix: String): Boolean {
        if (suffix.isEmpty()) {
            return true
        }
        if (source.isEmpty()) {
            return false
        }
        if (suffix.length > source.length) {
            return false
        }
        return source.substring(source.length - suffix.length)
            .lowercase(Locale.getDefault()).endsWith(suffix.lowercase(Locale.getDefault()))
    }

    /**
     * If the given text is null return "", the given defaultValue otherwise.
     *
     * @param text
     * @param defaultValue
     * @return If the given text is null "", the given defaultValue otherwise.
     */
    /**
     * If the given text is null return "", the original text otherwise.
     *
     * @param text
     * @return If the given text is null "", the original text otherwise.
     */
    @JvmOverloads
    fun defaultIfNull(text: String?, defaultValue: String = ""): String {
        if (text == null) {
            return defaultValue
        }
        return text
    }

    /**
     * Null-safe string comparator
     *
     * @param text1
     * @param text2
     * @return whether the two strings are equal
     */
    @JvmStatic
    fun equals(text1: String?, text2: String?): Boolean {
        if (text1 == null) {
            return (text2 == null)
        }
        return text1 == text2
    }

    /**
     * Pretty toString printer.
     *
     * @param keyValues
     * @return a string representation of the input values
     */
    @JvmStatic
    fun toString(vararg keyValues: Any?): String {
        val result = StringBuilder()
        result.append('[')
        var i = 0
        while (i < keyValues.size) {
            if (i > 0) {
                result.append(", ")
            }
            result.append(keyValues[i])
            result.append(": ")
            var value: Any? = null
            if ((i + 1) < keyValues.size) {
                value = keyValues[i + 1]
            }
            if (value == null) {
                result.append("<null>")
            } else {
                result.append('\'')
                result.append(value)
                result.append('\'')
            }
            i += 2
        }
        result.append(']')
        return result.toString()
    }

    @JvmStatic
    fun hashCode(vararg values: String): Int {
        var result = 31
        for (i in values.indices) {
            result = result xor values[i].toString().hashCode()
        }
        return result
    }
}

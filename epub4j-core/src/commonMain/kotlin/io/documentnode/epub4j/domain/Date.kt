package io.documentnode.epub4j.domain

import io.documentnode.epub4j.epub.PackageDocumentBase
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A Date used by the book's metadata.
 *
 * Examples: creation-date, modification-date, etc
 *
 * @author paul
 */
class Date(
    val value: String,
    val event: Event? = null
) : Serializable {
    enum class Event(private val value: String) {
        PUBLICATION("publication"),
        MODIFICATION("modification"),
        CREATION("creation");

        companion object {
            fun fromValue(value: String): Event? {
                return entries.firstOrNull { it.value == value }
            }
        }
    }

    constructor(date: Date) : this(
        SimpleDateFormat(PackageDocumentBase.dateFormat).format(date)
    )

    constructor(date: Date, event: String) : this(
        SimpleDateFormat(PackageDocumentBase.dateFormat).format(date),
        event
    )

    constructor(dateString: String, event: String) : this(
        dateString,
        Event.fromValue(event)
    )

    override fun toString(): String {
        if (event == null) {
            return value
        }
        return "$event:$value"
    }

    companion object {
        private const val serialVersionUID = 7533866830395120136L
    }
}


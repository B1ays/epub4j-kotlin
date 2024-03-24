package io.documentnode.epub4j.domain

import java.io.Serializable
import java.util.*

/**
 * A Book's identifier.
 *
 * Defaults to a random UUID and scheme "UUID"
 *
 * @author paul
 */
/**
 * Creates an Identifier with as value a random UUID and scheme "UUID"
 */
class Identifier(
    val scheme: String = Scheme.UUID,
    val value: String = UUID.randomUUID().toString()
) : Serializable {
    interface Scheme {
        companion object {
            const val UUID: String = "UUID"
            const val ISBN: String = "ISBN"
            const val URL: String = "URL"
            const val URI: String = "URI"
        }
    }

    /**
     * This bookId property allows the book creator to add multiple ids and
     * tell the epubwriter which one to write out as the bookId.
     *
     * The Dublin Core metadata spec allows multiple identifiers for a Book.
     * The epub spec requires exactly one identifier to be marked as the book id.
     *
     * @return whether this is the unique book id.
     */
    var isBookId: Boolean = false

    companion object {
        private const val serialVersionUID = 955949951416391810L

        /**
         * The first identifier for which the bookId is true is made the
         * bookId identifier.
         *
         * If no identifier has bookId == true then the first bookId identifier
         * is written as the primary.
         *
         * @param identifiers
         * @return The first identifier for which the bookId is true is made
         * the bookId identifier.
         */
        fun getBookIdIdentifier(identifiers: List<Identifier>): Identifier? {
            if (identifiers.isEmpty()) {
                return null
            }
            return identifiers.firstOrNull(Identifier::isBookId) ?: identifiers.first()
        }
    }
}

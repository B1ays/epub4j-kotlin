package io.documentnode.epub4j.epub

import io.documentnode.epub4j.Constants
import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.domain.Identifier
import org.xmlpull.v1.XmlSerializer
import java.io.IOException

object PackageDocumentMetadataWriter : PackageDocumentBase() {
    /**
     * Writes the book's metadata.
     *
     * @param book
     * @param serializer
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    fun writeMetaData(book: Book, serializer: XmlSerializer) {
        serializer.startTag(NAMESPACE_OPF, OPFTags.metadata)
        serializer.setPrefix(PREFIX_DUBLIN_CORE, NAMESPACE_DUBLIN_CORE)
        serializer.setPrefix(PREFIX_OPF, NAMESPACE_OPF)

        writeIdentifiers(book.metadata.getIdentifiers(), serializer)
        writeSimpleMetadataElements(
            DCTags.title, book.metadata.getTitles(),
            serializer
        )
        writeSimpleMetadataElements(
            DCTags.subject, book.metadata.subjects,
            serializer
        )
        writeSimpleMetadataElements(
            DCTags.description,
            book.metadata.getDescriptions(), serializer
        )
        writeSimpleMetadataElements(
            DCTags.publisher,
            book.metadata.getPublishers(), serializer
        )
        writeSimpleMetadataElements(
            DCTags.type, book.metadata.getTypes(),
            serializer
        )
        writeSimpleMetadataElements(
            DCTags.rights, book.metadata.rights,
            serializer
        )

        // write authors
        for ((firstname, lastname, relator) in book.metadata.getAuthors()) {
            serializer.startTag(NAMESPACE_DUBLIN_CORE, DCTags.creator)
            serializer.attribute(
                NAMESPACE_OPF, OPFAttributes.role,
                relator.code
            )
            serializer.attribute(
                NAMESPACE_OPF, OPFAttributes.file_as,
                "$lastname, $firstname"
            )
            serializer.text("$firstname $lastname")
            serializer.endTag(NAMESPACE_DUBLIN_CORE, DCTags.creator)
        }

        // write contributors
        for ((firstname, lastname, relator) in book.metadata.getContributors()) {
            serializer.startTag(NAMESPACE_DUBLIN_CORE, DCTags.contributor)
            serializer.attribute(
                NAMESPACE_OPF, OPFAttributes.role,
                relator.code
            )
            serializer.attribute(
                NAMESPACE_OPF, OPFAttributes.file_as,
                "$lastname, $firstname"
            )
            serializer.text("$firstname $lastname")
            serializer.endTag(NAMESPACE_DUBLIN_CORE, DCTags.contributor)
        }

        // write dates
        for (date in book.metadata.getDates()) {
            serializer.startTag(NAMESPACE_DUBLIN_CORE, DCTags.date)
            if (date.event != null) {
                serializer.attribute(
                    NAMESPACE_OPF, OPFAttributes.event,
                    date.event.toString()
                )
            }
            serializer.text(date.value)
            serializer.endTag(NAMESPACE_DUBLIN_CORE, DCTags.date)
        }

        // write language
        if (book.metadata.language.isNotBlank()) {
            serializer.startTag(NAMESPACE_DUBLIN_CORE, "language")
            serializer.text(book.metadata.language)
            serializer.endTag(NAMESPACE_DUBLIN_CORE, "language")
        }

        // write other properties
        for ((key, value) in book.metadata.otherProperties) {
            serializer.startTag(key.namespaceURI, OPFTags.meta)
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.property, key.localPart
            )
            serializer.text(value)
            serializer.endTag(key.namespaceURI, OPFTags.meta)
        }

        // write coverimage
        if (book.coverImage != null) { // write the cover image
            serializer.startTag(NAMESPACE_OPF, OPFTags.meta)
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.name,
                OPFValues.meta_cover
            )
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.content,
                book.coverImage?.id
            )
            serializer.endTag(NAMESPACE_OPF, OPFTags.meta)
        }

        // write generator
        serializer.startTag(NAMESPACE_OPF, OPFTags.meta)
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX, OPFAttributes.name,
            OPFValues.generator
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX, OPFAttributes.content,
            Constants.EPUB4J_GENERATOR_NAME
        )
        serializer.endTag(NAMESPACE_OPF, OPFTags.meta)

        serializer.endTag(NAMESPACE_OPF, OPFTags.metadata)
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeSimpleMetadataElements(
        tagName: String,
        values: List<String>,
        serializer: XmlSerializer
    ) = values.forEach { value ->
        if(value.isNotBlank()) {
            serializer.startTag(NAMESPACE_DUBLIN_CORE, tagName)
            serializer.text(value)
            serializer.endTag(NAMESPACE_DUBLIN_CORE, tagName)
        }
    }


    /**
     * Writes out the complete list of Identifiers to the package document.
     * The first identifier for which the bookId is true is made the bookId identifier.
     * If no identifier has bookId == true then the first bookId identifier is written as the primary.
     *
     * @param identifiers
     * @param serializer
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeIdentifiers(
        identifiers: List<Identifier>,
        serializer: XmlSerializer
    ) {
        val bookIdIdentifier = Identifier.getBookIdIdentifier(identifiers) ?: return

        serializer.startTag(NAMESPACE_DUBLIN_CORE, DCTags.identifier)
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            DCAttributes.id,
            BOOK_ID_ID
        )
        serializer.attribute(
            NAMESPACE_OPF,
            OPFAttributes.scheme,
            bookIdIdentifier.scheme
        )
        serializer.text(bookIdIdentifier.value)
        serializer.endTag(NAMESPACE_DUBLIN_CORE, DCTags.identifier)

        for (identifier in identifiers.subList(1, identifiers.size)) {
            if (identifier == bookIdIdentifier) {
                continue
            }
            serializer.startTag(NAMESPACE_DUBLIN_CORE, DCTags.identifier)
            serializer.attribute(NAMESPACE_OPF, "scheme", identifier.scheme)
            serializer.text(identifier.value)
            serializer.endTag(NAMESPACE_DUBLIN_CORE, DCTags.identifier)
        }
    }
}

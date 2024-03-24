package io.documentnode.epub4j.epub

import io.documentnode.epub4j.Constants
import io.documentnode.epub4j.domain.*
import org.xmlpull.v1.XmlSerializer
import java.io.IOException
import java.util.*

/**
 * Writes the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 */
object PackageDocumentWriter : PackageDocumentBase() {
    @Throws(IOException::class)
    fun write(
        epubWriter: EpubWriter,
        serializer: XmlSerializer,
        book: Book
    ) {
        try {
            serializer.startDocument(Constants.CHARACTER_ENCODING, false)
            serializer.setPrefix(PREFIX_OPF, NAMESPACE_OPF)
            serializer.setPrefix(PREFIX_DUBLIN_CORE, NAMESPACE_DUBLIN_CORE)
            serializer.startTag(NAMESPACE_OPF, OPFTags.packageTag)
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX, OPFAttributes.version,
                "2.0"
            )
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.uniqueIdentifier, BOOK_ID_ID
            )

            PackageDocumentMetadataWriter.writeMetaData(book, serializer)

            writeManifest(book, epubWriter, serializer)
            writeSpine(book, epubWriter, serializer)
            writeGuide(book, epubWriter, serializer)

            serializer.endTag(NAMESPACE_OPF, OPFTags.packageTag)
            serializer.endDocument()
            serializer.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Writes the package's spine.
     *
     * @param book
     * @param epubWriter
     * @param serializer
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws XMLStreamException
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeSpine(
        book: Book,
        epubWriter: EpubWriter,
        serializer: XmlSerializer
    ) {
        serializer.startTag(NAMESPACE_OPF, OPFTags.spine)
        val tocResource = book.spine.tocResource
        tocResource?.id?.let { tocResourceId ->
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.toc,
                tocResourceId
            )
        }

        if (book.coverPage != null &&
            book.spine.findFirstResourceById(book.coverPage?.id) < 0
        ) { // cover page is not already in the spine
            // write the cover html file
            serializer.startTag(NAMESPACE_OPF, OPFTags.itemref)
            book.coverPage?.id?.let { id ->
                serializer.attribute(
                    EpubWriter.EMPTY_NAMESPACE_PREFIX,
                    OPFAttributes.idref,
                    id
                )
            }
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.linear,
                "no"
            )
            serializer.endTag(NAMESPACE_OPF, OPFTags.itemref)
        }
        writeSpineItems(book.spine, serializer)
        serializer.endTag(NAMESPACE_OPF, OPFTags.spine)
    }


    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeManifest(
        book: Book,
        epubWriter: EpubWriter,
        serializer: XmlSerializer
    ) {
        serializer.startTag(NAMESPACE_OPF, OPFTags.manifest)

        serializer.startTag(NAMESPACE_OPF, OPFTags.item)
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            OPFAttributes.id,
            epubWriter.ncxId
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            OPFAttributes.href,
            epubWriter.ncxHref
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            OPFAttributes.media_type,
            epubWriter.ncxMediaType
        )
        serializer.endTag(NAMESPACE_OPF, OPFTags.item)

//        		writeCoverResources(book, serializer);
        for (resource in getAllResourcesSortById(book)) {
            writeItem(book, resource, serializer)
        }

        serializer.endTag(NAMESPACE_OPF, OPFTags.manifest)
    }

    private fun getAllResourcesSortById(book: Book): List<Resource> {
        val allResources: List<Resource> = book.resources.all.toList()/*.sortedBy {
            it.id
        }*/
        return allResources
    }

    /**
     * Writes a resources as an item element
     * @param resource
     * @param serializer
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws XMLStreamException
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeItem(
        book: Book,
        resource: Resource,
        serializer: XmlSerializer
    ) {
        if (
            resource.mediaType == MediaTypes.NCX &&
            book.spine.tocResource != null
        ) {
            return
        }
        if (resource.id.isNullOrBlank()) {
            println(
                "resource id must not be empty (href: " + resource.href
                        + ", mediatype:" + resource.mediaType + ")"
            )
            return
        }
        if (resource.href.isNullOrBlank()) {
            println(
                "resource href must not be empty (id: " + resource.id
                        + ", mediatype:" + resource.mediaType + ")"
            )
            return
        }
        if (resource.mediaType?.name == null) {
            println(
                "resource mediatype must not be empty (id: " + resource.id
                        + ", href:" + resource.href + ")"
            )
            return
        }
        serializer.startTag(NAMESPACE_OPF, OPFTags.item)
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            OPFAttributes.id,
            resource.id
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX, OPFAttributes.href,
            resource.href
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX, OPFAttributes.media_type,
            resource.mediaType?.name
        )
        serializer.endTag(NAMESPACE_OPF, OPFTags.item)
    }

    /**
     * List all spine references
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeSpineItems(spine: Spine, serializer: XmlSerializer) {
        for (spineReference in spine.getSpineReferences()) {
            serializer.startTag(NAMESPACE_OPF, OPFTags.itemref)
            spineReference.resourceId?.let { resourceId ->
                serializer.attribute(
                    EpubWriter.EMPTY_NAMESPACE_PREFIX,
                    OPFAttributes.idref,
                    resourceId
                )
            }
            if (!spineReference.linear) {
                serializer.attribute(
                    EpubWriter.EMPTY_NAMESPACE_PREFIX,
                    OPFAttributes.linear,
                    OPFValues.no
                )
            }
            serializer.endTag(NAMESPACE_OPF, OPFTags.itemref)
        }
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeGuide(
        book: Book,
        epubWriter: EpubWriter,
        serializer: XmlSerializer
    ) {
        serializer.startTag(NAMESPACE_OPF, OPFTags.guide)
        ensureCoverPageGuideReferenceWritten(
            book.guide,
            epubWriter,
            serializer
        )
        for (reference in book.guide.getReferences()) {
            writeGuideReference(reference, serializer)
        }
        serializer.endTag(NAMESPACE_OPF, OPFTags.guide)
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun ensureCoverPageGuideReferenceWritten(
        guide: Guide,
        epubWriter: EpubWriter,
        serializer: XmlSerializer
    ) {
        if (guide.getGuideReferencesByType(GuideReference.COVER).isNotEmpty()) {
            return
        }
        val coverPage = guide.coverPage
        if (coverPage != null) {
            writeGuideReference(
                GuideReference(
                    guide.coverPage,
                    GuideReference.COVER,
                    GuideReference.COVER
                ),
                serializer
            )
        }
    }


    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeGuideReference(
        reference: GuideReference,
        serializer: XmlSerializer
    ) {
        serializer.startTag(NAMESPACE_OPF, OPFTags.reference)
        if(!reference.type.isNullOrBlank()) {
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.type,
                reference.type
            )
        }
        if(!reference.completeHref.isNullOrBlank()) {
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.href,
                reference.completeHref
            )
        }
        if(!reference.completeHref.isNullOrBlank()) {
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.href,
                reference.completeHref
            )
        }
        if (!reference.title.isNullOrBlank()) {
            serializer.attribute(
                EpubWriter.EMPTY_NAMESPACE_PREFIX,
                OPFAttributes.title,
                reference.title
            )
        }
        serializer.endTag(NAMESPACE_OPF, OPFTags.reference)
    }
}
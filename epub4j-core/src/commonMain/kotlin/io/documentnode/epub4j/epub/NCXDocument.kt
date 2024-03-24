package io.documentnode.epub4j.epub

import io.documentnode.epub4j.Constants
import io.documentnode.epub4j.domain.*
import io.documentnode.epub4j.util.ResourceUtil
import io.documentnode.epub4j.util.StringUtil
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes the ncx document as defined by namespace http://www.daisy.org/z3986/2005/ncx/
 *
 * @author paul
 */
@Suppress("ConstPropertyName")
object NCXDocument {
    const val NAMESPACE_NCX: String = "http://www.daisy.org/z3986/2005/ncx/"
    const val PREFIX_NCX: String = "ncx"
    const val NCX_ITEM_ID: String = "ncx"
    const val DEFAULT_NCX_HREF: String = "toc.ncx"
    const val PREFIX_DTB: String = "dtb"

    fun read(book: Book, epubReader: EpubReader): Resource? {
        var ncxResource: Resource? = null
        if (book.spine.tocResource == null) {
            return null
        }
        try {
            ncxResource = book.spine.tocResource ?: return null
            val ncxDocument: Document = ResourceUtil.getAsDocument(ncxResource)!!
            val navMapElement = DOMUtil.getFirstElementByTagNameNS(
                ncxDocument.documentElement,
                NAMESPACE_NCX, NCXTags.navMap
            )!!
            val tableOfContents = TableOfContents(
                readTOCReferences(navMapElement.childNodes, book).toMutableList()
            )
            book.tableOfContents = tableOfContents
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ncxResource
    }

    private fun readTOCReferences(
        navpoints: NodeList,
        book: Book
    ): List<TOCReference> {
        return (0 until navpoints.length).asSequence()
            .map(navpoints::item)
            .filterIsInstance(Element::class.java)
            .mapNotNull { node ->
                if (node.localName != NCXTags.navPoint) {
                    return@mapNotNull null
                }
                readTOCReference(node, book)
            }
            .toList()
    }

    fun readTOCReference(navpointElement: Element, book: Book): TOCReference {
        val label = readNavLabel(navpointElement)
        val resourceHref = book.spine.tocResource?.href ?: ""
        var tocResourceRoot: String = resourceHref.substringBeforeLast('/')
        tocResourceRoot = if (
            tocResourceRoot.length == (resourceHref.length)
        ) {
            ""
        } else {
            "$tocResourceRoot/"
        }
        val reference = StringUtil.collapsePathDots(tocResourceRoot + readNavReference(navpointElement))
        val href: String = reference.substringBefore(Constants.FRAGMENT_SEPARATOR_CHAR)
        val fragmentId: String = reference.substringAfter(Constants.FRAGMENT_SEPARATOR_CHAR)
        val resource = book.resources.getByHref(href)
        if (resource == null) {
            println("Resource with href $href in NCX document not found")
        }
        val result = TOCReference(label, resource, fragmentId)
        val childTOCReferences: List<TOCReference> = readTOCReferences(
            navpointElement.childNodes, book
        )
        result.children = childTOCReferences.toMutableList()
        return result
    }

    private fun readNavReference(navpointElement: Element): String {
        val contentElement = DOMUtil.getFirstElementByTagNameNS(
            navpointElement, NAMESPACE_NCX,
            NCXTags.content
        )
        if(contentElement == null) return ""
        val result = DOMUtil.getAttribute(contentElement, NAMESPACE_NCX, NCXAttributes.src)
        return try {
            URLDecoder.decode(result, Constants.CHARACTER_ENCODING)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            result
        }
    }

    private fun readNavLabel(navpointElement: Element): String {
        val navLabel = DOMUtil.getFirstElementByTagNameNS(
            navpointElement, NAMESPACE_NCX,
            NCXTags.navLabel
        )
        if (navLabel == null) return ""
        val element = DOMUtil.getFirstElementByTagNameNS(
            navLabel,
            NAMESPACE_NCX,
            NCXTags.text
        ) ?: return ""
        return DOMUtil.getTextChildrenContent(element)
    }


    @Throws(IOException::class)
    fun write(
        epubWriter: EpubWriter,
        book: Book,
        resultStream: ZipOutputStream
    ) {
        val href = book.spine.tocResource?.href ?: return
        resultStream.putNextEntry(ZipEntry(href))
        val out = EpubProcessorSupport.createXmlSerializer(resultStream) ?: return
        write(out, book)
        out.flush()
    }


    /**
     * Generates a resource containing an xml document containing the table of contents of the book in ncx format.
     *
     * @param xmlSerializer the serializer used
     * @param book the book to serialize
     *
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    fun write(xmlSerializer: XmlSerializer, book: Book) {
        write(
            xmlSerializer,
            book.metadata.getIdentifiers(),
            book.title,
            book.metadata.getAuthors(),
            book.tableOfContents
        )
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    fun createNCXResource(book: Book): Resource? {
        return createNCXResource(
            book.metadata.getIdentifiers(),
            book.title,
            book.metadata.getAuthors(),
            book.tableOfContents
        )
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    fun createNCXResource(
        identifiers: List<Identifier>,
        title: String,
        authors: List<Author>,
        tableOfContents: TableOfContents
    ): Resource? {
        val data = ByteArrayOutputStream()
        val out = EpubProcessorSupport.createXmlSerializer(data) ?: return null
        write(out, identifiers, title, authors, tableOfContents)
        val resource = Resource(
            NCX_ITEM_ID,
            data.toByteArray(),
            DEFAULT_NCX_HREF,
            MediaTypes.NCX
        )
        return resource
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    fun write(
        serializer: XmlSerializer,
        identifiers: List<Identifier>,
        title: String,
        authors: List<Author>,
        tableOfContents: TableOfContents
    ) {
        serializer.startDocument(Constants.CHARACTER_ENCODING, false)
        serializer.setPrefix(EpubWriter.EMPTY_NAMESPACE_PREFIX, NAMESPACE_NCX)
        serializer.startTag(NAMESPACE_NCX, NCXTags.ncx)
        //		serializer.writeNamespace("ncx", NAMESPACE_NCX);
//		serializer.attribute("xmlns", NAMESPACE_NCX);
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.version,
            NCXAttributeValues.version
        )
        serializer.startTag(NAMESPACE_NCX, NCXTags.head)

        identifiers.forEach {
            writeMetaElement(
                it.scheme,
                it.value,
                serializer
            )
        }

        writeMetaElement("generator", Constants.EPUB4J_GENERATOR_NAME, serializer)
        writeMetaElement(
            "depth", tableOfContents.calculateDepth().toString(),
            serializer
        )
        writeMetaElement("totalPageCount", "0", serializer)
        writeMetaElement("maxPageNumber", "0", serializer)

        serializer.endTag(NAMESPACE_NCX, "head")

        serializer.startTag(NAMESPACE_NCX, NCXTags.docTitle)
        serializer.startTag(NAMESPACE_NCX, NCXTags.text)
        // write the first title
        serializer.text(StringUtil.defaultIfNull(title))
        serializer.endTag(NAMESPACE_NCX, NCXTags.text)
        serializer.endTag(NAMESPACE_NCX, NCXTags.docTitle)

        authors.forEach { author ->
            serializer.startTag(NAMESPACE_NCX, NCXTags.docAuthor)
            serializer.startTag(NAMESPACE_NCX, NCXTags.text)
            serializer.text(author.lastname + ", " + author.firstname)
            serializer.endTag(NAMESPACE_NCX, NCXTags.text)
            serializer.endTag(NAMESPACE_NCX, NCXTags.docAuthor)
        }

        serializer.startTag(NAMESPACE_NCX, NCXTags.navMap)
        writeNavPoints(tableOfContents.getTocReferences(), 1, serializer)
        serializer.endTag(NAMESPACE_NCX, NCXTags.navMap)

        serializer.endTag(NAMESPACE_NCX, "ncx")
        serializer.endDocument()
    }


    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeMetaElement(
        dtbName: String,
        content: String,
        serializer: XmlSerializer
    ) {
        serializer.startTag(NAMESPACE_NCX, NCXTags.meta)
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.name,
            "$PREFIX_DTB:$dtbName"
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.content,
            content
        )
        serializer.endTag(NAMESPACE_NCX, NCXTags.meta)
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeNavPoints(
        tocReferences: List<TOCReference>,
        playOrder: Int,
        serializer: XmlSerializer
    ): Int {
        var playOrder = playOrder
        for (tocReference in tocReferences) {
            if (tocReference.resource == null) {
                playOrder = writeNavPoints(
                    tocReferences = tocReference.children,
                    playOrder = playOrder,
                    serializer = serializer
                )
                continue
            }
            if(tocReference.title.isNullOrBlank().not()) {
                writeNavPointStart(
                    tocReference = tocReference,
                    playOrder = playOrder,
                    serializer = serializer
                )
                playOrder++
                if (tocReference.children.isNotEmpty()) {
                    playOrder = writeNavPoints(
                        tocReferences = tocReference.children,
                        playOrder = playOrder,
                        serializer = serializer
                    )
                }
                writeNavPointEnd(serializer)
            } else {
                continue
            }
        }
        return playOrder
    }


    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeNavPointStart(
        tocReference: TOCReference,
        playOrder: Int,
        serializer: XmlSerializer
    ) {
        serializer.startTag(NAMESPACE_NCX, NCXTags.navPoint)
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            NCXAttributes.id,
            "navPoint-$playOrder"
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            NCXAttributes.playOrder,
            playOrder.toString()
        )
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            NCXAttributes.clazz,
            NCXAttributeValues.chapter
        )
        serializer.startTag(NAMESPACE_NCX, NCXTags.navLabel)
        serializer.startTag(NAMESPACE_NCX, NCXTags.text)
        serializer.text(tocReference.title)
        serializer.endTag(NAMESPACE_NCX, NCXTags.text)
        serializer.endTag(NAMESPACE_NCX, NCXTags.navLabel)
        serializer.startTag(NAMESPACE_NCX, NCXTags.content)
        serializer.attribute(
            EpubWriter.EMPTY_NAMESPACE_PREFIX,
            NCXAttributes.src,
            tocReference.completeHref
        )
        serializer.endTag(NAMESPACE_NCX, NCXTags.content)
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    private fun writeNavPointEnd(serializer: XmlSerializer) {
        serializer.endTag(NAMESPACE_NCX, NCXTags.navPoint)
    }

    private interface NCXTags {
        companion object {
            const val ncx: String = "ncx"
            const val meta: String = "meta"
            const val navPoint: String = "navPoint"
            const val navMap: String = "navMap"
            const val navLabel: String = "navLabel"
            const val content: String = "content"
            const val text: String = "text"
            const val docTitle: String = "docTitle"
            const val docAuthor: String = "docAuthor"
            const val head: String = "head"
        }
    }

    private interface NCXAttributes {
        companion object {
            const val src: String = "src"
            const val name: String = "name"
            const val content: String = "content"
            const val id: String = "id"
            const val playOrder: String = "playOrder"
            const val clazz: String = "class"
            const val version: String = "version"
        }
    }

    private interface NCXAttributeValues {
        companion object {
            const val chapter: String = "chapter"
            const val version: String = "2005-1"
        }
    }
}

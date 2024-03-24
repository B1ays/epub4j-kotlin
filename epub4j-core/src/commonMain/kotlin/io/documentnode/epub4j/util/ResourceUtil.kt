package io.documentnode.epub4j.util

import io.documentnode.epub4j.Constants
import io.documentnode.epub4j.domain.MediaTypes
import io.documentnode.epub4j.domain.Resource
import io.documentnode.epub4j.epub.EpubProcessorSupport
import net.sf.jazzlib.ZipEntry
import net.sf.jazzlib.ZipInputStream
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.ParserConfigurationException

/**
 * Various resource utility methods
 *
 * @author paul
 */
object ResourceUtil {
    @Throws(IOException::class)
    fun createResource(file: File): Resource {
        val mediaType = MediaTypes.determineMediaType(file.name)
        val data = FileInputStream(file).readBytes()
        return Resource(data, mediaType)
    }


    /**
     * Creates a resource with as contents a html page with the given title.
     *
     * @param title
     * @param href
     * @return a resource with as contents a html page with the given title.
     */
    fun createResource(title: String, href: String): Resource {
        val content =
            ("<html><head><title>" + title + "</title></head><body><h1>" + title
                    + "</h1></body></html>")
        return Resource(
            null,
            content.toByteArray(),
            href,
            MediaTypes.XHTML,
            Constants.CHARACTER_ENCODING
        )
    }

    /**
     * Creates a resource out of the given zipEntry and zipInputStream.
     *
     * @param zipEntry
     * @param zipInputStream
     * @return a resource created out of the given zipEntry and zipInputStream.
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun createResource(
        zipEntry: ZipEntry,
        zipInputStream: ZipInputStream
    ): Resource {
        return Resource(zipInputStream, zipEntry.name)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createResource(
        zipEntry: ZipEntry,
        zipInputStream: InputStream
    ): Resource {
        return Resource(zipInputStream, zipEntry.name)
    }

    /**
     * Converts a given string from given input character encoding to the requested output character encoding.
     *
     * @param inputEncoding
     * @param outputEncoding
     * @param input
     * @return the string from given input character encoding converted to the requested output character encoding.
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun recode(
        inputEncoding: String,
        outputEncoding: String,
        input: ByteArray
    ): ByteArray {
        return String(input, charset(inputEncoding)).toByteArray(
            charset(outputEncoding)
        )
    }

    /**
     * Gets the contents of the Resource as an InputSource in a null-safe manner.
     *
     */
    @Throws(IOException::class)
    fun getInputSource(resource: Resource): InputSource? {
        val reader = resource.reader ?: return null
        return InputSource(reader)
    }


    /**
     * Reads parses the xml therein and returns the result as a Document
     */
    @JvmStatic
    @Throws(SAXException::class, IOException::class, ParserConfigurationException::class)
    fun getAsDocument(resource: Resource): Document? {
        val documentBuilder = EpubProcessorSupport.createDocumentBuilder()
        return documentBuilder?.let {
            getAsDocument(resource, it)
        }
    }

    /**
     * Reads the given resources inputstream, parses the xml therein and returns the result as a Document
     *
     * @param resource
     * @param documentBuilder
     * @return the document created from the given resource
     * @throws UnsupportedEncodingException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    @Throws(
        UnsupportedEncodingException::class,
        SAXException::class,
        IOException::class,
        ParserConfigurationException::class
    )
    fun getAsDocument(
        resource: Resource,
        documentBuilder: DocumentBuilder
    ): Document? {
        val inputSource = getInputSource(resource) ?: return null
        return documentBuilder.parse(inputSource)
    }
}

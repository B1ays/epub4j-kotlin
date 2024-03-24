package io.documentnode.epub4j.epub

import io.documentnode.epub4j.Constants
import io.documentnode.epub4j.domain.*
import io.documentnode.epub4j.util.ResourceUtil
import net.sf.jazzlib.ZipFile
import net.sf.jazzlib.ZipInputStream
import org.w3c.dom.Element
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Reads an epub file.
 *
 * @author paul
 */
class EpubReader {
    private val bookProcessor: BookProcessor = BookProcessor.IDENTITY_BOOKPROCESSOR

    @Throws(IOException::class)
    fun readEpub(zipfile: ZipFile): Book {
        return readEpub(zipfile, Constants.CHARACTER_ENCODING)
    }

    /**
     * Read epub from inputstream
     *
     * @param `in` the inputstream from which to read the epub
     * @param encoding the encoding to use for the html files within the epub
     * @return the Book as read from the inputstream
     * @throws IOException
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun readEpub(
        inputStream: InputStream,
        encoding: String = Constants.CHARACTER_ENCODING
    ): Book {
        return readEpub(ZipInputStream(inputStream), encoding)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readEpub(
        inputStream: ZipInputStream,
        encoding: String = Constants.CHARACTER_ENCODING
    ): Book {
        return readEpub(ResourcesLoader.loadResources(inputStream, encoding))
    }

    @Throws(IOException::class)
    fun readEpub(
        zipFile: ZipFile,
        encoding: String
    ): Book {
        return readEpub(ResourcesLoader.loadResources(zipFile, encoding))
    }

    /**
     * Reads this EPUB without loading all resources into memory.
     *
     * @param zipFile the file to load
     * @param encoding the encoding for XHTML files
     * @param lazyLoadedTypes a list of the MediaType to load lazily
     * @return this Book without loading all resources into memory.
     * @throws IOException
     */
    /**
     * Reads this EPUB without loading any resources into memory.
     *
     * @param zipFile the file to load
     * @param encoding the encoding for XHTML files
     *
     * @return this Book without loading all resources into memory.
     * @throws IOException
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun readEpubLazy(
        zipFile: ZipFile,
        encoding: String,
        lazyLoadedTypes: List<MediaType> = MediaTypes.mediaTypes.toList()
    ): Book {
        val resources = ResourcesLoader.loadResources(zipFile, encoding, lazyLoadedTypes)
        return readEpub(resources)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readEpub(
        resources: Resources,
        result: Book = Book()
    ): Book {
        handleMimeType(result, resources)
        val packageResourceHref = getPackageResourceHref(resources)
        val packageResource = processPackageResource(
            packageResourceHref,
            result,
            resources
        )
        if(packageResource == null) return result
        result.opfResource = packageResource
        val ncxResource = processNcxResource(packageResource, result)
        result.ncxResource = ncxResource
        return postProcessBook(result)
    }

    private fun postProcessBook(book: Book): Book {
        return bookProcessor.processBook(book)
    }

    private fun processNcxResource(
        packageResource: Resource,
        book: Book
    ): Resource? {
        return NCXDocument.read(book, this)
    }

    private fun processPackageResource(
        packageResourceHref: String,
        book: Book,
        resources: Resources
    ): Resource? {
        val packageResource = resources.remove(packageResourceHref)
        try {
            packageResource?.let {
                PackageDocumentReader.read(it, this, book, resources)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return packageResource
    }

    private fun getPackageResourceHref(resources: Resources): String {
        val defaultResult = "OEBPS/content.opf"
        var result = defaultResult

        val containerResource = resources.remove("META-INF/container.xml") ?: return result
        try {
            val document = ResourceUtil.getAsDocument(containerResource) ?: return result
            val rootFileElement = (document.documentElement
                .getElementsByTagName("rootfiles").item(0) as Element)
                .getElementsByTagName("rootfile").item(0) as Element
            result = rootFileElement.getAttribute("full-path")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (result.isBlank()) {
            result = defaultResult
        }
        return result
    }

    private fun handleMimeType(result: Book, resources: Resources) {
        resources.remove("mimetype")
    }
}

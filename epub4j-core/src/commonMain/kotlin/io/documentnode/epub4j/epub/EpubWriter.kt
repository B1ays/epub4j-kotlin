package io.documentnode.epub4j.epub

import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.domain.MediaTypes
import io.documentnode.epub4j.domain.Resource
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Generates an epub file. Not thread-safe, single use object.
 *
 * @author paul
 */
class EpubWriter @JvmOverloads constructor(
    private val bookProcessor: BookProcessor = BookProcessor.IDENTITY_BOOKPROCESSOR
) {
    @Throws(IOException::class)
    fun write(
        book: Book,
        out: OutputStream
    ) {
        val processedBook = processBook(book)
        val resultStream = ZipOutputStream(out)
        writeMimeType(resultStream)
        writeContainer(resultStream)
        initTOCResource(processedBook)
        writeResources(processedBook, resultStream)
        writePackageDocument(processedBook, resultStream)
        resultStream.close()
    }

    private fun processBook(
        book: Book
    ): Book {
        return bookProcessor.processBook(book)
    }

    private fun initTOCResource(book: Book) {
        try {
            val tocResource = NCXDocument.createNCXResource(book)
            val currentTocResource = book.spine.tocResource
            if (currentTocResource != null) {
                book.resources.remove(currentTocResource.href)
            }
            book.spine.tocResource = tocResource
            book.resources.add(tocResource!!)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    @Throws(IOException::class)
    private fun writeResources(
        book: Book,
        resultStream: ZipOutputStream
    ) {
        book.resources.all.forEach {
            writeResource(it, resultStream)
        }
    }

    /**
     * Writes the resource to the resultStream.
     *
     * @param resource
     * @param resultStream
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeResource(resource: Resource, resultStream: ZipOutputStream) {
        val inputStream = resource.inputStream
        try {
            if(inputStream == null) {
                throw IllegalStateException("Can't create input stream for resource: ${resource.href}")
            }
            resultStream.putNextEntry(ZipEntry("OEBPS/" + resource.href))
            inputStream.copyTo(resultStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
    }


    @Throws(IOException::class)
    private fun writePackageDocument(
        book: Book,
        resultStream: ZipOutputStream
    ) {
        resultStream.putNextEntry(ZipEntry("OEBPS/content.opf"))
        val xmlSerializer = EpubProcessorSupport.createXmlSerializer(resultStream) ?: return
        PackageDocumentWriter.write(
            this,
            xmlSerializer,
            book
        )
        xmlSerializer.flush()
    }

    /**
     * Writes the META-INF/container.xml file.
     *
     * @param resultStream
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeContainer(resultStream: ZipOutputStream) {
        resultStream.putNextEntry(ZipEntry("META-INF/container.xml"))
        val out: Writer = OutputStreamWriter(resultStream)
        out.write("<?xml version=\"1.0\"?>\n")
        out.write(
            "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n"
        )
        out.write("\t<rootfiles>\n")
        out.write(
            "\t\t<rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n"
        )
        out.write("\t</rootfiles>\n")
        out.write("</container>")
        out.flush()
    }

    /**
     * Stores the mimetype as an uncompressed file in the ZipOutputStream.
     *
     * @param resultStream
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeMimeType(resultStream: ZipOutputStream) {
        val mimetypeZipEntry = ZipEntry("mimetype")
        mimetypeZipEntry.method = ZipEntry.STORED
        val mimetypeBytes: ByteArray = MediaTypes.EPUB.name.toByteArray()
        mimetypeZipEntry.size = mimetypeBytes.size.toLong()
        mimetypeZipEntry.crc = calculateCrc(mimetypeBytes)
        resultStream.putNextEntry(mimetypeZipEntry)
        resultStream.write(mimetypeBytes)
    }

    private fun calculateCrc(data: ByteArray): Long {
        val crc = CRC32()
        crc.update(data)
        return crc.value
    }

    val ncxId: String
        get() = "ncx"

    val ncxHref: String
        get() = "toc.ncx"

    val ncxMediaType: String
        get() = MediaTypes.NCX.name


    companion object {
        // package
        const val EMPTY_NAMESPACE_PREFIX: String = ""
    }
}

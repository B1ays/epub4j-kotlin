package io.documentnode.epub4j.util.commons.io

import java.io.*
import java.text.MessageFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Character stream that handles all the necessary Voodo to figure out the
 * charset encoding of the XML document within the stream.
 *
 *
 * IMPORTANT: This class is not related in any way to the org.xml.sax.XMLReader.
 * This one IS a character stream.
 *
 *
 * All this has to be done without consuming characters from the stream, if not
 * the XML parser will not recognized the document as a valid XML. This is not
 * 100% true, but it's close enough (UTF-8 BOM is not handled by all parsers
 * right now, XmlStreamReader handles it and things work in all parsers).
 *
 *
 * The XmlStreamReader class handles the charset encoding of XML documents in
 * Files, raw streams and HTTP streams by offering a wide set of constructors.
 *
 *
 * By default the charset encoding detection is lenient, the constructor with
 * the lenient flag can be used for an script (following HTTP MIME and XML
 * specifications). All this is nicely explained by Mark Pilgrim in his blog, [
 * Determining the character encoding of a feed](http://diveintomark.org/archives/2004/02/13/xml-media-types).
 *
 *
 * Originally developed for [ROME](http://rome.dev.java.net) under
 * Apache License 2.0.
 *
 * @author Alejandro Abdelnur
 * @version $Id: XmlStreamReader.java 1052161 2010-12-23 03:12:09Z niallp $
 * @see "org.apache.commons.io.output.XmlStreamWriter"
 *
 * @since Commons IO 2.0
 */
class XmlStreamReader(
    inputStream: InputStream,
    private val lenient: Boolean = true,
    private val defaultEncoding: String? = null
): Reader() {
    private val bom = BOMInputStream(BufferedInputStream(inputStream, BUFFER_SIZE), false, *BOMS)
    private val pis = BOMInputStream(bom, true, *XML_GUESS_BYTES)

    private val reader: Reader = InputStreamReader(inputStream, defaultEncoding)

    /**
     * Returns the charset encoding of the XmlStreamReader.
     *
     * @return charset encoding.
     */
    val encoding: String? = doRawStream(bom, pis, lenient)

    /**
     * Creates a Reader for a File.
     *
     *
     * It looks for the UTF-8 BOM first, if none sniffs the XML prolog charset,
     * if this is also missing defaults to UTF-8.
     *
     *
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param file File to create a Reader from.
     * @throws IOException thrown if there is a problem reading the file.
     */
    constructor(file: File) : this(
        file.inputStream()
    )

    /**
     * Creates a Reader using an InputStream an the associated content-type
     * header. This constructor is lenient regarding the encoding detection.
     *
     *
     * First it checks if the stream has BOM. If there is not BOM checks the
     * content-type encoding. If there is not content-type encoding checks the
     * XML prolog encoding. If there is not XML prolog encoding uses the default
     * encoding mandated by the content-type MIME type.
     *
     *
     * If lenient detection is indicated and the detection above fails as per
     * specifications it then attempts the following:
     *
     *
     * If the content type was 'text/html' it replaces it with 'text/xml' and
     * tries the detection again.
     *
     *
     * Else if the XML prolog had a charset encoding that encoding is used.
     *
     *
     * Else if the content type had a charset encoding that encoding is used.
     *
     *
     * Else 'UTF-8' is used.
     *
     *
     * If lenient detection is indicated an XmlStreamReaderException is never
     * thrown.
     *
     * @param is InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     * the charset encoding.
     * @param lenient indicates if the charset encoding detection should be
     * relaxed.
     * @param defaultEncoding The default encoding
     * @throws IOException thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     * be determined according to the specs.
     */
    /**
     * Creates a Reader using an InputStream an the associated content-type
     * header. This constructor is lenient regarding the encoding detection.
     *
     *
     * First it checks if the stream has BOM. If there is not BOM checks the
     * content-type encoding. If there is not content-type encoding checks the
     * XML prolog encoding. If there is not XML prolog encoding uses the default
     * encoding mandated by the content-type MIME type.
     *
     *
     * If lenient detection is indicated and the detection above fails as per
     * specifications it then attempts the following:
     *
     *
     * If the content type was 'text/html' it replaces it with 'text/xml' and
     * tries the detection again.
     *
     *
     * Else if the XML prolog had a charset encoding that encoding is used.
     *
     *
     * Else if the content type had a charset encoding that encoding is used.
     *
     *
     * Else 'UTF-8' is used.
     *
     *
     * If lenient detection is indicated an XmlStreamReaderException is never
     * thrown.
     *
     * @param is InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     * the charset encoding.
     * @param lenient indicates if the charset encoding detection should be
     * relaxed.
     * @throws IOException thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     * be determined according to the specs.
     */
    /**
     * Creates a Reader using an InputStream an the associated content-type
     * header.
     *
     *
     * First it checks if the stream has BOM. If there is not BOM checks the
     * content-type encoding. If there is not content-type encoding checks the
     * XML prolog encoding. If there is not XML prolog encoding uses the default
     * encoding mandated by the content-type MIME type.
     *
     *
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param is InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     * the charset encoding.
     * @throws IOException thrown if there is a problem reading the file.
     */
    /*@JvmOverloads
    constructor(
        `is`: InputStream?,
        httpContentType: String?,
        lenient: Boolean = true,
        defaultEncoding: String? = null
    ) : this() {
        this.defaultEncoding = defaultEncoding
        val bom = BOMInputStream(BufferedInputStream(`is`, BUFFER_SIZE), false, *BOMS)
        val pis = BOMInputStream(bom, true, *XML_GUESS_BYTES)
        this.encoding = doHttpStream(bom, pis, httpContentType, lenient)
        this.reader = InputStreamReader(pis, encoding)
    }*/

    /**
     * Invokes the underlying reader's `read(char[], int, int)` method.
     * @param buf the buffer to read the characters into
     * @param offset The start offset
     * @param len The number of bytes to read
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(buf: CharArray, offset: Int, len: Int): Int {
        return reader.read(buf, offset, len)
    }

    /**
     * Closes the XmlStreamReader stream.
     *
     * @throws IOException thrown if there was a problem closing the stream.
     */
    @Throws(IOException::class)
    override fun close() {
        reader.close()
    }

    /**
     * Process the raw stream.
     *
     * @param bom BOMInputStream to detect byte order marks
     * @param pis BOMInputStream to guess XML encoding
     * @param lenient indicates if the charset encoding detection should be
     * relaxed.
     * @return the encoding to be used
     * @throws IOException thrown if there is a problem reading the stream.
     */
    @Throws(IOException::class)
    private fun doRawStream(bom: BOMInputStream, pis: BOMInputStream, lenient: Boolean): String? {
        val bomEnc = bom.bOMCharsetName
        val xmlGuessEnc = pis.bOMCharsetName
        val xmlEnc = getXmlProlog(pis, xmlGuessEnc)
        return try {
            calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc)
        } catch (ex: XmlStreamReaderException) {
            if (lenient) {
                doLenientDetection(null, ex)
            } else {
                throw ex
            }
        }
    }

    /**
     * Process a HTTP stream.
     *
     * @param bom BOMInputStream to detect byte order marks
     * @param pis BOMInputStream to guess XML encoding
     * @param httpContentType The HTTP content type
     * @param lenient indicates if the charset encoding detection should be
     * relaxed.
     * @return the encoding to be used
     * @throws IOException thrown if there is a problem reading the stream.
     */
    @Throws(IOException::class)
    private fun doHttpStream(
        bom: BOMInputStream, pis: BOMInputStream, httpContentType: String?,
        lenient: Boolean
    ): String? {
        val bomEnc = bom.bOMCharsetName
        val xmlGuessEnc = pis.bOMCharsetName
        val xmlEnc = getXmlProlog(pis, xmlGuessEnc)
        return try {
            calculateHttpEncoding(
                httpContentType, bomEnc,
                xmlGuessEnc, xmlEnc, lenient
            )
        } catch (ex: XmlStreamReaderException) {
            if (lenient) {
                doLenientDetection(httpContentType, ex)
            } else {
                throw ex
            }
        }
    }

    /**
     * Do lenient detection.
     *
     * @param httpContentType content-type header to use for the resolution of
     * the charset encoding.
     * @param ex The thrown exception
     * @return the encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    @Throws(IOException::class)
    private fun doLenientDetection(
        httpContentType: String?,
        ex: XmlStreamReaderException
    ): String {
        var httpContentType = httpContentType
        var ex = ex
        if (httpContentType != null && httpContentType.startsWith("text/html")) {
            httpContentType = httpContentType.substring("text/html".length)
            httpContentType = "text/xml$httpContentType"
            try {
                return calculateHttpEncoding(
                    httpContentType, ex.bomEncoding,
                    ex.xmlGuessEncoding, ex.xmlEncoding, true
                )
            } catch (ex2: XmlStreamReaderException) {
                ex = ex2
            }
        }
        var encoding = ex.xmlEncoding
        if (encoding == null) {
            encoding = ex.contentTypeEncoding
        }
        if (encoding == null) {
            encoding = if ((defaultEncoding == null)) UTF_8 else defaultEncoding
        }
        return encoding
    }

    /**
     * Calculate the raw encoding.
     *
     * @param bomEnc BOM encoding
     * @param xmlGuessEnc XML Guess encoding
     * @param xmlEnc XML encoding
     * @return the raw encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    @Throws(IOException::class)
    fun calculateRawEncoding(
        bomEnc: String?, xmlGuessEnc: String?,
        xmlEnc: String?
    ): String {
        // BOM is Null

        if (bomEnc == null) {
            if (xmlGuessEnc == null || xmlEnc == null) {
                return (defaultEncoding ?: UTF_8)
            }
            if (xmlEnc == UTF_16 &&
                (xmlGuessEnc == UTF_16BE || xmlGuessEnc == UTF_16LE)
            ) {
                return xmlGuessEnc
            }
            return xmlEnc
        }

        // BOM is UTF-8
        if (bomEnc == UTF_8) {
            if (xmlGuessEnc != null && xmlGuessEnc != UTF_8) {
                val msg = MessageFormat.format(RAW_EX_1, *arrayOf<Any?>(bomEnc, xmlGuessEnc, xmlEnc))
                throw XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc)
            }
            if (xmlEnc != null && xmlEnc != UTF_8) {
                val msg = MessageFormat.format(RAW_EX_1, *arrayOf<Any?>(bomEnc, xmlGuessEnc, xmlEnc))
                throw XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc)
            }
            return bomEnc
        }

        // BOM is UTF-16BE or UTF-16LE
        if (bomEnc == UTF_16BE || bomEnc == UTF_16LE) {
            if (xmlGuessEnc != null && xmlGuessEnc != bomEnc) {
                val msg = MessageFormat.format(RAW_EX_1, *arrayOf<Any?>(bomEnc, xmlGuessEnc, xmlEnc))
                throw XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc)
            }
            if (xmlEnc != null && xmlEnc != UTF_16 && xmlEnc != bomEnc) {
                val msg = MessageFormat.format(RAW_EX_1, *arrayOf<Any?>(bomEnc, xmlGuessEnc, xmlEnc))
                throw XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc)
            }
            return bomEnc
        }

        // BOM is something else
        val msg = MessageFormat.format(RAW_EX_2, *arrayOf<Any?>(bomEnc, xmlGuessEnc, xmlEnc))
        throw XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc)
    }


    /**
     * Calculate the HTTP encoding.
     *
     * @param httpContentType The HTTP content type
     * @param bomEnc BOM encoding
     * @param xmlGuessEnc XML Guess encoding
     * @param xmlEnc XML encoding
     * @param lenient indicates if the charset encoding detection should be
     * relaxed.
     * @return the HTTP encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    @Throws(IOException::class)
    fun calculateHttpEncoding(
        httpContentType: String?,
        bomEnc: String?, xmlGuessEnc: String?, xmlEnc: String?,
        lenient: Boolean
    ): String {
        // Lenient and has XML encoding

        if (lenient && xmlEnc != null) {
            return xmlEnc
        }

        // Determine mime/encoding content types from HTTP Content Type
        val cTMime = getContentTypeMime(httpContentType)
        val cTEnc = getContentTypeEncoding(httpContentType)
        val appXml = isAppXml(cTMime)
        val textXml = isTextXml(cTMime)

        // Mime type NOT "application/xml" or "text/xml"
        if (!appXml && !textXml) {
            val msg = MessageFormat.format(HTTP_EX_3, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc)
            throw XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc)
        }

        // No content type encoding
        if (cTEnc == null) {
            return if (appXml) {
                calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc)
            } else {
                if ((defaultEncoding == null)) US_ASCII else defaultEncoding
            }
        }

        // UTF-16BE or UTF-16LE content type encoding
        if (cTEnc == UTF_16BE || cTEnc == UTF_16LE) {
            if (bomEnc != null) {
                val msg = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc)
                throw XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc)
            }
            return cTEnc
        }

        // UTF-16 content type encoding
        if (cTEnc == UTF_16) {
            if (bomEnc != null && bomEnc.startsWith(UTF_16)) {
                return bomEnc
            }
            val msg = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc)
            throw XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc)
        }

        return cTEnc
    }

    companion object {
        private const val BUFFER_SIZE = 4096

        private const val UTF_8 = "UTF-8"

        private const val US_ASCII = "US-ASCII"

        private const val UTF_16BE = "UTF-16BE"

        private const val UTF_16LE = "UTF-16LE"

        private const val UTF_16 = "UTF-16"

        private const val EBCDIC = "CP1047"

        private val BOMS = arrayOf(
            ByteOrderMark.UTF_8,
            ByteOrderMark.UTF_16BE,
            ByteOrderMark.UTF_16LE
        )
        private val XML_GUESS_BYTES = arrayOf(
            ByteOrderMark(UTF_8, intArrayOf(0x3C, 0x3F, 0x78, 0x6D)),
            ByteOrderMark(UTF_16BE, intArrayOf(0x00, 0x3C, 0x00, 0x3F)),
            ByteOrderMark(UTF_16LE, intArrayOf(0x3C, 0x00, 0x3F, 0x00)),
            ByteOrderMark(EBCDIC, intArrayOf(0x4C, 0x6F, 0xA7, 0x94))
        )


        /**
         * Returns MIME type or NULL if httpContentType is NULL.
         *
         * @param httpContentType the HTTP content type
         * @return The mime content type
         */
        fun getContentTypeMime(httpContentType: String?): String? {
            var mime: String? = null
            if (httpContentType != null) {
                val i = httpContentType.indexOf(";")
                mime = if (i >= 0) {
                    httpContentType.substring(0, i)
                } else {
                    httpContentType
                }
                mime = mime.trim { it <= ' ' }
            }
            return mime
        }

        private val CHARSET_PATTERN: Pattern = Pattern
            .compile("charset=[\"']?([.[^; \"']]*)[\"']?")

        /**
         * Returns charset parameter value, NULL if not present, NULL if
         * httpContentType is NULL.
         *
         * @param httpContentType the HTTP content type
         * @return The content type encoding
         */
        fun getContentTypeEncoding(httpContentType: String?): String? {
            var encoding: String? = null
            if (httpContentType != null) {
                val i = httpContentType.indexOf(";")
                if (i > -1) {
                    val postMime = httpContentType.substring(i + 1)
                    val m = CHARSET_PATTERN.matcher(postMime)
                    encoding = if ((m.find())) m.group(1) else null
                    encoding = if ((encoding != null)) encoding.uppercase(Locale.getDefault()) else null
                }
            }
            return encoding
        }

        private val ENCODING_PATTERN: Pattern = Pattern.compile(
            "<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))",
            Pattern.MULTILINE
        )

        /**
         * Returns the encoding declared in the , NULL if none.
         *
         * @param is InputStream to create the reader from.
         * @param guessedEnc guessed encoding
         * @return the encoding declared in the
         * @throws IOException thrown if there is a problem reading the stream.
         */
        @Throws(IOException::class)
        private fun getXmlProlog(`is`: InputStream, guessedEnc: String?): String? {
            var encoding: String? = null
            if (guessedEnc != null) {
                val bytes = ByteArray(BUFFER_SIZE)
                `is`.mark(BUFFER_SIZE)
                var offset = 0
                var max = BUFFER_SIZE
                var c = `is`.read(bytes, offset, max)
                var firstGT = -1
                var xmlProlog: String? = null
                while (c != -1 && firstGT == -1 && offset < BUFFER_SIZE) {
                    offset += c
                    max -= c
                    c = `is`.read(bytes, offset, max)
                    xmlProlog = String(bytes, 0, offset, charset(guessedEnc))
                    firstGT = xmlProlog.indexOf('>')
                }
                if (firstGT == -1) {
                    if (c == -1) {
                        throw IOException("Unexpected end of XML stream")
                    } else {
                        throw IOException(
                            "XML prolog or ROOT element not found on first "
                                    + offset + " bytes"
                        )
                    }
                }
                val bytesRead = offset
                if (bytesRead > 0) {
                    `is`.reset()
                    val bReader = BufferedReader(
                        StringReader(
                            xmlProlog!!.substring(0, firstGT + 1)
                        )
                    )
                    val prolog = StringBuilder()
                    var line = bReader.readLine()
                    while (line != null) {
                        prolog.append(line)
                        line = bReader.readLine()
                    }
                    val m = ENCODING_PATTERN.matcher(prolog)
                    if (m.find()) {
                        encoding = m.group(1).uppercase(Locale.getDefault())
                        encoding = encoding.substring(1, encoding.length - 1)
                    }
                }
            }
            return encoding
        }

        /**
         * Indicates if the MIME type belongs to the APPLICATION XML family.
         *
         * @param mime The mime type
         * @return true if the mime type belongs to the APPLICATION XML family,
         * otherwise false
         */
        fun isAppXml(mime: String?): Boolean {
            return mime != null &&
                    (mime == "application/xml" || mime == "application/xml-dtd" || mime == "application/xml-external-parsed-entity" ||
                            (mime.startsWith("application/") && mime.endsWith("+xml")))
        }

        /**
         * Indicates if the MIME type belongs to the TEXT XML family.
         *
         * @param mime The mime type
         * @return true if the mime type belongs to the TEXT XML family,
         * otherwise false
         */
        fun isTextXml(mime: String?): Boolean {
            return mime != null &&
                    (mime == "text/xml" || mime == "text/xml-external-parsed-entity" ||
                            (mime.startsWith("text/") && mime.endsWith("+xml")))
        }

        private const val RAW_EX_1 = "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch"

        private const val RAW_EX_2 = "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM"

        private const val HTTP_EX_1 =
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be NULL"

        private const val HTTP_EX_2 =
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch"

        private const val HTTP_EX_3 =
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Invalid MIME"
    }
}

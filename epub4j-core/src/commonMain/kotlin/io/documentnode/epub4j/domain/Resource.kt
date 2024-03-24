package io.documentnode.epub4j.domain

import io.documentnode.epub4j.Constants
import io.documentnode.epub4j.domain.MediaTypes.determineMediaType
import io.documentnode.epub4j.util.commons.io.XmlStreamReader
import io.documentnode.epub4j.util.toByteArray
import java.io.*

/**
 * Represents a resource that is part of the epub.
 * A resource can be a html file, image, xml, etc.
 *
 * @author paul
 */
open class Resource(
    var id: String?,
    open var data: ByteArray?,
    var href: String?,
    var mediaType: MediaType?,
    var inputEncoding: String = Constants.CHARACTER_ENCODING
) : Serializable {
    /**
     * Creates an empty Resource with the given href.
     *
     * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
     *
     * @param href The location of the resource within the epub. Example: "chapter1.html".
     */
    constructor(href: String) : this(null, ByteArray(0), href, determineMediaType(href))

    /**
     * Creates a Resource with the given data and MediaType.
     * The href will be automatically generated.
     *
     * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
     *
     * @param data The Resource's contents
     * @param mediaType The MediaType of the Resource
     */
    constructor(data: ByteArray, mediaType: MediaType?) : this(null, data, null, mediaType)

    /**
     * Creates a resource with the given data at the specified href.
     * The MediaType will be determined based on the href extension.
     *
     * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
     *
     * @see MediaTypes.determineMediaType
     * @param data The Resource's contents
     * @param href The location of the resource within the epub. Example: "chapter1.html".
     */
    constructor(data: ByteArray, href: String) : this(
        null,
        data,
        href,
        determineMediaType(href),
        Constants.CHARACTER_ENCODING
    )

    /**
     * Creates a resource with the given data at the specified href.
     * The MediaType will be determined based on the href extension.
     *
     * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
     *
     * @see MediaTypes.determineMediaType
     * @param id The Resource's id
     * @param data The Resource's contents
     * @param href The location of the resource within the epub. Example: "chapter1.html".
     */
    constructor(id: String, data: ByteArray, href: String) : this(
        id,
        data,
        href,
        determineMediaType(href),
        Constants.CHARACTER_ENCODING
    )

    /**
     * Creates a resource with the data from the given Reader at the specified href.
     * The MediaType will be determined based on the href extension.
     *
     * @see MediaTypes.determineMediaType
     * @param reader The Resource's contents
     * @param href The location of the resource within the epub. Example: "cover.jpg".
     */
    constructor(reader: Reader, href: String) : this(
        null,
        reader.toByteArray(Constants.CHARACTER_ENCODING),
        href,
        determineMediaType(href),
        Constants.CHARACTER_ENCODING
    )

    /**
     * Creates a resource with the data from the given InputStream at the specified href.
     * The MediaType will be determined based on the href extension.
     *
     * @see MediaTypes.determineMediaType
     * @param inputStream The Resource's contents
     * @param href The location of the resource within the epub. Example: "cover.jpg".
     */
    constructor(inputStream: InputStream, href: String) : this(
        null,
        inputStream.readBytes(),
        href,
        determineMediaType(href)
    )

    @get:Throws(IOException::class)
    open val inputStream: InputStream?
        /**
         * Gets the contents of the Resource as an InputStream.
         *
         * @return The contents of the Resource.
         *
         * @throws IOException
         */
        get() = ByteArrayInputStream(data)

    /**
     * Tells this resource to release its cached data.
     *
     * If this resource was not lazy-loaded, this is a no-op.
     */
    open fun close() {}

    open val size: Long
        /**
         * Returns the size of this resource in bytes.
         *
         * @return the size.
         */
        get() = data?.size?.toLong() ?: 0

    @get:Throws(IOException::class)
    val reader: Reader
        /**
         * Gets the contents of the Resource as Reader.
         *
         * Does all sorts of smart things (courtesy of apache commons io XMLStreamREader) to handle encodings, byte order markers, etc.
         *
         * @return the contents of the Resource as Reader.
         * @throws IOException
         */
        get() = XmlStreamReader(
            inputStream = ByteArrayInputStream(data),
            defaultEncoding = inputEncoding
        )

    companion object {
        private const val serialVersionUID = 1043946707835004037L
    }
}

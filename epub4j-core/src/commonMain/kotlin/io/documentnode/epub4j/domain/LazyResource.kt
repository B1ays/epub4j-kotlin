package io.documentnode.epub4j.domain

import io.documentnode.epub4j.util.toByteArray
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * A Resource that loads its data only on-demand from a EPUB book file.
 * This way larger books can fit into memory and can be opened faster.
 */
/**
 * Creates a Lazy resource, by not actually loading the data for this entry.
 *
 * The data will be loaded on the first call to getData()
 *
 * @param resourceProvider The resource provider loads data on demand.
 * @param size The size of this resource.
 * @param href The resource's href within the epub.
 */
class LazyResource(
    private val resourceProvider: LazyResourceProvider,
    private val cachedSize: Long,
    href: String
) : Resource(
    null,
    ByteArray(0),
    href,
    MediaTypes.determineMediaType(href)
) {
    /**
     * Creates a lazy resource, when the size is unknown.
     *
     * @param resourceProvider The resource provider loads data on demand.
     * @param href The resource's href within the epub.
     */
    constructor(resourceProvider: LazyResourceProvider, href: String) : this(resourceProvider, -1, href)

    /**
     * Tells this resource to release its cached data.
     *
     * If this resource was not lazy-loaded, this is a no-op.
     */
    override fun close() {
        data = null
    }

    private val isInitialized: Boolean
        /**
         * Returns if the data for this resource has been loaded into memory.
         *
         * @return true if data was loaded.
         */
        get() = data != null

    companion object {
        private const val serialVersionUID = 5089400472352002866L
    }
}

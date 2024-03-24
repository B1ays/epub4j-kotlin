package io.documentnode.epub4j.domain

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * A wrapper class for closing a ZipFile object when the InputStream derived
 * from it is closed.
 *
 * @author ttopalov
 */
/**
 * Constructor.
 *
 * @param `in`
 * The InputStream object.
 * @param zipFile
 * The ZipFile object.
 */
class ResourceInputStream(
    inputStream: InputStream,
    private val zipFile: ZipFile
) : FilterInputStream(inputStream) {
    @Throws(IOException::class)
    override fun close() {
        super.close()
        zipFile.close()
    }
}

package io.documentnode.epub4j.util

import java.io.IOException
import java.io.Writer

/**
 * Writer with the close() disabled.
 * We write multiple documents to a ZipOutputStream.
 * Some of the formatters call a close() after writing their data.
 * We don't want them to do that, so we wrap regular Writers in this NoCloseWriter.
 *
 * @author paul
 */
class NoCloseWriter(
    private val writer: Writer
) : Writer() {
    @Throws(IOException::class)
    override fun close() = Unit

    @Throws(IOException::class)
    override fun flush() {
        writer.flush()
    }

    @Throws(IOException::class)
    override fun write(cbuf: CharArray, off: Int, len: Int) {
        writer.write(cbuf, off, len)
    }
}

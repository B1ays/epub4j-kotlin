package io.documentnode.epub4j.util

import java.io.IOException
import java.io.OutputStream

/**
 * OutputStream with the close() disabled.
 * We write multiple documents to a ZipOutputStream.
 * Some of the formatters call a close() after writing their data.
 * We don't want them to do that, so we wrap regular OutputStreams in this NoCloseOutputStream.
 *
 * @author paul
 */
class NoCloseOutputStream(
    private val outputStream: OutputStream
) : OutputStream() {
    @Throws(IOException::class)
    override fun write(byte: Int) {
        outputStream.write(byte)
    }

    /**
     * A close() that does not call it's parent's close()
     */
    override fun close() = Unit
}

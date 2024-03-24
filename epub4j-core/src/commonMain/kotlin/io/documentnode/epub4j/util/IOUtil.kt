package io.documentnode.epub4j.util

import java.io.*


/**
 * Gets the contents of the Reader as a byte[], with the given character encoding.
 *
 * @param `in`
 * @param encoding
 * @return the contents of the Reader as a byte[], with the given character encoding.
 * @throws IOException
 */
@Throws(IOException::class)
fun Reader.toByteArray(encoding: String): ByteArray {
    return readText().toByteArray(charset(encoding))
}

/**
 * Reads data from the InputStream, using the specified buffer size.
 *
 * This is meant for situations where memory is tight, since
 * it prevents buffer expansion.
 *
 * @param `in` the stream to read data from
 * @param size the size of the array to create
 * @return the array, or null
 * @throws IOException
 */
@Throws(IOException::class)
fun InputStream.toByteArray(size: Int): ByteArray? {
    try {
        val result = if (size > 0) {
            ByteArrayOutputStream(size)
        } else {
            ByteArrayOutputStream()
        }

        copyTo(result)
        result.flush()
        return result.toByteArray()
    } catch (error: OutOfMemoryError) {
        //Return null so it gets loaded lazily.
        return null
    }
}
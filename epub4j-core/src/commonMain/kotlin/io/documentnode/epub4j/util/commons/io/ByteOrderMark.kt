package io.documentnode.epub4j.util.commons.io

import java.io.Serializable

/**
 * Byte Order Mark (BOM) representation -
 * see [BOMInputStream].
 *
 * @see BOMInputStream
 *
 * @see [Wikipedia - Byte Order Mark](http://en.wikipedia.org/wiki/Byte_order_mark)
 *
 * @version $Id: ByteOrderMark.java 1005099 2010-10-06 16:13:01Z niallp $
 * @since Commons IO 2.0
 */
data class ByteOrderMark(
    val charsetName: String,
    val bytes: IntArray
) : Serializable {
    /**
     * Return the length of the BOM's bytes.
     *
     * @return the length of the BOM's bytes
     */
    val length: Int
        get() = bytes.size

    /**
     * The byte at the specified position.
     *
     * @param pos The position
     * @return The specified byte
     */
    operator fun get(pos: Int): Int {
        return bytes[pos]
    }

    fun asSequence(): Sequence<Int> = bytes.asSequence()

    /**
     * Return a copy of the BOM's bytes.
     *
     * @return a copy of the BOM's bytes
     */
    fun getBytes(): ByteArray {
        return ByteArray(bytes.size) { bytes[it].toByte() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ByteOrderMark

        if (charsetName != other.charsetName) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = charsetName.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }

    companion object {
        private const val serialVersionUID = 1L

        /** UTF-8 BOM  */
        @JvmField
        val UTF_8: ByteOrderMark = ByteOrderMark("UTF-8", intArrayOf(0xEF, 0xBB, 0xBF))

        /** UTF-16BE BOM (Big Endian)  */
        @JvmField
        val UTF_16BE: ByteOrderMark = ByteOrderMark("UTF-16BE", intArrayOf(0xFE, 0xFF))

        /** UTF-16LE BOM (Little Endian)  */
        @JvmField
        val UTF_16LE: ByteOrderMark = ByteOrderMark("UTF-16LE", intArrayOf(0xFF, 0xFE))
    }
}

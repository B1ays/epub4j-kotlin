package io.documentnode.epub4j.util

import java.util.*

object CollectionUtil {
    /**
     * Creates an Enumeration out of the given Iterator.
     * @param <T>
     * @param it
     * @return an Enumeration created out of the given Iterator.
    </T> */
    fun <T> createEnumerationFromIterator(
        it: Iterator<T>
    ): Enumeration<T> {
        return IteratorEnumerationAdapter(it)
    }


    /**
     * Returns the first element of the list, null if the list is null or empty.
     *
     * @param <T>
     * @param list
     * @return the first element of the list, null if the list is null or empty.
    </T> */
    fun <T> first(list: List<T>?): T? {
        if (list.isNullOrEmpty()) {
            return null
        }
        return list[0]
    }

    /**
     * Whether the given collection is null or has no elements.
     *
     * @param collection
     * @return Whether the given collection is null or has no elements.
     */
    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }

    /**
     * Wraps an Enumeration around an Iterator
     * @author paul.siegmann
     *
     * @param <T>
    </T> */
    private class IteratorEnumerationAdapter<T>(private val iterator: Iterator<T>) : Enumeration<T> {
        override fun hasMoreElements(): Boolean {
            return iterator.hasNext()
        }

        override fun nextElement(): T = iterator.next()
    }
}

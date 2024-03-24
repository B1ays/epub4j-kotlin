package io.documentnode.epub4j.domain

import java.io.IOException
import java.io.InputStream

/**
 * @author jake
 */
interface LazyResourceProvider {
    @Throws(IOException::class)
    fun getResourceStream(href: String): InputStream
}

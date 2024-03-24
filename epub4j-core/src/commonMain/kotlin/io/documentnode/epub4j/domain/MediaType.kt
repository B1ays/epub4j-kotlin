package io.documentnode.epub4j.domain

import java.io.Serializable

/**
 * MediaType is used to tell the type of content a resource is.
 *
 * Examples of mediatypes are image/gif, text/css and application/xhtml+xml
 *
 * All allowed mediaTypes are maintained bye the MediaTypeService.
 *
 * @see MediaTypes
 *
 *
 * @author paul
 */
data class MediaType(
  val name: String,
  val defaultExtension: String,
  val extensions: List<String>
) : Serializable {
    constructor(
        name: String,
        defaultExtension: String,
        vararg extensions: String = arrayOf(defaultExtension)
    ) : this(name, defaultExtension, extensions.toList())

    companion object {
        private const val serialVersionUID = -7256091153727506788L
    }
}

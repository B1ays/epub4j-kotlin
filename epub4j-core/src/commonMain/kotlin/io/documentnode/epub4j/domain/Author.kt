package io.documentnode.epub4j.domain

import java.io.Serializable

/**
 * Represents one of the authors of the book
 *
 * @author paul
 */
data class Author(
    val firstname: String = "",
    val lastname: String = "",
    var relator: Relator = Relator.AUTHOR
) : Serializable {
    fun setRole(code: String?): Relator {
        var result = Relator.byCode(code)
        if (result == null) {
            result = Relator.AUTHOR
        }
        this.relator = result
        return result
    }

    companion object {
        private const val serialVersionUID = 6663408501416574200L
    }
}

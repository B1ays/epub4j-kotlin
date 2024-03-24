package io.documentnode.epub4j.domain

import java.io.Serializable
import java.lang.String.CASE_INSENSITIVE_ORDER

/**
 * An item in the Table of Contents.
 *
 * @see TableOfContents
 *
 *
 * @author paul
 */
class TOCReference(
    title: String?,
    resource: Resource?,
    fragmentId: String?,
    var children: MutableList<TOCReference> = mutableListOf()
) : TitledResourceReference(resource, title, fragmentId), Serializable {

    fun addChildSection(childSection: TOCReference): TOCReference {
        children.add(childSection)
        return childSection
    }

    companion object {
        private const val serialVersionUID = 5787958246077042456L
        val comparatorByTitleIgnoreCase: Comparator<TOCReference> = Comparator { tocReference1, tocReference2 ->
            CASE_INSENSITIVE_ORDER.compare(tocReference1.title, tocReference2.title)
        }
    }
}

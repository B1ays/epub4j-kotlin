package io.documentnode.epub4j.epub

import io.documentnode.epub4j.domain.Author
import io.documentnode.epub4j.domain.Date
import io.documentnode.epub4j.domain.Identifier
import io.documentnode.epub4j.domain.Metadata
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.namespace.QName

/**
 * Reads the package document metadata.
 *
 * In its own separate class because the PackageDocumentReader became a bit large and unwieldy.
 *
 * @author paul
 */
// package
internal object PackageDocumentMetadataReader : PackageDocumentBase() {
    @JvmStatic
    fun readMetadata(packageDocument: Document): Metadata {
        val result = Metadata()
        val metadataElement = DOMUtil.getFirstElementByTagNameNS(
            packageDocument.documentElement,
            NAMESPACE_OPF, OPFTags.metadata
        )
        if (metadataElement == null) {
            println("Package does not contain element " + OPFTags.metadata)
            return result
        }
        result.setTitles(
            DOMUtil.getElementsTextChild(
                metadataElement,
                NAMESPACE_DUBLIN_CORE,
                DCTags.title
            )
        )
        result.setPublishers(
            DOMUtil.getElementsTextChild(
                metadataElement,
                NAMESPACE_DUBLIN_CORE,
                DCTags.publisher
            )
        )
        result.setDescriptions(
            DOMUtil.getElementsTextChild(
                metadataElement,
                NAMESPACE_DUBLIN_CORE,
                DCTags.description
            )
        )
        result.setRights(
            DOMUtil.getElementsTextChild(
                metadataElement,
                NAMESPACE_DUBLIN_CORE,
                DCTags.rights
            )
        )
        result.setTypes(
            DOMUtil.getElementsTextChild(
                metadataElement,
                NAMESPACE_DUBLIN_CORE,
                DCTags.type
            )
        )
        result.subjects = DOMUtil.getElementsTextChild(
            metadataElement,
            NAMESPACE_DUBLIN_CORE,
            DCTags.subject
        )
        result.setIdentifiers(readIdentifiers(metadataElement))
        result.setAuthors(readCreators(metadataElement))
        result.setContributors(readContributors(metadataElement))
        result.setDates(readDates(metadataElement))
        result.otherProperties = readOtherProperties(metadataElement)
        result.setMetaAttributes(readMetaProperties(metadataElement))
        val languageTag = DOMUtil
            .getFirstElementByTagNameNS(
                metadataElement, NAMESPACE_DUBLIN_CORE,
                DCTags.language
            )
        if (languageTag != null) {
            result.language = DOMUtil.getTextChildrenContent(languageTag)
        }

        return result
    }

    /**
     * consumes meta tags that have a property attribute as defined in the standard. For example:
     * &lt;meta property="rendition:layout"&gt;pre-paginated&lt;/meta&gt;
     * @param metadataElement
     * @return
     */
    private fun readOtherProperties(
        metadataElement: Element
    ): Map<QName, String> {
        val result: MutableMap<QName, String> = HashMap()

        val metaTags = metadataElement.getElementsByTagName(OPFTags.meta)
        for (i in 0 until metaTags.length) {
            val metaNode = metaTags.item(i)
            val property = metaNode.attributes
                .getNamedItem(OPFAttributes.property)
            if (property != null) {
                val name = property.nodeValue
                val value = metaNode.textContent
                result[QName(name)] = value
            }
        }

        return result
    }

    /**
     * consumes meta tags that have a property attribute as defined in the standard. For example:
     * &lt;meta property="rendition:layout"&gt;pre-paginated&lt;/meta&gt;
     * @param metadataElement
     * @return
     */
    private fun readMetaProperties(
        metadataElement: Element
    ): Map<String, String> {
        val metaTags = metadataElement.getElementsByTagName(OPFTags.meta)

        return (0 until metaTags.length).asSequence()
            .map(metaTags::item)
            .filterIsInstance(Element::class.java)
            .associateBy(
                keySelector = { it.getAttribute(OPFAttributes.name) },
                valueTransform = { it.getAttribute(OPFAttributes.content) }
            )
    }

    private fun getBookIdId(document: Document): String? {
        val packageElement = DOMUtil.getFirstElementByTagNameNS(
            document.documentElement,
            NAMESPACE_OPF,
            OPFTags.packageTag
        )

        return packageElement?.getAttributeNS(
            NAMESPACE_OPF,
            OPFAttributes.uniqueIdentifier
        )
    }

    private fun readCreators(metadataElement: Element): List<Author> {
        return readAuthors(DCTags.creator, metadataElement)
    }

    private fun readContributors(metadataElement: Element): List<Author> {
        return readAuthors(DCTags.contributor, metadataElement)
    }

    private fun readAuthors(
        authorTag: String,
        metadataElement: Element
    ): List<Author> {
        val elements = metadataElement.getElementsByTagNameNS(
            NAMESPACE_DUBLIN_CORE,
            authorTag
        )

        return (0 until elements.length).asSequence()
            .map(elements::item)
            .filterIsInstance(Element::class.java)
            .mapNotNull(::createAuthor)
            .toList()
    }

    private fun readDates(metadataElement: Element): List<Date> {
        val elements = metadataElement
            .getElementsByTagNameNS(NAMESPACE_DUBLIN_CORE, DCTags.date)
        val result: MutableList<Date> = ArrayList(elements.length)
        for (i in 0 until elements.length) {
            val dateElement = elements.item(i) as Element
            var date: Date
            try {
                date = Date(
                    DOMUtil.getTextChildrenContent(dateElement),
                    dateElement.getAttributeNS(NAMESPACE_OPF, OPFAttributes.event)
                )
                result.add(date)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
        return result
    }

    private fun createAuthor(authorElement: Element): Author? {
        val authorString = DOMUtil.getTextChildrenContent(authorElement)
        if (authorString.isBlank()) {
            return null
        }
        val spacePos = authorString.lastIndexOf(' ')
        val result: Author = if (spacePos < 0) {
            Author(authorString)
        } else {
            Author(
                authorString.substring(0, spacePos),
                authorString.substring(spacePos + 1)
            )
        }

        result.setRole(
            authorElement.getAttributeNS(NAMESPACE_OPF, OPFAttributes.role)
        )
        return result
    }


    private fun readIdentifiers(metadataElement: Element): List<Identifier> {
        val identifierElements = metadataElement.getElementsByTagNameNS(
            NAMESPACE_DUBLIN_CORE,
            DCTags.identifier
        )
        if (identifierElements.length == 0) {
            println("Package does not contain element " + DCTags.identifier)
            return emptyList()
        }
        val bookIdId = getBookIdId(metadataElement.ownerDocument)

        return (0 until identifierElements.length).asSequence()
            .map(identifierElements::item)
            .filterIsInstance<Element>()
            .mapNotNull { identifierElement ->
                val schemeName = identifierElement.getAttributeNS(NAMESPACE_OPF, DCAttributes.scheme)
                val identifierValue = DOMUtil.getTextChildrenContent(identifierElement)
                if (identifierValue.isBlank()) {
                    return@mapNotNull null
                }
                val identifier = Identifier(schemeName, identifierValue)
                if (identifierElement.getAttribute("id") == bookIdId) {
                    identifier.isBookId = true
                }
                identifier
            }
            .toList()
    }
}

package io.documentnode.epub4j.epub

import io.documentnode.epub4j.util.StringUtil
import org.w3c.dom.*

/**
 * Utility methods for working with the DOM.
 *
 * @author paul
 */
// package
internal object DOMUtil {
    /**
     * First tries to get the attribute value by doing an getAttributeNS on the element, if that gets an empty element it does a getAttribute without namespace.
     *
     * @param element
     * @param namespace
     * @param attribute
     * @return
     */
    @JvmStatic
    fun getAttribute(
        element: Element,
        namespace: String,
        attribute: String
    ): String {
        val result = element.getAttributeNS(namespace, attribute)
        return result.ifEmpty { element.getAttribute(attribute) }
    }

    /**
     * Gets all descendant elements of the given parentElement with the given namespace and tagname and returns their text child as a list of String.
     *
     * @param parentElement
     * @param namespace
     * @param tagname
     * @return
     */
    @JvmStatic
    fun getElementsTextChild(
        parentElement: Element,
        namespace: String,
        tagname: String
    ): List<String> {
        val elements = parentElement.getElementsByTagNameNS(namespace, tagname)

        return (0 until elements.length).mapNotNull { i ->
            getTextChildrenContent(elements.item(i) as Element)
        }
    }

    /**
     * Finds in the current document the first element with the given namespace and elementName and with the given findAttributeName and findAttributeValue.
     * It then returns the value of the given resultAttributeName.
     *
     * @param document
     * @param namespace
     * @param elementName
     * @param findAttributeName
     * @param findAttributeValue
     * @param resultAttributeName
     * @return
     */
    @JvmStatic
    fun getFindAttributeValue(
        document: Document,
        namespace: String,
        elementName: String,
        findAttributeName: String,
        findAttributeValue: String,
        resultAttributeName: String
    ): String? {
        val metaTags = document.getElementsByTagNameNS(namespace, elementName)

        return (0 until metaTags.length).asSequence()
            .map(metaTags::item)
            .filterIsInstance(Element::class.java)
            .firstOrNull { metaElement ->
                findAttributeValue.equals(metaElement.getAttribute(findAttributeName), true) &&
                metaElement.getAttribute(resultAttributeName).isNotBlank()
            }
            ?.getAttribute(resultAttributeName)
    }

    /**
     * Gets the first element that is a child of the parentElement and has the given namespace and tagName
     *
     * @param parentElement
     * @param namespace
     * @param tagName
     * @return
     */
    @JvmStatic
    fun getFirstElementByTagNameNS(
        parentElement: Element,
        namespace: String,
        tagName: String
    ): Element? {
        val nodes = parentElement.getElementsByTagNameNS(namespace, tagName)
        if (nodes.length == 0) {
            return null
        }
        return nodes.item(0) as Element
    }

    /**
     * The contents of all Text nodes that are children of the given parentElement.
     * The result is trim()-ed.
     *
     * The reason for this more complicated procedure instead of just returning the data of the firstChild is that
     * when the text is Chinese characters then on Android each Characater is represented in the DOM as
     * an individual Text node.
     *
     * @param parentElement
     * @return
     */
    @JvmStatic
    fun getTextChildrenContent(parentElement: Element): String {
        val childNodes = parentElement.childNodes

        return (0 until childNodes.length).asSequence()
            .map(childNodes::item)
            .filterNotNull()
            .filter { it.nodeType == Node.TEXT_NODE }
            .fold(StringBuilder()) { builder, node ->
                builder.append((node as Text).data)
            }
            .trim { it <= ' ' }
            .toString()
    }
}

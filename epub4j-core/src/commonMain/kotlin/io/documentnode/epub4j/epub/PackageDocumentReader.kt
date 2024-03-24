package io.documentnode.epub4j.epub

import io.documentnode.epub4j.Constants
import io.documentnode.epub4j.domain.*
import io.documentnode.epub4j.util.ResourceUtil
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*
import javax.xml.parsers.ParserConfigurationException

/**
 * Reads the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 */
object PackageDocumentReader : PackageDocumentBase() {
    private val POSSIBLE_NCX_ITEM_IDS = arrayOf(
        "toc",
        "ncx",
        "ncxtoc"
    )


    @Throws(
        UnsupportedEncodingException::class,
        SAXException::class,
        IOException::class,
        ParserConfigurationException::class
    )
    fun read(
        packageResource: Resource,
        epubReader: EpubReader,
        book: Book,
        resources: Resources
    ) {
        var resources = resources
        val packageDocument = ResourceUtil.getAsDocument(packageResource) ?: return
        val packageHref = packageResource.href ?: return
        resources = fixHrefs(packageHref, resources)
        readGuide(packageDocument, epubReader, book, resources)

        // Books sometimes use non-identifier ids. We map these here to legal ones
        val idMapping: MutableMap<String, String?> = HashMap()

        resources = readManifest(
            packageDocument,
            packageHref,
            epubReader,
            resources,
            idMapping
        )
        book.resources = resources
        readCover(packageDocument, book)
        book.metadata = PackageDocumentMetadataReader.readMetadata(packageDocument)
        book.spine = readSpine(packageDocument, book.resources, idMapping)

        // if we did not find a cover page then we make the first page of the book the cover page
        if (
            book.coverPage == null &&
            book.spine.size > 0
        ) {
            book.coverPage = book.spine.getResource(0)
        }
    }


    //	private static Resource readCoverImage(Element metadataElement, Resources resources) {
    //		String coverResourceId = DOMUtil.getFindAttributeValue(metadataElement.getOwnerDocument(), NAMESPACE_OPF, OPFTags.meta, OPFAttributes.name, OPFValues.meta_cover, OPFAttributes.content);
    //		if (StringUtil.isBlank(coverResourceId)) {
    //			return null;
    //		}
    //		Resource coverResource = resources.getByIdOrHref(coverResourceId);
    //		return coverResource;
    //	}
    /**
     * Reads the manifest containing the resource ids, hrefs and mediatypes.
     *
     * @param packageDocument
     * @param packageHref
     * @param epubReader
     * @param resources
     * @param idMapping
     * @return a Map with resources, with their id's as key.
     */
    private fun readManifest(
        packageDocument: Document,
        packageHref: String,
        epubReader: EpubReader,
        resources: Resources,
        idMapping: MutableMap<String, String?>
    ): Resources {
        val manifestElement = DOMUtil.getFirstElementByTagNameNS(
            packageDocument.documentElement,
            NAMESPACE_OPF, OPFTags.manifest
        )
        val result = Resources()
        if (manifestElement == null) {
            println("Package document does not contain element " + OPFTags.manifest)
            return result
        }
        val itemElements = manifestElement.getElementsByTagNameNS(
            NAMESPACE_OPF,
            OPFTags.item
        )
        for (i in 0 until itemElements.length) {
            val itemElement = itemElements.item(i) as Element
            val id = DOMUtil.getAttribute(
                itemElement,
                NAMESPACE_OPF,
                OPFAttributes.id
            )
            var href = DOMUtil.getAttribute(
                itemElement,
                NAMESPACE_OPF,
                OPFAttributes.href
            )
            try {
                href = URLDecoder.decode(href, Constants.CHARACTER_ENCODING)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            val mediaTypeName = DOMUtil.getAttribute(
                itemElement,
                NAMESPACE_OPF,
                OPFAttributes.media_type
            )
            val resource = resources.remove(href)
            if (resource == null) {
                println("resource with href '$href' not found")
                continue
            }
            resource.id = id
            val mediaType = MediaTypes.getMediaTypeByName(mediaTypeName)
            if (mediaType != null) {
                resource.mediaType = mediaType
            }
            result.add(resource)
            idMapping[id] = resource.id
        }
        return result
    }


    /**
     * Reads the book's guide.
     * Here some more attempts are made at finding the cover page.
     *
     * @param packageDocument
     * @param epubReader
     * @param book
     * @param resources
     */
    private fun readGuide(
        packageDocument: Document,
        epubReader: EpubReader, book: Book, resources: Resources
    ) {
        val guideElement = DOMUtil.getFirstElementByTagNameNS(
            packageDocument.documentElement,
            NAMESPACE_OPF, OPFTags.guide
        )
        if (guideElement == null) {
            return
        }
        val guide = book.guide
        val guideReferences = guideElement.getElementsByTagNameNS(
            NAMESPACE_OPF,
            OPFTags.reference
        )
        for (i in 0 until guideReferences.length) {
            val referenceElement = guideReferences.item(i) as Element
            val resourceHref = DOMUtil.getAttribute(
                referenceElement,
                NAMESPACE_OPF,
                OPFAttributes.href
            )
            if (resourceHref.isBlank()) continue
            val resource = resources.getByHref(
                resourceHref.substringBefore(Constants.FRAGMENT_SEPARATOR_CHAR)
            )
            if (resource == null) {
                println(
                    "Guide is referencing resource with href " + resourceHref
                            + " which could not be found"
                )
                continue
            }
            val type = DOMUtil.getAttribute(
                referenceElement,
                NAMESPACE_OPF,
                OPFAttributes.type
            )
            if (type.isBlank()) {
                println(
                    "Guide is referencing resource with href " + resourceHref
                            + " which is missing the 'type' attribute"
                )
                continue
            }
            val title = DOMUtil.getAttribute(
                referenceElement,
                NAMESPACE_OPF,
                OPFAttributes.title
            )
            if (GuideReference.COVER.equals(type, ignoreCase = true)) {
                continue  // cover is handled elsewhere
            }
            val reference = GuideReference(
                resource,
                type,
                title,
                resourceHref.substringAfter(Constants.FRAGMENT_SEPARATOR_CHAR)
            )
            guide.addReference(reference)
        }
    }


    /**
     * Strips off the package prefixes up to the href of the packageHref.
     *
     * Example:
     * If the packageHref is "OEBPS/content.opf" then a resource href like "OEBPS/foo/bar.html" will be turned into "foo/bar.html"
     *
     * @param packageHref
     * @param resourcesByHref
     * @return The stripped package href
     */
    fun fixHrefs(
        packageHref: String,
        resourcesByHref: Resources
    ): Resources {
        val lastSlashPos = packageHref.lastIndexOf('/')
        if (lastSlashPos < 0) {
            return resourcesByHref
        }
        val result = Resources()
        for (resource in resourcesByHref.all) {
            if (!resource.href.isNullOrBlank() &&
                (resource.href?.length ?: 0) > lastSlashPos
            ) {
                resource.href = resource.href?.substring(lastSlashPos + 1)
            }
            result.add(resource)
        }
        return result
    }

    /**
     * Reads the document's spine, containing all sections in reading order.
     *
     * @param packageDocument
     * @param resources
     * @param idMapping
     * @return the document's spine, containing all sections in reading order.
     */
    private fun readSpine(
        packageDocument: Document, resources: Resources,
        idMapping: Map<String, String?>
    ): Spine {
        val spineElement = DOMUtil.getFirstElementByTagNameNS(
            packageDocument.documentElement,
            NAMESPACE_OPF, OPFTags.spine
        )
        if (spineElement == null) {
            println(
                "Element " + OPFTags.spine
                + " not found in package document, generating one automatically"
            )
            return generateSpineFromResources(resources)
        }
        val result = Spine()
        val tocResourceId = DOMUtil.getAttribute(
            spineElement,
            NAMESPACE_OPF,
            OPFAttributes.toc
        )
        result.tocResource = findTableOfContentsResource(tocResourceId, resources)
        val spineNodes = packageDocument.getElementsByTagNameNS(
            NAMESPACE_OPF,
            OPFTags.itemref
        )
        val spineReferences: MutableList<SpineReference> = mutableListOf()
        for (i in 0 until spineNodes.length) {
            val spineItem = spineNodes.item(i) as Element
            val itemref = DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.idref)
            if (itemref.isBlank()) {
                println("itemref with missing or empty idref") // XXX
                continue
            }
            val id = idMapping.getOrElse(itemref) { itemref }
            val resource = id?.let(resources::getByIdOrHref)
            if (resource == null) {
                println("resource with id \'$id\' not found")
                continue
            }

            val spineReference = SpineReference(resource)
            if (OPFValues.no.equals(
                    DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.linear), ignoreCase = true
                )
            ) {
                spineReference.linear = false
            }
            spineReferences.add(spineReference)
        }
        result.setSpineReferences(spineReferences)
        return result
    }

    /**
     * Creates a spine out of all resources in the resources.
     * The generated spine consists of all XHTML pages in order of their href.
     *
     * @param resources
     * @return a spine created out of all resources in the resources.
     */
    private fun generateSpineFromResources(resources: Resources): Spine {
        val result = Spine()
        val resourceHrefs = resources.allHrefs.sortedWith(String.CASE_INSENSITIVE_ORDER)
        for (resourceHref in resourceHrefs) {
            val resource = resources.getByHref(resourceHref) ?: continue
            if (resource.mediaType == MediaTypes.NCX) {
                result.tocResource = resource
            } else if (resource.mediaType == MediaTypes.XHTML) {
                result.addSpineReference(SpineReference(resource))
            }
        }
        return result
    }


    /**
     * The spine tag should contain a 'toc' attribute with as value the resource id of the table of contents resource.
     *
     * Here we try several ways of finding this table of contents resource.
     * We try the given attribute value, some often-used ones and finally look through all resources for the first resource with the table of contents mimetype.
     *
     * @param tocResourceId
     * @param resources
     * @return the Resource containing the table of contents
     */
    fun findTableOfContentsResource(
        tocResourceId: String,
        resources: Resources
    ): Resource? {
        var tocResource: Resource? = null
        if (tocResourceId.isNotBlank()) {
            tocResource = resources.getByIdOrHref(tocResourceId)
        }

        if (tocResource != null) {
            return tocResource
        }

        // get the first resource with the NCX mediatype
        tocResource = resources.findFirstResourceByMediaType(MediaTypes.NCX)

        if (tocResource == null) {
            for (i in POSSIBLE_NCX_ITEM_IDS.indices) {
                tocResource = resources.getByIdOrHref(POSSIBLE_NCX_ITEM_IDS[i])
                if (tocResource != null) {
                    break
                }
                tocResource = resources.getByIdOrHref(
                    POSSIBLE_NCX_ITEM_IDS[i].uppercase(Locale.getDefault())
                )
                if (tocResource != null) {
                    break
                }
            }
        }

        if (tocResource == null) {
            println(
                "Could not find table of contents resource. Tried resource with id '"
                        + tocResourceId + "', " + Constants.DEFAULT_TOC_ID + ", "
                        + Constants.DEFAULT_TOC_ID.uppercase(Locale.getDefault())
                        + " and any NCX resource."
            )
        }
        return tocResource
    }


    /**
     * Find all resources that have something to do with the coverpage and the cover image.
     * Search the meta tags and the guide references
     *
     * @param packageDocument
     * @return all resources that have something to do with the coverpage and the cover image.
     */
    // package
    fun findCoverHrefs(packageDocument: Document): Set<String> {
        val result: MutableSet<String> = mutableSetOf()

        // try and find a meta tag with name = 'cover' and a non-blank id
        val coverResourceId = DOMUtil.getFindAttributeValue(
            packageDocument,
            NAMESPACE_OPF,
            OPFTags.meta,
            OPFAttributes.name,
            OPFValues.meta_cover,
            OPFAttributes.content
        )

        if (coverResourceId?.isNotBlank() == true) {
            val coverHref = DOMUtil.getFindAttributeValue(
                packageDocument,
                NAMESPACE_OPF,
                OPFTags.item,
                OPFAttributes.id,
                coverResourceId,
                OPFAttributes.href
            )
            if (coverHref?.isNotBlank() == true) {
                result.add(coverHref)
            } else {
                result.add(coverResourceId) // maybe there was a cover href put in the cover id attribute
            }
        }
        // try and find a reference tag with type is 'cover' and reference is not blank
        val coverHref = DOMUtil.getFindAttributeValue(
            packageDocument,
            NAMESPACE_OPF,
            OPFTags.reference,
            OPFAttributes.type,
            OPFValues.reference_cover,
            OPFAttributes.href
        )
        if (coverHref?.isNotBlank() == true) {
            result.add(coverHref)
        }
        return result
    }

    /**
     * Finds the cover resource in the packageDocument and adds it to the book if found.
     * Keeps the cover resource in the resources map
     * @param packageDocument
     * @param book
     * @param resources
     */
    private fun readCover(packageDocument: Document, book: Book) {
        val coverHrefs: Collection<String> = findCoverHrefs(packageDocument).filterNotNull()
        for (coverHref in coverHrefs) {
            val resource = book.resources.getByHref(coverHref)
            if (resource == null) {
                println("Cover resource $coverHref not found")
                continue
            }
            if (resource.mediaType === MediaTypes.XHTML) {
                book.coverPage = resource
            } else if (resource.mediaType?.let(MediaTypes::isBitmapImage) == true) {
                book.coverImage = resource
            }
        }
    }
}

package io.documentnode.epub4j.epub

import io.documentnode.epub4j.domain.*
import io.documentnode.epub4j.util.ResourceUtil
import net.sf.jazzlib.ZipEntry
import net.sf.jazzlib.ZipException
import net.sf.jazzlib.ZipFile
import net.sf.jazzlib.ZipInputStream
import java.io.IOException
import java.util.*

/**
 * Loads Resources from inputStreams, ZipFiles, etc
 *
 * @author paul
 */
object ResourcesLoader {

    /**
     * Loads the entries of the zipFile as resources.
     *
     * The MediaTypes that are in the lazyLoadedTypes will not get their
     * contents loaded, but are stored as references to entries into the
     * ZipFile and are loaded on demand by the Resource system.
     *
     * @param zipFile
     * @param defaultHtmlEncoding
     * @param lazyLoadedTypes
     * @return
     * @throws IOException
     */
    /**
     * Loads all entries from the ZipInputStream as Resources.
     *
     * Loads the contents of all ZipEntries into memory.
     * Is fast, but may lead to memory problems when reading large books
     * on devices with small amounts of memory.
     *
     * @param zipFile
     * @param defaultHtmlEncoding
     * @return
     * @throws IOException
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun loadResources(
        zipFile: ZipFile,
        defaultHtmlEncoding: String,
        lazyLoadedTypes: List<MediaType> = emptyList()
    ): Resources {
        val resourceProvider = EpubResourceProvider(zipFile.name)
        val entries = zipFile.entries().asSequence().filterIsInstance<ZipEntry>()

        return entries.fold(Resources()) { resources, zipEntry ->
            if (zipEntry.isDirectory) return@fold resources
            val resource: Resource = if (shouldLoadLazy(zipEntry.name, lazyLoadedTypes)) {
                LazyResource(resourceProvider, zipEntry.size, zipEntry.name)
            } else {
                ResourceUtil.createResource(zipEntry, zipFile.getInputStream(zipEntry))
            }
            if (resource.mediaType == MediaTypes.XHTML) {
                resource.inputEncoding = defaultHtmlEncoding
            }
            resources.add(resource)
            return@fold resources
        }
    }

    /**
     * Whether the given href will load a mediaType that is in the
     * collection of lazilyLoadedMediaTypes.
     *
     * @param href
     * @param lazilyLoadedMediaTypes
     * @return Whether the given href will load a mediaType that is
     * in the collection of lazilyLoadedMediaTypes.
     */
    private fun shouldLoadLazy(
        href: String,
        lazilyLoadedMediaTypes: List<MediaType>
    ): Boolean {
        if (lazilyLoadedMediaTypes.isEmpty()) {
            return false
        }
        val mediaType = MediaTypes.determineMediaType(href)
        return lazilyLoadedMediaTypes.contains(mediaType)
    }

    /**
     * Loads all entries from the ZipInputStream as Resources.
     *
     * Loads the contents of all ZipEntries into memory.
     * Is fast, but may lead to memory problems when reading large books
     * on devices with small amounts of memory.
     *
     * @param zipInputStream
     * @param defaultHtmlEncoding
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadResources(
        zipInputStream: ZipInputStream,
        defaultHtmlEncoding: String
    ): Resources {
        val resources = Resources()

        generateSequence(zipInputStream::getNextEntry)
            .filterNot(ZipEntry::isDirectory)
            .forEach { zipEntry ->
                val resource: Resource = ResourceUtil.createResource(zipEntry, zipInputStream)
                if (resource.mediaType == MediaTypes.XHTML) {
                    resource.inputEncoding = defaultHtmlEncoding
                }
                resources.add(resource)
            }

        return resources
    }


    @Throws(IOException::class)
    private fun getNextZipEntry(zipInputStream: ZipInputStream): ZipEntry {
        try {
            return zipInputStream.nextEntry
        } catch (e: ZipException) {
            //see <a href="https://github.com/psiegman/epublib/issues/122">Issue #122 Infinite loop</a>.
            //when reading a file that is not a real zip archive or a zero length file, zipInputStream.getNextEntry()
            //throws an exception and does not advance, so loadResources enters an infinite loop
            println("Invalid or damaged zip file. $e")
            try {
                zipInputStream.closeEntry()
            } catch (_: Exception) {
            }
            throw e
        }
    }
}

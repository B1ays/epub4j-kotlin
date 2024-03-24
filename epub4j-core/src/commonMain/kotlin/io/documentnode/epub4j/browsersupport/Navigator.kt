package io.documentnode.epub4j.browsersupport

import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.domain.Resource
import java.io.Serializable

/**
 * A helper class for epub browser applications.
 *
 * It helps moving from one resource to the other, from one resource
 * to the other and keeping other elements of the application up-to-date
 * by calling the NavigationEventListeners.
 *
 * @author paul
 */
class Navigator(
    var book: Book? = null
) : Serializable {
    internal var currentSpinePos = 0
    var currentResource: Resource? = book?.coverPage
    var currentSectionPos: Int = 0
        private set
    var currentFragmentId: String? = null
        private set

    private val eventListeners: MutableList<NavigationEventListener> = ArrayList()

    @Synchronized
    private fun handleEventListeners(
        navigationEvent: NavigationEvent
    ) {
        for (i in eventListeners.indices) {
            val navigationEventListener = eventListeners[i]
            navigationEventListener.navigationPerformed(navigationEvent)
        }
    }

    fun addNavigationEventListener(
        navigationEventListener: NavigationEventListener
    ): Boolean {
        return eventListeners.add(navigationEventListener)
    }

    fun removeNavigationEventListener(
        navigationEventListener: NavigationEventListener
    ): Boolean {
        return eventListeners.remove(navigationEventListener)
    }

    fun gotoFirstSpineSection(source: Any?): Int {
        return gotoSpineSection(0, source)
    }

    fun gotoPreviousSpineSection(source: Any?): Int {
        return gotoPreviousSpineSection(0, source)
    }

    fun gotoPreviousSpineSection(pagePos: Int, source: Any?): Int {
        return if (currentSpinePos < 0) {
            gotoSpineSection(0, pagePos, source)
        } else {
            gotoSpineSection(currentSpinePos - 1, pagePos, source)
        }
    }

    fun hasNextSpineSection(): Boolean {
        return currentSpinePos < (book?.spine?.size?.minus(1) ?: -1)
    }

    fun hasPreviousSpineSection(): Boolean {
        return (currentSpinePos > 0)
    }

    fun gotoNextSpineSection(source: Any?): Int {
        return if (currentSpinePos < 0) {
            gotoSpineSection(0, source)
        } else {
            gotoSpineSection(currentSpinePos + 1, source)
        }
    }

    fun gotoResource(resourceHref: String, source: Any): Int {
        val resource = book?.resources?.getByHref(resourceHref)
        return resource?.let { gotoResource(it, source) } ?: -1
    }


    fun gotoResource(resource: Resource, source: Any?): Int {
        return gotoResource(resource, 0, null, source)
    }

    fun gotoResource(resource: Resource, fragmentId: String, source: Any?): Int {
        return gotoResource(resource, 0, fragmentId, source)
    }

    fun gotoResource(resource: Resource, pagePos: Int, source: Any?): Int {
        return gotoResource(resource, pagePos, null, source)
    }

    fun gotoResource(
        resource: Resource,
        pagePos: Int,
        fragmentId: String?,
        source: Any?
    ): Int {
        val navigationEvent = NavigationEvent(source, this)
        this.currentResource = resource
        this.currentSpinePos = book?.spine?.getResourceIndex(resource) ?: 0
        this.currentSectionPos = pagePos
        this.currentFragmentId = fragmentId
        handleEventListeners(navigationEvent)

        return currentSpinePos
    }

    fun gotoResourceId(resourceId: String?, source: Any?): Int {
        return gotoSpineSection(
            book!!.spine.findFirstResourceById(resourceId),
            source
        )
    }

    fun gotoSpineSection(newSpinePos: Int, source: Any?): Int {
        return gotoSpineSection(newSpinePos, 0, source)
    }

    /**
     * Go to a specific section.
     * Illegal spine positions are silently ignored.
     *
     * @param newSpinePos
     * @param source
     * @return The current position within the spine
     */
    fun gotoSpineSection(newSpinePos: Int, newPagePos: Int, source: Any?): Int {
        if (newSpinePos == currentSpinePos) {
            return currentSpinePos
        }
        if (newSpinePos < 0 || newSpinePos >= (book?.spine?.size ?: 0)) {
            return currentSpinePos
        }
        val navigationEvent = NavigationEvent(source, this)
        currentSpinePos = newSpinePos
        currentSectionPos = newPagePos
        currentResource = book?.spine?.getResource(currentSpinePos)
        handleEventListeners(navigationEvent)
        return currentSpinePos
    }

    fun gotoLastSpineSection(source: Any?): Int {
        return gotoSpineSection(
            book?.spine?.size?.minus(1) ?: 0,
            source
        )
    }

    fun gotoBook(book: Book, source: Any?) {
        val navigationEvent = NavigationEvent(source, this)
        this.book = book
        this.currentFragmentId = null
        this.currentSectionPos = 0
        this.currentResource = null
        this.currentSpinePos = -1
        handleEventListeners(navigationEvent)
    }

    /**
     * The current position within the spine.
     *
     * @return something &lt; 0 if the current position is not within the spine.
     */
    fun getCurrentSpinePos(): Int {
        return currentSpinePos
    }

    /**
     * Sets the current index and resource without calling the eventlisteners.
     *
     * If you want the eventListeners called use gotoSection(index);
     *
     * @param currentIndex
     */
    fun setCurrentSpinePos(currentIndex: Int) {
        this.currentSpinePos = currentIndex
        this.currentResource = book?.spine?.getResource(currentIndex)
    }

    /**
     * Sets the current index and resource without calling the eventlisteners.
     *
     * If you want the eventListeners called use gotoSection(index);
     *
     */
    fun setCurrentResource(currentResource: Resource): Int {
        this.currentSpinePos = book?.spine?.getResourceIndex(currentResource) ?: 0
        this.currentResource = currentResource
        return currentSpinePos
    }

    companion object {
        private const val serialVersionUID = 1076126986424925474L
    }
}

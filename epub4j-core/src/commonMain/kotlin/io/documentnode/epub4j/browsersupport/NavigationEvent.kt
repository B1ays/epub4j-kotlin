package io.documentnode.epub4j.browsersupport

import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.domain.Resource
import io.documentnode.epub4j.util.StringUtil.equals
import io.documentnode.epub4j.util.StringUtil.toString
import java.util.*

/**
 * Used to tell NavigationEventListener just what kind of navigation action
 * the user just did.
 *
 * @author paul
 */
class NavigationEvent(
    source: Any?,
    private val navigator: Navigator? = null
): EventObject(source) {
    var oldResource: Resource? = navigator?.currentResource
    private var oldSpinePos: Int = navigator?.currentSpinePos ?: 0
    private var oldBook: Book? = navigator?.book

    /**
     * The previous position within the section.
     *
     * @return The previous position within the section.
     */
    private var oldSectionPos: Int = navigator?.currentSectionPos ?: 0

    // package
    private var oldFragmentId: String? = navigator?.currentFragmentId

    // package
    fun setOldPagePos(oldPagePos: Int) {
        this.oldSectionPos = oldPagePos
    }

    private val currentSectionPos: Int
        get() = navigator?.currentSectionPos ?: 0

    private val currentSpinePos: Int
        get() = navigator?.currentSpinePos ?: 0

    private val currentFragmentId: String
        get() = navigator?.currentFragmentId ?: ""

    val isBookChanged: Boolean
        get() {
            if (oldBook == null) {
                return true
            }
            return oldBook !== navigator!!.book
        }

    val isSpinePosChanged: Boolean
        get() = oldSpinePos != currentSpinePos

    val isFragmentChanged: Boolean
        get() = equals(oldFragmentId, currentFragmentId)

    val currentResource: Resource?
        get() = navigator?.currentResource


    val currentBook: Book?
        get() = navigator?.book

    val isResourceChanged: Boolean
        get() = oldResource !== currentResource

    override fun toString(): String {
        return toString(
            "oldSectionPos", oldSectionPos,
            "oldResource", oldResource,
            "oldBook", oldBook,
            "oldFragmentId", oldFragmentId,
            "oldSpinePos", oldSpinePos,
            "currentPagePos", currentSectionPos,
            "currentResource", currentResource,
            "currentBook", currentBook,
            "currentFragmentId", currentFragmentId,
            "currentSpinePos", currentSpinePos
        )
    }

    val isSectionPosChanged: Boolean
        get() = oldSectionPos != currentSectionPos

    companion object {
        private const val serialVersionUID = -6346750144308952762L
    }
}

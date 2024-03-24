package io.documentnode.epub4j.domain

import java.io.Serializable

abstract class ResourceReference(
    /**
     * Besides setting the resource it also sets the fragmentId to null.
     *
     * @param resource
     */
    open var resource: Resource?
) : Serializable {
    val resourceId: String?
        /**
         * The id of the reference referred to.
         *
         * null of the reference is null or has a null id itself.
         *
         * @return The id of the reference referred to.
         */
        get() = resource?.id


    companion object {
        private const val serialVersionUID = 2596967243557743048L
    }
}

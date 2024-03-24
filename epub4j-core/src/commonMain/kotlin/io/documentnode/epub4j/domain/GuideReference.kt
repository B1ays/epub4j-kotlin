package io.documentnode.epub4j.domain

import io.documentnode.epub4j.util.StringUtil
import java.io.Serializable
import java.util.*

/**
 * These are references to elements of the book's guide.
 *
 * @see Guide
 *
 *
 * @author paul
 */
class GuideReference : TitledResourceReference, Serializable {
    var type: String? = null

    constructor(resource: Resource?, title: String? = null) : super(resource, title)

    constructor(
        resource: Resource?,
        type: String,
        title: String?,
        fragmentId: String? = null
    ) : super(resource, title, fragmentId) {
        this.type = type.takeIf(String::isNotBlank)?.lowercase(Locale.getDefault())
    }

    companion object {
        private const val serialVersionUID = -316179702440631834L

        /**
         * the book cover(s), jacket information, etc.
         */
        const val COVER: String = "cover"

        /**
         * human-readable page with title, author, publisher, and other metadata
         */
        const val TITLE_PAGE: String = "title-page"

        /**
         * Human-readable table of contents.
         * Not to be confused the epub file table of contents
         *
         */
        const val TOC: String = "toc"

        /**
         * back-of-book style index
         */
        const val INDEX: String = "index"
        const val GLOSSARY: String = "glossary"
        const val ACKNOWLEDGEMENTS: String = "acknowledgements"
        const val BIBLIOGRAPHY: String = "bibliography"
        const val COLOPHON: String = "colophon"
        const val COPYRIGHT_PAGE: String = "copyright-page"
        const val DEDICATION: String = "dedication"

        /**
         * an epigraph is a phrase, quotation, or poem that is set at the
         * beginning of a document or component.
         *
         * source: http://en.wikipedia.org/wiki/Epigraph_%28literature%29
         */
        const val EPIGRAPH: String = "epigraph"

        const val FOREWORD: String = "foreword"

        /**
         * list of illustrations
         */
        const val LOI: String = "loi"

        /**
         * list of tables
         */
        const val LOT: String = "lot"
        const val NOTES: String = "notes"
        const val PREFACE: String = "preface"

        /**
         * A page of content (e.g. "Chapter 1")
         */
        const val TEXT: String = "text"
    }
}

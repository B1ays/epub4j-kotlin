package io.documentnode.epub4j.epub

import io.documentnode.epub4j.domain.Resource
import java.io.OutputStream

interface HtmlProcessor {
    fun processHtmlResource(resource: Resource, out: OutputStream)
}

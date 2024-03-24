package io.documentnode.epub4j.util

import javax.xml.stream.XMLStreamWriter

/**
 * Abstract class for writing filtered XML streams. This class provides methods
 * that merely delegate to the contained stream. Subclasses should override some
 * of these methods, and may also provide additional methods and fields.
 *
 * @author [John Kristian](mailto:jk2006@engineer.com)
 */
abstract class StreamWriterDelegate(
    xmlStreamWriter: XMLStreamWriter
) : XMLStreamWriter by xmlStreamWriter
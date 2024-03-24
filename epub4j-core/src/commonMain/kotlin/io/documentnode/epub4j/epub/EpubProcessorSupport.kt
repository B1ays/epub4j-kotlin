package io.documentnode.epub4j.epub

import io.documentnode.epub4j.Constants
import org.kxml2.io.KXmlSerializer
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Various low-level support methods for reading/writing epubs.
 *
 * @author paul.siegmann
 */
class EpubProcessorSupport {
    internal class EntityResolverImpl : EntityResolver {
        private var previousLocation: String? = null

        @Throws(SAXException::class, IOException::class)
        override fun resolveEntity(
            publicId: String,
            systemId: String
        ): InputSource {
            val resourcePath: String
            if (systemId.startsWith("http:")) {
                val url = URL(systemId)
                resourcePath = "dtd/" + url.host + url.path
                previousLocation = resourcePath.substring(0, resourcePath.lastIndexOf('/'))
            } else {
                resourcePath = previousLocation + systemId.substring(systemId.lastIndexOf('/'))
            }

            if (this::class.java.classLoader.getResource(resourcePath) == null) {
                throw RuntimeException(
                    "remote resource is not cached : [" + systemId
                            + "] cannot continue"
                )
            }

            val inputStream = EpubProcessorSupport::class.java.classLoader
                .getResourceAsStream(resourcePath)
            return InputSource(inputStream)
        }
    }

    val documentBuilderFactory: DocumentBuilderFactory?
        get() = Companion.documentBuilderFactory

    companion object {
        private val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isValidating = false
        }

        @JvmStatic
        @Throws(UnsupportedEncodingException::class)
        fun createXmlSerializer(out: OutputStream): XmlSerializer? {
            return createXmlSerializer(
                OutputStreamWriter(out, Constants.CHARACTER_ENCODING)
            )
        }

        fun createXmlSerializer(out: Writer?): XmlSerializer? {
            var result: XmlSerializer? = null
            try {
                /*
               * Disable XmlPullParserFactory here before it doesn't work when
               * building native image using GraalVM
               */
                // XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                // factory.setValidating(true);
                // result = factory.newSerializer();

                result = KXmlSerializer()
                result.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output", true
                )
                result.setOutput(out)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        val entityResolver: EntityResolver
            /**
             * Gets an EntityResolver that loads dtd's and such from the epub4j classpath.
             * In order to enable the loading of relative urls the given EntityResolver contains the previousLocation.
             * Because of a new EntityResolver is created every time this method is called.
             * Fortunately the EntityResolver created uses up very little memory per instance.
             *
             * @return an EntityResolver that loads dtd's and such from the epub4j classpath.
             */
            get() = EntityResolverImpl()

        /**
         * Creates a DocumentBuilder that looks up dtd's and schema's from epub4j's classpath.
         *
         * @return a DocumentBuilder that looks up dtd's and schema's from epub4j's classpath.
         */
        fun createDocumentBuilder(): DocumentBuilder? {
            var result: DocumentBuilder? = null
            try {
                result = documentBuilderFactory.newDocumentBuilder()
                result.setEntityResolver(entityResolver)
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            }
            return result
        }
    }
}

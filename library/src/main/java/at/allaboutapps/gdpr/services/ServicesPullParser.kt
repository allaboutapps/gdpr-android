package at.allaboutapps.gdpr.services

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.SparseArray
import android.util.Xml
import androidx.annotation.XmlRes
import org.xmlpull.v1.XmlPullParser

class ServicesPullParser(private val context: Context, @XmlRes servicesResId: Int) {

    private val resources: Resources = context.resources
    private val ns: String? = null
    private val parser: XmlPullParser = resources.getXml(servicesResId)

    private val attributeSet: AttributeSet = Xml.asAttributeSet(parser)

    fun parse(): ArrayList<Service> {
        parser.next()

        parser.nextTag()
        val entries = ArrayList<Service>()

        parser.require(XmlPullParser.START_TAG, ns, "services")
        readElements { name ->
            when (name) {
                "service" -> entries.add(readService())
                else -> skip()
            }
        }
        return entries
    }

    private inline fun readElements(element: (String) -> Unit) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            element(name)
        }
    }

    /**
     * Skip the whole tag and its contents.
     */
    private fun skip() {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun readService(): Service {
        parser.require(XmlPullParser.START_TAG, ns, "service")
        val resourceId = attributeSet.getAttributeResourceValue(ns, "name", 0)
        val title: TextResource =
            if (resourceId == 0) TextResource(
                rawText = parser.getAttributeValue(
                    ns,
                    "name"
                )
            )
            else TextResource(stringResId = resourceId)
        val supportsDeletion = attributeSet.getAttributeBooleanValue(ns, "supportsDeletion", false)
        val service = Service(title, supportsDeletion)

        readElements { name ->
            when (name) {
                "bind" -> {
                    val id: Int = attributeSet.getAttributeResourceValue(ns, "id", 0)
                    val text = parseString(readText())
                    service.bindings.put(id, text)
                }
                else -> skip()
            }
        }

        return service
    }

    private fun parseString(value: String): TextResource {
        return if (value.startsWith("@string/")) {
            val resourceId =
                resources.getIdentifier(value.substring("@string/".length), "string", context.packageName)
            TextResource(stringResId = resourceId)
        } else {
            TextResource(rawText = value)
        }
    }

    private fun readText(): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }
}

data class Service(val name: TextResource, val supportsDeletion: Boolean) {
    val bindings: SparseArray<TextResource> = SparseArray()
}

data class TextResource(
    val stringResId: Int = 0,
    val rawText: String = ""
)

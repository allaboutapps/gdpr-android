package at.allaboutapps.gdpr.services

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Xml
import androidx.annotation.XmlRes
import org.xmlpull.v1.XmlPullParser

internal class ServicesPullParser(context: Context, @XmlRes servicesResId: Int) {

    private val resources: Resources = context.resources
    private val ns: String? = null
    private val parser: XmlPullParser = resources.getXml(servicesResId)

    private val attributeSet: AttributeSet = Xml.asAttributeSet(parser)
    private var isOptIn = false

    fun parse(): ArrayList<Service> {
        parser.next()

        parser.nextTag()
        val entries = ArrayList<Service>()

        parser.require(XmlPullParser.START_TAG, ns, TAG_SERVICES)
        isOptIn = readAttributeBoolean(SERVICES_OPT_IN, false)

        readElements { name ->
            when (name) {
                TAG_SERVICE -> entries.add(readService())
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
        parser.require(XmlPullParser.START_TAG, ns, TAG_SERVICE)

        val id = attributeSet.getAttributeValue(ns, SERVICE_ID)
        require(id.isNotEmpty()) { "service needs an `id`" }

        val title: TextResource = readAttributeText(SERVICE_NAME)
        val description: TextResource = readAttributeText(SERVICE_DESCRIPTION)
        val supportsDeletion = readAttributeBoolean(SERVICE_SUPPORT_DELETION, false)
        val isOptIn = readAttributeBoolean(SERVICE_OPT_IN, isOptIn)

        skip()

        return Service(id, title, description, supportsDeletion, isOptIn)
    }

    private fun readAttributeBoolean(attributeName: String, defaultValue: Boolean) =
        attributeSet.getAttributeBooleanValue(ns, attributeName, defaultValue)

    private fun readAttributeText(attributeName: String): TextResource {
        val resourceId = attributeSet.getAttributeResourceValue(ns, attributeName, 0)
        return if (resourceId == 0) {
            val text = parser.getAttributeValue(ns, attributeName)
            if (text == null) TextResource(rawText = "") else TextResource(rawText = text)
        } else
            TextResource(stringResId = resourceId)
    }

    companion object {
        private const val TAG_SERVICES = "services"
        private const val SERVICES_OPT_IN = "isOptIn"
        private const val TAG_SERVICE = "service"
        private const val SERVICE_ID = "id"
        private const val SERVICE_NAME = "name"
        private const val SERVICE_DESCRIPTION = "description"
        private const val SERVICE_SUPPORT_DELETION = "supportsDeletion"
        private const val SERVICE_OPT_IN = "isOptIn"
    }
}

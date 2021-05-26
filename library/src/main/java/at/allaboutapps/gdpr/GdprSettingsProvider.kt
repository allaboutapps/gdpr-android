package at.allaboutapps.gdpr

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.annotation.IdRes

public class GdprSettingsProvider : ContentProvider() {

    private lateinit var settings: SharedPreferences

    override fun onCreate(): Boolean {
        val context = context!!
        GDPRPolicyManager.initialize(context)

        initialize(context)
        settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

        return true
    }

    override fun getType(uri: Uri): String =
        ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + ".item"

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (uri != BASE_URI) throw IllegalArgumentException("Not supported $uri")

        val count = settings.all.size
        settings.edit().clear().apply()

        return count
    }

    @SuppressLint("NewApi")
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        require(uri == BASE_URI) { "Not supported $uri" }
        requireNotNull(values)

        val editor = settings.edit()

        for ((key, value) in values.valueSet()) {
            when (value) {
                null -> editor.remove(key)
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                else -> throw IllegalArgumentException("$key with ${value::class.java.name} Not supported")
            }
        }

        editor.apply()
        return null
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        when (val match = matcher.match(uri)) {
            PROPERTY -> {
                val pathSegments = uri.pathSegments
                val key = pathSegments[0]
                val type = pathSegments[1]

                val cursor = MatrixCursor(arrayOf(key))

                if (!settings.contains(key)) return cursor
                val rowBuilder = cursor.newRow()

                val value: Any? = when (type) {
                    TYPE_STRING -> settings.getString(key, null)
                    TYPE_BOOLEAN -> if (settings.getBoolean(key, false)) 1 else 0
                    TYPE_LONG -> settings.getLong(key, 0L)
                    TYPE_INT -> settings.getInt(key, 0)
                    TYPE_FLOAT -> settings.getFloat(key, 0f)
                    TYPE_CONTAINS -> settings.contains(key)
                    else -> throw IllegalArgumentException("Not supported $uri ($match)")
                }
                rowBuilder.add(value)

                return cursor
            }
            SERVICES -> {
                val cursor = MatrixCursor(arrayOf("id", "enabled"))

                settings.all.filterKeys { it.startsWith(SERVICE_PREFIX) }
                    .forEach { (key, value) ->
                        val enabled = value as Boolean
                        cursor.newRow().add(key).add(enabled.asInt())
                    }

                return cursor
            }
            else -> throw IllegalArgumentException("Not supported $uri ($match)")
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException()
    }

    internal class GdprPreferences internal constructor(private val context: Context) :
        SharedPreferences {

        fun getNameForId(@IdRes serviceId: Int): String =
            context.resources.getResourceEntryName(serviceId)

        override fun edit(): Editor {
            return Editor(context)
        }

        override fun getString(key: String, fallback: String?): String? =
            fetchItem(key, TYPE_STRING).getStringValue(fallback)

        override fun getLong(key: String, fallback: Long): Long =
            fetchItem(key, TYPE_LONG).getLongValue(fallback)

        override fun getFloat(key: String, fallback: Float): Float =
            fetchItem(key, TYPE_FLOAT).getFloatValue(fallback)

        override fun getBoolean(key: String, fallback: Boolean): Boolean =
            fetchItem(key, TYPE_BOOLEAN).getBooleanValue(fallback)

        override fun getInt(key: String, fallback: Int): Int =
            fetchItem(key, TYPE_INT).getIntValue(fallback)

        override fun contains(key: String): Boolean =
            fetchItem(key, TYPE_CONTAINS).use { it.moveToFirst() && !it.isNull(0) }

        @SuppressLint("Recycle")
        private fun fetchItem(key: String, type: String): Cursor {
            return context.contentResolver
                .query(getContentUri(key, type), null, null, null, null)!!
        }

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) =
            Unit

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) =
            Unit

        override fun getAll(): MutableMap<String, *> = error("not supported")

        override fun getStringSet(
            key: String?,
            defValues: MutableSet<String>?
        ): MutableSet<String> = error("set not supported")

        @SuppressLint("Recycle")
        fun readServiceStates(): Map<String, Boolean> {
            val query = context.contentResolver.query(SERVICES_URI, null, null, null, null)
            val services = mutableMapOf<String, Boolean>()
            query!!.use { q ->
                q.moveToPosition(-1)
                while (q.moveToNext()) {
                    val serviceId = q.getString(0).substring(SERVICE_PREFIX.length)
                    val isEnabled = q.getInt(1) != 0
                    services[serviceId] = isEnabled
                }
            }
            return services
        }

        class Editor internal constructor(internal var context: Context) :
            SharedPreferences.Editor {

            private val values = ContentValues()

            override fun apply() {
                commit()
            }

            override fun commit(): Boolean {
                context.contentResolver.insert(BASE_URI, values)
                return true
            }

            override fun putBoolean(key: String, value: Boolean) = edit { put(key, value) }
            override fun putString(key: String, value: String?) = edit { put(key, value) }
            override fun putLong(key: String, value: Long) = edit { put(key, value) }
            override fun putInt(key: String, value: Int) = edit { put(key, value) }
            override fun putFloat(key: String, value: Float) = edit { put(key, value) }
            override fun remove(key: String) = edit { putNull(key) }

            override fun clear() = edit {
                clear()
                context.contentResolver.delete(BASE_URI, null, null)
            }

            private inline fun edit(block: ContentValues.() -> Unit): Editor {
                block(values)
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?
            ): SharedPreferences.Editor = error("set not supported")
        }
    }

    public companion object {
        public const val SERVICE_PREFIX = "SERVICE_"

        private const val PREFERENCE_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".GDPR"

        private const val TYPE_CONTAINS = "contains"

        private const val TYPE_STRING = "string"
        private const val TYPE_BOOLEAN = "boolean"
        private const val TYPE_INT = "int"
        private const val TYPE_LONG = "long"
        private const val TYPE_FLOAT = "float"

        private const val PROPERTY = 1
        private const val SERVICES = 2

        private lateinit var AUTHORITY: String

        private lateinit var BASE_URI: Uri
        private lateinit var SERVICES_URI: Uri

        private lateinit var matcher: UriMatcher
        private var isInitialized = false

        internal fun initialize(context: Context) {
            if (isInitialized) {
                // initializing once is enough
                return
            }
            isInitialized = true

            val componentName =
                ComponentName(context.packageName, GdprSettingsProvider::class.java.name)
            val info = context.packageManager.getProviderInfo(componentName, 0)
            AUTHORITY = info.authority

            matcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, "services", SERVICES)
            matcher.addURI(AUTHORITY, "*/*", PROPERTY)

            BASE_URI = Uri.parse("content://$AUTHORITY")
            SERVICES_URI = BASE_URI.buildUpon().path("services").build()
        }

        private fun getContentUri(key: String, type: String) =
            BASE_URI.buildUpon().appendPath(key).appendPath(type).build()

        private fun Cursor.getStringValue(fallback: String?): String? =
            readFromCursor { getString(0) } ?: fallback

        private fun Cursor.getBooleanValue(fallback: Boolean): Boolean =
            readFromCursor { getInt(0) != 0 } ?: fallback

        private fun Cursor.getIntValue(fallback: Int): Int =
            readFromCursor { getInt(0) } ?: fallback

        private fun Cursor.getLongValue(fallback: Long): Long =
            readFromCursor { getLong(0) } ?: fallback

        private fun Cursor.getFloatValue(fallback: Float): Float =
            readFromCursor { getFloat(0) } ?: fallback

        private inline fun <T> Cursor.readFromCursor(block: Cursor.() -> T): T? {
            use {
                if (moveToFirst()) {
                    return block(this)
                }
            }
            return null
        }
    }
}

private fun Boolean.asInt() = if (this) 1 else 0

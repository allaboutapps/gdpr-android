package at.allaboutapps.gdpr

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class GdprSettingsProvider : ContentProvider() {

  private lateinit var settings: SharedPreferences

  override fun onCreate(): Boolean {
    GDPRPolicyManager.initialize(context)

    initialize(context)
    settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    return true
  }

  override fun getType(uri: Uri) = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + ".item"


  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
    if (uri != BASE_URI) throw IllegalArgumentException("Not supported $uri")

    val count = settings.all.size
    settings.edit().clear().apply()

    return count
  }

  @SuppressLint("NewApi")
  override fun insert(uri: Uri, values: ContentValues): Uri? {
    if (uri != BASE_URI) throw IllegalArgumentException("Not supported $uri")

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
      uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

    val match = matcher.match(uri)
    when (match) {
      PROPERTY -> {
        val pathSegments = uri.pathSegments
        val key = pathSegments[0]
        val type = pathSegments[1]

        val cursor = MatrixCursor(arrayOf(key))

        if (!settings.contains(key)) return cursor
        val rowBuilder = cursor.newRow()

        val value: Any = when {
          TYPE_STRING == type -> settings.getString(key, null)
          TYPE_BOOLEAN == type -> if (settings.getBoolean(key, false)) 1 else 0
          TYPE_LONG == type -> settings.getLong(key, 0L)
          TYPE_INT == type -> settings.getInt(key, 0)
          TYPE_FLOAT == type -> settings.getFloat(key, 0f)
          TYPE_CONTAINS == type -> settings.contains(key)
          else -> throw IllegalArgumentException("Not supported $uri ($match)")
        }
        rowBuilder.add(value)

        return cursor
      }
      else -> throw IllegalArgumentException("Not supported $uri ($match)")
    }
  }

  override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
    throw UnsupportedOperationException()
  }

  class GdprPreferences internal constructor(private val context: Context) : SharedPreferences {

    override fun edit(): Editor {
      return Editor(context)
    }

    override fun getString(key: String, fallback: String): String =
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

    private fun fetchItem(key: String, type: String): Cursor {
      return context.contentResolver
          .query(getContentUri(key, type), null, null, null, null)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    override fun getAll(): MutableMap<String, *> = error("not supported")

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String> = error("set not supported")


    class Editor internal constructor(internal var context: Context) : SharedPreferences.Editor {

      private val values = ContentValues()

      override fun apply() {
        commit()
      }

      override fun commit(): Boolean {
        context.contentResolver.insert(BASE_URI, values)
        return true
      }

      override fun putString(key: String, value: String) = edit { put(key, value) }
      override fun putLong(key: String, value: Long) = edit { put(key, value) }
      override fun putBoolean(key: String, value: Boolean) = edit { put(key, value) }
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

      override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = error("set not supported")

    }
  }

  companion object {
    private const val PREFERENCE_NAME = BuildConfig.APPLICATION_ID + ".GDPR"

    private const val TYPE_CONTAINS = "contains"

    private const val TYPE_STRING = "string"
    private const val TYPE_BOOLEAN = "boolean"
    private const val TYPE_INT = "int"
    private const val TYPE_LONG = "long"
    private const val TYPE_FLOAT = "float"

    private const val PROPERTY = 1

    private lateinit var AUTHORITY: String

    lateinit var BASE_URI: Uri

    private lateinit var matcher: UriMatcher

    private fun initialize(context: Context) {
      val componentName = ComponentName(context.packageName, GdprSettingsProvider::class.java.name)
      val info = context.packageManager.getProviderInfo(componentName, 0)
      AUTHORITY = info.authority

      matcher = UriMatcher(UriMatcher.NO_MATCH)
      matcher.addURI(AUTHORITY, "*/*", PROPERTY)

      BASE_URI = Uri.parse("content://$AUTHORITY")
    }

    private fun getContentUri(key: String, type: String) = BASE_URI.buildUpon().appendPath(key).appendPath(type).build()

    private fun Cursor.getStringValue(fallback: String): String =
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

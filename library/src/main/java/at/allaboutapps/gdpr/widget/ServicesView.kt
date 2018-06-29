package at.allaboutapps.gdpr.widget

import android.content.Context
import android.support.annotation.XmlRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import at.allaboutapps.gdpr.R
import at.allaboutapps.gdpr.services.ServicesPullParser
import at.allaboutapps.gdpr.services.TextResource

class ServicesView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

  private val layoutResId: Int

  init {
    orientation = LinearLayout.VERTICAL

    val typedArray = context.obtainStyledAttributes(attrs,
      R.styleable.ServicesView, 0, 0)

    layoutResId = typedArray.getResourceId(
      R.styleable.ServicesView_gdpr_itemLayout,
      R.layout.gdpr_policy_item_service
    )

    typedArray.recycle()
  }

  fun inflateServices(@XmlRes resId: Int) {
    removeAllViews()

    val parser = ServicesPullParser(context, resId)
    val services = parser.parse()

    val inflater = LayoutInflater.from(context)

    services.forEach { service ->
      val serviceView = inflater.inflate(layoutResId, this, false)

      serviceView.findViewById<TextView>(android.R.id.text1).setText(service.name)
      val count = service.bindings.size()
      for (i in 0 until count) {
        val key = service.bindings.keyAt(0)
        val binding = service.bindings[key]
        serviceView.findViewById<TextView>(key)?.setText(binding)
      }

      addView(serviceView)
    }

  }

  private fun TextView.setText(text: TextResource) {
    if (text.stringResId == 0) {
      setText(text.rawText)
    } else {
      setText(text.stringResId)
    }
  }
}
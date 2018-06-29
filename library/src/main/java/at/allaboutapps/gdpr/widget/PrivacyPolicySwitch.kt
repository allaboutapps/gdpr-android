package at.allaboutapps.gdpr.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.SwitchCompat
import android.text.*
import android.text.Annotation
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import at.allaboutapps.gdpr.GDPRPolicyManager
import at.allaboutapps.gdpr.R

/**
 * Use `android:text` to specify the link text and add `<annotation link="">` to add links. e.g.
 *
 *     I agree to the <annotation link="">Privacy Policy and Terms of Service</annotation>
 */
class PrivacyPolicySwitch(context: Context, attrs: AttributeSet? = null) :
        LinearLayout(context, attrs), Checkable {

  private val policyManager = GDPRPolicyManager.instance()

  private val text: TextView
  private val switch: SwitchCompat

  var checkedChangeListener: CompoundButton.OnCheckedChangeListener? = null

  init {
    View.inflate(context, R.layout.widget_privacy_policy_switch, this)

    text = findViewById(android.R.id.text1)
    switch = findViewById(android.R.id.switchInputMethod)

    val typedArray =
      context.obtainStyledAttributes(attrs, R.styleable.PrivacyPolicySwitch)

    text.text =
        wrapInClickableSpan(typedArray.getText(R.styleable.PrivacyPolicySwitch_android_text))

    typedArray.recycle()

    text.setOnClickListener { showPolicy() }

    val textListener = object : TextWatcher {
      override fun afterTextChanged(s: Editable) {
        text.removeTextChangedListener(this)
        wrapInClickableSpan(s)
        text.addTextChangedListener(this)
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }
    text.addTextChangedListener(textListener)

    switch.setOnCheckedChangeListener { view, isChecked ->
      checkedChangeListener?.onCheckedChanged(view, isChecked)
    }

    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
  }

  override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
    text.setPadding(left, text.paddingTop, text.paddingRight, text.paddingBottom)
    switch.setPadding(switch.paddingLeft, switch.paddingTop, right, switch.paddingBottom)
    super.setPadding(0, top, 0, bottom)
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
    text.setPaddingRelative(start, text.paddingTop, ViewCompat.getPaddingEnd(text), text.paddingBottom)
    switch.setPaddingRelative(ViewCompat.getPaddingStart(switch), switch.paddingTop, end, switch.paddingBottom)
    super.setPaddingRelative(0, top, 0, bottom)
  }

  override fun isChecked(): Boolean = switch.isChecked
  override fun toggle() = switch.toggle()
  override fun setChecked(checked: Boolean) {
    switch.isChecked = checked
  }

  private fun wrapInClickableSpan(s: CharSequence?): CharSequence? {
    s ?: return null


    if (s is SpannedString) {
      val annotations = s.getSpans(0, s.length, Annotation::class.java)
      return SpannableStringBuilder(s).apply {
        for (annotation in annotations) {
          setSpan(
            PolicyClickSpan(), s.getSpanStart(annotation), s.getSpanEnd(annotation),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
          )
        }
      }
    }

    // fallback wrap everything
    return SpannableStringBuilder(s).apply {
      setSpan(PolicyClickSpan(), 0, length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
  }

  inner class PolicyClickSpan : ClickableSpan() {

    override fun onClick(widget: View?) {
      clearFocus()
      showPolicy()
    }

  }

  private fun showPolicy() {
    val intent = policyManager.getPolicyIntent()
    context.startActivity(intent)
  }

}
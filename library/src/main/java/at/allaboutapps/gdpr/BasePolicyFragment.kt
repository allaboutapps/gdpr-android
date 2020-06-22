package at.allaboutapps.gdpr

abstract class BasePolicyFragment : androidx.fragment.app.Fragment() {

    open fun onBackPressed(): Boolean = false

    abstract fun getTitle(): String
}

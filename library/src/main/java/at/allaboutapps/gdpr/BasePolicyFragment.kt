package at.allaboutapps.gdpr

public abstract class BasePolicyFragment : androidx.fragment.app.Fragment() {

    internal open fun onBackPressed(): Boolean = false

    internal abstract fun getTitle(): String
}

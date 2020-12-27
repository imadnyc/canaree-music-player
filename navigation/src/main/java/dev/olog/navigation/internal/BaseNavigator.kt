package dev.olog.navigation.internal

import android.graphics.Color
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.*
import com.google.android.material.snackbar.Snackbar
import dev.olog.navigation.R

private const val NEXT_REQUEST_THRESHOLD: Long = 400 // ms

internal abstract class BaseNavigator {

    // fragment tag, last added
    private val backStackCount = mutableMapOf<String, Int>()

    private var lastRequest: Long = -1

    protected fun addFragment(
        activity: FragmentActivity,
        fragment: Fragment?,
        tag: String,
        @IdRes containerId: Int = R.id.fragmentContainer,
        forced: Boolean = false,
        block: FragmentTransaction.(Fragment) -> Any? = {}
    ) {
        doTransaction(activity, fragment, tag, forced) {
            add(containerId, fragment!!, tag)
            block(it)
        }
    }

    protected fun replaceFragment(
        activity: FragmentActivity,
        fragment: Fragment?,
        tag: String,
        @IdRes containerId: Int = R.id.fragmentContainer,
        forced: Boolean = false,
        block: FragmentTransaction.(Fragment) -> Any? = {}
    ) {
        doTransaction(activity, fragment, tag, forced) {
            replace(containerId, fragment!!, tag)
            block(it)
        }
    }

    private fun doTransaction(
        activity: FragmentActivity,
        fragment: Fragment?,
        tag: String,
        forced: Boolean = false,
        block: FragmentTransaction.(Fragment) -> Any? = {}
    ) {
        mandatory(activity, fragment != null, forced) ?: return

        if (fragment is DialogFragment) {
            fragment.show(activity.supportFragmentManager, tag)
            return
        }

        activity.supportFragmentManager.commit {
            setReorderingAllowed(true)
            block(fragment!!)
        }
    }

    /**
     * @param forced, skips [allowed] check
     */
    protected fun mandatory(
        activity: FragmentActivity,
        condition: Boolean,
        forced: Boolean = false
    ): Unit? {
        if (!forced && !allowed()) {
            // avoid click spam
            return null
        }
        if (condition) {
            return Unit
        }

        val rootView = activity.findViewById<View>(android.R.id.content)

        val snackBar = Snackbar.make(rootView, "Module not plugged", Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(Color.parseColor("#bf485a"))
        snackBar.show()

        return null
    }

    /**
     * Use this when you can instantiate multiple times same fragment
     */
    protected fun createBackStackTag(fragmentTag: String): String {
        // get last + 1
        val counter = backStackCount.getOrPut(fragmentTag) { 0 } + 1
        // update
        backStackCount[fragmentTag] = counter
        // creates new
        return "$fragmentTag$counter"
    }

    fun allowed(): Boolean {
        val allowed = (System.currentTimeMillis() - lastRequest) > NEXT_REQUEST_THRESHOLD
        lastRequest = System.currentTimeMillis()
        return allowed
    }

}
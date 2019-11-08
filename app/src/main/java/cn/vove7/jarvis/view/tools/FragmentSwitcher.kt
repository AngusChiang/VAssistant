package cn.vove7.jarvis.view.tools

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

class FragmentSwitcher(val activity: AppCompatActivity, @IdRes val containId: Int) {

    var currentFragment: androidx.fragment.app.Fragment? = null
    private val fragments = hashMapOf<String, androidx.fragment.app.Fragment>()

    fun switchFragment(f: androidx.fragment.app.Fragment): Boolean {
        return switchFragment(f::class.simpleName!!, f)
    }

    private fun switchFragment(tag: String, f: androidx.fragment.app.Fragment): Boolean {
        if (currentFragment == f) return true
        fragments[tag] = f
        if (currentFragment != null) {
            activity.supportFragmentManager.beginTransaction()
                    .hide(currentFragment!!)
                    .commit()
        }
        try {
            activity.supportFragmentManager.beginTransaction()
                    .replace(containId, f)
                    .commit()
        } catch (e: Exception) {
            return false
        }
        currentFragment = f
        return true
    }

    fun getFragmentInstance(cls: Class<androidx.fragment.app.Fragment>): androidx.fragment.app.Fragment? {
        return getFragmentInstance(cls::class.simpleName)
    }

    fun getFragmentInstance(tag: String?): androidx.fragment.app.Fragment? {
        return fragments[tag]
    }
}
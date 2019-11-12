package cn.vove7.jarvis.view.tools

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class FragmentSwitcher(val activity: AppCompatActivity, @IdRes val containId: Int) {

    var currentFragment: Fragment? = null
    private val fragments = mutableSetOf<Fragment>()

    fun switchFragment(f: Fragment): Boolean {
        if (currentFragment == f) return true
        currentFragment?.also {
            activity.supportFragmentManager.beginTransaction().apply {
                hide(it)
                commit()
            }
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

    fun getFragmentInstance(cls: Class<Fragment>): Fragment? {
        return fragments.find { it.javaClass == cls }
    }

}
package cn.vove7.jarvis.view.tools

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

class FragmentSwitcher(val activity: AppCompatActivity, @IdRes val containId: Int) {

        var currentFragment: Fragment? = null
        private val fragments = hashMapOf<String, Fragment>()

        fun switchFragment(f: Fragment): Boolean {
            return switchFragment(f::class.simpleName!!, f)
        }

        private fun switchFragment(tag: String, f: Fragment): Boolean {
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

        fun getFragmentInstance(cls: Class<Fragment>): Fragment? {
            return getFragmentInstance(cls::class.simpleName)
        }

        fun getFragmentInstance(tag: String?): Fragment? {
            return fragments[tag]
        }
    }
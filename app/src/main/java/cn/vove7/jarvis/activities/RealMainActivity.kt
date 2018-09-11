package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import cn.vove7.jarvis.R
import cn.vove7.jarvis.fragments.HomeFragment
import cn.vove7.jarvis.fragments.MineFragment
import cn.vove7.jarvis.fragments.StoreFragment
import kotlinx.android.synthetic.main.activity_real_main.*


class RealMainActivity : AppCompatActivity() {

    val fSwitcher = FragmentSwitcher(this, R.id.fragment)
    val homeF = HomeFragment.newInstance()
    val storeF = StoreFragment.newInstance()
    val mineF = MineFragment.newInstance()
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        return@OnNavigationItemSelectedListener when (item.itemId) {
            R.id.nav_home -> fSwitcher.switchFragment(homeF)
            R.id.nav_store -> fSwitcher.switchFragment(storeF)
            R.id.nav_me -> fSwitcher.switchFragment(mineF)
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fSwitcher.switchFragment(homeF)
    }


    class FragmentSwitcher(val activity: AppCompatActivity, @IdRes val containId: Int) {

        var currentFragment: Fragment? = null
        private val fragments = hashMapOf<String, Fragment>()

        fun switchFragment(f: Fragment): Boolean {
            return switchFragment(f::class.simpleName!!, f)
        }

        fun switchFragment(tag: String, f: Fragment): Boolean {
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
}

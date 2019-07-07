package cn.vove7.jarvis.activities.base

import android.support.v7.app.AppCompatActivity
import com.umeng.analytics.MobclickAgent


/**
 * # BaseActivity
 *
 * @author Vove
 * 2019/6/12
 */
abstract class BaseActivity : AppCompatActivity() {

    private val pageName: String
        get() {
            val clz = this::class.java
            return try {
                clz.getDeclaredField("PAGE_NAME").get(this) as String
            } catch (e: NoSuchFieldException) {
                clz.simpleName
            }
        }

    public override fun onResume() {
        super.onResume()
        MobclickAgent.onPageStart(pageName)
    }

    public override fun onPause() {
        super.onPause()
        MobclickAgent.onPageEnd(pageName)
    }

}
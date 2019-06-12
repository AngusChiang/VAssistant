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

    public override fun onResume() {
        super.onResume()
        MobclickAgent.onPageStart(this::class.java.simpleName)
    }

    public override fun onPause() {
        super.onPause()
        MobclickAgent.onPageEnd(this::class.java.simpleName)
    }

}
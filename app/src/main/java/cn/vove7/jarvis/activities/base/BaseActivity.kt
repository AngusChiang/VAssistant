package cn.vove7.jarvis.activities.base

import androidx.appcompat.app.AppCompatActivity
import cn.vove7.jarvis.tools.DataCollector


/**
 * # BaseActivity
 *
 * @author Vove
 * 2019/6/12
 */
abstract class BaseActivity : AppCompatActivity() {

    open val pageName: String
        get() = this::class.java.simpleName

    public override fun onResume() {
        super.onResume()
        DataCollector.onPageStart(this, pageName)
    }

    public override fun onPause() {
        super.onPause()
        DataCollector.onPageEnd(this, pageName)
    }

}
package cn.vove7.jarvis.activities.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import cn.vove7.jarvis.lifecycle.LifeCycleScopeDelegate
import cn.vove7.jarvis.lifecycle.LifecycleScope
import cn.vove7.jarvis.tools.DataCollector
import kotlinx.coroutines.CoroutineScope


/**
 * # BaseActivity
 *
 * @author Vove
 * 2019/6/12
 */
abstract class BaseActivity : AppCompatActivity(), LifeCycleScopeDelegate {

    override val lifecycleScope by lazy {
        LifecycleScope(lifecycle)
    }

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
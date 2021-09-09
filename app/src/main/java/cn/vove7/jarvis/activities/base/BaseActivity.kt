package cn.vove7.jarvis.activities.base

import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import cn.vove7.android.scaffold.ui.base.ScaffoldActivity
import cn.vove7.jarvis.lifecycle.LifeCycleScopeDelegate
import cn.vove7.jarvis.lifecycle.LifecycleScope
import cn.vove7.jarvis.tools.DataCollector


/**
 * # BaseActivity
 *
 * @author Vove
 * 2019/6/12
 */
abstract class BaseActivity<T : ViewBinding> :
        ScaffoldActivity2<T>(), LifeCycleScopeDelegate {

    val wic by lazy {
        ViewCompat.getWindowInsetsController(viewBinding.root)
    }

    override val needToolbar: Boolean
        get() = false

    val isDarkTheme
        get() = ScaffoldActivity::class.java.getDeclaredMethod("isDarkMode").run {
            isAccessible = true
            invoke(this@BaseActivity) as Boolean
        }
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
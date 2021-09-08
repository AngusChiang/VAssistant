package cn.vove7.jarvis.activities.base

import androidx.viewbinding.ViewBinding
import cn.vove7.android.scaffold.ui.base.ScaffoldActivity

/**
 * #
 *
 * @author Vove
 * @date 2021/5/16
 */
abstract class ScaffoldActivity2<T : ViewBinding> : ScaffoldActivity<T>() {
    val viewBinding get() = binding
}
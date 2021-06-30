package cn.vove7.jarvis.activities.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import cn.vove7.android.scaffold.ui.base.ScaffoldActivity
import java.lang.reflect.ParameterizedType

/**
 * #
 *
 * @author liben
 * @date 2021/5/16
 */
abstract class ScaffoldActivity2<T : ViewBinding> : ScaffoldActivity<ViewDataBinding>() {

    lateinit var viewBinding: T

    @Suppress("UNCHECKED_CAST")
    fun buildViewBinding(
            container: ViewGroup?,
            inflater: LayoutInflater
    ) {
        var superCls = this.javaClass.genericSuperclass
        while (superCls !is ParameterizedType) {
            superCls = (superCls as Class<*>).genericSuperclass
        }
        val vbType = superCls.actualTypeArguments[0] as Class<*>

        if(vbType == ViewBinding::class.java) {
            throw RuntimeException("ViewBinding type is $vbType")
        }

        viewBinding = vbType.getDeclaredMethod("inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
        ).invoke(null, inflater,container,false) as T
    }

    override fun buildView(container: ViewGroup?, layoutInflater: LayoutInflater): View? {
        buildViewBinding(container, layoutInflater)
        return viewBinding.root
    }
}
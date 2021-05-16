package cn.vove7.jarvis.view.floatwindows

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import androidx.viewbinding.ViewBinding
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import java.lang.reflect.ParameterizedType

/**
 * 悬浮窗
 * 需权限
 *
 * Created by Vove on 2018/7/1
 */
abstract class AbFloatWindow<T : ViewBinding>(
        val context: Context,
        val width: Int,
        val height: Int
) : IFloatyPanel {
    open val posX: Int = 0
    open val posY: Int = 0

    abstract val onNoPermission: () -> Unit

    var contentView: View? = null

    private val winParams get() = buildLayoutParams()
    lateinit var animationBody: View

    var viewBinding: T? = null

    val aniBodyInit get() = ::animationBody.isInitialized

    private val themeInflater: LayoutInflater
        get() = LayoutInflater.from(ContextThemeWrapper(context,
                if (SystemBridge.isDarkMode) R.style.DarkTheme
                else R.style.AppTheme
        ))

    private val rootView: ViewGroup
        get() = themeInflater.inflate(R.layout.float_panel_root, null) as ViewGroup

    private val newView: View
        get() {
            val contentView = rootView
            viewBinding = buildView(LayoutInflater.from(context), contentView)
            viewBinding!!.root.also {
                animationBody = contentView.getChildAt(0)
                onCreateView(it)
            }
            return contentView
        }

    private fun buildView(inflater: LayoutInflater, parent: ViewGroup): T {
        val parameterizedType = this.javaClass.genericSuperclass as ParameterizedType
        val vbType = parameterizedType.actualTypeArguments[0] as Class<*>

        if (vbType == ViewBinding::class.java) {
            throw RuntimeException("ViewBinding type is $vbType")
        }

        return vbType.getDeclaredMethod("inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
        ).invoke(null, inflater, parent, true) as T
    }

    open fun onCreateView(view: View) {}

    private var windowManager: WindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE)
            as WindowManager

    protected val isShowing get() = contentView != null

    val statusbarHeight: Int by lazy {
        var statusBarHeight1 = -1
        //获取status_bar_height资源的ID
        val resourceId = context.resources.getIdentifier("status_bar_height",
                "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = context.resources.getDimensionPixelSize(resourceId)
        }
        Vog.d("状态栏高度 ---> $statusBarHeight1")
        statusBarHeight1
    }

    open fun afterShow() {}


    val hasOverlayPermission
        get() =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PermissionUtils.canDrawOverlays(context)

    open val postAfterShow = false

    open fun show() {
        if (!hasOverlayPermission) {
            Vog.d("show ---> 无悬浮窗")
            onNoPermission.invoke()
        } else {
            synchronized(this) {
                if (!isShowing) {
                    try {
                        contentView = newView
                        if (postAfterShow) {
                            contentView?.post { afterShow() }
                        } else {
                            afterShow()
                        }
                        windowManager.addView(contentView, winParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    open fun hide() {
        Vog.d("隐藏悬浮窗")
        synchronized(this) {
            removeView()
        }
    }

    private fun removeView() {
        Vog.d("移除悬浮窗")
        onRemove()
    }

    /**
     * 移除视图
     * 可继承执行动画
     */
    @Synchronized
    open fun onRemove() {
        try {
            contentView?.also { windowManager.removeView(it) }
            contentView = null
            viewBinding = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun update(params: WindowManager.LayoutParams) {
        synchronized(this) {
            if (isShowing) {
                windowManager.updateViewLayout(contentView, params)
            }
        }
    }

    fun updatePoint(x: Int, y: Int) {
        windowManager.updateViewLayout(contentView, buildLayoutParams(x, y))
    }

    open fun buildLayoutParams(x: Int = posX, y: Int = posY): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.packageName = context.packageName
        params.width = width
        params.height = height
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE // 无法触摸

        params.format = PixelFormat.RGBA_8888
        params.gravity = Gravity.TOP or Gravity.START
        params.x = x
        params.y = y

        params.type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        return params
    }

    override fun hideImmediately() {
        runOnUi { hide() }
    }
}
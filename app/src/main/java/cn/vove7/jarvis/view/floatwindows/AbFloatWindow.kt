package cn.vove7.jarvis.view.floatwindows

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils

/**
 * 悬浮窗
 * 需权限
 *
 * Created by Vove on 2018/7/1
 */
abstract class AbFloatWindow(
        val context: Context,
        var mParams: WindowManager.LayoutParams? = null
) {
    open var posX: Int = 0
    open var posY: Int = 0

    abstract val onNoPermission: () -> Unit

    var contentView: View? = null

    val winParams get() = mParams ?: buildLayoutParams().also { mParams = it }

    private val newView: View
        get() {
            return LayoutInflater.from(context)
                    .inflate(this.layoutResId(), null).also {
                        contentView = it
                        onCreateView(it)
                    }
        }

    open fun onCreateView(view: View) {}

    var windowManager: WindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE)
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

    init {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
    }


    /**
     * 布局Id
     */
    abstract fun layoutResId(): Int

    open fun afterShow() {}


    val hasOverlayPermission
        get() =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PermissionUtils.canDrawOverlays(context)

    open fun show() {
        if (!hasOverlayPermission) {
            Vog.d("show ---> 无悬浮窗")
            onNoPermission.invoke()
        } else {
            synchronized(this) {
                if (!isShowing) {
                    try {
                        contentView = newView
                        windowManager.addView(contentView, winParams)
                        contentView?.post {
                            afterShow()
                        }
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
            if (isShowing)
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
            windowManager.removeView(contentView)
            contentView = null
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

    private fun buildLayoutParams(x: Int = posX, y: Int = posY): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.packageName = context.packageName
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
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

}
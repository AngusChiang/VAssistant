package cn.vove7.jarvis.view.floatwindows

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import kotlinx.android.synthetic.main.toast_listening_text.view.*

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

    var isShowing = false

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

    open fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.canDrawOverlays(context)) {
            Vog.d("show ---> 无悬浮窗")
            onNoPermission.invoke()
        } else {
            synchronized(isShowing) {
                if (mParams == null) mParams = buildLayoutParams()
                if (!isShowing) {
                    try {
                        if (contentView != null) hide {
                            contentView = newView
                            windowManager.addView(contentView, mParams)
                            afterShow()
                        }
                        else {
                            contentView = newView
                            windowManager.addView(contentView, mParams)
                            afterShow()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isShowing = true
            }
        }
    }

    open val exitAni: Int? = null

    open fun hide(onEnd: (() -> Unit)? = null) {
//        windowManager.removeView()
        synchronized(isShowing) {
            val ei = exitAni
            if (ei != null) {
                val ani = AnimationUtils.loadAnimation(context, ei)
                ani.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        removeView()
                        onEnd?.invoke()
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
                contentView?.body?.startAnimation(ani)
            } else {
                removeView()
            }
            isShowing = false
        }
    }

    private fun removeView() {
        try {
            windowManager.removeView(contentView)
            contentView = null
            onRemove()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun onRemove() {}

    fun update(params: WindowManager.LayoutParams) {
        synchronized(isShowing) {
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

    open class ViewHolder(val floatView: View)

}
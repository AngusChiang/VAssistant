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
abstract class AbFloatWindow<VH : AbFloatWindow.ViewHolder>(
        val context: Context,
        var mParams: WindowManager.LayoutParams? = null
) {
    open var posX: Int = 0
    open var posY: Int = 0
    abstract val onNoPermission: () -> Unit

    val contentView: View
    var windowManager: WindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var holder: VH

    var isShowing = false

    init {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        contentView = LayoutInflater.from(context).inflate(this.layoutResId(), null)
        holder = this.onCreateViewHolder(contentView)
    }

    /**
     * 注册事件
     */
    abstract fun onCreateViewHolder(view: View): VH

    /**
     * 布局Id
     */
    abstract fun layoutResId(): Int

    open fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.canDrawOverlays(context)) {
            Vog.d("show ---> 无悬浮窗")
            onNoPermission.invoke()
        } else {
            synchronized(isShowing) {
                if (mParams == null) mParams = buildLayoutParams()
                if (!isShowing) {
                    try {
                        windowManager.addView(contentView, mParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isShowing = true
            }
        }
    }

    open fun hide() {
//        windowManager.removeView()
        synchronized(isShowing) {
            try {
                windowManager.removeView(contentView)
                Vog.d("hide ---> remove float")
            } catch (e: Exception) {
            }
            isShowing = false
        }
    }

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
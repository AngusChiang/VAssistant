package cn.vove7.vtp.floatwindow

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import cn.vove7.vtp.log.Vog

/**
 * 悬浮窗
 *
 * Created by Vove on 2018/7/1
 */
abstract class AbFloatWindow<VH : AbFloatWindow.ViewHolder>(
        val context: Context,
        var mParams: WindowManager.LayoutParams? = null,
        var posX: Int = 0,
        var posY: Int = 0
) {
    val contentView: View
    var windowManager: WindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var holder: VH

    init {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        if (mParams == null) mParams = buildLayoutParams()

        mParams?.type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

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
        try {
            windowManager.addView(contentView, mParams)
        } catch (e: Exception) {
            e.printStackTrace()
            Vog.e(this, "无悬浮窗")
        }
    }

    open fun hide() = windowManager.removeView(contentView)

    fun update(params: WindowManager.LayoutParams) {
        windowManager.updateViewLayout(contentView, params)
    }

    fun updatePoint(x: Int, y: Int) {
        windowManager.updateViewLayout(contentView, buildLayoutParams(x, y))
    }

    private fun buildLayoutParams(x: Int = posX, y: Int = posY): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.packageName = context.packageName
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
        params.format = PixelFormat.RGBA_8888
        params.gravity = Gravity.TOP or Gravity.START
        params.x = x
        params.y = y
        return params
    }

    open class ViewHolder(val floatView: View)

}
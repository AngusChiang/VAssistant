package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.util.Log
import android.util.Pair
import android.view.ViewConfiguration
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.ScreenAdapter
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.vtp.log.Vog
import java.util.*


@SuppressLint("StaticFieldLeak")
/**
 * 无障碍全局执行器
 */
object GlobalActionExecutor : GlobalActionExecutorI {
//    var screenAdapter = ScreenAdapter()

    private var mService: AccessibilityService? = null

    override fun setService(mService: AccessibilityService) {
        this.mService = mService
    }

    override fun back(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    override fun home(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }


    override fun powerDialog(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
    }

    override fun notificationBar(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    }


    override fun quickSettings(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
    }

    private fun performGlobalAction(globalAction: Int): Boolean {
        if (mService == null) {
            mService = AccessibilityApi.accessibilityService
            if (mService == null) return false
        }
        return mService?.performGlobalAction(globalAction) == true
    }

    override fun recents(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun splitScreen(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
    }

//    override fun screenShot(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
//        } else {
//            GlobalLog.err("截屏需要Android9.0+")
//            //todo 手动截屏 other fun
//            false
//        }
//    }

    fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        return gesture(0, duration, points)
    }

    override fun gesture(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }
        if (mService == null) {
            mService = AccessibilityApi.accessibilityService
            if (mService == null) return false
        }

        val path = pointsToPath(points)
        return playGestures(GestureDescription.StrokeDescription(path, start, duration))
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>) {

        val path = pointsToPath(points)
        gesturesAsync(GestureDescription.StrokeDescription(path, start, duration))
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun playGestures(vararg strokes: GestureDescription.StrokeDescription): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }
        val builder = GestureDescription.Builder()
        for (stroke in strokes) {
            builder.addStroke(stroke)
        }

        return gesturesWithoutHandler(builder.build())
    }

    private fun pointsToPath(points: Array<Pair<Int, Int>>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        var x = points[0].first
        var y = points[0].second
        path.moveTo(ScreenAdapter.scaleX(x), ScreenAdapter.scaleY(y))

        for (i in 1 until points.size) {
            val point = points[i]
            x = point.first
            y = point.second
            path.lineTo(ScreenAdapter.scaleX(x), ScreenAdapter.scaleY(y))

        }
        Log.d("Debug :", "pointsToPath  ----> ${Arrays.toString(points)}\n$path")
        return path
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun gesturesWithoutHandler(description: GestureDescription): Boolean {
        if (mService == null) {
            mService = AccessibilityApi.accessibilityService
            if (mService == null) return false
        }
        val result = ResultBox(false).prepare()
        val handler = Handler(Looper.myLooper())
        mService!!.dispatchGesture(description, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                result.setAndQuit(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                result.setAndQuit(false)
            }
        }, handler)
        return result.loopGet() ?: false
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun gesturesAsync(vararg strokes: GestureDescription.StrokeDescription) {
        if (mService == null) {
            mService = AccessibilityApi.accessibilityService
        }
        val builder = GestureDescription.Builder()
        for (stroke in strokes) {
            builder.addStroke(stroke)
        }
        mService?.dispatchGesture(builder.build(), null, null)
    }


    /**
     * 名称避免和函数名同名
     */
    private val voast = ColorfulToast(GlobalApp.APP).yellow()//???

    override fun toast(msg: String?) {
        GlobalApp.toastShort(msg ?: "null")
//        voast.showShort(msg)
    }

    override fun click(x: Int, y: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            press(x, y, ViewConfiguration.getTapTimeout() + 50)
        } else {
            Vog.d(this, "需SDK版本->N")
            false
        }
    }

    override fun press(x: Int, y: Int, delay: Int): Boolean {
        return gesture(0, delay.toLong(), arrayOf(Pair(x, y)))
    }

    override fun longClick(x: Int, y: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gesture(0, (ViewConfiguration.getLongPressTimeout() + 200).toLong(),
                    arrayOf(Pair(x, y)))
        } else {
            Vog.d(this, "需SDK版本->N")
            false
        }
    }


    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gesture(0, dur.toLong(), arrayOf(Pair(x1, y1),
                    Pair(x2, y2)))
        } else {
            Vog.d(this, "需SDK版本 -> N")
            false
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun scrollUp(): Boolean {
        val mtop = (ScreenAdapter.relHeight * 0.1).toInt()
        val mBottom = (ScreenAdapter.relHeight * 0.85).toInt()
        val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()

        return swipe(xCenter, mBottom, xCenter, mtop, 400)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun scrollDown(): Boolean {
        val mtop = (ScreenAdapter.relHeight * 0.15).toInt()
        val mBottom = (ScreenAdapter.relHeight * 0.9).toInt()
        val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()
        return swipe(xCenter, mtop, xCenter, mBottom, 400)
    }
}

interface GlobalActionExecutorI {
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun press(x: Int, y: Int, delay: Int): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun longClick(x: Int, y: Int): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun scrollDown(): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun click(x: Int, y: Int): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun scrollUp(): Boolean

    fun setService(mService: AccessibilityService)

    fun back(): Boolean

    fun home(): Boolean

    fun powerDialog(): Boolean

    fun notificationBar(): Boolean

    fun quickSettings(): Boolean

    fun recents(): Boolean
    fun splitScreen(): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gesture(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>)

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun playGestures(vararg strokes: GestureDescription.StrokeDescription): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gesturesAsync(vararg strokes: GestureDescription.StrokeDescription)


    /**
     * 通知
     */
    fun toast(msg: String?)
}
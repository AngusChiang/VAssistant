package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.view.ViewConfiguration

import cn.vove7.common.model.ResultBox
import cn.vove7.common.model.ScreenMetrics
import cn.vove7.vtp.log.Vog

/**
 * 无障碍全局执行器
 */
class GlobalActionAutomator : ActionAutomatorI {

    lateinit var mService: AccessibilityService
    private var mHandler: Handler? = null
    private val mScreenMetrics = ScreenMetrics()

    constructor(mService: AccessibilityService, mHandler: Handler) {
        this.mService = mService
        this.mHandler = mHandler
    }

    override fun setService(mService: AccessibilityService) {
        this.mService = mService
    }

    override fun back(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    override fun home(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    constructor(handler: Handler?) {
        mHandler = handler
    }

    override fun powerDialog(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
    }

    override fun notifications(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    }

    override fun quickSettings(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
    }

    private fun performGlobalAction(globalAction: Int): Boolean {
        return mService.performGlobalAction(globalAction)
    }

    override fun recents(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun splitScreen(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
    }

    override fun gesture(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }
        val path = pointsToPath(points)
        return gestures(GestureDescription.StrokeDescription(path, start, duration))
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>) {
        val path = pointsToPath(points)
        gesturesAsync(GestureDescription.StrokeDescription(path, start, duration))
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun gestures(vararg strokes: GestureDescription.StrokeDescription): Boolean {
        val builder = GestureDescription.Builder()
        for (stroke in strokes) {
            builder.addStroke(stroke)
        }
        return if (mHandler == null) {
            gesturesWithoutHandler(builder.build())
        } else {
            gesturesWithHandler(builder.build())
        }
    }

    private fun pointsToPath(points: Array<Pair<Int, Int>>): Path {
        val path = Path()
        path.moveTo(scaleX(points[0].first).toFloat(), scaleY(points[0].second).toFloat())

        for (i in 1 until points.size) {
            val point = points[i]
            path.lineTo(scaleX(point.first).toFloat(), scaleY(point.second).toFloat())
        }
        return path
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun gesturesWithHandler(description: GestureDescription): Boolean {
        val result = ResultBox<Boolean>()
        mService.dispatchGesture(description, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                result.setAndNotify(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                result.setAndNotify(false)
            }
        }, mHandler)
        return result.blockedGet()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun gesturesWithoutHandler(description: GestureDescription): Boolean {
        prepareLooperIfNeeded()
        val result = ResultBox(false)
        val handler = Handler(Looper.myLooper())
        mService.dispatchGesture(description, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                result.set(true)
                quitLoop()
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                result.set(false)
                quitLoop()
            }
        }, handler)
        Looper.loop()
        return result.get()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun gesturesAsync(vararg strokes: GestureDescription.StrokeDescription) {
        val builder = GestureDescription.Builder()
        for (stroke in strokes) {
            builder.addStroke(stroke)
        }
        mService.dispatchGesture(builder.build(), null, null)
    }

    private fun quitLoop() {
        val looper = Looper.myLooper()
        looper?.quit()
    }

    private fun prepareLooperIfNeeded() {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }
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
            gesture(0, (ViewConfiguration.getLongPressTimeout() + 200).toLong(), arrayOf(Pair(x, y)))
        } else {
            Vog.d(this, "需SDK版本->N")
            false
        }
    }

    private fun scaleX(x: Int): Int {
        return mScreenMetrics.scaleX(x)
    }

    private fun scaleY(y: Int): Int {
        return mScreenMetrics.scaleY(y)
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, delay: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gesture(0, delay.toLong(), arrayOf(Pair(x1, y1), Pair(x2, y2)))
        } else {
            Vog.d(this, "需SDK版本->N")
            false
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun scrollUp(): Boolean {
        return swipe(550, 1600, 550, 300, 400)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun scrollDown(): Boolean {
        return swipe(550, 300, 550, 1600, 400)
    }

}

interface ActionAutomatorI {
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, delay: Int): Boolean

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

    fun notifications(): Boolean

    fun quickSettings(): Boolean

    fun recents(): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gesture(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>)

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gestures(vararg strokes: GestureDescription.StrokeDescription): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gesturesAsync(vararg strokes: GestureDescription.StrokeDescription)
}
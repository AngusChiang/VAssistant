package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.Pair
import android.view.ViewConfiguration
import android.widget.Toast
import cn.vove7.common.MessageException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.annotation.ScriptApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.interfaces.api.GlobalActionExecutorI
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.ScreenAdapter
import cn.vove7.vtp.log.Vog

/**
 * 无障碍全局执行器
 */
object GlobalActionExecutor : GlobalActionExecutorI {

    private val gestureService: AccessibilityService?
        get() = (AccessibilityApi.isAdvanServiceOn
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.N).let {
            if (it) AccessibilityApi.gestureService!!
            else {
                GlobalApp.toastError("全局手势执行失败: 版本低于7.0或高级无障碍服务未开启")
                throw MessageException("高级无障碍服务未开启")
            }
        }

    private val baseService: AccessibilityService?
        get() = AccessibilityApi.accessibilityService

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

    override fun lockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            GlobalLog.err("lockScreen 仅支持Android9.0+")
            false
        }
    }

    override fun screenShot(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            GlobalLog.err("screenShot 仅支持Android9.0+")
            false
        }
    }

    private fun performGlobalAction(globalAction: Int): Boolean {
        if (baseService == null) {
            return false
        }
        return baseService?.performGlobalAction(globalAction) == true
    }

    override fun recents(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun splitScreen(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
    }

    /**
     * 手势 一条路径
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     * @return Boolean
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        return gesture(0, duration, points)
    }

    /**
     * 手势 一条路径
     * @param start Long
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     * @return Boolean
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun gesture(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        if (gestureService == null) {
            return false
        }
        val path = pointsToPath(points)
        return playGestures(listOf(GestureDescription.StrokeDescription(path, start, duration)))
    }

    /**
     * api 异步手势
     * @param start Long
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>) {
        gestureService ?: return
        val path = pointsToPath(points)
        doGesturesAsync(listOf(GestureDescription.StrokeDescription(path, 0, duration)))
    }

    /**
     * api 多路径手势
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>) {
        gestureService ?: return
        val list = mutableListOf<GestureDescription.StrokeDescription>()
        ppss.forEach {
            list.add(GestureDescription.StrokeDescription(pointsToPath(it), 0, duration))
        }
        playGestures(list)
    }

    /**
     * api 多路径手势 异步
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>) {
        gestureService ?: return
        val list = mutableListOf<GestureDescription.StrokeDescription>()
        ppss.forEach {
            list.add(GestureDescription.StrokeDescription(pointsToPath(it), 0, duration))
        }
        doGesturesAsync(list)
    }

    /**
     * 同步
     * @param strokes Array<out StrokeDescription>
     * @return Boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun playGestures(strokeList: List<GestureDescription.StrokeDescription>): Boolean {
        val builder = GestureDescription.Builder()
        for (stroke in strokeList) {
            builder.addStroke(stroke)
        }
        return gesturesWithoutHandler(builder.build())
    }

    /**
     * 点转路径
     * @param points Array<Pair<Int, Int>>
     * @return Path
     */
    private fun pointsToPath(points: Array<Pair<Int, Int>>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        var x = points[0].first.toInt()
        var y = points[0].second.toInt()
        path.moveTo(ScreenAdapter.scaleX(x), ScreenAdapter.scaleY(y))

        for (i in 1 until points.size) {
            val point = points[i]
            x = point.first
            y = point.second
            path.lineTo(ScreenAdapter.scaleX(x), ScreenAdapter.scaleY(y))
        }
        return path
    }

    /**
     * 同步手势
     * @param description GestureDescription
     * @return Boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun gesturesWithoutHandler(description: GestureDescription): Boolean {
        val gs = gestureService ?: return false
        val result = ResultBox(false)
        gs.dispatchGesture(description, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                result.setAndNotify(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                result.setAndNotify(false)
            }
        }, null).also {
            Vog.d("gesturesWithoutHandler ---> 手势执行$it")
            if (!it) {
                Vog.d("gesturesWithoutHandler ---> 手势执行失败 ")
                return false
            }
        }
        return result.blockedGet() ?: false
    }

    /**
     * 异步手势
     * @param strokeList List<out StrokeDescription>
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun doGesturesAsync(strokeList: List<GestureDescription.StrokeDescription>) {
        val builder = GestureDescription.Builder()
        for (stroke in strokeList) {
            builder.addStroke(stroke)
        }
        gestureService?.dispatchGesture(builder.build(), null, null)
    }

    override fun toast(msg: String?) {
        GlobalApp.toastInfo(msg ?: "null")
    }

    override fun toastLong(msg: String?) {
        GlobalApp.toastInfo(msg ?: "null", Toast.LENGTH_LONG)
    }

    override fun click(x: Int, y: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            press(x, y, ViewConfiguration.getTapTimeout() + 50)
        } else {
            GlobalLog.log("click 需SDK版本->N")
            false
        }
    }

    /**
     *
     * @param x Int
     * @param y Int
     * @param delay Int
     * @return Boolean
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun press(x: Int, y: Int, delay: Int): Boolean {
        return gesture(0, delay.toLong(), arrayOf(Pair(x, y)))
    }

    /**
     * @param x Int
     * @param y Int
     * @return Boolean
     */
    override fun longClick(x: Int, y: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gesture(0, (ViewConfiguration.getLongPressTimeout() + 200).toLong(),
                    arrayOf(Pair(x, y)))
        } else {
            GlobalLog.log("longClick 需SDK版本->N")
            false
        }
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gesture(0, dur.toLong(), arrayOf(Pair(x1, y1),
                    Pair(x2, y2)))
        } else {
            Vog.d("需SDK版本 -> N")
            false
        }
    }

    override fun scrollUp(): Boolean {
        val mtop = (ScreenAdapter.relHeight * 0.1).toInt()
        val mBottom = (ScreenAdapter.relHeight * 0.85).toInt()
        val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()

        return swipe(xCenter, mBottom, xCenter, mtop, 400)
    }

    override fun scrollDown(): Boolean {
        val mtop = (ScreenAdapter.relHeight * 0.15).toInt()
        val mBottom = (ScreenAdapter.relHeight * 0.9).toInt()
        val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()
        return swipe(xCenter, mtop, xCenter, mBottom, 400)
    }
}

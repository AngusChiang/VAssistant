package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Pair
import android.view.ViewConfiguration
import androidx.annotation.RequiresApi
import cn.vove7.common.MessageException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.interfaces.api.GlobalActionExecutorI
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.ScreenAdapter
import cn.vove7.vtp.log.Vog

/**
 * # AcsActionExecutor
 * 基于无障碍的 执行器
 * @author Vove
 * @date 2021/8/16
 */
object AcsActionExecutor : GlobalActionExecutorI {

    private val gestureService: AccessibilityService
        get() = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            AccessibilityApi.requireGestureService()
        } else {
            GlobalApp.toastError("全局手势执行失败: 系统版本低于7.0")
            throw MessageException("高级无障碍服务未开启")
        }


    private val baseService: AccessibilityService
        get() = AccessibilityApi.requireAccessibility(AccessibilityApi.WHICH_SERVICE_BASE)

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
            false
        }
    }

    override fun screenShot(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            false
        }
    }

    private fun performGlobalAction(globalAction: Int): Boolean {
        return baseService.performGlobalAction(globalAction)
    }

    override fun recents(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    override fun splitScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        } else {
            false
        }
    }

    /**
     * 手势 一条路径
     * @param start Long
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     * @return Boolean
     */
    override fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        val path = pointsToPath(points)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            playGestures(listOf(GestureDescription.StrokeDescription(path, 0, duration)))
        } else {
            false
        }
    }

    /**
     * api 异步手势
     * @param start Long
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     */
    override fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        val path = pointsToPath(points)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            doGesturesAsync(listOf(GestureDescription.StrokeDescription(path, 0, duration)))
            true
        } else {
            false
        }
    }

    /**
     * api 多路径手势
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    override fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }
        val list = mutableListOf<GestureDescription.StrokeDescription>()
        ppss.forEach {
            list.add(GestureDescription.StrokeDescription(pointsToPath(it), 0, duration))
        }
        playGestures(list)
        return true
    }

    /**
     * api 多路径手势 异步
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    override fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }
        val list = mutableListOf<GestureDescription.StrokeDescription>()
        ppss.forEach {
            list.add(GestureDescription.StrokeDescription(pointsToPath(it), 0, duration))
        }
        doGesturesAsync(list)
        return true
    }

    /**
     * 同步
     * @param strokes Array<out StrokeDescription>
     * @return Boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun playGestures(strokeList: List<GestureDescription.StrokeDescription>): Boolean {
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
        val gs = gestureService
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
    fun doGesturesAsync(strokeList: List<GestureDescription.StrokeDescription>) {
        val builder = GestureDescription.Builder()
        for (stroke in strokeList) {
            builder.addStroke(stroke)
        }
        gestureService.dispatchGesture(builder.build(), null, null)
    }


    /**
     *
     * @param x Int
     * @param y Int
     * @param delay Int
     * @return Boolean
     */
    override fun press(x: Int, y: Int, delay: Int): Boolean {
        return gesture(delay.toLong(), arrayOf(Pair(x, y)))
    }

    /**
     * @param x Int
     * @param y Int
     * @return Boolean
     */
    override fun longClick(x: Int, y: Int): Boolean {
        return gesture((ViewConfiguration.getLongPressTimeout() + 50).toLong(), arrayOf(Pair(x, y)))
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean {
        return gesture(dur.toLong(), arrayOf(Pair(x1, y1),
            Pair(x2, y2)))
    }

}
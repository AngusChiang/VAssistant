package cn.vove7.common.interfaces.api

import android.accessibilityservice.GestureDescription
import android.os.Build
import android.support.annotation.RequiresApi
import cn.vove7.common.annotation.ScriptApi
import cn.vove7.common.annotation.ScriptApiClass
import cn.vove7.common.annotation.ScriptApiParamDesc
import android.util.Pair

@ScriptApiClass("无障碍操作","accessibility")
interface GlobalActionExecutorI {

    @ScriptApi("模拟直线滑动手势")
    @ScriptApiParamDesc(["x1: Int", "y1: Int", "x2: Int", "y2: Int", "dur: Int"])
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean

    fun press(x: Int, y: Int, delay: Int): Boolean

    fun longClick(x: Int, y: Int): Boolean

    fun scrollDown(): Boolean

    fun click(x: Int, y: Int): Boolean

    fun scrollUp(): Boolean

    fun back(): Boolean

    fun home(): Boolean

    fun powerDialog(): Boolean

    fun notificationBar(): Boolean

    fun quickSettings(): Boolean
    /**
     * 无障碍锁屏
     * @return Boolean
     */
    fun lockScreen(): Boolean

    /**
     * 无障碍截屏
     */
    fun screenShot(): Boolean

    fun recents(): Boolean
    fun splitScreen(): Boolean
    /**
     * 单手势同步
     * @param start Long
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     * @return Boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gesture(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean

    /**
     * 多手势同步
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>)

    /**
     * 单手势异步
     * @param start Long
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>)

    /**
     * 多手势异步
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>)

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun playGestures(strokeList: List<GestureDescription.StrokeDescription>): Boolean

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun doGesturesAsync(strokeList: List<GestureDescription.StrokeDescription>)


    /**
     * 通知
     */
    fun toast(msg: String?)
    fun toastLong(msg: String?)
}


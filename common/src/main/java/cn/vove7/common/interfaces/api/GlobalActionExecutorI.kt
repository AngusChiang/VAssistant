package cn.vove7.common.interfaces.api

import android.util.Pair
import android.widget.Toast
import cn.vove7.common.annotation.ScriptApi
import cn.vove7.common.annotation.ScriptApiClass
import cn.vove7.common.annotation.ScriptApiParamDesc
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.ScreenAdapter

@ScriptApiClass("无障碍操作", "accessibility")
interface GlobalActionExecutorI {

    @ScriptApi("模拟直线滑动手势")
    @ScriptApiParamDesc(["x1: Int", "y1: Int", "x2: Int", "y2: Int", "dur: Int"])
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean

    fun press(x: Int, y: Int, delay: Int): Boolean

    fun longClick(x: Int, y: Int): Boolean

    fun click(x: Int, y: Int): Boolean {
        return press(x, y, 30)
    }

    fun scrollUp(): Boolean {
        val mtop = (ScreenAdapter.relHeight * 0.1).toInt()
        val mBottom = (ScreenAdapter.relHeight * 0.85).toInt()
        val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()

        return swipe(xCenter, mBottom, xCenter, mtop, 400)
    }

    fun scrollDown(): Boolean {
        val mtop = (ScreenAdapter.relHeight * 0.15).toInt()
        val mBottom = (ScreenAdapter.relHeight * 0.9).toInt()
        val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()
        return swipe(xCenter, mtop, xCenter, mBottom, 400)
    }

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
    fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean

    /**
     * 多手势同步
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean

    /**
     * 单手势异步
     * @param duration Long
     * @param points Array<Pair<Int, Int>>
     */
    fun gestureAsync(duration: Long, points: Array<Pair<Int, Int>>): Boolean

    /**
     * 多手势异步
     * @param duration Long
     * @param ppss Array<Array<Pair<Int, Int>>>
     */
    fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean

    /**
     * 通知
     */
    fun toast(msg: String?) {
        GlobalApp.toastInfo(msg ?: "null")
    }

    fun toastLong(msg: String?) {
        GlobalApp.toastInfo(msg ?: "null", Toast.LENGTH_LONG)
    }

    fun release() {}
}


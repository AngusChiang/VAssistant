package cn.vove7.common.bridges

import android.util.Pair
import android.view.KeyEvent
import android.view.ViewConfiguration
import cn.vove7.common.interfaces.api.GlobalActionExecutorI
import cn.vove7.common.utils.ScreenAdapter

/**
 * # RootActionExecutor
 *
 * @author Vove
 * @date 2021/9/2
 */
object RootActionExecutor : GlobalActionExecutorI {

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean {
        ShellHelper.execWithSu("input swipe ${ScreenAdapter.scaleX(x1)} " +
            "${ScreenAdapter.scaleY(y1)} " +
            "${ScreenAdapter.scaleX(x2)} " +
            "${ScreenAdapter.scaleY(y2)} $dur", waitResult = false)
        return true
    }

    override fun press(x: Int, y: Int, delay: Int): Boolean {
        ShellHelper.execWithSu("input motionevent DOWN " +
            "${ScreenAdapter.scaleX(x)} " +
            "${ScreenAdapter.scaleY(y)}", waitResult = false)
        Thread.sleep(delay.toLong())
        ShellHelper.execWithSu("input motionevent UP " +
            "${ScreenAdapter.scaleX(x)} " +
            "${ScreenAdapter.scaleY(y)}", waitResult = false)
        return true
    }

    override fun longClick(x: Int, y: Int): Boolean {
        return press(x, y, ViewConfiguration.getLongPressTimeout())
    }

    override fun click(x: Int, y: Int): Boolean {
        ShellHelper.execWithSu(
            "input tap ${ScreenAdapter.scaleX(x)} ${ScreenAdapter.scaleY(y)}",
            waitResult = false
        )
        return true
    }

    override fun back(): Boolean {
        sendKey(KeyEvent.KEYCODE_BACK)
        return true
    }

    override fun home(): Boolean {
        sendKey(KeyEvent.KEYCODE_HOME)
        return true
    }

    override fun powerDialog(): Boolean {
        return false
    }

    override fun notificationBar(): Boolean = false

    override fun quickSettings(): Boolean = false

    override fun lockScreen(): Boolean {
        sendKey(KeyEvent.KEYCODE_SLEEP)
        return true
    }

    override fun screenShot(): Boolean = false

    override fun recents(): Boolean {
        sendKey(KeyEvent.KEYCODE_APP_SWITCH)
        return true
    }

    private fun sendKey(key: Int) {
        ShellHelper.execWithSu("input keyevent $key", waitResult = false)
    }

    override fun splitScreen(): Boolean = false

    override fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        throw UnsupportedOperationException("Root不支持手势操作")
    }

    override fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        throw UnsupportedOperationException("Root不支持手势操作")
    }

    override fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        throw UnsupportedOperationException("Root不支持手势操作")
    }

    override fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        throw UnsupportedOperationException("Root不支持手势操作")
    }
}
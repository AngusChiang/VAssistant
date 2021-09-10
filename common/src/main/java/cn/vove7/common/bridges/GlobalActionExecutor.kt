package cn.vove7.common.bridges

import android.util.Pair
import cn.vove7.common.MessageException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.AppPermission
import cn.vove7.common.interfaces.api.GlobalActionExecutorI

/**
 * 无障碍全局执行器
 */
object GlobalActionExecutor : GlobalActionExecutorI by AutoExecutor()

class AutoExecutor : GlobalActionExecutorI {

//    private val impl
//        get() = when {
//            AccessibilityApi.isGestureServiceOn -> AcsActionExecutor
//            SystemBridge.isWirelessAdbEnabled() -> AdbActionExecutor
//            ShellHelper.isRoot() -> RootActionExecutor
//            else -> throw MessageException("此操作需要高级无障碍服务或root/adb权限")
//        }

    private fun impls(which: Int) = run {
        val availableExecutors = mutableListOf<GlobalActionExecutorI>()

        fun checkAcs() {
            if (AccessibilityApi.isServiceEnable(which)) {
                availableExecutors.add(AcsActionExecutor)
            }
        }
        if (AppConfig.gestureAdbFirst && ScrcpyActionExecutor.availiable) {
            availableExecutors.add(ScrcpyActionExecutor)
            checkAcs()
        } else {
            checkAcs()
            if (ScrcpyActionExecutor.availiable) {
                availableExecutors.add(ScrcpyActionExecutor)
            }
        }

        if (availableExecutors.isEmpty() && AppPermission.canWriteSecureSettings) {
            kotlin.runCatching {
                AccessibilityApi.requireAccessibility(which, jump = true)
                checkAcs()
            }
        }
        if (availableExecutors.isEmpty()) {
            throw MessageException("此操作需要高级无障碍服务或root/adb权限")
        }
        availableExecutors
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.swipe(x1, y1, x2, y2, dur) }
    }

    override fun press(x: Int, y: Int, delay: Int): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.press(x, y, delay) }
    }

    override fun longClick(x: Int, y: Int): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.longClick(x, y) }
    }

    override fun scrollDown(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.scrollDown() }
    }

    override fun click(x: Int, y: Int): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.click(x, y) }
    }

    override fun scrollUp(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.scrollUp() }
    }

    override fun back(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.back() }
    }

    override fun home(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.home() }
    }

    override fun powerDialog(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.powerDialog() }
    }

    override fun notificationBar(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.notificationBar() }
    }

    override fun quickSettings(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.quickSettings() }
    }

    override fun lockScreen(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.lockScreen() }
    }

    override fun screenShot(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.screenShot() }
    }

    override fun recents(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.recents() }
    }

    override fun splitScreen(): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_BASE).any { it.splitScreen() }
    }

    override fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.gesture(duration, points) }
    }

    override fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.gestures(duration, ppss) }
    }

    override fun gestureAsync(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.gestureAsync(duration, points) }
    }

    override fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        return impls(AccessibilityApi.WHICH_SERVICE_GESTURE).any { it.gesturesAsync(duration, ppss) }
    }

    override fun release() {
        AcsActionExecutor.release()
        ScrcpyActionExecutor.release()
    }
}
package cn.vove7.common.interfaces

/**
 * Created by Administrator on 2018/10/9
 */
interface VApi {
    companion object {

        val myApiName = arrayOf(
                "app",
                "runtime",
                "ViewFinder()",
                "system"
        )
        val runtimeFunctions = arrayOf("currentApp",
                "currentActivity", "actionCount", "currentActionIndex", "isGlobal()",
                "commandType", "command", "DEBUG")

        val finderFuns = arrayOf(
                "ViewFinder()", "waitFor()", "depths()",
                "containsText()", "matchesText()", "equalsText()",
                "similaryText()", "id()", "desc()",
                "editable()", "scrollable()", "type()", "await()",
                "findFirst()", "find"
        )
        val utilFuns = arrayOf(
                "toPinyin()", "arr2String()", "print()", "matches()", "matchValues()"
        )
        val globalFuns = arrayOf(
                "toast()", "back()", "home()", "powerDialog()", "quickSettings()",
                "recents()", "notificationBar()",
                "setScreenSize()", "swipe()", "click()", "longClick()",
                "gesture()", "scrollDown()", "scrollUp()", "screenShot()"
                , "notifyFailed()"
        )
        val JS_OPERATORS = charArrayOf('(', ')', '{', '}', ',', ';', '=',
                '+', '-', '/', '*', '&', '!', '|', ':', '[', ']', '<', '>', '?', '~', '%', '^')
        val systemFuncs = arrayOf(
                "openAppDetail()", "getPkgByWord()",
                "openAppByPkg()", "openAppByWord()", "call()",
                "openFlashlight()", "closeFlashlight()",
                "getDeviceInfo()", "getAppInfo()", "openUrl()",
                "mediaPause()", "mediaStart()", "mediaResume()",
                "mediaStop()", "mediaNext()", "mediaPre()", "volumeMute()",
                "volumeUnmute()", "volumeUp()", "volumeDown()",
                "setMusicVolume()", "setAlarmVolume()", "setNotificationVolume()",
                "isMediaPlaying()", "musicMaxVolume",
                "musicCurrentVolume", "vibrate()", "openBluetooth()",
                "closeBluetooth()", "openWifi()",
                "closeWifi()", "openWifiAp()", "closeWifiAp()",
                "isScreenOn()", "getClipText()", "setClipText()", "sendEmail()",
                "lockScreen()", "screenShot()",
                "screen2File()", "shareText()", "shareImage()", "location()",
                "getIpAddress()"
        )
        val appFunctions = arrayOf(
                "startActivity()", "getSystemService()"
        )
        val viewNodeFunctions =
            arrayOf(
                    "tryClick()", "getCenterPoint()", "centerPoint()",
                    "getChilds()", "childs", "bounds", "getBounds()",
                    "getBoundsInParent()", "boundsInParent", "getParent()",
                    "parent", "click()", "swipe()", "tryLongClick()",
                    "getChildCount()", "childCount", "longClick()",
                    "doubleClick()", "setText()", "text", "trySetText()",
                    "getText()", "select()", "trySelect()", "focus()", "scrollUp()",
                    "scrollDown()", "setTextWithInitial()", "isClickable"
            )
        val executorFunctions =
            arrayOf("interrupt()", "setScreenSize()",
                    "checkAccessibilityService()", "alert()",
                    "singleChoiceDialog()", "waitForVoiceParam()",
                    "waitForApp()", "waitForViewId()",
                    "waitForDesc()", "waitForText()", "sleep()", "smartOpen()",
                    "smartClose()", "speak()", "speakSync()"
            )

        val spFuncs = arrayOf(
                "set()", "getInt()", "getString()", "getBoolean()", "getStringSet()"
        )

        val keywordss = arrayOf("break", "else", "new",
                "var", "case", "finally", "return", "void", "catch", "for",
                "switch", "while", "continue", "function", "this", "with", "default",
                "if", "throw", "delete", "in", "try", "do", "instranceof", "typeof")



        //map
        val runtimeMap= hashMapOf(
                Pair("currentApp","当前App信息"),
                Pair("currentActivity","当前Activity"),
                Pair("actionCount","执行队列长度"),
                Pair("currentActionIndex","当前执行位置"),
                Pair("isGlobal()","全局标志"),
                Pair("commandType","指令类型(1打开|-1关闭)"),
                Pair("command","用户命令"),
                Pair("DEBUG","调试标志")
        )
    }
}

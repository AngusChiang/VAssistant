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
                "system",
                "http"
        )
        val runtimeFunctions = arrayOf("currentApp",
                "currentActivity", "actionCount", "currentActionIndex", "isGlobal()",
                "commandType", "command", "DEBUG")

        val httpFunctions = arrayOf("get()","post()","postJson()","getAsPc()")

        val finderFuns = arrayOf(
                "ViewFinder()", "waitFor()", "depths()",
                "containsText()", "matchesText()", "equalsText()",
                "similaryText()", "id()", "desc()",
                "editable()", "scrollable()", "type()", "await()",
                "findFirst()", "find()"
        )
        val utilFuns = arrayOf(
                "toPinyin()", "arr2String()", "print()", "matches()", "matchValues()"
        )
        val globalFuns = arrayOf(
                "toast()", "back()", "home()", "powerDialog()", "quickSettings()",
                "recents()", "notificationBar()",
                "setScreenSize()", "swipe()", "click()", "longClick()",
                "gesture()", "scrollDown()", "scrollUp()", "screenShot()"
                , "notifyFailed()",
                "waitForApp()", "waitForId()",
                "waitForDesc()"
        )
        val JS_OPERATORS = charArrayOf('(', ')', '{', '}', ',', ';', '=',
                '+', '-', '/', '*', '&', '!', '|', ':', '[', ']', '<', '>', '?', '~', '%', '^')
        val systemFuncs = arrayOf(
                "openAppDetail(pkg)", "getPkgByWord(s)",
                "openAppByPkg(pkg)", "openAppByWord(s)", "call(p)",
                "openFlashlight()", "closeFlashlight()",
                "getDeviceInfo()", "getAppInfo(s)", "openUrl(url)",
                "mediaPause()", "mediaStart()", "mediaResume()",
                "mediaStop()", "mediaNext()", "mediaPre()", "volumeMute()",
                "volumeUnmute()", "volumeUp()", "volumeDown()",
                "setMusicVolume()", "setAlarmVolume()", "setNotificationVolume()",
                "isMediaPlaying()", "musicMaxVolume",
                "musicCurrentVolume", "vibrate()", "openBluetooth()",
                "closeBluetooth()", "openWifi()",
                "closeWifi()", "openWifiAp()", "closeWifiAp()",
                "isScreenOn()", "getClipText()", "setClipText(s)", "sendEmail(to,subject,content)",
                /* "lockScreen()",*/ "screenShot()",
                "screen2File()", "shareText(text)", "shareImage(imgPah)", "location()",
                "getLocalIpAddress()","getNetAddress()","createAlarm()","createCalendarEvent()"
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
                    "checkService()", "alert()",
                    "singleChoiceDialog()", "waitForVoiceParam()", "waitForText()", "sleep()", "smartOpen()",
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
        val runtimeMap = hashMapOf(
                Pair("currentApp", "当前App信息"),
                Pair("currentActivity", "当前Activity"),
                Pair("actionCount", "执行队列长度"),
                Pair("currentActionIndex", "当前执行位置"),
                Pair("isGlobal()", "全局标志"),
                Pair("commandType", "指令类型(1打开|-1关闭)"),
                Pair("command", "用户命令"),
                Pair("DEBUG", "调试标志")
        )
        val systemFunMap = hashMapOf(
                Pair("openAppDetail(pkg)", "打开app详情页"),
                Pair("getPkgByWord(s)", "从app标记和app列表获取包名"),
                Pair("openAppByPkg(pkg, reset)", "打开指定app,reset:可选,跳转'首页'"),
                Pair("openAppByWord(s)", "从标记和安装列表匹配，打开app"),
                Pair("call(p)", "拨打电话"),
                Pair("openFlashlight()", "打开闪光灯"),
                Pair("closeFlashlight()", "关闭闪光灯"),
                Pair("getDeviceInfo()", "获取设备信息，返回DeviceInfo"),
                Pair("getAppInfo(s)", "根据包名 或 App 名获取app信息"),
                Pair("openUrl(url)", "使用系统打开链接"),
                Pair("mediaPause()", "媒体暂停"),
                Pair("mediaStart()", "媒体开始"),
                Pair("mediaResume()", "媒体继续"),
                Pair("mediaStop()", "媒体停止"),
                Pair("mediaNext()", "下一媒体"),
                Pair("mediaPre()", "上一媒体"),
                Pair("volumeMute()", "当前音量静音"),
                Pair("volumeUnmute()", "当前音量关闭静音"),
                Pair("volumeUp()", "加大当前音量"),
                Pair("volumeDown()", "减少当前音量"),
                Pair("setMusicVolume()", "设置媒体音量"),
                Pair("setAlarmVolume()", "设置闹钟音量"),
                Pair("setNotificationVolume()", "设置通知音量"),
                Pair("isMediaPlaying()", "是否有媒体播放"),
                Pair("musicMaxVolume", "媒体最大音量"),
                Pair("musicCurrentVolume", "当前媒体音量"),
                Pair("vibrate(m)", "震动时长m"),
                Pair("vibrate(arr)", "按指定数组震动"),
                Pair("openBluetooth()", "打开蓝牙"),
                Pair("closeBluetooth()", "关闭蓝牙"),
                Pair("openWifi()", "打开wifi"),
                Pair("closeWifi()", "关闭wifi"),
                Pair("openWifiAp()", "打开热点"),
                Pair("closeWifiAp()", "关闭ap(不稳定)"),
                Pair("isScreenOn()", "屏幕是否开启"),
                Pair("getClipText()", "获取剪切板内容"),
                Pair("setClipText(s)", "设置剪切板内容"),
                Pair("sendEmail(to,subject,content)", "调用系统发送邮件，to:收件人,subject:标题,content:内容"),
//                Pair("lockScreen()", ""),
                Pair("screenShot()", "截屏，返回Bitmap"),
                Pair("screen2File()", "截屏保存至文件,返回File?"),
                Pair("shareText(text)", "分享文本"),
                Pair("shareImage(imgPah)", "分享图片,imgPah:图片路径"),
                Pair("location()", "获取位置信息，Location?"),
                Pair("getLocalIpAddress()", "获取内网ip地址"),
                Pair("getNetAddress()", "获取外网ip地址"),
                Pair("createCalendarEvent()", "创建日历事件,参数title: String, content: String?, " +
                        "beginTime: Long, endTime: Long?, isAlarm: Boolean\n" +
                        "title:标题,content:事件详情,beginTime:开始时间(毫秒数),endTime:结束时间(毫秒数,为空默认十分钟)，isAlarm:是否(响铃?)提醒"),
                Pair("createAlarm()", "创建闹钟，参数(message: String?, day: Int?, hour: Int, minutes: Int, noUi: Boolean)\n" +
                        "message:备注,day:重复周,hour:时,minutes:分,noUi:是否显示闹钟界面")

        )
        val executorMap = hashMapOf(
                Pair("interrupt()", "终止执行"),
                Pair("setScreenSize()", "设置屏幕尺寸值，其他使用到屏幕坐标的都基于此尺寸，默认为本机屏幕实际尺寸"),
                Pair("checkService()", "检查无障碍 返回无障碍是否开启"),
                Pair("alert()", "显示对话框，返回是否继续"),
                Pair("singleChoiceDialog()", "显示单选对话框，返回选择文本，若取消返回空"),
                Pair("waitForVoiceParam()", "等待用户说话，并返回识别结果，识别失败返回空"),
                Pair("waitForApp()", "等待应用出现,参数：(pkg[,activity[,millis]])"),
                Pair("speak(s)", "语音合成（异步）无返回值"),
                Pair("speakSync(s)", "语音合成（同步） 参数:待合成text文本 返回是否成功")
        )
    }
}

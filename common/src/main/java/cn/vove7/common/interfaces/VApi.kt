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
                "commandType", "command", "DEBUG", "focusView", "userInterrupt","notSupport()")

        val httpFunctions = arrayOf("get()", "post()", "postJson()", "getAsPc()")

        val androRuntimeFuncs = arrayOf(
                "hasRoot()", "exec()", "execWithSu()"
        )
        val finderFuns = arrayOf(
                "ViewFinder()", "waitFor()", "depths()",
                "containsText()", "matchesText()", "equalsText()",
                "similaryText()", "id()", "desc()",
                "editable()", "scrollable()", "type()", "await()",
                "findFirst()", "find()", "containsDesc()", "waitHide()"
        )
        val utilFuns = arrayOf(
                "toPinyin()", "arr2String()", "print()", "matches()", "matchValues()",
                "parseDateText()"
        )
        val globalFuns = arrayOf(
                "toast()", "back()", "home()", "powerDialog()", "quickSettings()",
                "recents()", "notificationBar()",
                "setScreenSize()", "swipe()", "click()", "longClick()",
                "gesture()", "scrollDown()", "scrollUp()", "screenShot()"
                , "notifyFailed()",
                "waitForApp()", "waitForId()",
                "waitForDesc()", "requireAccessibility()"
        )
        val JS_OPERATORS = charArrayOf('(', ')', '{', '}', ',', ';', '=',
                '+', '-', '/', '*', '&', '!', '|', ':', '[', ']', '<', '>', '?', '~', '%', '^')
        val systemFuncs = arrayOf(
                "openAppDetail(pkg)", "getPkgByWord(s)",
                "openAppByPkg(pkg)", "openAppByWord(s)", "call(p)",
                "openFlashlight()", "closeFlashlight()",
                "getDeviceInfo()",
                "hasInstall(pkg)",
                "getAppInfo(s)",
                "openUrl(url)",
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
                "getLocalIpAddress()", "getNetAddress()", "createAlarm()", "createCalendarEvent()",
                "startActivity(pkg,fullActivityName)", "screenOn()", "screenOff()", "sendKey()",
                "sendSMS()", "getLaunchIntent()", "getPhoneByName()", "getContactByName()", "isCharging()", "batteryLevel",
                "simCount", "contacts", "saveMarkedContact()", "saveMarkedApp()", "enableNfc()", "disableNfc()"
        )
        val appFunctions = arrayOf(
                "startActivity()", "getSystemService()"
        )
        val viewNodeFunctions =
            arrayOf(
                    "tryClick()", "getCenterPoint()", "centerPoint()",
                    "getChilds()", "childs", "bounds", "getBounds()",
                    "getBoundsInParent()", "boundsInParent", "getParent()",
                    "parent", "click()", "globalClick()", "swipe()", "tryLongClick()",
                    "getChildCount()", "childCount", "longClick()",
                    "doubleClick()", "setText()", "text", "trySetText()",
                    "getText()", "select()", "trySelect()", "focus()", "scrollUp()",
                    "scrollDown()", "setTextWithInitial()", "isClickable", "appendText()"
            )
        val executorFunctions =
            arrayOf("interrupt()", "setScreenSize()",
                    "checkService()", "alert()",
                    "singleChoiceDialog()", "waitForVoiceParam()", "waitForText()", "sleep()", "smartOpen()",
                    "smartClose()", "speak()", "speakSync()", "cancelRecog()"
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
                Pair("DEBUG", "调试标志"),
                Pair("focusView", "当前获得焦点的ViewNode,可能为空"),
                Pair("userInterrupt", "用户终止的标志\n此标志用于在脚本长时间循环操作"),
                Pair("notSupport()", "当指令无法完成请求时，提示用户无法完成该指令")
        )
        val systemFunMap = hashMapOf(
                Pair("simCount", "当前插卡数量"),
                Pair("contacts", "获得联系人数组Array<Pair<>String,String>  数组元素：Pair(contactName,phone)\n提示：Pair类 使用 pair.first和pair.second 获取 里面的值"),
                Pair("saveMarkedContact()", "保存到标记联系人\n参数(name: String, regex: String, phone: String)"),
                Pair("saveMarkedApp()", "保存到标记应用\n参数(name: String, regex: String, pkg: String)"),

                Pair("openAppDetail(pkg)", "打开app详情页"),
                Pair("startActivity(pkg,fullActivityName)", "打开指定app的Activity"),
                Pair("getPkgByWord(s)", "从app标记和app列表获取包名"),
                Pair("openAppByPkg(pkg, reset)", "打开指定app,reset:可选,跳转'首页'"),
                Pair("openAppByWord(s)", "从标记和安装列表匹配，打开app"),
                Pair("getPhoneByName()", "根据姓名查找手机号，搜索范围：标记数据、通讯录\n参数：getPhoneByName(name: String): String?"),
                Pair("getContactByName()", "同getPhoneByName()\n返回值：Pair<String,String[]> first: 匹配的联系人姓名 second: 手机号数组"),
                Pair("call(p)", "拨打电话\n参数：call(s:String [,simId:Int])\ns:纯数字电话(此时phone根据getPhoneByName获取)，simId(可选)为卡号，0:卡1  1:卡2，出错将按默认卡呼出"),
                Pair("openFlashlight()", "打开闪光灯"),
                Pair("closeFlashlight()", "关闭闪光灯"),
                Pair("getDeviceInfo()", "获取设备信息，返回DeviceInfo"),
                Pair("hasInstall(pkg)", "是否安装此应用"),
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
                Pair("createAlarm()", "创建闹钟\n1.创建一次性闹钟参数(hour: Int, minutes: Int)" +
                        "\n2.参数(message: String?, days: Array<Int>?, hour: Int, minutes: Int, noUi: Boolean)\n" +
                        "message:备注,days:重复周,hour:时,minutes:分,noUi:是否显示闹钟界面\n" +
                        "注:参数days 周日 - 周六 对应 1 - 7"),
                Pair("screenOn()", "熄屏状态下,唤醒屏幕"),
                Pair("screenOff()", "熄屏,需要root"),
                Pair("sendKey()", "模拟按键,需要root,参数keyCode:Int\n" +
                        "0 -->  \"KEYCODE_UNKNOWN\"\n" +
                        "1 -->  \"KEYCODE_MENU\"\n" +
                        "2 -->  \"KEYCODE_SOFT_RIGHT\"\n" +
                        "3 -->  \"KEYCODE_HOME\"\n" +
                        "4 -->  \"KEYCODE_BACK\"\n" +
                        "5 -->  \"KEYCODE_CALL\" \n" +
                        "6 -->  \"KEYCODE_ENDCALL\" \n" +
                        "7 -->  \"KEYCODE_0\" \n" +
                        "8 -->  \"KEYCODE_1\" \n" +
                        "9 -->  \"KEYCODE_2\" \n" +
                        "10 -->  \"KEYCODE_3\"\n" +
                        "11 -->  \"KEYCODE_4\" \n" +
                        "12 -->  \"KEYCODE_5\" \n" +
                        "13 -->  \"KEYCODE_6\" \n" +
                        "14 -->  \"KEYCODE_7\" \n" +
                        "15 -->  \"KEYCODE_8\" \n" +
                        "16 -->  \"KEYCODE_9\" \n" +
                        "17 -->  \"KEYCODE_STAR\" \n" +
                        "18 -->  \"KEYCODE_POUND\" \n" +
                        "19 -->  \"KEYCODE_DPAD_UP\" \n" +
                        "20 -->  \"KEYCODE_DPAD_DOWN\" \n" +
                        "21 -->  \"KEYCODE_DPAD_LEFT\" \n" +
                        "22 -->  \"KEYCODE_DPAD_RIGHT\"\n" +
                        "23 -->  \"KEYCODE_DPAD_CENTER\"\n" +
                        "24 -->  \"KEYCODE_VOLUME_UP\" \n" +
                        "25 -->  \"KEYCODE_VOLUME_DOWN\" \n" +
                        "26 -->  \"KEYCODE_POWER\" \n" +
                        "27 -->  \"KEYCODE_CAMERA\" \n" +
                        "28 -->  \"KEYCODE_CLEAR\" \n" +
                        "29 -->  \"KEYCODE_A\" \n" +
                        "30 -->  \"KEYCODE_B\" \n" +
                        "31 -->  \"KEYCODE_C\" \n" +
                        "32 -->  \"KEYCODE_D\" \n" +
                        "33 -->  \"KEYCODE_E\" \n" +
                        "34 -->  \"KEYCODE_F\" \n" +
                        "35 -->  \"KEYCODE_G\" \n" +
                        "36 -->  \"KEYCODE_H\" \n" +
                        "37 -->  \"KEYCODE_I\" \n" +
                        "38 -->  \"KEYCODE_J\" \n" +
                        "39 -->  \"KEYCODE_K\" \n" +
                        "40 -->  \"KEYCODE_L\" \n" +
                        "41 -->  \"KEYCODE_M\"\n" +
                        "42 -->  \"KEYCODE_N\" \n" +
                        "43 -->  \"KEYCODE_O\" \n" +
                        "44 -->  \"KEYCODE_P\" \n" +
                        "45 -->  \"KEYCODE_Q\" \n" +
                        "46 -->  \"KEYCODE_R\" \n" +
                        "47 -->  \"KEYCODE_S\" \n" +
                        "48 -->  \"KEYCODE_T\" \n" +
                        "49 -->  \"KEYCODE_U\" \n" +
                        "50 -->  \"KEYCODE_V\" \n" +
                        "51 -->  \"KEYCODE_W\" \n" +
                        "52 -->  \"KEYCODE_X\"\n" +
                        "53 -->  \"KEYCODE_Y\" \n" +
                        "54 -->  \"KEYCODE_Z\" \n" +
                        "55 -->  \"KEYCODE_COMMA\" \n" +
                        "56 -->  \"KEYCODE_PERIOD\"\n" +
                        "57 -->  \"KEYCODE_ALT_LEFT\" \n" +
                        "58 -->  \"KEYCODE_ALT_RIGHT\" \n" +
                        "59 -->  \"KEYCODE_SHIFT_LEFT\" \n" +
                        "60 -->  \"KEYCODE_SHIFT_RIGHT\" \n" +
                        "61 -->  \"KEYCODE_TAB\" \n" +
                        "62 -->  \"KEYCODE_SPACE\" \n" +
                        "63 -->  \"KEYCODE_SYM\" \n" +
                        "64 -->  \"KEYCODE_EXPLORER\" \n" +
                        "65 -->  \"KEYCODE_ENVELOPE\" \n" +
                        "66 -->  \"KEYCODE_ENTER\" \n" +
                        "67 -->  \"KEYCODE_DEL\" \n" +
                        "68 -->  \"KEYCODE_GRAVE\" \n" +
                        "69 -->  \"KEYCODE_MINUS\" \n" +
                        "70 -->  \"KEYCODE_EQUALS\" \n" +
                        "71 -->  \"KEYCODE_LEFT_BRACKET\" \n" +
                        "72 -->  \"KEYCODE_RIGHT_BRACKET\" \n" +
                        "73 -->  \"KEYCODE_BACKSLASH\"\n" +
                        "74 -->  \"KEYCODE_SEMICOLON\" \n" +
                        "75 -->  \"KEYCODE_APOSTROPHE\"\n" +
                        "76 -->  \"KEYCODE_SLASH\" \n" +
                        "77 -->  \"KEYCODE_AT\" \n" +
                        "78 -->  \"KEYCODE_NUM\" \n" +
                        "79 -->  \"KEYCODE_HEADSETHOOK\" \n" +
                        "80 -->  \"KEYCODE_FOCUS\"\n" +
                        "81 -->  \"KEYCODE_PLUS\"\n" +
                        "82 -->  \"KEYCODE_MENU\"\n" +
                        "83 -->  \"KEYCODE_NOTIFICATION\"\n" +
                        "84 -->  \"KEYCODE_SEARCH\" \n" +
                        "85 -->  \"TAG_LAST_KEYCODE\"\n " +
                        "from https://blog.csdn.net/chen825919148/article/details/18732041"),
                Pair("sendSMS()", "发送短信\n参数：(phone: String, content: String)"),
                Pair("getLaunchIntent(pkg)", "根据pkg(包名)获取App的启动Intent,类似桌面启动App\n参数：(pkg:String)"),
                Pair("batteryLevel", "返回当前电量，范围0-100(Int)"),
                Pair("isCharging()", "返回是否在充电"),
                Pair("enableNfc()", "开启nfc，由于系统限制，只会跳转至nfc设置界面"),
                Pair("disableNfc()", "关闭nfc，由于系统限制，只会跳转至nfc设置界面")
        )
        val executorMap = hashMapOf(
                Pair("interrupt()", "终止执行"),
                Pair("setScreenSize()", "setScreenSize(width:Int,height:Int)\n设置此脚本相对屏幕尺寸值，其脚本中使用到屏幕坐标的函数都基于此尺寸，默认为本机屏幕实际尺寸"),
                Pair("checkService()", "检查无障碍 返回无障碍是否开启"),
                Pair("alert()", "显示对话框，返回是否继续"),
                Pair("singleChoiceDialog()", "显示单选对话框，返回选择的索引，若取消返回空\n" +
                        "(1)参数：(title:String, items:Array<String>)\n" +
                        "(2)参数：(title:String, items:Array<Pair<String,String>>)"),
                Pair("waitForVoiceParam()", "waitForVoiceParam():String\n等待用户说话，并返回识别结果，识别失败返回空"),
                Pair("waitForApp()", "等待应用出现,参数：(pkg[,activity[,millis]]) 返回Boolean 出现：true 等待超时：false"),
                Pair("speak()", "speak(text)\n语音合成（异步）无返回值"),
                Pair("speakSync()", "语音合成（同步）\nspeakSync(text)参数:待合成text文本 返回是否成功"),
                Pair("cancelRecog()", "取消语音识别，可终止长语音")

        )
    }
}

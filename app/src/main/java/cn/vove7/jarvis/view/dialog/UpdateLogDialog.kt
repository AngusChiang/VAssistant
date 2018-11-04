package cn.vove7.jarvis.view.dialog

import android.content.Context
import cn.vove7.common.view.editor.MultiSpan
import com.afollestad.materialdialogs.callbacks.onDismiss

/**
 * # UpdateLogDialog
 *
 * @author Administrator
 * 2018/10/28
 */
class UpdateLogDialog(context: Context, onDismiss: (() -> Unit)? = null) {
    init {
        val d = ProgressTextDialog(context, "更新日志", false)
        d.dialog.onDismiss {
            onDismiss?.invoke()
        }
        logs.forEach {
            d.appendln()
            d.appendln(MultiSpan(context, it.first, fontSize = 20, bold = true).spanStr)
//            d.appendln()
            d.appendln(it.second)
        }
        d.finish()
    }

    private val logs: List<Pair<String, String>>
        get() = listOf(
                Pair("1.2.7", "更新方式换为增量更新\n" +
                        "修改后台音乐逻辑\n" +
                        "加入结束词 (实验室)\n" +
                        "优化首次进入app速度\n" +
                        "打开自动开启无障碍，返回首页即可自动开启\n" +
                        "添加基础引导"),
                Pair("1.2.6", "加入指令优先级设置   (指令详情菜单) ps: 指令列表按优先级排列\n" +
                        "加入连续对话 (实验室)\n" +
                        "修复若干问题"),
                Pair("1.2.5", "修复图灵机器人调用问题\n" +
                        "修复解析日期‘x小时’后错误\n" +
                        "其他问题"),
                Pair("1.2.4", "加入对话系统：图灵机器人 (/实验室)"),
                Pair("1.2.3", "修复部分机型识别后崩溃"),
                Pair("1.2.2", "加入App自动检查更新\n" +
                        "加入对话系统\n" +
                        "加入语音唤醒自动休眠后亮屏后自动开启\n" +
                        "加入在播放语音合成时，长按音量下可停止播放"),
                Pair("1.2.1_0", "fix 7.1.1及以下设备识别结束崩溃问题\n" +
                        "移除对[5.0-6.0)的支持\n" +
                        "fix 部分设备截屏崩溃问题"),
                Pair("1.2.1", "在[文字提取]加入分词功能\n" +
                        "离线命令词，识别联系人，和应用更准确\n" +
                        "支持阿拉伯数字与中文模糊匹配\n" +
                        "加入 关于/更新日志"),
                Pair("1.2.0", "fix 语音唤醒响应词结束后，播放音乐\n" +
                        "添加非充电状态下，语音唤醒自动休眠\n" +
                        "支持有线耳机耳麦唤醒和语音识别\n" +
                        "支持多唤醒词，支持的唤醒词(唤醒即执行)： 播放,停止,暂停,上一首,下一首,打开手电筒,关闭手电筒,截屏分享,文字提取)\n" +
                        "(自定义过唤醒词的，需要手动恢复默认，如需自定义，可以将上面唤醒词和你的自定义在获取唤醒词一起导出）\n" +
                        "添加耳机中键唤醒\n" +
                        "添加代码编辑器快速插入 require 'accessibility'\n" +
                        "tips: 蓝牙快捷键(若支持)可触发默认语音助手"),
                Pair("1.1.8", "代码编辑器添加插入声明无障碍模式\n" +
                        "添加系统Api screenOn screenOff sendKey\n" +
                        "修复 在speakSync 音量静音时，显示toast后 回调失败问题\n" +
                        "其他问题"),
                Pair("1.1.7_5", "fix 指令设置初始化失败\n" +
                        "新增 运行时 状态栏通知显示命令"),
                Pair("1.1.7_3", "\n" +
                        "添加日期解析 'x天后'\n" +
                        "fix some bug"),
                Pair("1.1.7_2",
                        "修复指令同步失败\n" +
                                "修复打开标记功能\n" +
                                "自启无障碍同时保持其他服务\n" +
                                "若干其他其他问题"),
                Pair("1.1.7",
                        "支持指令多参数\n" +
                                "添加自动开启无障碍服务(设置/其他 需手动开启)\n" +
                                "添加指令 创建闹钟和日历事件（均此版本可用）\n" +
                                "添加api androRuntime (终端/Root命令相关)\n" +
                                "添加api parseDateText (解析日期文本 ;Fx其他页)\n" +
                                "更新时数据显示更新进度和内容\n" +
                                "解决误认输入法为当前activity")
                ,
                Pair("1.1.6",
                        "增加创建日历事件、闹钟api (系统函数)\n" +
                                "修复语音合成通道设置问题及some bug")
                ,
                Pair("1.1.5", "fix 唤醒响应词，后台音乐未播放时，识别结束后音乐响起问题\n" +
                        "完成备份功能\n" +
                        "自定义唤醒时响应词\n" +
                        "加入语音合成选择音频输出通道\n" +
                        "移除对x86的支持\n" +
                        "修复若干问题")
                ,
                Pair("1.1.4_1",
                        "修复部分机型无障碍错误崩溃\n" +
                                "修复部分机型闪光灯打开失败\n" +
                                "修复其他若干问题")
                ,
                Pair("1.1.4",
                        "加入数据自动更新和一键更新(高级中)\n" +
                                "加入网络api（示例参考js编辑器中的'天气.js'）\n" +
                                "加入屏幕文字提取(同步最新数据后，使用文字提取 )\n" +
                                "加入指令详情代码复制")
                , Pair("1.1.0",
                "完成 代码编辑器\n" +
                        "自定义唤醒词\n" +
                        "修复截屏后投屏图标不消失\n" +
                        "加入云解析\n" +
                        "增加本地解析失败逻辑")
                , Pair("1.0.4",
                "- 加入充电时自动开启语音唤醒\n" +
                        "- 添加shortcut功能 可添加 唤醒/指令 桌面快捷方式\n" +
                        "- 可设置解析失败，自动执行smart打开操作，详情见 高级/命令解析\n")
        )
}
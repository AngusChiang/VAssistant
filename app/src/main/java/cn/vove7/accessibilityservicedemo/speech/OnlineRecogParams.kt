package cn.vove7.accessibilityservicedemo.speech

import android.content.Context
import android.content.SharedPreferences
import cn.vove7.accessibilityservicedemo.speech.util.PidBuilder
import com.baidu.speech.asr.SpeechConstant
import java.util.*

class OnlineRecogParams(context: Context) : CommonRecogParams(context) {

    init {
        stringParams.addAll(Arrays.asList(
                "_language", // 用于生成PID参数
                "_model" // 用于生成PID参数
        ))
        intParams.addAll(Arrays.asList(SpeechConstant.PROP))
        boolParams.addAll(Arrays.asList(SpeechConstant.DISABLE_PUNCTUATION))
    }


    override fun fetch(sp: SharedPreferences): Map<String, Any> {
        var map: MutableMap<String, Any> = super.fetch(sp) as MutableMap<String, Any>
        val builder = PidBuilder()
        map = builder.addPidInfo(map) as MutableMap<String, Any> // 生成PID， PID 网络在线有效
        return map
    }

    companion object {
    }

}
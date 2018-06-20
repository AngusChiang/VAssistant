package cn.vove7.accessibilityservicedemo.speech.message

import android.os.Bundle
import android.os.Message
import java.io.Serializable

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
object SpeechMessage {
    fun buildMessage(what: Int, msg: String): Message {
        val message = Message()
        message.what = what
        val data = Bundle()
        data.putString("msg", msg)
        message.data = data
        return message
    }

    fun buildMessage(what: Int, obj: Serializable): Message {
        val message = Message()
        message.what = what
        val data = Bundle()
        data.putSerializable("data", obj)
        message.data = data
        return message
    }
}
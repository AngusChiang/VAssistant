package cn.vove7.common.bridges

import android.content.Intent
import android.graphics.Bitmap
import android.os.Looper
import cn.vove7.common.MessageException
import cn.vove7.common.annotation.ScriptApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.tasker.TaskerIntent
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import top.zibin.luban.Luban
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap


/**
 * # UtilBridge
 *
 * @author Administrator
 * 2018/10/6
 */
object UtilBridge {

    fun compressImage(file: String): File = compressImage(File(file))

    /**
     * 压缩图片，使用Luban
     * @param file File
     * @return File
     */
    fun compressImage(file: File): File {
        return Luban.with(GlobalApp.APP).load(file).ignoreBy(100)
                .setTargetDir(file.parent)
                .get()[0] ?: file
    }

    fun bitmap2File(bitmap: Bitmap, fullPath: String): File? {
        return try {
            File(fullPath).apply {
                if (!parentFile.exists())
                    parentFile.mkdirs()
                FileOutputStream(this).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        } catch (se: SecurityException) {
            GlobalApp.toastError("无存储权限")
            null
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalLog.err("bitmap2File 保存到失败")
            null
        }
    }

    /**
     * json解析为Map
     * @param json String?
     * @return Map<String, Any?>?
     */
    fun parseJson(json: String?): Map<String, Any?>? {
        json ?: return null
        return toMap(Gson().fromJson(json, JsonObject::class.java))
    }

    /**
     * 将JSONObjec对象转换成Map-List集合
     * @param json
     * @return
     */
    private fun toMap(json: JsonObject): Map<String, Any> {
        val map = HashMap<String, Any>()
        val entrySet = json.entrySet()
        val iter = entrySet.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            val key = entry.key
            val value = entry.value
            map[key as String] = when (value) {
                is JsonArray -> toList(value)
                is JsonObject -> toMap(value)
                else -> value
            }
        }
        return map
    }

    /**
     * 将JSONArray对象转换成List集合
     * @param json
     * @return
     */
    private fun toList(json: JsonArray): List<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until json.size()) {
            val value = json.get(i)
            list.add(when (value) {
                is JsonArray -> toList(value)
                is JsonObject -> toMap(value)
                else -> value
            })
        }
        return list
    }

    @JvmStatic
    fun runTask(name: String) {
        GlobalLog.log("exec task: $name")
        val status = TaskerIntent.testStatus(GlobalApp.APP)
        if (status == TaskerIntent.Status.OK) {
            val i = TaskerIntent(name)
            GlobalApp.APP.sendBroadcast(i)
            GlobalLog.log("tasker[$name] success")
        } else {
            val s = TaskerIntent.statusToString(status)
            when (status) {
                TaskerIntent.Status.AccessBlocked -> {
                    GlobalApp.toastError("请进入Tasker [首选项/杂项] 开启[允许外部访问]", 1)
                }
                else -> {
                    GlobalApp.toastError(s)
                }
            }
            GlobalLog.err("tasker[$name] $s")
        }
    }

    /**
     * 执行App内指令
     * 用于全局调用App内指令
     * @param pkg String
     * @param cmd String
     */
    @ScriptApi
    @JvmStatic
    @JvmOverloads
    fun runAppCommand(pkg: String, cmd: String, argMap: Map<String, Any?>? = null): Boolean {
        return ServiceBridge.instance.runAppCommand(pkg, cmd, argMap)
    }

    /**
     * 抛出异常
     * @param msg String?
     */
    @ScriptApi
    @JvmStatic
    fun `throw`(msg: String?) {
        throw MessageException(msg)
    }

    @JvmStatic
    private var qrBox: ResultBox<String?>? = null

    //非主线程调用
    fun scanCode(): String? = try {
        if (Looper.myLooper() == Looper.getMainLooper())
            `throw`("请在非主线程调用")
        val intent = Intent(GlobalApp.APP.packageName + ".qr")
        intent.putExtra("callback", QrCallback())
        val box = ResultBox<String?>()
        qrBox = box
        runOnUi {
            GlobalApp.APP.startActivity(intent)
        }
        box.blockedGet(false)
    } finally {
        qrBox = null
    }

    class QrCallback : Serializable {
        fun onResult(result: String?) {
            qrBox?.setAndNotify(result)
            qrBox = null
        }
    }
}
package cn.vove7.common.utils.tasker

import android.app.Application
import android.net.Uri

/**
 * # TaskerHelper
 *
 * Created on 2020/1/13
 * @author Vove
 */
class TaskerHelper {
    companion object {

        fun taskList(): List<TaskInfo>? {
            try {
                val list by lazy { mutableListOf<TaskInfo>() }
                val app = Class.forName("android.app.ActivityThread").let {
                    it.getMethod("currentApplication").invoke(null) as Application
                }
                app.contentResolver.query(
                    Uri.parse("content://net.dinglisch.android.tasker/tasks"),
                    null, null, null, null
                )?.use { c ->
                    val nameCol: Int = c.getColumnIndex("name")
                    val projNameCol: Int = c.getColumnIndex("project_name")
                    while (c.moveToNext()) {
                        list += c.getString(projNameCol).toString() to c.getString(nameCol)
                    }
                }
                return list
            } catch (e: Throwable) {
                e.printStackTrace()
                return null
            }
        }

    }
}
typealias TaskInfo = Pair<String, String>

val TaskInfo.name get() = second
val TaskInfo.projectName get() = second
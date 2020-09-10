package cn.vove7.jarvis.activities

import android.view.View
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.format
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.databinding.DialogNewTaskBinding
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.tools.timedtask.*
import cn.vove7.jarvis.tools.timedtask.TimedTask.Companion.TYPE_COMMAND
import cn.vove7.jarvis.tools.timedtask.TimedTask.Companion.TYPE_SCRIPT_JS
import cn.vove7.jarvis.tools.timedtask.TimedTask.Companion.TYPE_SCRIPT_LUA
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.datetime.timePicker
import java.util.*

/**
 * # TimedTaskManagerActivity
 *
 * @author Vove
 * 2020/9/10
 */
class TimedTaskManagerActivity : OneFragmentActivity() {

    override var fragments: Array<Fragment> = arrayOf(ListFragment())

    class ListFragment : SimpleListFragment<TimedTask>() {
        override fun onLoadData(pageIndex: Int) {
            notifyLoadSuccess(TimedTaskManager.getTasks(), true)
        }

        override var floatClickListener: View.OnClickListener? = View.OnClickListener { v ->
            editTask()
        }
        override val itemCheckable: Boolean = true

        override fun unification(data: TimedTask): ListViewModel<TimedTask>? {
            return ListViewModel(
                    data.name,
                    if (data.enabled) "已启用；下次执行时间：${Date(data.nextTime()).format("MM-dd HH:mm")}"
                    else "未启用",
                    null, data, checked = data.enabled
            )
        }

        override val itemClickListener: SimpleListAdapter.OnItemClickListener<TimedTask>? = object : SimpleListAdapter.OnItemClickListener<TimedTask> {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<TimedTask>) {
                showTaskDetail(item.extra)
            }

            override fun onItemCheckedStatusChanged(
                    holder: SimpleListAdapter.VHolder?,
                    item: ListViewModel<TimedTask>,
                    isChecked: Boolean
            ) {
                val task = item.extra
                if (isChecked && task.nextTime() < System.currentTimeMillis()) {
                    GlobalApp.toastError("任务时间无效，请重新设置")
                    refresh()
                    return
                }
                if (isChecked) {
                    task.enabled = true
                    GlobalApp.toastInfo("任务[${task.name}]启动")
                    task.sendPendingIntent()
                } else if (task.enabled) {
                    task.enabled = false
                    GlobalApp.toastInfo("任务[${task.name}]关闭")
                    task.cancel()
                }
                refresh()
            }
        }

        private fun showTaskDetail(task: TimedTask) {
            MaterialDialog(requireActivity(), BottomSheet()).show {
                title(text = task.name)
                message(text = """
                    定时类型：${
                    when (task) {
                        is OnceTimedTask -> "单次任务 ${Date(task.timeInMillis).format()}"
                        is EveryDayTimedTask -> "每天 ${task.time.timeString()}"
                        is IntervalTimedTask -> "间隔 ${task.intervalMinutes} 分钟"
                        else -> ""
                    }
                }
                    ${task.execBody}
                """.trimIndent())
                positiveButton(text = "编辑") {
                    editTask(task)
                }
                negativeButton(text = "删除") {
                    confirmDelete(task)
                }
            }
        }

        private fun confirmDelete(task: TimedTask) {
            MaterialDialog(requireActivity()).show {
                title(text = "确认删除 ${task.name}?")
                positiveButton {
                    TimedTaskManager.removeTask(task)
                    TimedTaskManager.persistence()
                    refresh()
                }
                negativeButton()
            }
        }

        private fun editTask(task: TimedTask? = null) {
            val binding = DialogNewTaskBinding.inflate(layoutInflater)

            var dayTime: HourMinute? = null
            var onceTime: Long? = null
            var intervalMinute: Int? = null
            fun refreshTimeText() {
                binding.selectTime.text = when (binding.timedTypeRadio.checkedRadioButtonId) {
                    R.id.timed_type_once -> {
                        onceTime?.let {
                            Date(it).format()
                        } ?: "选择时间"
                    }
                    R.id.timed_type_day -> {
                        dayTime?.let {
                            "每天 %02d:%02d".format(it.hour, it.minute)
                        } ?: "选择时间"
                    }
                    R.id.timed_type_interval -> {
                        intervalMinute?.let { value ->
                            val hour = value / 60
                            val minute = value % 60
                            val intervalText = "${if (hour != 0) "${hour}小时" else ""}${if (minute != 0) "${minute}分钟" else ""}"
                            "间隔：$intervalText"
                        } ?: "选择时间"
                    }
                    else -> "选择时间"
                }
            }
            MaterialDialog(requireActivity(), BottomSheet()).show {
                title(text = if (task == null) "新建定时任务" else "编辑任务")
                cancelable(false)
                customView(view = binding.root)
                task?.apply {
                    binding.taskNameText.setText(name)
                    binding.timedTypeRadio.check(when (this) {
                        is OnceTimedTask -> {
                            onceTime = this.timeInMillis
                            R.id.timed_type_once
                        }
                        is EveryDayTimedTask -> {
                            dayTime = this.time
                            R.id.timed_type_day
                        }
                        is IntervalTimedTask -> {
                            intervalMinute = this.intervalMinutes
                            R.id.timed_type_interval
                        }
                        else -> R.id.timed_type_once
                    })
                    refreshTimeText()
                    binding.execTypeRadio.check(when (this.execType) {
                        TYPE_COMMAND -> R.id.exec_type_cmd
                        TYPE_SCRIPT_LUA -> R.id.exec_type_lua
                        TYPE_SCRIPT_JS -> R.id.exec_type_js
                        else -> R.id.exec_type_cmd
                    })
                    binding.execBodyText.setText(execBody)
                }

                binding.timedTypeRadio.setOnCheckedChangeListener { _, _ -> refreshTimeText() }
                binding.selectTime.setOnClickListener {
                    when (binding.timedTypeRadio.checkedRadioButtonId) {
                        R.id.timed_type_once -> {
                            pickDateTime {
                                onceTime = it.timeInMillis
                                refreshTimeText()
                            }
                        }
                        R.id.timed_type_day -> {
                            pickHourMinute {
                                dayTime = it
                                refreshTimeText()
                            }
                        }
                        R.id.timed_type_interval -> {
                            pickInterval(intervalMinute ?: 5) {
                                intervalMinute = it
                                refreshTimeText()
                            }
                        }
                    }
                }
                noAutoDismiss()
                positiveButton(text = "保存") {
                    val name = binding.taskNameText.text.toString()
                    if (name.isBlank()) {
                        binding.taskNameText.error = "不可空"
                        return@positiveButton
                    }
                    try {
                        val execType = when (binding.execTypeRadio.checkedRadioButtonId) {
                            R.id.exec_type_cmd -> TYPE_COMMAND
                            R.id.exec_type_lua -> TYPE_SCRIPT_LUA
                            R.id.exec_type_js -> TYPE_SCRIPT_JS
                            else -> throw Exception("请选择动作类型")
                        }
                        val execBody = binding.execBodyText.text.toString()
                        val newTask = when (binding.timedTypeRadio.checkedRadioButtonId) {
                            R.id.timed_type_once -> {
                                OnceTimedTask(
                                        name, false, execType, execBody,
                                        onceTime ?: throw Exception("请选择时间")
                                )
                            }
                            R.id.timed_type_day -> {
                                EveryDayTimedTask(
                                        name, false, execType, execBody,
                                        dayTime ?: throw Exception("请选择时间")
                                )
                            }
                            R.id.timed_type_interval -> {
                                IntervalTimedTask(
                                        name, false, execType, execBody,
                                        intervalMinute ?: throw Exception("请选择时间")
                                )
                            }
                            else -> throw Exception("请选择任务类型")
                        }
                        if (task != null) {
                            task.cancel()
                            TimedTaskManager.removeTask(task)
                        }
                        TimedTaskManager.addTask(newTask)
                        TimedTaskManager.persistence()
                        refresh()
                        dismiss()
                    } catch (e: Throwable) {
                        GlobalApp.toastError(e.message)
                    }
                }
                negativeButton {
                    it.dismiss()
                }
            }
        }

        private fun pickInterval(init: Int, function: (Int) -> Unit) {
            val dialog = MaterialDialog(requireActivity()).show {
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR_OF_DAY, 1)
                c.set(Calendar.MINUTE, 0)
                var t = 1 to 0
                timePicker(currentTime = c) { _, datetime ->
                    function(t.first * 60 + t.second)
                }
                findViewById<TimePicker>(R.id.datetimeTimePicker).setOnTimeChangedListener { view, hourOfDay, minute ->
                    t = hourOfDay to minute
                    val intervalText = "${if (hourOfDay != 0) "${hourOfDay}小时" else ""}${if (minute != 0) "${minute}分钟" else ""}"
                    title(text = "执行间隔: $intervalText")
                }
                title(text = "执行间隔")
                positiveButton()
                negativeButton()
            }
        }

        private fun pickDateTime(function: (Calendar) -> Unit) {
            MaterialDialog(requireActivity()).show {
                title(text = "选择时间")
                val c = Calendar.getInstance()
                dateTimePicker(minDateTime = c, currentDateTime = c, show24HoursView = true) { _, datetime ->
                    datetime.set(Calendar.SECOND, 0)
                    function(datetime)
                }
            }
        }

        private fun pickHourMinute(cb: (HourMinute) -> Unit) {
            MaterialDialog(requireActivity()).show {
                title(text = "选择时间")
                timePicker { _, datetime ->
                    cb(datetime.get(Calendar.HOUR_OF_DAY) to datetime.get(Calendar.MINUTE))
                }
            }
        }
    }


}
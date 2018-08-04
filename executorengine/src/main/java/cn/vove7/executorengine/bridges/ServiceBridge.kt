package cn.vove7.executorengine.bridges

import android.graphics.drawable.Drawable
import android.widget.Toast
import cn.vove7.datamanager.parse.model.Action
import java.io.Serializable

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
interface ServiceBridge {
    /**
     * 中途获取未知参数
     * @param action 执行动作
     */
    fun getVoiceParam(action: Action)

    /**
     * 选择对话框
     */
    fun showChoiceDialog(event: ShowDialogEvent)

    /**
     * 取认对话框
     */
    fun showAlert(r: ShowAlertEvent)

    fun toast(msg: String, showMillis: Int=Toast.LENGTH_SHORT)

}

class ShowAlertEvent(
        val msg: String,
        val action: Action
)

class ShowDialogEvent(
        val whichDialog: Int,
        val action: Action,
        val askTitle: String,
        val choiceDataSet: List<ChoiceData>
) {
    companion object {
        const val WHICH_SINGLE = 330
        const val WHICH_MULTI = 660
    }
}

open class ChoiceData(
        val title: String,
        val iconDrawable: Drawable? = null,
        val subtitle: String? = null,
        val originalData: Any
) : Serializable, Comparable<ChoiceData> {
    override fun compareTo(other: ChoiceData): Int {
        return when {
            title > other.title -> 1
            title < other.title -> -1
            else -> 0
        }
    }
}

/**
 * 生成选择列表
 */
interface GenChoiceData {
    /**
     * 获取标识列表
     */
    fun getChoiceData(): List<ChoiceData>
}


package cn.vove7.common.bridges

import android.graphics.drawable.Drawable
import cn.vove7.common.executor.CExecutorI
import cn.vove7.datamanager.parse.model.Action
import java.io.Serializable
import java.text.Collator


/**
 *
 * 保留Java调用
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

    fun speak(text: String)
    fun speakSync(text: String)
}

class ShowAlertEvent(
        val title: String,
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

    companion object {
        var CollChina = Collator.getInstance(java.util.Locale.CHINA)
    }

    override fun compareTo(other: ChoiceData): Int {
        var tt1 = CollChina.getCollationKey(this.title)
        var tt2 = CollChina.getCollationKey(other.title)
        val c = CollChina.compare(tt1.sourceString, tt2.sourceString)
        return if (c == 0) {
            tt1 = CollChina.getCollationKey(this.subtitle)
            tt2 = CollChina.getCollationKey(other.subtitle)
            CollChina.compare(tt1.sourceString, tt2.sourceString)
        } else c
    }

    override fun toString(): String {
        return "ChoiceData(title='$title', subtitle=$subtitle, originalData=$originalData)"
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


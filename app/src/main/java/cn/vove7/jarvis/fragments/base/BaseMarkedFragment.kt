package cn.vove7.jarvis.fragments.base

import android.annotation.SuppressLint
import com.google.android.material.textfield.TextInputLayout
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.builder.message
import cn.vove7.bottomdialog.builder.title
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.jarvis.tools.DialogUtil
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # BaseMarkedFragment
 *
 * @author Administrator
 * 2018/9/16
 */
abstract class BaseMarkedFragment : SimpleListFragment<MarkedData>(), OnSyncMarked {

    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        showEditDialog()
    }

    abstract var markedType: String
    abstract val keyHint: Int
    abstract val valueHint: Int
    open fun onSelect() {}

    fun onEdit(data: MarkedData) {
        showEditDialog(data)
    }

    private val editDialog: MaterialDialog by lazy {
        val s = MaterialDialog(context!!)
                .customView(R.layout.dialig_edit_marked_data, scrollable = true)
                .cancelable(false)
                .noAutoDismiss()
                .negativeButton { it.dismiss() }
                .positiveButton {
                    val key = keyText.editText!!.text.toString().trim()
                    val reg = regexText.editText!!.text.toString().trim()
                    val phone = valueText.editText!!.text.toString().trim()
                    if (key == "") {
                        keyText.error = getString(R.string.text_not_empty)
                        return@positiveButton
                    }
                    if (reg == "") {
                        regexText.error = getString(R.string.text_not_empty)
                        return@positiveButton
                    }
                    if (phone == "") {
                        valueText.error = getString(R.string.text_not_empty)
                        return@positiveButton
                    }
                    try {
                        if (editData != null) {
                            editData!!.key = key
                            editData!!.value = phone
                            editData!!.regStr = reg
                            DAO.daoSession.markedDataDao.update(editData)
                        } else {
                            val markedData = MarkedData(key, markedType,
                                    reg, phone, DataFrom.FROM_USER)
                            DAO.daoSession.markedDataDao.insert(markedData)
                        }
                        DAO.clear()
                        GlobalApp.toastSuccess(R.string.text_complete)
                        refresh()
                        it.dismiss()
                    } catch (e: Exception) {
                        GlobalLog.err(e.message)
                        GlobalApp.toastError(R.string.text_error_occurred)
                    }
                }
        s
    }

    private val keyText: TextInputLayout by lazy {
        val s = editDialog.findViewById<TextInputLayout>(R.id.key_text)
        s.hint = getString(keyHint)
        s
    }

    fun setValue(s: String) {
        valueText.editText?.setText(s)
    }

    fun setKey(s: String) {
        keyText.editText?.setText(s)
    }

    fun getKey(): CharSequence {
        return keyText.editText?.text!!
    }

    private val regexText: TextInputLayout by lazy {
        editDialog.findViewById<TextInputLayout>(R.id.regex_text)
    }

    private val scriptLang: Spinner by lazy {
        editDialog.findViewById<Spinner>(R.id.script_lang).also {
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> markedType = MarkedData.MARKED_TYPE_SCRIPT_LUA
                        1 -> markedType = MarkedData.MARKED_TYPE_SCRIPT_JS
                    }
                    Vog.d(markedType)
                }
            }
        }
    }
    private val valueText: TextInputLayout by lazy {
        val s = editDialog.findViewById<TextInputLayout>(R.id.value_text)
        s.hint = getString(valueHint)
        s
    }

    private val selectButton: Button by lazy { editDialog.findViewById<Button>(R.id.sel_btn) }
    var editData: MarkedData? = null

    open val showSel = true
    /**
     * 编辑or新建
     * @param data MarkedData?
     */
    @SuppressLint("CheckResult")
    fun showEditDialog(data: MarkedData? = null) {
        if (!AppConfig.checkLogin()) {
            return
        }
        editData = data
        editDialog.title(
                if (editData == null) R.string.text_new
                else R.string.text_edit
        )
        if (showSel)
            selectButton.setOnClickListener {
                onSelect()
                keyText.clearFocus()
                regexText.clearFocus()
                valueText.clearFocus()
            }
        else {
            selectButton.visibility = View.GONE
            scriptLang.visibility = View.VISIBLE
            valueText.editText?.minHeight = 300
        }
        keyText.editText?.setText(data?.key)
        regexText.editText?.setText(data?.regStr)
        valueText.editText?.setText(data?.value)
        editDialog.show()
    }

    override val itemClickListener =
        object : SimpleListAdapter.OnItemClickListener<MarkedData> {

            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<MarkedData>) {
                //dialog edit
                val data = item.extra
                //todo colorful
                BottomDialog.builder(activity!!) {
                    title(item.title ?: "")
                    message(data.toString())
                    if (data.belongUser()) {
                        buttons {
                            negativeButton(getString(R.string.text_share)) {
                                share(data)
                            }
                            positiveButton(getString(R.string.text_edit)) {
                                onEdit(item.extra)
                            }
                            neutralButton(getString(R.string.text_delete)) {
                                DialogUtil.dataDelAlert(context) {
                                    if (data.tagId != null) {
                                        deleteShare(data.tagId)
                                    }
                                    DAO.daoSession.markedDataDao.delete(data)
                                    GlobalApp.toastSuccess(R.string.text_delete_complete)
                                    refresh()
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun deleteShare(tagId: String) {
        WrapperNetHelper.postJson<Any>(ApiUrls.DELETE_SHARE_MARKED, tagId) {
            success { _, bean ->
                if (bean.isOk()) {
                    Vog.d("云端删除成功")
                } else
                    Vog.d("云端删除失败")
            }
            fail { _, e ->
                e.printStackTrace()
                Vog.d("云端删除失败")
            }
        }
    }

    /**
     * 分享上传
     * @param data MarkedData
     */
    private fun share(data: MarkedData) {
        if (!UserInfo.isLogin()) {
            GlobalApp.toastInfo(R.string.text_please_login_first)
            return
        }
        WrapperNetHelper.postJson<String>(ApiUrls.SHARE_MARKED, data) {
            success { _, bean ->
                if (bean.isOk()) {
                    //return tagId
                    val tag = bean.data
                    if (tag != null) {
                        data.tagId = tag
                        data.publishUserId = UserInfo.getUserId()
                        data.from = DataFrom.FROM_SHARED
                        DAO.daoSession.markedDataDao.update(data)
                    }
                    GlobalApp.toastInfo(bean.message)
                } else {
                    GlobalApp.toastInfo(bean.message)
                }
            }
            fail { _, e ->
                GlobalApp.toastError(e.message ?: "error")
            }
        }
    }

    override fun onSync(types: Array<String>) {
        if (!UserInfo.isLogin()) {
            GlobalApp.toastInfo("请登陆后操作")
            return
        }
        showProgressBar()
        DataUpdator.syncMarkedData(null, types, lastKeyId) {
            if (it) {
                GlobalApp.toastSuccess("同步完成")
                refresh()
            }
            hideProgressBar()
        }
    }

    abstract val lastKeyId: Int

}

interface OnSyncMarked {
    fun onSync(types: Array<String>)
}
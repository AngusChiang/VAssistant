package cn.vove7.jarvis.fragments.base

import android.annotation.SuppressLint
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.jarvis.utils.NetHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.DialogUtil
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # BaseMarkedFragment
 *
 * @author Administrator
 * 2018/9/16
 */
abstract class BaseMarkedFragment<T> : SimpleListFragment<T>(), OnSyncMarked {

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
                        toast.green().showLong(R.string.text_complete)
                        refresh()
                        it.dismiss()
                    } catch (e: Exception) {
                        GlobalLog.err(e.message + "code: mc63")
                        toast.red().showLong(R.string.text_error_occurred)
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
                    Vog.d(this, "onItemSelected ---> $markedType")
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
        if (!AppConfig.checkUser()) {
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
        object : SimpleListAdapter.OnItemClickListener {

        @SuppressLint("CheckResult")
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            //dialog edit
            val data = item.extra as MarkedData
            MaterialDialog(context!!).show {
                if (data.belongUser()) {
                    negativeButton(R.string.text_share) {
                        share(data)
                    }
                    positiveButton(R.string.text_edit) {
                        onEdit(item.extra)
                    }
                    neutralButton(R.string.text_delete) {
                        DialogUtil.dataDelAlert(context) {
                            if (data.tagId != null) {
                                deleteShare(data.tagId)
                            }
                            DAO.daoSession.markedDataDao.delete(data)
                            toast.showShort(R.string.text_delete_complete)
                            refresh()
                        }
                    }

                }
                title(text = item.title)
                message(text = data.toString())
            }
        }
    }

    private fun deleteShare(tagId: String) {
        NetHelper.postJson<Any>(ApiUrls.DELETE_SHARE_MARKED, BaseRequestModel(tagId)) { _, bean ->
            if (bean?.isOk() == true) {
                Vog.d(this, "deleteShare ---> 云端删除成功")
            } else
                Vog.d(this, "deleteShare ---> 云端删除失败")
        }
    }

    /**
     * 分享上传
     * @param data MarkedData
     */
    private fun share(data: MarkedData) {
        if (!AppConfig.checkUser()) {
            return
        }
        NetHelper.postJson<String>(ApiUrls.SHARE_MARKED, BaseRequestModel(data),
                type = NetHelper.StringType) { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    //return tagId
                    val tag = bean.data
                    if (tag != null) {
                        data.tagId = tag
                        data.from = DataFrom.FROM_SHARED
                        DAO.daoSession.markedDataDao.update(data)
                    }
                    toast.showLong(bean.message)
                } else {
                    toast.showLong(bean.message)
                }
            } else
                toast.red().showShort(R.string.text_error_occurred)

        }
    }


    override fun onSync(types: Array<String>) {
        if(!UserInfo.isLogin()){
            toast.blue().showShort("请登陆后操作")
            return
        }
        showProgressBar()
        val syncData = TextHelper.arr2String(types)
        val requestModel = BaseRequestModel(syncData)

        NetHelper.postJson<List<MarkedData>>(ApiUrls.SYNC_MARKED, requestModel, type = NetHelper.MarkedDataListType) { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    DaoHelper.updateMarkedData(types, bean.data ?: emptyList())
                    toast.showShort("同步完成")
                    SpHelper(GlobalApp.APP).set(lastKeyId, System.currentTimeMillis())

                    refresh()
                } else {
                    toast.showShort(bean.message)
                }
            } else toast.showShort(R.string.text_net_err)
            hideProgressBar()
        }
    }
    abstract val lastKeyId:Int

}

interface OnSyncMarked {
    fun onSync(types: Array<String>)
}
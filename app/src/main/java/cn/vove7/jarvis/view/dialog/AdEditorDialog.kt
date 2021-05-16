package cn.vove7.jarvis.view.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.model.UserInfo

import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.lifecycle.LifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # AdEditorDialog
 * 编辑新建广告标记
 * @author Administrator
 * 9/22/2018
 */
class AdEditorDialog(val context: BaseActivity<*>, val onUpdate: () -> Unit) {

    private fun clearErr() {
        showNameText.error = ""
        pkgText.error = ""
        activityText.error = ""
        adViewIdText.error = ""
        classText.error = ""
        adTexts.error = ""
        adDescs.error = ""
        depthsText.error = ""
    }

    val dialog: MaterialDialog by lazy {
        MaterialDialog(context).noAutoDismiss()
                .customView(R.layout.dialig_edit_ad, scrollable = true)
                .cancelable(false)
                .positiveButton { dia ->
                    clearErr()
                    //check name pkg
                    val name = showNameText.editText?.text.toString().let { if (it.trim() == "") null else it }
                    val pkg = pkgText.editText?.text.toString().let { if (it.trim() == "") null else it }
                    val activity = activityText.editText?.text.toString().let { if (it.trim() == "") null else it }
                    if (name == null) {
                        showNameText.error = GlobalApp.getString(R.string.text_not_empty)
                        return@positiveButton
                    }
                    if (pkg == null) {
                        pkgText.error = GlobalApp.getString(R.string.text_not_empty)
                        return@positiveButton
                    } else if (AdvanAppHelper.getAppInfo(pkg) == null) {
                        pkgText.error = GlobalApp.getString(R.string.text_app_not_install)
                        return@positiveButton
                    }

                    if (activity == null) {
                        activityText.error = GlobalApp.getString(R.string.text_not_empty)
                        return@positiveButton
                    }
                    //check by type
                    when (typeGroup.checkedRadioButtonId) {
                        R.id.by_custom -> {
                            val viewId = adViewIdText.editText?.text.toString().let { if (it.trim() == "") null else it }
                            val classTex = classText.editText?.text.toString().let { if (it.trim() == "") null else it }
                            val adText = adTexts.editText?.text.toString().let { if (it.trim() == "") null else it }
                            val adDesc = adDescs.editText?.text.toString().let { if (it.trim() == "") null else it }
                            if (viewId == null && adText == null && adDesc == null) {
                                adViewIdText.error = "."
                                adTexts.error = "."
                                GlobalApp.toastError("至少一个")
                                return@positiveButton
                            }

                            val newData = if (editData != null) editData!! else AppAdInfo()
                            newData.depths = null

                            newData.descTitle = name
                            newData.pkg = pkg
                            newData.activity = activity
                            newData.from = DataFrom.FROM_USER

                            newData.viewId = viewId
                            newData.texts = adText
                            newData.descs = adDesc
                            newData.type = classTex
                            newData.from = DataFrom.FROM_USER
                            newData.publishUserId = UserInfo.getUserId()

                            if (newData.id == null) {
                                DAO.daoSession.appAdInfoDao.insertInTx(newData)
                            } else {
                                DAO.daoSession.appAdInfoDao.update(newData)
                            }
                        }
                        R.id.by_depths -> {

                            depthStr = depthsText.editText?.text.toString().trim()
                            if (!checkDepths()) {
                                depthsText.error = "格式错误"
                                return@positiveButton
                            }
                            val newData = if (editData != null) editData!! else AppAdInfo()
                            newData.descTitle = name
                            newData.pkg = pkg
                            newData.activity = activity
                            newData.from = DataFrom.FROM_USER
                            newData.publishUserId = UserInfo.getUserId()

                            newData.viewId = null
                            newData.texts = null
                            newData.descs = null
                            newData.type = null

                            newData.depths = depthStr
                            if (newData.id == null) {
                                DAO.daoSession.appAdInfoDao.insertInTx(newData)
                            } else {
                                DAO.daoSession.appAdInfoDao.update(newData)
                            }
                        }
                    }
                    DAO.clear()
                    AdKillerService.update()
                    GlobalApp.toastSuccess(R.string.text_complete)
                    dia.dismiss()
                    onUpdate.invoke()
                }.negativeButton { it.dismiss() }
    }
    var depthStr: String = ""
    private val showNameText: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.name_text) }
    private val pkgText: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.pkg_text) }
    private val activityText: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.activity_text) }
    private val adViewIdText: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.ad_view_id_text) }
    private val classText: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.class_text) }
    private val adTexts: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.ad_texts_text) }
    private val adDescs: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.ad_descs_text) }
    private val depthsText: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.depths_text) }
    private val typeGroup: RadioGroup  by lazy { dialog.findViewById<RadioGroup>(R.id.type_group) }

    init {
        typeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.by_custom -> {
                    adViewIdText.visibility = View.VISIBLE
                    classText.visibility = View.VISIBLE
                    adTexts.visibility = View.VISIBLE
                    adDescs.visibility = View.VISIBLE
                    depthsText.visibility = View.GONE
                }
                else -> {
                    adViewIdText.visibility = View.GONE
                    classText.visibility = View.GONE
                    adTexts.visibility = View.GONE
                    adDescs.visibility = View.GONE
                    depthsText.visibility = View.VISIBLE
                }
            }
        }
        val d by lazy {
            SelectAppDialog.get(context) {
                pkgText.editText?.setText(it.packageName)
            }
        }
        dialog.findViewById<Button>(R.id.sel_app_btn).setOnClickListener { view ->
            d.show()
            clearF()
        }
    }

    fun clearF() {
        showNameText.clearFocus()
        pkgText.clearFocus()
        activityText.clearFocus()
        adViewIdText.clearFocus()
        classText.clearFocus()
        adTexts.clearFocus()
        adDescs.clearFocus()
        depthsText.clearFocus()
    }

    var editData: AppAdInfo? = null
    @SuppressLint("CheckResult")
    fun show(data: AppAdInfo? = null, pkg: String? = null) {
        if (!AppConfig.checkLogin()) {
            return
        }
        editData = data
        dialog.title(
                if (data != null) R.string.text_edit
                else R.string.text_new
        )
        if (data?.depths != null) {
            typeGroup.check(R.id.by_depths)
            depthsText.editText?.setText(data.depths)
        } else {
            typeGroup.check(R.id.by_custom)
            adViewIdText.editText?.setText(data?.viewId)
            adTexts.editText?.setText(data?.texts)
            adDescs.editText?.setText(data?.descs)
        }
        classText.editText?.setText(data?.type)
        showNameText.editText?.setText(data?.descTitle)
        if (data == null)
            pkgText.editText?.setText(pkg)
        else
            pkgText.editText?.setText(data.pkg)

        activityText.editText?.setText(data?.activity)

        dialog.show()
    }

    /**
     *
     * @return Boolean
     */
    private fun checkDepths(): Boolean {
        depthStr = depthStr.replace(" ", "")
        val ss = depthStr.split(',')
        return try {
            ss.forEach { it.toInt() }
            true
        } catch (e: NumberFormatException) {
            false
        }

    }

}
package cn.vove7.jarvis.tools.backup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.CoroutineExt.delayLaunch
import cn.vove7.vtp.net.GsonHelper
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.checkBoxText
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.jarvis.view.dialog.ProgressTextDialog
import cn.vove7.vtp.file.FileHelper
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.File
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*

/**
 * # BackupHelper
 *
 * 备份内容：本地标记（联系人|app|打开|广告），本地指令（全局|应用内）
 * 恢复（冲突?）
 * tips: 备份本地数据，不包括已分享的数据。云端未审核通过和信息过旧的记录，请自行处理
 * @author Administrator
 * 2018/10/17
 */
object BackupHelper {
    @SuppressLint("SetTextI18n")
    fun showBackupDialog(activity: Activity) {
        var indices = intArrayOf(0, 1, 2, 3, 4, 5)
        MaterialDialog(activity).title(text = "选择备份数据")
                .noAutoDismiss()
                .checkBoxText(text = "tips: 备份本地数据，不包括已分享的数据。云端未审核通过和信息过旧的记录，请自行处理\n" +
                        "保存路径：$backupPath")
                .listItemsMultiChoice(items = listOf(
                        "全局指令", "应用内指令", "标记联系人", "标记应用", "标记(打开)功能", "标记广告信息"
                ), waitForPositiveButton = false, initialSelection = indices) { _, indexes, _ ->
                    indices = indexes
                    Vog.d("indexes ---> ${Arrays.toString(indexes)}")
                }.show {

                    positiveButton(text = "备份到本地") {
                        if (!checkSel(indices)) return@positiveButton
                        val p = ProgressDialog(activity)
                        delayLaunch(if (!UserInfo.isVip()) 500 else 500) {
                            if (backup(indices, true)) {
                                GlobalApp.toastSuccess("备份成功")
                                it.dismiss()
                            } else {
                                GlobalApp.toastError("备份失败，详情请查看日志")
                            }
                            p.dismiss()
                        }
                    }
                    negativeButton(text = "备份到云端") {
                        if (!checkSel(indices)) return@negativeButton
                        backup(indices, false)
                    }
                    neutralButton(text = "预览") {
                        if (!checkSel(indices)) return@neutralButton
                        val data = wrapData(indices)
                        showPreviewDialog(activity, data, "返回")
                    }
                }
    }

    private fun checkSel(indices: IntArray): Boolean {
        return if (indices.isEmpty()) {
            GlobalApp.toastInfo("至少选择一项")
            false
        } else true
    }

    /**
     * 根据选择 包装备份数据
     * @param types IntArray
     * @return BackupWrap
     */
    private fun wrapData(types: IntArray): BackupWrap {
        val data = BackupWrap()
        types.forEach {
            when (it) {
                0 -> data.instList.addAll(getLocalGlobalInst())
                1 -> data.instList.addAll(getLocalInappInst())
                2 -> data.markedDataList.addAll(getLocalMarkedContact())
                3 -> data.markedDataList.addAll(getLocalMarkedApp())
                4 -> data.markedDataList.addAll(getLocalMarkedOpen())
                5 -> data.appAdList.addAll(getLocalMarkedAd())
                else -> {
                    GlobalLog.err("错误类型：$it")
                }
            }
        }
        return data
    }

    /**
     * 0 -> 全局指令 1 -> 应用内指令 2 -> 联系人
     * 3 -> App     4 -> 打开      5 -> 广告
     * @param types Array<Int>
     */
    private fun backup(types: IntArray, local: Boolean): Boolean {
        return try {
            val data = wrapData(types)
            val json = GsonHelper.toJson(data, true)
            Vog.d("json ---> $json")
            if (local) toFile(json) else toCloud(json)
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalLog.err("转换json失败")
            false
        }
    }

    private fun toCloud(json: String): Boolean {
        //todo
        GlobalApp.toastInfo(R.string.text_coming_soon)
        return false
    }

    private val backupPath: String by lazy { StorageHelper.backupPath }

    private fun toFile(json: String): Boolean {
        val format = SimpleDateFormat("yyyyMMdd_HH_mm", Locale.getDefault())
        return try {
            val f = File(backupPath, "backup_${format.format(Date())}.bak")
            if (!f.parentFile.exists()) f.parentFile.mkdirs()
            f.writeText(json)
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    /**
     * 从本地恢复
     */
    fun showBackupFileList(activity: Activity) {
        val localFileList = File(backupPath).let {
            if (!it.exists()) emptyArray<File>()
            else it.listFiles()
        }
        Vog.d("showBackupFileList ---> ${Arrays.toString(localFileList)}")

        val fileList = mutableListOf<String>()
        localFileList.filter {
            it.exists() && it.isFile
        }.forEach { fileList.add(it.name) }
        Vog.d("showBackupFileList ---> $fileList")
        MaterialDialog(activity).title(text = "备份文件")
                .listItemsSingleChoice(items = fileList) { dialog, index, text ->
                    restoreFromFile(activity, File(backupPath, text.toString()))
                }.show {
                    neutralButton(text = "选择文件") {
                        val selIntent = Intent(Intent.ACTION_GET_CONTENT)
                        selIntent.type = "*/*"
                        selIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        try {
                            activity.startActivityForResult(selIntent, 1)
                            this.dismiss()
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            GlobalApp.toastInfo(R.string.text_cannot_open_file_manager)
                        }
                    }
                    positiveButton(text = "下一步")
                }
    }

    fun restoreFromFile(activity: Activity, file: File) {
        if (!file.exists() || file.isDirectory) {
            GlobalApp.toastError("文件错误")
        } else {//读取
            val json = file.readText()
            restoreFromJsonData(activity, json)
        }
    }

    fun restoreFromJsonData(activity: Activity, json: String) {
        try {
            val data = Gson().fromJson(json, BackupWrap::class.java)
            if (data == null) {
                GlobalApp.toastError("数据解析失败")
                return
            }
            restoreData(activity, data)
        } catch (e: JsonSyntaxException) {
            GlobalLog.err(e)
            GlobalApp.toastError("恢复失败，数据错误")
        }
    }

    /**
     * 开始恢复
     * 显示预览数据
     * 选择恢复模式
     * 开始恢复
     * @param activity Activity
     * @param data BackupWrap
     * @param mode Int
     */
    private fun restoreData(activity: Activity, data: BackupWrap) {
        showPreviewDialog(activity, data, "选择恢复模式") { _ ->
            val nsleep = BuildConfig.DEBUG || !UserInfo.isVip()
            showModeDialog(activity) { mode ->
                val dialog = ProgressTextDialog(activity, "正在恢复", cancelable = false, autoScroll = true)
                launch {
                    try {
                        DAO.daoSession.runInTx {
                            //指令
                            if (data.instList?.isNotEmpty() == true && mode == 0) {
                                dialog.append("清空本地指令..  ")
                                DaoHelper.deleteAllUserInst()
                                if (nsleep) sleep(100)
                                dialog.appendlnGreen("成功")
                            }
                            data.instList?.forEach {
                                dialog.append("恢复指令： ${it.actionTitle}  ")
                                if (nsleep) sleep(100)
                                if (!it.infoIsOk()) {
                                    dialog.appendlnRed("指令信息不完整 跳过")
                                    return@forEach
                                }
                                if (mode == 1 || mode == 2) {//检查
                                    val old = DaoHelper.getSimActionNode(it)
                                    if (old != null && mode == 2) {//删除旧数据
                                        dialog.appendlnRed("\n删除本地指令：${it.actionTitle}")
                                        DaoHelper.deleteActionNode(old.id)
                                        DaoHelper.insertNewActionNode(it)
                                    } else if (old == null) {//insert
                                        DaoHelper.insertNewActionNode(it)
                                    } else {//else 保留 do nothing
                                        dialog.appendlnAmber("本地存在 跳过")
                                    }
                                } else if (mode == 0 || mode == 3)
                                    DaoHelper.insertNewActionNode(it)
                                dialog.appendlnGreen("成功")
                            }
                            //广告
                            if (data.appAdList?.isNotEmpty() == true && mode == 0) {
                                dialog.append("清空本地标记广告.. ")
                                if (nsleep) sleep(100)
                                DaoHelper.deleteAllUserMarkedAd()
                                dialog.appendlnGreen("成功")
                            }
                            data.appAdList?.forEach {
                                dialog.append("恢复广告标记：${it.descTitle}  ")
                                if (nsleep) sleep(100)
                                if (!it.infoIsOk()) {
                                    dialog.appendlnRed("信息不完整 跳过")
                                    return@forEach
                                }
                                if (mode == 1 || mode == 2) {//检查
                                    val old = DaoHelper.getSimMarkedAppAd(it)
                                    if (old != null && mode == 2) {//删除旧数据
                                        dialog.appendlnRed("\n删除本地广告标记：${it.descTitle}")
                                        DAO.daoSession.appAdInfoDao.apply {
                                            delete(old)
                                            insert(it)
                                        }
                                    } else if (old == null) {//insert
                                        DAO.daoSession.appAdInfoDao.insert(it)
                                    } else {//else do nothing
                                        dialog.appendlnAmber("本地存在 跳过")
                                    }
                                } else
                                    DAO.daoSession.appAdInfoDao.insert(it)
                                dialog.appendlnGreen("成功")
                            }
                            //标记
                            if (data.markedDataList?.isNotEmpty() == true && mode == 0) {
                                dialog.append("清空本地标记.. ")
                                DaoHelper.deleteAllUserMarkedData()
                                if (nsleep) sleep(100)
                                dialog.appendlnGreen("成功")
                            }
                            data.markedDataList?.forEach {
                                dialog.append("恢复标记： ${it.key}  ")
                                if (nsleep) sleep(100)
                                if (!it.infoIsOk()) {
                                    dialog.appendlnRed("信息不完整 跳过")
                                    return@forEach
                                }
                                if (mode == 1 || mode == 2) {//检查
                                    val old = DaoHelper.getSimMarkedData(it)
                                    if (old != null && mode == 2) {
                                        dialog.appendln("\n删除本地标记：${it.key}")
                                        DAO.daoSession.markedDataDao.apply {
                                            delete(old)
                                            insert(it)
                                        }
                                    } else if (old == null) {//insert
                                        DAO.daoSession.markedDataDao.insert(it)
                                    } else {//本地优先
                                        dialog.appendlnAmber("本地存在 跳过")
                                    }
                                } else
                                    DAO.daoSession.markedDataDao.insert(it)
                                dialog.appendlnGreen("成功")
                            }
                        }
                        if (nsleep) sleep(100)
                        dialog.appendlnGreen("恢复完成")
                        dialog.finish()
                        DAO.clear()
                        ParseEngine.updateNode()
                    } catch (e: Exception) {
                        GlobalLog.err(e)
                        dialog.appendlnRed("失败：${e.message}")
                        dialog.appendln("已恢复原状态")
                        dialog.finish()
                    }
                }
            }
        }
    }

    /**
     * 选择恢复模式
     * 清空本地数据   0
     * 本地数据优先   1
     * 备份数据优先   2
     * 无操作        3
     * @param activity Activity
     * @param call (Int) -> Unit
     */
    private fun showModeDialog(activity: Activity, call: (Int) -> Unit) {
        MaterialDialog(activity).title(text = "恢复模式")
                .listItemsSingleChoice(waitForPositiveButton = true, items = listOf(
                        "清空本地数据\n清空本地用户数据(仅备份中恢复的3种类型[指令/标记/广告])，再进行恢复",//0
                        "本地数据优先\n以显示名作为依据，优先保留本地数据",//1
                        "备份数据优先\n以显示名作为依据，优先使用备份数据",//2
                        "无操作\n若本地和备份数据存在相同会导致重复数据"//3
                )) { _, i, _ ->
                    call.invoke(i)
                }
                .positiveButton(text = "开始恢复")
                .negativeButton()
                .show()

    }


    private fun showPreviewDialog(activity: Activity, data: BackupWrap, next: String? = null, call: DialogCallback? = null) {
        MaterialDialog(activity).title(text = "预览数据")
                .message(text = previewData(data))
                .positiveButton(text = next, click = call)
                .negativeButton()
                .show()
    }


    private fun previewData(data: BackupWrap): String {
        return buildString {
            data.instList?.also {
                if (it.isNotEmpty()) {
                    appendln("指令  数量：${it.size}")
                    it.forEach { i ->
                        appendln(i?.actionTitle)
                    }
                    appendln()
                }
            }
            data.markedDataList?.also {
                if (it.isNotEmpty()) {
                    appendln("标记   数量：${it.size}")
                    it.forEach { i ->
                        appendln(i?.key)
                    }
                    appendln()
                }
            }
            data.appAdList?.also {
                if (it.isNotEmpty()) {
                    appendln("标记广告   数量：${it.size}")
                    it.forEach { i ->
                        appendln(i?.descTitle)
                    }
                }
            }
        }

    }

    private fun getLocalMarkedContact(): List<MarkedData> {
        val list = DaoHelper.getLocalMarkedByType(
                arrayOf(MarkedData.MARKED_TYPE_CONTACT))
        Vog.d("getLocalMarkedData 本地标记联系人 ---> $list")
        return list
    }

    private fun getLocalMarkedApp(): List<MarkedData> {
        val list = DaoHelper.getLocalMarkedByType(
                arrayOf(MarkedData.MARKED_TYPE_APP))
        Vog.d("getLocalMarkedData 本地标记应用 ---> $list")
        return list
    }

    private fun getLocalMarkedOpen(): List<MarkedData> {
        val list = DaoHelper.getLocalMarkedByType(
                arrayOf(MarkedData.MARKED_TYPE_SCRIPT_LUA,
                        MarkedData.MARKED_TYPE_SCRIPT_JS))
        Vog.d("getLocalMarkedData 本地标记打开 ---> $list")
        return list
    }

    private fun getLocalMarkedAd(): List<AppAdInfo> {
        val list = DAO.daoSession.appAdInfoDao.queryBuilder()
                .where(AppAdInfoDao.Properties.From.eq(DataFrom.FROM_USER)).list()
        Vog.d("getLocalMarkedAd 广告信息 ---> $list")
        return list
    }


    private fun getLocalInappInst(): List<ActionNode> {
        val list = DaoHelper
                .getLocalInstByType(ActionNode.NODE_SCOPE_IN_APP)
        Vog.d("getLocalInst 本地应用内指令 ---> $list")
        return list

    }

    private fun getLocalGlobalInst(): List<ActionNode> {
        val list = DaoHelper
                .getLocalInstByType(ActionNode.NODE_SCOPE_GLOBAL)
        Vog.d("getLocalInst 本地全局指令 ---> $list")
        return list
    }


    //////////////////设置备份/////////////////
    fun backupAppConfig(): Boolean {
        return try {
            val fList = arrayOf(GlobalApp.APP.packageName + "_preferences.xml", "tutorials.xml", "plugin.xml", "float_panel_config.xml")
            val spDir = File(GlobalApp.APP.cacheDir.parent, "shared_prefs")
            fList.forEach {
                try {//有些文件未初始化
                    FileHelper.easyCopy(File(spDir, it), File(StorageHelper.spPath, it))
                } catch (e: Exception) {

                }
            }
            true
        } catch (e: Throwable) {
            GlobalLog.err(e)
            false
        }
    }

    /**
     * 恢复设置
     * @return String 结果
     */
    fun restoreAppConfig(): Pair<Boolean, String> {
        return try {
            val fList = arrayOf(GlobalApp.APP.packageName + "_preferences.xml", "tutorials.xml", "plugin.xml", "float_panel_config.xml")
            val spDir = File(GlobalApp.APP.cacheDir.parent, "shared_prefs")
            if (File(StorageHelper.spPath).list().isEmpty()) {
                return Pair(false, "未发现备份文件")
            }
            fList.forEach {
                val baF = File(StorageHelper.spPath, it)
                if (baF.exists()) {
                    FileHelper.easyCopy(baF, File(spDir, it))
                }
            }
            Pair(true, "恢复完成，请重启App")
        } catch (e: Throwable) {
            GlobalLog.err(e)
            Pair(false, "发生错误 ${e.message}")
        }
    }

}
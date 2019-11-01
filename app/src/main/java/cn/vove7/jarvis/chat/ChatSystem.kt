package cn.vove7.jarvis.chat

import android.widget.ImageView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.inVisibility
import cn.vove7.common.utils.show
import cn.vove7.jarvis.fragments.AwesomeItem
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.view.floatwindows.IFloatyPanel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.Serializable

/**
 * # ChatSystem
 *
 * @author Administrator
 * 2018/10/28
 */
interface ChatSystem {
    fun onChat(s: String, fp: IFloatyPanel): Boolean {
        val data = chatWithText(s) ?: return false

        if (data.resultUrls.isNotEmpty()) {
            fp.showListResult(data.word, data.resultUrls)
        } else {
            data.word.let { word ->
                AppBus.post(CommandHistory(UserInfo.getUserId(), s, word))
                MainService.speak(word)
                fp.showTextResult(if (word.contains("="))
                    word.replace("=", "\n=") else word)
                MainService.executeAnimation.begin()
                MainService.executeAnimation.show(word)
            }
        }
        return true
    }

    //耗时操作
    fun chatWithText(s: String): ChatResult?
}

interface ChatResultBuilder {
    fun getChatResult(): ChatResult? {
        val urls = getResultUrls()
        var word = getWord()
        if (word == null) {
            if (urls.isEmpty()) return null
            else word = "已为您找到如下结果"
        }

        return ChatResult(word, urls)
    }

    fun getWord(): String?
    fun getResultUrls(): ArrayList<UrlItem>
}

data class ChatResult(
        val word: String,
        val resultUrls: ArrayList<UrlItem> //title url

)

data class UrlItem(
        override val title: String,
        val iconUrl: String? = null,
        val url: String,
        val info: String? = null,
        val source: String? = null
) : AwesomeItem, Serializable {
    override val subTitle: String?
        get() = info ?: if (source != null) "来源: $source" else null

    override fun onLoadDrawable(imgView: ImageView) {
        if (iconUrl == null || iconUrl == "") {
            imgView.inVisibility()
            return
        }
        imgView.show()
        Glide.with(GlobalApp.APP).applyDefaultRequestOptions(RequestOptions().also {
            it.centerCrop()
        }).load(iconUrl).into(imgView)

    }

}
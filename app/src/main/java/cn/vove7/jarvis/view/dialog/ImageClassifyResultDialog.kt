package cn.vove7.jarvis.view.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.baiduaip.model.ImageClassifyResult
import cn.vove7.vtp.log.Vog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


/**
 * # ImageClassifyResultDialog
 * 图片识别Dialog
 * @author Administrator
 * 2018/11/7
 */
class ImageClassifyResultDialog(val result: ImageClassifyResult.Rlt, context: Context,
                                val screen: Bitmap?, val hideEvent: () -> Unit) : FloatAlertDialog(context, R.style.TransparentDialog) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView()
    }

    lateinit var viewHolder: Holder
    private fun setContentView() {
        setContentView(R.layout.dialog_image_classify_result)
        viewHolder = Holder(findViewById(R.id.root))
        bindView(viewHolder)
    }

    @SuppressLint("SetTextI18n")
    private fun bindView(viewHolder: Holder) {
        viewHolder.titleView.text = "${result.keyword} | ${result.root}"
        viewHolder.subtitleView.text = "匹配率：${result.score?.times(100)}%"
        result.baikeInfo.apply {
            if (this == null || description == null) {
                GlobalApp.toastInfo("无详细信息")
            } else {
                viewHolder.descView.text = description
            }
            val s = Glide.with(context).applyDefaultRequestOptions(RequestOptions().also {
                it.centerCrop()
            })
            (if (this?.imageUrl != null)
                s.load(imageUrl)
            else s.load(screen)).listener(getLis()).into(viewHolder.imgView)

            viewHolder.headerView.setOnClickListener {
                Vog.d(this ?: "", "bindView ---> ${this?.baikeUrl}")
                SystemBridge.openUrl(this?.baikeUrl
                    ?: "https://baike.baidu.com/item/${result.keyword}").also { r ->
                    if (r) {
                        dismiss()
                        hideEvent.invoke()
                    }
                }
            }
        }
    }

    private fun getLis() = object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            GlobalApp.toastError("图片加载失败")
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            if (resource == null) return false
            Palette.from((resource as BitmapDrawable).bitmap)
                    .generate().darkMutedSwatch?.rgb?.also {
                viewHolder.titleBgLay.setBackgroundColor((0x88000000 + it).toInt())//半透明
            }
            viewHolder.imgView.setImageDrawable(resource)
            return true
        }
    }

    class Holder(val dialogView: View) {
        val imgView: ImageView = dialogView.findViewById(R.id.image_view)
        val titleView: TextView = dialogView.findViewById(R.id.title)
        val subtitleView: TextView = dialogView.findViewById(R.id.subtitle)
        val descView: TextView = dialogView.findViewById(R.id.desc_text)
        val titleBgLay = dialogView.findViewById<View>(R.id.title_lay)
        val headerView = dialogView.findViewById<View>(R.id.header_view)
    }

}

//1. Palette.Swatch s1 = Palette.getVibrantSwatch(); //充满活力的色板
//
//2. Palette.Swatch s2 = Palette.getDarkVibrantSwatch(); //充满活力的暗色类型色板
//
//3. Palette.Swatch s3 = Palette.getLightVibrantSwatch(); //充满活力的亮色类型色板
//
//4. Palette.Swatch s4 = Palette.getMutedSwatch(); //黯淡的色板
//
//5. Palette.Swatch s5 = Palette.getDarkMutedSwatch(); //黯淡的暗色类型色板（翻译过来没有原汁原味的赶脚啊！）
//
//6. Palette.Swatch s6 = Palette.getLightMutedSwatch(); //黯淡的亮色类型色板
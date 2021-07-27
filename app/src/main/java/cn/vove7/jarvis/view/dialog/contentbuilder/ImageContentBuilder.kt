package cn.vove7.jarvis.view.dialog.contentbuilder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.bottomdialog.interfaces.ContentBuilder
import cn.vove7.jarvis.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File

/**
 * # ImageContentBuilder
 *
 * @author Vove
 * 2019/7/7
 */
class ImageContentBuilder : ContentBuilder() {
    override val layoutRes: Int
        get() = R.layout.content_image

    var onInit: (() -> Unit)? = null
    lateinit var imageView: ImageView
    lateinit var tv: TextView

    override fun init(view: View) {
        imageView = view.findViewById(R.id.image_view)
        tv = view.findViewById(R.id.text_view)
        onInit?.invoke()
    }

    fun loadFile(file: File, text:CharSequence?) {
        Glide.with(dialog.context)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        tv.text = text
    }

    fun init(action: () -> Unit) {
        onInit = action
    }

    override fun updateContent(type: Int, data: Any?) {
    }
}
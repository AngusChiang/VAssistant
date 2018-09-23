package cn.vove7.jarvis.view.custom

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.ImageView
import cn.vove7.jarvis.R

/**
 * # IconView
 *
 * @author 17719247306
 * 2018/9/9
 */
class IconView : ImageView {
    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)

    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context)
    }

    fun init(context: Context?) {
        if (context == null) return
        setColorFilter(context.resources.getColor(android.R.color.black), PorterDuff.Mode.SRC_IN)

    }


}
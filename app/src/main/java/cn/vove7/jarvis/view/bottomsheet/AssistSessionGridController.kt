package cn.vove7.jarvis.view.bottomsheet

import android.content.Context
import android.view.View
import android.widget.GridView
import android.widget.ImageView
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.vtp.easyadapter.BaseListAdapter

/**
 * # AssistSessionGridController
 *
 * @author 11324
 * 2019/1/21
 */
class AssistSessionGridController(context: Context, bottomView: View, val click: (Int) -> Unit)
    : BottomSheetController(context, bottomView) {

    private val gridView: GridView = bottomView.findViewById(R.id.fun_grid)
    fun initView() {
        gridView.numColumns=items.size
        gridView.adapter = object : BaseListAdapter<VH, SessionFunItem>(context, items) {
            override fun layoutId(position: Int): Int = R.layout.image_view

            override fun onBindView(holder: VH, pos: Int, item: SessionFunItem) {
                holder.imageView.apply {
                    setImageDrawable(context.getDrawable(item.iconId))
                    setOnClickListener { click.invoke(pos) }
                    setOnLongClickListener { GlobalApp.toastInfo(item.desc ?: item.name); true }
                }
            }

            override fun onCreateViewHolder(view: View): VH = VH(view)
        }
    }

    inner class VH(itemView: View) : BaseListAdapter.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }

    private val items = mutableListOf(
            SessionFunItem("屏幕识别", R.drawable.ic_twotone_center_focus_weak_24px, "识别屏幕内容"),
            SessionFunItem("文字识别", R.drawable.ic_twotone_text_box, "适用于图片中的文字识别"),
            SessionFunItem("文字提取", R.drawable.ic_twotone_text_fields_24px, "适用于屏幕内文本提取"),
            SessionFunItem("分享屏幕", R.drawable.ic_twotone_linked_camera_24px, "分享截屏"),
            SessionFunItem("二维码/条码识别", R.drawable.ic_twotone_qr_code),
            SessionFunItem("保存截图", R.drawable.ic_twotone_save)
    )

    class SessionFunItem(
            val name: String,
            val iconId: Int,
            val desc: String? = null
    ) {
        fun viewModel(context: Context) =
            ListViewModel(name, icon = context.getDrawable(iconId), subTitle = desc, extra = this)
    }

}
package cn.vove7.jarvis.adapters

class IconTitleEntity(
        val iconId: Int? = null,
        val titleId: Int,
        val summaryId: Int? = null,
        val onclick: Function0<Unit>
)
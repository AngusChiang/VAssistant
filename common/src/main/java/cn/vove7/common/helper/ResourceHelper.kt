package cn.vove7.common.helper

import android.content.Context

object ResourceHelper {

    fun getId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "id")
    }

    fun getLayoutId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "layout")
    }

    fun getStringId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "string")
    }

    fun getDrawableId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "drawable")
    }

    fun getMipmapId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "mipmap")
    }

    fun getColorId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "color")
    }

    fun getDimenId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "dimen")
    }

    fun getAttrId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "attr")
    }

    fun getStyleId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "style")
    }

    fun getAnimId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "anim")
    }

    fun getArrayId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "array")
    }

    fun getIntegerId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "integer")
    }

    fun getBoolId(context: Context, resourceName: String): Int {
        return getIdentifierByType(context, resourceName, "bool")
    }

    private fun getIdentifierByType(context: Context, resourceName: String, defType: String): Int {
        return context.resources.getIdentifier(resourceName,
                defType,
                context.packageName)
    }
}
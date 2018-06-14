package cn.vove7.vtp.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import cn.vove7.vtp.log.Vog


class SpUtil(private val context: Context) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private fun s(@StringRes id: Int): String {
        return context.getString(id)
    }

    /**
     * @param keyId @StringRes keyId
     * @return null when have no this keyValue
     */
    fun getString(@StringRes keyId: Int): String? {
        return preferences.getString(s(keyId), null)
    }

    /**
     * @param keyId @StringRes keyId
     * @return false when have no this keyValue
     */
    fun getBoolean(@StringRes keyId: Int): Boolean {
        return preferences.getBoolean(s(keyId), false)
    }

    fun setValue(@StringRes keyId: Int, value: Any) {
        setValue(s(keyId), value)
    }

    private fun setValue(key: String, value: Any) {
        val editor = preferences.edit()
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is String -> editor.putString(key, value)
            is Set<*> -> editor.putStringSet(key, value as Set<String>)
            else -> Vog.e(this, "设置值类型出错 key: $key")
        }
        editor.apply()
    }
}

package cn.vove7.vtp.contact

import android.content.Context
import android.provider.ContactsContract
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils


/**
 *
 *
 * Created by Vove on 2018/6/19
 */
object ContactHelper {

    private val contactProjection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
    private val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

    private val requirePermissions = arrayOf(
            "android.permission.READ_CONTACTS"
    )

    /**
     * 获取联系人列表
     * <uses-permission android:name="android.permission.READ_CONTACTS"/>
     */
    fun getAllContacts(context: Context): HashMap<String, ContactInfo> {
        if (!PermissionUtils.isAllGranted(context, requirePermissions)) {
            PermissionUtils.autoRequestPermission(context, requirePermissions)
        }
        val contactUri = ContactsContract.Contacts.CONTENT_URI
        val cursor = context.contentResolver.query(contactUri, contactProjection, null, null, null)
        val list = hashMapOf<String, ContactInfo>()
        with(cursor) {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(0)
                    //获取姓名
                    val name = cursor.getString(1)

                    val phonesCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null)

                    val phoneList = mutableListOf<String>()
                    with(phonesCursor) {
                        if (phonesCursor.moveToFirst()) {
                            do {
                                val num = phonesCursor.getString(0)
                                phoneList.add(num.replace(" ",""))
                            } while (phonesCursor.moveToNext())
                        }
                    }
                    list[name] = ContactInfo(name, phoneList)
                } while (cursor!!.moveToNext())
            } else {
                Vog.d(this, "联系人 moveToFirst failed")
            }
        }
        Vog.d(this, "联系人更新: ${list.size}")
        return list
    }

}
package cn.vove7.vtp.contact

/**
 *
 *
 * Created by Vove on 2018/6/19
 */
open class ContactInfo(
        var contactName: String = "",
        val phones: MutableList<String> = mutableListOf()
) : Comparable<ContactInfo> {

    override fun compareTo(other: ContactInfo): Int {
        return contactName.compareTo(other.contactName)
    }
}
package cn.vove7.vtp.contact

/**
 *
 *
 * Created by Vove on 2018/6/19
 */
class ContactInfo(
        val contactName: String,
        val phones: List<String>
) : Comparable<ContactInfo> {

    override fun compareTo(other: ContactInfo): Int {
        return contactName.compareTo(other.contactName)
    }
}
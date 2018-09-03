package cn.vove7.jarvis.fragments

import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.vtp.contact.ContactInfo

/**
 * # MarkedContractFragment
 *
 * @author 17719247306
 * 2018/9/4
 */
class MarkedContractFragment : SimpleListFragment<ContactInfo>() {
    override fun transData(nodes: List<ContactInfo>): List<ViewModel> {
        return emptyList()
    }
}
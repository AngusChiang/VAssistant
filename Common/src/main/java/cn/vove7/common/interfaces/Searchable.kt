package cn.vove7.common.interfaces

/**
 * # Searchable
 * @[SimpleListFragment]
 * @author 11324
 * 2019/4/10
 */
interface Searchable {
    fun onSearch(text: String): Boolean
}
package cn.vove7.common.model

/**
 * 数据 匹配率
 * @param matchRate 匹配率
 * @param data 数据
 * @param DataType 数据类型
 * Created by Vove on 2018/6/19
 */
class MatchedData<DataType>(val matchRate: Float, val data: DataType) : Comparable<MatchedData<DataType>> {
    override fun compareTo(other: MatchedData<DataType>): Int {
        return ((other.matchRate - matchRate) * 100).toInt()
    }
}
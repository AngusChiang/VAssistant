package cn.vove7.common.model

/**
 * # LocationInfo
 *
 * @author Vove
 * 2020/2/24
 */
data class LocationInfo(
        val ip: String,
        val pro: String,
        val proCode: String,
        val city: String,
        val cityCode: String,
        val region: String,
        val regionCode: String,
        val addr: String,
        val regionNames: String,
        val err: String
)

/*
* {"ip":"112.65.8.29","pro":"上海市","proCode":"310000","city":"上海市","cityCode":"310000",
* "region":"徐汇区","regionCode":"310104","addr":"上海市徐汇区 联通漕河泾数据中心","regionNames":"","err":""}
* */
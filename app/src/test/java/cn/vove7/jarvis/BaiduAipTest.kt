package cn.vove7.jarvis

import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import org.junit.Test

/**
 * # BaiduAipTest
 *
 * @author 11324
 * 2019/4/8
 */
class BaiduAipTest {
    @Test
    fun testTextOcr() {
        print(BaiduAipHelper.ocr("C:\\Users\\11324\\Desktop\\1.jpg"))
    }
}
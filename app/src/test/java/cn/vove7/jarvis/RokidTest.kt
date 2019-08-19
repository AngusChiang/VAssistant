package cn.vove7.jarvis

import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.netacc.tool.base64
import cn.vove7.vtp.net.NetHelper
import org.junit.Test

/**
 * # RokidTest
 *
 * @author Vove
 * 2019/8/19
 */
class RokidTest {
    @Test
    fun testApi() {
        //房间列表
        NetHelper.get<String>("http://baduxiyang.oicp.net:9001/api/rooms", mapOf(
                "Authorization" to "Basic " + "admin:admin".base64
        )) {
            fail { _, exception ->
                GlobalApp.toastError("若琪用户认证失败，请检查用户名及密码")
                exception.log()
            }
            success { _, s ->
                print(s)
            }
        }

    }
}
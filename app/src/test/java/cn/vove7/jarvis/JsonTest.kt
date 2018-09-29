package cn.vove7.jarvis

import cn.vove7.common.utils.RegUtils
import com.google.gson.Gson
import org.junit.Test

/**
 * # JsonTest
 *
 * @author Administrator
 * 9/26/2018
 */
class JsonTest {
    @Test
    fun t() {
        val gson = Gson()
        val json = "{\"is\":[1,2,3]}"
        val o = gson.fromJson(json, HashMap::class.java)
        print(o)

    }


    /**
     * 截取字符串
     */
    @Test
    fun regexSub() {
        val s = "settings = {\n" +
                "\n" +
                "    number= { title= \"震动强度\", t= 'int', default= 123, range= {1, 10} },\n" +
                "\n" +
                "    text= { title= \"文本测试\", t= 'string', default= '你好' },\n" +
                "\n" +
                "    bool= { title= \"布尔变量\", summary= '我是说明', t= 'boolean', default= false },\n" +
                "\n" +
                "    choice = { title= \"单选\", summary= '选择类型', t= 'single_choice', items= {'一', '二'} }\n" +
                "}\n" +
                "config = registerSettings('lua_sample', settings, 2 )\n" +
                "\n" +
                "print(config.getBoolean('bool'))"


        val ss = "settings = {\n" +
                "    number: { title: \"震动强度\", type: 'int', default: 123, range: [1, 10] },\n" +
                "    text: { title: \"文本测试\", type: 'string', default: '你好' },\n" +
                "    bool: { title: \"布尔变量\", summary: '我是说明', type: 'boolean', default: false },\n" +
                "    choice: { title: \"单选\", summary: '选择类型', type: 'single_choice', items: ['一', '二'] }\n" +
                "}\n" +
                "\n" +
                "config = registerSettings( \"js_sample\" , settings , 120 )\n" +
                "\n" +
                "print(config.getInt('number'))\n" +
                "config.set('number', 1)\n" +
                "print(config.getInt('number'))\n" +
                "\n" +
                "s = config.getString('text')\n" +
                "speak(s)\n" +
                "print(s)\n" +
                "print(config.getBoolean('bool'))"
        val a = "settings[\\S\\s\n]*registerSettings\\([ ]*%[\\\"'](%)[\"']%settings%([0-9]*)[ ]*\\)"
                .replace("%", "[\\S ]*?").toRegex().find(s)
        if (a != null) {
            println(a.groupValues[0])
            println(a.groupValues[1])
            println(a.groupValues[2])
        } else print("err")
    }

    @Test
    fun replaceSrcHeader() {
        arrayOf(
                "require 'accessibility'",
                "require \"accessibility\"\nprint(1)",
                "print(2)\n\n require \"accessibility\"\nprint(1)"
        ).forEach {
            println("lua:\n${RegUtils.replaceLuaHeader(it)}")
            println()
            println()
            println("rhino:\n${RegUtils.replaceRhinoHeader(it)}")
            println()
            println()
        }
    }
}
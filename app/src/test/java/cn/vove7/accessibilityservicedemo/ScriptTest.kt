package cn.vove7.accessibilityservicedemo

import org.junit.Test


class ScriptTest {
    @Test
    fun main() {
        System.out.print("function openApp(s){\n" +
                "    return window.at_api.openApp(s);\n" +
                "}\n" +
                "function openAppByWord(s){\n" +
                "    return window.at_api.openAppByWord(s);\n" +
                "}\n" +
                "function startActivity(p,n){\n" +
                "    return window.at_api.startActivity(p,n);\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "function findFirstNodeById(id){\n" +
                "    return window.as_api.findFirstNodeById(id)\n" +
                "}\n" +
                "function findNodeById(id){\n" +
                "    return window.as_api.findNodeById(id)\n" +
                "}\n" +
                "function findNodeById(text){\n" +
                "    return window.as_api.findFirstNodeByText(text)\n" +
                "}\n" +
                "function findNodeByText(text){\n" +
                "    return window.as_api.findNodeByText(text)\n" +
                "}\n" +
                "console.log(\"加载完成\")\n")
    }

}

package cn.vove7.jarvis

import org.junit.Test


class ScriptTest {
    @Test
    fun main() {
        System.out.print("function openAppByPkg(s){\n" +
                "    return window.at_api.openAppByPkg(s);\n" +
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
                "function findFirstNodeById(viewId){\n" +
                "    return window.as_api.findFirstNodeById(viewId)\n" +
                "}\n" +
                "function findNodeById(viewId){\n" +
                "    return window.as_api.findNodeById(viewId)\n" +
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

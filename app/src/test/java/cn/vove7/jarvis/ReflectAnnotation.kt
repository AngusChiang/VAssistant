package cn.vove7.jarvis

import cn.vove7.common.annotation.ScriptApi
import cn.vove7.common.annotation.ScriptApiClass
import cn.vove7.common.annotation.ScriptApiDocument
import cn.vove7.common.annotation.ScriptApiParamDesc
import cn.vove7.common.utils.GsonHelper
import cn.vove7.common.utils.getOrSetDefault
import org.junit.Test
import org.reflections.Reflections
import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * # ReflectAnnotation
 *
 * @author 11324
 * 2019/3/14
 */
class ReflectAnnotation {
    @Test
    fun an() {
        val hashMap =
            hashMapOf<String, MutableList<FunctionDocument>>()

        val anoClass = Reflections("cn.vove7").getTypesAnnotatedWith(ScriptApiClass::class.java)

        anoClass.forEach { clazz ->
            val classApi = clazz.getAnnotation(ScriptApiClass::class.java) ?: return@forEach
            //类文档展示名
            val classDisplayName = classApi.name.let { n -> if (n.isEmpty()) clazz.simpleName else n }

            println("类: $classDisplayName")
            clazz.declaredMethods.forEach { m ->
                val apiAno = m.getAnnotation(ScriptApi::class.java)
                apiAno?.also { api ->
                    val apiFunName = if (api.summary.isEmpty()) {
                        m.name
                    } else api.summary
                    val docAno = m.getAnnotation(ScriptApiDocument::class.java)

                    val paramSescAno = m.getAnnotation(ScriptApiParamDesc::class.java)

                    val funDoc = docAno?.document?.let { docVal ->
                        if (docVal.isEmpty()) generateFuncDocument(m, paramSescAno) else docVal
                    } ?: generateFuncDocument(m, paramSescAno)

                    hashMap.getOrSetDefault(classDisplayName, mutableListOf())
                            .add(FunctionDocument(apiFunName, funDoc))
                }
            }

        }

        hashMap.keys.forEach {
            println("类:$it")
            hashMap[it]?.forEach {
                println(it.document)
            }
        }

    }

    private fun generateFuncDocument(method: Method, apiparamDesccs: ScriptApiParamDesc?): String {
//        method.parameters
        val descs = apiparamDesccs?.description ?: arrayOf()
        return buildString {
            appendln("fun ${method.name}(${params2String(method.parameters, descs)}): ${method.returnType.simpleName}")

        }
    }

    /**
     * 构造参数
     * @param ss Array<Class<*>>?
     * @param descs Array<String>
     * @return String?
     */
    private fun params2String(ss: Array<Parameter>?, descs: Array<String>): String? {
        if (ss == null) return null
        return buildString {
            ss.withIndex().forEach {
                val desc = "(" + (descs.getOrNull(it.index) ?: "") + ")"
                val text = it.value.name + " " + it.value.type.simpleName
                append(if (it.index == 0) (text + desc) else
                    ", $text")
            }
        }
    }

}

@ScriptApiClass
class FunctionDocument(
        val summary: String,
        val document: String
) {
    @ScriptApi
    @ScriptApiDocument("""
            啦啦啦
        """)
    @ScriptApiParamDesc(["a", "b"])
    fun test(a: Int, b: String) {

    }

    override fun toString(): String {
        return GsonHelper.toJson(this, true)
    }
}


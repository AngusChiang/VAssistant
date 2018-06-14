package cn.vove7.accessibilityservicedemo

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import javax.script.Invocable
import javax.script.ScriptEngineManager

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("cn.vove7.accessibilityservicedemo", appContext.packageName)
    }

    @Test
    fun testScriptOnPhone() {
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByName("JavaScript")
        val scriptPath = "e:/js_header.js"
        engine.eval("load(\"$scriptPath\")")
        val calculator = engine.get("calculator")

        val x = 1
        val y = 3
        val inv = engine as Invocable
        val addResult = inv.invokeMethod(calculator, "add", x, y)
        val subResult = inv.invokeMethod(calculator, "subtract", x, y)
        val mulResult = inv.invokeMethod(calculator, "multiply", x, y)
        val divResult = inv.invokeMethod(calculator, "divide", x, y)
        System.out.println(addResult)
        System.out.println(subResult)
        System.out.println(mulResult)
        System.out.println(divResult)
    }

}

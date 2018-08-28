package cn.vove7.rhino;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    /**
     * Use app context.
     */
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("cn.vove7.rhino.test", appContext.getPackageName());
    }

    @Test
    public void rhinoTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        RhinoHelper helper = new RhinoHelper(appContext);
        helper.execFile("/sdcard/s.js");

    }

}

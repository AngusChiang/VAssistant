package cn.vove7.androlua;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getFeildTest() {
        TestClass s = new TestClass("123");
        Class claszz = s.getClass();

        try {
            Field name = claszz.getDeclaredField("name");
            name.setAccessible(true);
            try {
                System.out.println(name.get(s));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        System.out.print(Arrays.toString(claszz.getDeclaredFields()));

    }
}
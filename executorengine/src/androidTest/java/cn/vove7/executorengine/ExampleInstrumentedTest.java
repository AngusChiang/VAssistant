package cn.vove7.executorengine;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import cn.vove7.executorengine.greendao.DaoMaster;
import cn.vove7.datamanager.executor.entity.MarkedOpen;
import cn.vove7.executorengine.greendao.MarkedAppDao;
import cn.vove7.vtp.app.AppInfo;
import cn.vove7.vtp.app.AppUtil;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
   @Test
   public void useAppContext() {
      // Context of the app under test.
      Context appContext = InstrumentationRegistry.getTargetContext();

      assertEquals("cn.vove7.executorengine.test", appContext.getPackageName());

      MarkedAppDao dao =DaoMaster.newDevSession(appContext,"test").getMarkedAppDao();
      MarkedOpen app= new MarkedOpen();
      app.setKey("微博");
      AppInfo info = AppUtil.INSTANCE.getAppInfo(appContext, "Share", "");
      if(info!=null){
         app.setName(info.getName());
         app.setPackageName(info.getPackageName());
         app.setIcon(info.getIcon());

         dao.insert(app);
      }


   }
}

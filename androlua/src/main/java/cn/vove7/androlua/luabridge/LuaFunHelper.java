package cn.vove7.androlua.luabridge;

import com.luajava.LuaException;
import com.luajava.LuaObject;
import com.luajava.LuaState;

import cn.vove7.androlua.LuaHelper;
import cn.vove7.vtp.log.Vog;

/**
 * @author SYSTEM
 * <p>封装
 * 2018/8/3
 */
public class LuaFunHelper {

   private LuaHelper luaHelper;
   private LuaState L;

   public LuaFunHelper(LuaHelper luaHelper, LuaState l) {
      this.luaHelper = luaHelper;
      L = l;
   }

   public void newLuaThread(String str, Object... args) {
      luaHelper.autoRun(str, args);
   }

   /**
    * 复制运行时信息：require，imported
    *
    * @param from 源
    */
   public void copyRuntimeFrom(LuaState from) {
      try {
         // fixme
         //int G = from.getGlobal("_G");
         //L.setField(G, "_G");
         //int t = from.type(G);
         //L.setGlobal("_G");
         //L.getGlobal("_G");
         //LuaObject GG = L.getLuaObject("_G");
         //GG.setI(0,G);

         Object[] loadeds;
         LuaObject g = from.getLuaObject("luajava");
         LuaObject loaded = g.getField("imported");
         if (!loaded.isNil()) {
            loadeds = loaded.asArray();
         } else return;
         if (loadeds != null) {
            LuaObject require = L.getLuaObject("require");
            //require "bridges"
            require.call("bridges");
            LuaObject _import = L.getLuaObject("import");
            for (Object s : loadeds) {
               Vog.INSTANCE.v(this, "load -- " + s.toString());
               _import.call(s.toString());
            }
         }
      } catch (LuaException e) {
         e.printStackTrace();
      }
   }

   public void newLuaThread(byte[] buf, Object... args) {
      L.setTop(0);
      int ok = L.LloadBuffer(buf, "Thread");
      if (ok == 0) {
         L.getGlobal("debug");
         L.getField(-1, "traceback");
         L.remove(-2);
         L.insert(-2);
         int l = args.length;
         for (Object arg : args) {
            try {
               L.pushObjectValue(arg);
            } catch (LuaException e) {
               luaHelper.handleError(e);
            }
         }
         ok = L.pcall(l, 0, -2 - l);
         if (ok == 0) {
            return;
         } else luaHelper.checkErr(ok);
      }
   }


   public void runFunc(String funcName, Object... args) {
      L.setTop(0);
      L.getGlobal(funcName);
      if (L.isFunction(-1)) {
         L.getGlobal("debug");
         L.getField(-1, "traceback");
         L.remove(-2);
         L.insert(-2);

         int l = args.length;
         for (Object arg : args) {
            try {
               L.pushObjectValue(arg);
            } catch (LuaException e) {
               luaHelper.handleError(e);
            }
         }

         int ok = L.pcall(l, 1, -2 - l);
         if (ok == 0) {
            return;
         } else luaHelper.checkErr(ok);
      }
   }

   public void setField(String key, Object value) throws LuaException {
      L.pushObjectValue(value);
      L.setGlobal(key);
   }
}


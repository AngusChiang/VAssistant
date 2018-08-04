package cn.vove7.androlua.luautils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.luajava.JavaFunction;
import com.luajava.LuaState;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static cn.vove7.androlua.luabridge.LuaUtil.readAll;

/**
 * LuaAssetLoader
 * <p>
 * Created by Vove on 2018/7/31
 */
public class LuaAssetLoader extends JavaFunction {
    private Context context;

    public LuaAssetLoader(Context context, LuaState L) {
        super(L);
        this.context = context;
    }

    @Override
    public int execute() {
        String name = L.toString(-1);

        AssetManager am = context.getAssets();
        try {
            Log.d("Vove :", "assetLoader  ----> " + name);
            InputStream is = am.open(name + ".lua");
            byte[] bytes = readAll(is);
            L.LloadBuffer(bytes, name);
            return 1;
        } catch (Exception e) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(os));
            L.pushString("Cannot load module " + name + ":\n" + os.toString());
            return 1;
        }
    }
}

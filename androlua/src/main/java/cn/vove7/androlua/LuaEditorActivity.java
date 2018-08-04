package cn.vove7.androlua;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.vove7.androlua.luabridge.LuaUtil;
import com.luajava.LuaException;

import java.io.IOException;
import java.util.Arrays;

import cn.vove7.androlua.luautils.LuaEditor;
import cn.vove7.androlua.luautils.LuaManagerI;
import cn.vove7.androlua.luautils.LuaPrinter;


public class LuaEditorActivity extends Activity implements OnClickListener {

    Button execute;

    public LuaEditor source;
    public TextView status;
    public LuaHelper luaHelper;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lua_editor);

        execute = findViewById(R.id.executeBtn);
        execute.setOnClickListener(this);

        source = findViewById(R.id.source);

        status = findViewById(R.id.statusText);
        status.setMovementMethod(ScrollingMovementMethod.getInstance());

        luaHelper = LuaApp.getInstance().getLuaHelper();

        try {
            testFiles = getAssets().list("test");
            Log.d("Vove :", "onCreate  ----> " + Arrays.toString(testFiles));
            if (testFiles.length > 0) {
                source.setText(LuaUtil.getTextFromAsset(this, "test/" + testFiles[0]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    int nowIndex = 0;

    String[] testFiles = new String[0];

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LuaManagerI.E:
                    status.append("E: ");
                    break;
                case LuaManagerI.W:
                    status.append("W: ");
                    break;
            }
            status.append(msg.getData().getString("data"));
            if (!status.isFocused()) {
                int offset = status.getLineCount() * status.getLineHeight();
                if (offset > status.getHeight()) {
                    status.scrollTo(0, offset - status.getHeight());
                }
            }
        }
    };
    LuaPrinter.OnPrint print = new LuaPrinter.OnPrint() {
        @Override
        public void onPrint(int l, String output) {
            Message m = new Message();
            Bundle b = new Bundle();
            b.putString("data", output);
            m.what = l;
            m.setData(b);
            handler.sendMessage(m);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        luaHelper.regPrint(print);
        //serverThread = new RemoteDebugServer(this, luaHelper, handler);
        //serverThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        luaHelper.unRegPrint(print);
        //serverThread.stopped = true;
    }

    private static final String rHeader = "require 'import'\n";

    public void onClick(View view) {
        int len = testFiles.length;
        int i = view.getId();
        if (i == R.id.executeBtn) {
            String src = source.getText().toString();
            status.setText("");
            try {
                luaHelper.evalString(rHeader + src);
            } catch (LuaException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else if (i == R.id.r) {
            nowIndex = (++nowIndex) % len;
            source.setText(LuaUtil.getTextFromAsset(this, "test/" + testFiles[nowIndex]));

        } else if (i == R.id.l) {
            nowIndex = (--nowIndex + len) % len;
            source.setText(LuaUtil.getTextFromAsset(this, "test/" + testFiles[nowIndex]));

        } else if (i == R.id.stop) {
            luaHelper.stop();
        }
    }

    /*
    function f()
print('f()')
end

thread(f)
print('end')
     */
}
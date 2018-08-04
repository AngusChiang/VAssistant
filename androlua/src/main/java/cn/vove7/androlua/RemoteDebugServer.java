package cn.vove7.androlua;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cn.vove7.androlua.luautils.LuaPrinter;

class RemoteDebugServer extends Thread {
    public boolean stopped;
    private Context context;
    private LuaHelper luaHelper;

    public RemoteDebugServer(Context context) {
        this.context = context;
        this.luaHelper = new LuaHelper(context);
    }

    private final static int LISTEN_PORT = 3333;

    @Override
    public void run() {
        stopped = false;
        try (ServerSocket server = new ServerSocket(LISTEN_PORT)) {
            show("Server started on port " + LISTEN_PORT);
            Socket client = server.accept();
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            final DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));

            final StringBuffer buffer = new StringBuffer();
            LuaPrinter.OnPrint print = new LuaPrinter.OnPrint() {
                @Override
                public void onPrint(int l, String output) {
                    buffer.append(output);
                }
            };
            luaHelper.regPrint(print);
            while (!stopped) {
                String data = inputStream.readUTF();
                Log.d("Vove :", "run  ----> " + data);
                luaHelper.safeEvalLua("require 'import'\n" + data);
                outputStream.writeUTF(buffer.toString());
                buffer.setLength(0);
                outputStream.flush();
            }
            show("RemoteDebug finished！");
        } catch (Exception e) {
            e.printStackTrace();
            show(e.getMessage());
        }
    }

    private void show(final String s) {
        Log.d("Vove :", "show  ----> " + s);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
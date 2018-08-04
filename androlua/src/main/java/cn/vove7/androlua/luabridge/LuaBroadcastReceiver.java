package cn.vove7.androlua.luabridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 动态广播接收器
 */
public class LuaBroadcastReceiver extends BroadcastReceiver {

    private OnReceiveListener mRlt;

    public LuaBroadcastReceiver(OnReceiveListener rlt) {
        mRlt = rlt;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mRlt.onReceive(context, intent);
    }

    public interface OnReceiveListener {
        void onReceive(Context context, Intent intent);
    }
}

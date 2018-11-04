package cn.vove7.jarvis.tools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import cn.vove7.common.app.GlobalApp;
import cn.vove7.common.app.GlobalLog;
import cn.vove7.vtp.log.Vog;

/**
 * Created by fujiayi on 2017/11/27.
 */

public class MicrophoneInputStream extends InputStream {
    private static AudioRecord audioRecord;

    private static MicrophoneInputStream is;

    private boolean isStarted = false;

    private static final String TAG = "MicrophoneInputStream";

    private AudioManager mAudioManager = (AudioManager) GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE);

    public MicrophoneInputStream() {
        initAudioSource();
    }
    private void initAudioSource(){
        Vog.INSTANCE.d(this, "MicrophoneInputStream ---> load");
        if (audioRecord == null) {
            int bufferSize = AudioRecord.getMinBufferSize(16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 16;
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    16000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            mAudioManager.setBluetoothScoOn(true);
            mAudioManager.startBluetoothSco();
        }
    }

    public static MicrophoneInputStream getInstance() {

        if (is != null) {
            is.close();
        }
        //    synchronized (MicrophoneInputStream.class) {
        //        if (is == null) {
        is = new MicrophoneInputStream();
        //}
        //}
        //}
        return is;
    }

    public void start() {
        //openBTHeadsetMicro();
        initAudioSource();
        Log.i(TAG, " MicrophoneInputStream start recoding");
        if (audioRecord == null
                || audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalStateException(
                    "startRecording() called on an uninitialized AudioRecord.");
        }
        Vog.INSTANCE.d(this, "start ---> " + toLogFriendlyAudioSource(audioRecord.getAudioSource()));
        audioRecord.getAudioSource();
        audioRecord.startRecording();
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        //Vog.INSTANCE.d(this, "MicrophoneInputStream read ---> begin");
        if (!isStarted) {
            start(); // 建议在CALLBACK_EVENT_ASR_READY事件中调用。
            isStarted = true;
        }
        int count = audioRecord.read(b, off, len);
        //Log.i(TAG, " MicrophoneInputStream read count:" + count);
        return count;
    }

    @Override
    public void close() {
        Log.i(TAG, " MicrophoneInputStream close");
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
                //closeBTHeadsetMicro();
                audioRecord = null;
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            } catch (Exception e) {
                GlobalLog.INSTANCE.err(e);
            }finally {
                isStarted = false;
            }
        }
        is = null;
    }

    public static String toLogFriendlyAudioSource(int source) {
        switch (source) {
            case MediaRecorder.AudioSource.DEFAULT:
                return "DEFAULT";
            case MediaRecorder.AudioSource.MIC:
                return "MIC";
            case MediaRecorder.AudioSource.VOICE_UPLINK:
                return "VOICE_UPLINK";
            case MediaRecorder.AudioSource.VOICE_DOWNLINK:
                return "VOICE_DOWNLINK";
            case MediaRecorder.AudioSource.VOICE_CALL:
                return "VOICE_CALL";
            case MediaRecorder.AudioSource.CAMCORDER:
                return "CAMCORDER";
            case MediaRecorder.AudioSource.VOICE_RECOGNITION:
                return "VOICE_RECOGNITION";
            case MediaRecorder.AudioSource.VOICE_COMMUNICATION:
                return "VOICE_COMMUNICATION";
            case MediaRecorder.AudioSource.REMOTE_SUBMIX:
                return "REMOTE_SUBMIX";
            case MediaRecorder.AudioSource.UNPROCESSED:
                return "UNPROCESSED";
            //case MediaRecorder.AudioSource.RADIO_TUNER:
            //    return "RADIO_TUNER";
            //case MediaRecorder.AudioSource.HOTWORD:
            //    return "HOTWORD";
            //case MediaRecorder.AudioSource.AUDIO_SOURCE_INVALID:
            //    return "AUDIO_SOURCE_INVALID";
            default:
                return "unknown source " + source;
        }
    }

    private void openBTHeadsetMicro() {
        if (isBTHSConnect()) {
            try {
                AudioManager mAudioManager = (AudioManager) GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE);
                Context app=GlobalApp.APP;
                mAudioManager.startBluetoothSco();
                app.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                        if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                            mAudioManager.setBluetoothScoOn(true);  //打开SCO
                            //mRecorder.start();//开始录音
                            app.unregisterReceiver(this);  //别遗漏
                        }else{//等待一秒后再尝试启动SCO
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mAudioManager.startBluetoothSco();
                        }
                    }
                }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
                //mAudioManager.setBluetoothScoOn(true);
            } catch (Exception e) {
                GlobalLog.INSTANCE.err(e);
            }
        }
    }

    private boolean isBTHSConnect() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        boolean is = BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        Vog.INSTANCE.d(this, "isBTHSConnect ---> 蓝牙连接" + is);
        return is;
    }

    private void closeBTHeadsetMicro() {
        if (isBTHSConnect()) {
            try {
                AudioManager mAudioManager = (AudioManager) GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            } catch (Exception e) {
                GlobalLog.INSTANCE.err(e);
            }
        }
    }
}

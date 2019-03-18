package cn.vove7.jarvis.speech.baiduspeech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import cn.vove7.common.app.GlobalApp;
import cn.vove7.common.app.GlobalLog;
import cn.vove7.vtp.log.Vog;

/**
 * Baidu语音识别 自定义音频流
 * <p>
 * 保留Java 兼容
 */

public class MicrophoneInputStream extends InputStream {
    private static AudioRecord audioRecord;

    private static MicrophoneInputStream is;

    private boolean isStarted = false;

    private static final String TAG = "MicrophoneInputStream";

    private final Object lock = new Object();
    private AudioManager mAudioManager = (AudioManager) GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE);

    public MicrophoneInputStream() {
    }

    private void initAudioSource() {
        Vog.INSTANCE.d("MicrophoneInputStream ---> load");
        initSCO();

        if (audioRecord == null) {
            int bufferSize = AudioRecord.getMinBufferSize(16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 16;
            audioRecord = new AudioRecord(
                    //AppConfig.INSTANCE.getIS_SYS_APP() ? MediaRecorder.AudioSource.VOICE_CALL :
                    MediaRecorder.AudioSource.VOICE_CALL,
                    16000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        }
    }

    public static MicrophoneInputStream getInstance() {

        if (is != null) is.close();
        is = new MicrophoneInputStream();
        return is;
    }

    public void start() {
        initAudioSource();
        Log.i(TAG, " MicrophoneInputStream start recoding");
        if (audioRecord == null
                || audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalStateException(
                    "startRecording() called on an uninitialized AudioRecord.");
        }
        Vog.INSTANCE.d("start ---> " + toLogFriendlyAudioSource(audioRecord.getAudioSource()));
        audioRecord.startRecording();
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        //Vog.INSTANCE.d( "MicrophoneInputStream read ---> begin");
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
                audioRecord.release();
                audioRecord = null;
                //closeBTHeadsetMicro();
                closeSCO();

            } catch (Exception e) {
                GlobalLog.INSTANCE.err(e);
            } finally {
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

    private BluetoothHeadset bluetoothHeadset;
    private BluetoothDevice bluetoothDevice;
    private BluetoothProfile.ServiceListener blueHeadsetListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i("blueHeadsetListener>>", "onServiceDisconnected:" + profile);
            if (profile == BluetoothProfile.HEADSET) {
                if (bluetoothHeadset != null)
                    bluetoothHeadset.stopVoiceRecognition(bluetoothDevice);
                mAudioManager.stopBluetoothSco();
                mAudioManager.setBluetoothScoOn(false);
                bluetoothHeadset = null;
            }
//            initBlueToothHeadset();
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.i("blueHeadsetListener", "onServiceConnected:" + profile);
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = (BluetoothHeadset) proxy;
                scoEnable();
                Log.i("sco", mAudioManager.isBluetoothA2dpOn() + "," + mAudioManager.isBluetoothScoOn());
                if (bluetoothHeadset.getConnectedDevices().size() > 0) {
                    bluetoothDevice = bluetoothHeadset.getConnectedDevices().get(0);
                    Log.i("Main2Activity:", bluetoothHeadset.startVoiceRecognition(bluetoothDevice) + "");

                }
            }
        }
    };

    private void scoEnable() {
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.stopBluetoothSco();
            mAudioManager.setBluetoothScoOn(false);
        }
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.startBluetoothSco();
    }

    private void initBlueToothHeadset() {
        BluetoothAdapter adapter;
        BluetoothManager bm = (BluetoothManager) GlobalApp.APP.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bm.getAdapter();

        adapter.getProfileProxy(GlobalApp.APP, blueHeadsetListener, BluetoothProfile.HEADSET);
    }

    private void initSCO() {
        //if (!isBTHSConnect()) return;
        //Context app = GlobalApp.APP;
        //IntentFilter newintent = new IntentFilter();
        //newintent.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        //newintent.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        //app.registerReceiver(mSCOHeadsetAudioState, newintent);

        AudioManager mAudioManager = (AudioManager) GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.startBluetoothSco();

    }

    private void closeSCO() {
        AudioManager mAudioManager = (AudioManager) GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setBluetoothScoOn(false);
        mAudioManager.stopBluetoothSco();
    }

    private BroadcastReceiver mSCOHeadsetAudioState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            //if(DEBUG)
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            Log.e("SCO", " mSCOHeadsetAudioState--->onReceive:" + state);
            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                Log.i("SCO", "SCO_AUDIO_STATE_CONNECTED");
                lock.notify();
                GlobalApp.APP.unregisterReceiver(this);
            } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                Log.i("SCO  ", "SCO_AUDIO_STATE_DISCONNECTED");
            }
        }
    };

    private void openBTHeadsetMicro() {
        if (isBTHSConnect()) {
            mAudioManager.startBluetoothSco();
            synchronized (lock) {
                try {
                    long b = System.currentTimeMillis();
                    lock.wait(3000);
                    long end = System.currentTimeMillis();
                    if (end - b >= 2990) {//timeout
                        throw new InterruptedException("蓝牙通道开启失败");
                    } else {
                        Vog.INSTANCE.d("openBTHeadsetMicro ---> 蓝牙通道开启成功");
                    }
                } catch (InterruptedException e) {
                    GlobalApp.Companion.toastError("蓝牙通道开启失败", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean isBTHSConnect() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        boolean is = BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        Vog.INSTANCE.d("isBTHSConnect ---> 蓝牙连接" + is);
        return is;
    }

    private void closeBTHeadsetMicro() {
        if (isBTHSConnect()) {
            try {
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            } catch (Exception e) {
                GlobalLog.INSTANCE.err(e);
            }
        }
    }
}

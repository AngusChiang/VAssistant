package cn.vove7.jarvis.speech.baiduspeech;

import android.media.AudioRecord;

import com.baidu.speech.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;

import cn.vove7.vtp.log.Vog;

public class MicInputStream extends InputStream {
    private String TAG = MicInputStream.class.getSimpleName();
    private static final int DEFAULT_BUFFER_SIZE = 160000;
    private AudioRecord mAudioRecord;

    public static MicInputStream instance(){
        return new MicInputStream();
    }

    public MicInputStream() {
        Vog.INSTANCE.d("打开麦克风");
        try {
            this.mAudioRecord = new AudioRecord(0, 16000, 16, 2, 160000);
            LogUtil.i(this.TAG, "startRecordingAndCheckStatus recorder status is " + this.mAudioRecord.getState());
            this.mAudioRecord.startRecording();
            int var3 = 0;
            byte[] var4 = new byte[32];

            for (int var5 = 0; var5 < 10; ++var5) {
                int var6 = this.mAudioRecord.read(var4, 0, var4.length);
                if (var6 > 0) {
                    var3 += var6;
                    break;
                }
            }

            if (var3 <= 0) {
                this.mAudioRecord.release();
                new Exception("bad recorder, read(byte[])");
            }
        } catch (Exception var15) {
            var15.printStackTrace();
        } finally {
            label143:
            {
                if (this.mAudioRecord == null || this.mAudioRecord.getRecordingState() == 3) {
                    int var10000 = this.mAudioRecord.getState();
                    AudioRecord var10001 = this.mAudioRecord;
                    if (var10000 != 0) {
                        break label143;
                    }
                }

                try {
                    this.mAudioRecord.release();
                } catch (Exception var14) {
                    var14.printStackTrace();
                }

                LogUtil.d(this.TAG, "recorder start failed, RecordingState=" + this.mAudioRecord.getRecordingState());
            }

        }

    }

    public int read(byte[] var1, int var2, int var3) throws IOException {
        if (this.mAudioRecord == null) {
            throw new IOException("audio recorder is null");
        } else {
            int var4 = this.mAudioRecord.read(var1, var2, var3);
            LogUtil.v(this.TAG, " AudioRecord read: len:" + var4 + " byteOffset:" + var2 + " byteCount:" + var3);
            if (var4 >= 0 && var4 <= var3) {
                return var4;
            } else {
                throw new IOException("audio recdoder read error, len = " + var4);
            }
        }
    }

    public void close() throws IOException {
        if (this.mAudioRecord != null) {
            this.mAudioRecord.release();
        }

    }

    public int read() throws IOException {
        throw new IOException("read not support");
    }
}
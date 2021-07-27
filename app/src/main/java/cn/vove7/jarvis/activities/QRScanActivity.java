package cn.vove7.jarvis.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.king.zxing.CameraConfig;
import com.king.zxing.CameraScan;
import com.king.zxing.CaptureActivity;
import com.king.zxing.DecodeConfig;
import com.king.zxing.DecodeFormatManager;
import com.king.zxing.analyze.MultiFormatAnalyzer;

import java.lang.reflect.Field;

import androidx.constraintlayout.widget.ConstraintLayout;
import cn.vove7.common.bridges.UtilBridge;
import cn.vove7.jarvis.R;
import cn.vove7.jarvis.tools.MyCameraScan;

public class QRScanActivity extends CaptureActivity {

    ImageView pointView;

    @Override
    public int getLayoutId() {
        return R.layout.qr_scan_activity;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        qrCallback = (UtilBridge.QrCallback) getIntent().getSerializableExtra("callback");
        pointView = findViewById(R.id.pointView);
    }

    UtilBridge.QrCallback qrCallback;

    @Override
    public void initCameraScan() {
        try {
            Field s = CaptureActivity.class.getDeclaredField("mCameraScan");
            s.setAccessible(true);
            CameraScan sc = new MyCameraScan(this, previewView);
            sc.setOnScanResultCallback(this);
            s.set(this, sc);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        //初始化解码配置
        DecodeConfig decodeConfig = new DecodeConfig();
        decodeConfig.setHints(DecodeFormatManager.ALL_HINTS)////设置解码
                .setSupportVerticalCode(true)//设置是否支持扫垂直的条码 （增强识别率，相应的也会增加性能消耗）
                .setSupportLuminanceInvert(true)//设置是否支持识别反色码，黑白颜色反转（增强识别率，相应的也会增加性能消耗）
                .setAreaRectRatio(1f)//设置识别区域比例，默认0.8，设置的比例最终会在预览区域裁剪基于此比例的一个矩形进行扫码识别
//                .setAreaRectVerticalOffset(0)//设置识别区域垂直方向偏移量，默认为0，为0表示居中，可以为负数
//                .setAreaRectHorizontalOffset(0)//设置识别区域水平方向偏移量，默认为0，为0表示居中，可以为负数
                .setFullAreaScan(true);//设置是否全区域识别，默认false

        //获取CameraScan，里面有扫码相关的配置设置。CameraScan里面包含部分支持链式调用的方法，即调用返回是CameraScan本身的一些配置建议在startCamera之前调用。
        getCameraScan().setPlayBeep(true)//设置是否播放音效，默认为false
                .setVibrate(true)//设置是否震动，默认为false
                .setCameraConfig(new CameraConfig())//设置相机配置信息，CameraConfig可覆写options方法自定义配置
                .setNeedAutoZoom(true)//二维码太小时可自动缩放，默认为false
                .setNeedTouchZoom(true)//支持多指触摸捏合缩放，默认为true
                .setDarkLightLux(45f)//设置光线足够暗的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
                .setBrightLightLux(100f)//设置光线足够明亮的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
                .bindFlashlightView(ivFlashlight)//绑定手电筒，绑定后可根据光线传感器，动态显示或隐藏手电筒按钮
                .setOnScanResultCallback(this)//设置扫码结果回调，需要自己处理或者需要连扫时，可设置回调，自己去处理相关逻辑
                .setAnalyzer(new MultiFormatAnalyzer(decodeConfig))//设置分析器,DecodeConfig可以配置一些解码时的配置信息，如果内置的不满足您的需求，你也可以自定义实现，
                .setAnalyzeImage(true)//设置是否分析图片，默认为true。如果设置为false，相当于关闭了扫码识别功能
                .startCamera();//启动预览
    }

    @Override
    protected void onStop() {
        super.onStop();
        getCameraScan().setAnalyzeImage(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCameraScan().setAnalyzeImage(true);
    }

    private void showPoints(ResultPoint[] ps) {
        Log.d("CustomCaptureActivity", "showPoints  ----> " + ps.length);
        int x = 0;
        int y = 0;
        for (ResultPoint p : ps) {
            x += p.getX();
            y += p.getY();
        }
        x /= ps.length;
        y /= ps.length;

        Size s = ((MyCameraScan) getCameraScan()).getResolution();

        Log.d("CustomCaptureActivity", "showPoints  ----> " + s);
        int w1 = previewView.getWidth();
        int h1 = previewView.getHeight();

        int w2, h2;
        if (w1 > h1) {
            w2 = Math.max(s.getHeight(), s.getWidth());
            h2 = Math.min(s.getHeight(), s.getWidth());
        } else {
            w2 = Math.min(s.getHeight(), s.getWidth());
            h2 = Math.max(s.getHeight(), s.getWidth());
        }
        x = (int) (x * ((float) w1) / w2);
        y = (int) (y * ((float) h1) / h2);

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) pointView.getLayoutParams();
        lp.setMargins(x - pointView.getHeight() / 2, y - pointView.getHeight() / 2, 0, 0);
        pointView.setLayoutParams(lp);
        pointView.setVisibility(View.VISIBLE);
        AnimatorSet animSet = new AnimatorSet();

        animSet.play(
                ObjectAnimator.ofFloat(pointView, "alpha", 0.8f, 1f).setDuration(500)
        ).with(
                ObjectAnimator.ofFloat(pointView, "scaleX", 0.75f, 1f).setDuration(500)
        ).with(
                ObjectAnimator.ofFloat(pointView, "scaleY", 0.75f, 1f).setDuration(500)
        );
        animSet.start();
    }

    @Override
    public boolean onScanResultCallback(Result result) {
        ResultPoint[] ps = result.getResultPoints();
        if (ps != null) {
            showPoints(ps);
        }
        getCameraScan().setAnalyzeImage(false);
        getCameraScan().stopCamera();
        return notifyResult(result);
    }

    private boolean notifyResult(Result result) {
        UtilBridge.QrCallback callback = qrCallback;
        qrCallback = null;
        if (callback != null) {
            previewView.postDelayed(() -> {
                if (result != null) {
                    callback.onResult(result.getText());
                } else {
                    callback.onResult(null);
                }
                finish();
            }, 800);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        notifyResult(null);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        notifyResult(null);
        super.onDestroy();
    }
}
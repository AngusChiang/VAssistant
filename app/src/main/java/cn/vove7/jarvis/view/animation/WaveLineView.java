package cn.vove7.jarvis.view.animation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 正弦曲线
 *
 */
public class WaveLineView extends View {

    private static final int DEF_HEIGHT = 60;

    private Paint waveFirstPaint;
    private Paint waveSecondPaint;
    private float waveFirstAmplifier;
    private float waveSecondAmplifier;
    private float waveFirstFrequency;
    private float waveSecondFrequency;
    private float waveFirstPhase;
    private float waveSecondPhase;
    private int waveLineFirstWidth;
    private int waveLineSecondWidth;
    private int viewWidth;
    private float viewCenterY;
    private int waveFirstColor;
    private int waveSecondColor;

    public WaveLineView(Context context) {
        this(context, null);
    }

    public WaveLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        waveFirstColor = Color.parseColor("#fefefe");//第一条线颜色
        waveSecondColor = Color.parseColor("#eeeeee");//第二条线颜色
        waveLineFirstWidth = 7;//第一条线的宽度，即粗细
        waveLineSecondWidth = 5;//第二条线的宽度，即粗细
        waveFirstAmplifier = 60.0f;//第一条线的振幅
        waveSecondAmplifier = 20.0f;//第二条线的振幅
        waveFirstPhase = 45.0f;//第一条线的相位，初始X轴偏移
        waveSecondPhase = 45.0f;//第二条线的相位，初始X轴偏移
        waveFirstFrequency = 0.8f;//第一条线的频率，可改变波长
        waveSecondFrequency = 1.0f;//第二条线的频率，可改变波长
        initTools();
    }

    private void initTools() {
        waveFirstPaint = new Paint();
        waveFirstPaint.setColor(waveFirstColor);
        waveFirstPaint.setAntiAlias(true);
        waveFirstPaint.setStyle(Paint.Style.FILL);
        waveFirstPaint.setStrokeJoin(Paint.Join.ROUND);
        waveFirstPaint.setStrokeCap(Paint.Cap.ROUND);
        waveFirstPaint.setStrokeWidth(waveLineFirstWidth);
        waveSecondPaint = new Paint();
        waveSecondPaint.setColor(waveSecondColor);
        waveSecondPaint.setAntiAlias(true);
        waveSecondPaint.setStyle(Paint.Style.FILL);
        waveSecondPaint.setStrokeJoin(Paint.Join.ROUND);
        waveSecondPaint.setStrokeCap(Paint.Cap.ROUND);
        waveSecondPaint.setStrokeWidth(waveLineSecondWidth);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < viewWidth - 1; i++) {
            canvas.drawLine((float) i, viewCenterY - waveFirstAmplifier * (float) (Math.sin(waveFirstPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveFirstFrequency * i / viewWidth)), (float) (i + 1), viewCenterY - waveFirstAmplifier * (float) (Math.sin(waveFirstPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveFirstFrequency * (i + 1) / viewWidth)), waveFirstPaint);
            canvas.drawLine((float) i, viewCenterY - waveSecondAmplifier * (float) (Math.sin(-waveSecondPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveSecondFrequency * i / viewWidth)), (float) (i + 1), viewCenterY - waveSecondAmplifier * (float) (Math.sin(-waveSecondPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveSecondFrequency * (i + 1) / viewWidth)), waveSecondPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewCenterY = h / 2;
        waveFirstAmplifier = (waveFirstAmplifier * 2 > h) ? (h / 2) : waveFirstAmplifier;
        waveSecondAmplifier = (waveSecondAmplifier * 2 > h) ? (h / 2) : waveSecondAmplifier;
        waveAnim();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightMeasure;
        if (heightMeasureMode == MeasureSpec.AT_MOST || heightMeasureMode == MeasureSpec.UNSPECIFIED) {
            heightMeasure = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_HEIGHT, getResources().getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightMeasure, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void waveAnim() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0F, 1.F);
        valueAnimator.setDuration(4000);//控制移动快慢，值越小越快
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);//重新启动
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限重复
        valueAnimator.setInterpolator(new LinearInterpolator());//速率变化
        valueAnimator.addUpdateListener(animation -> {
            Float aFloat = Float.valueOf(animation.getAnimatedValue().toString());
            waveFirstPhase = 360.F * aFloat;
            waveSecondPhase = 360.F * aFloat;
            invalidate();
        });
        valueAnimator.start();
    }
}

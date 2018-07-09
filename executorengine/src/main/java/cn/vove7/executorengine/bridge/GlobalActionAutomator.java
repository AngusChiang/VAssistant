package cn.vove7.executorengine.bridge;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.ViewConfiguration;

import cn.vove7.executorengine.model.ResultBox;
import cn.vove7.executorengine.model.ScreenMetrics;
import cn.vove7.vtp.log.Vog;


public class GlobalActionAutomator {

    private AccessibilityService mService;
    private Handler mHandler;
    private ScreenMetrics mScreenMetrics = new ScreenMetrics();

    public GlobalActionAutomator(AccessibilityService mService, Handler mHandler) {
        this.mService = mService;
        this.mHandler = mHandler;
    }

    public void setService(AccessibilityService mService) {
        this.mService = mService;
    }

    public GlobalActionAutomator(@Nullable Handler handler) {
        mHandler = handler;
    }

    public boolean back() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public boolean home() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    public boolean powerDialog() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
    }

    private boolean performGlobalAction(int globalAction) {
        if (mService == null)
            return false;
        return mService.performGlobalAction(globalAction);
    }

    public boolean notifications() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
    }

    public boolean quickSettings() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS);
    }

    public boolean recents() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean splitScreen() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean gesture(long start, long duration, int[]... points) {
        Path path = pointsToPath(points);
        return gestures(new GestureDescription.StrokeDescription(path, start, duration));
    }

    private Path pointsToPath(int[][] points) {
        Path path = new Path();
        path.moveTo(scaleX(points[0][0]), scaleY(points[0][1]));
        for (int i = 1; i < points.length; i++) {
            int[] point = points[i];
            path.lineTo(scaleX(point[0]), scaleY(point[1]));
        }
        return path;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void gestureAsync(long start, long duration, int[]... points) {
        Path path = pointsToPath(points);
        gesturesAsync(new GestureDescription.StrokeDescription(path, start, duration));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean gestures(GestureDescription.StrokeDescription... strokes) {
        if (mService == null)
            return false;
        GestureDescription.Builder builder = new GestureDescription.Builder();
        for (GestureDescription.StrokeDescription stroke : strokes) {
            builder.addStroke(stroke);
        }
        if (mHandler == null) {
            return gesturesWithoutHandler(builder.build());
        } else {
            return gesturesWithHandler(builder.build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean gesturesWithHandler(GestureDescription description) {
        final ResultBox<Boolean> result = new ResultBox<>();
        mService.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                result.setAndNotify(true);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                result.setAndNotify(false);
            }
        }, mHandler);
        return result.blockedGet();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean gesturesWithoutHandler(GestureDescription description) {
        prepareLooperIfNeeded();
        final ResultBox<Boolean> result = new ResultBox<>(false);
        Handler handler = new Handler(Looper.myLooper());
        mService.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                result.set(true);
                quitLoop();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                result.set(false);
                quitLoop();
            }
        }, handler);
        Looper.loop();
        return result.get();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void gesturesAsync(GestureDescription.StrokeDescription... strokes) {
        if (mService == null)
            return;
        GestureDescription.Builder builder = new GestureDescription.Builder();
        for (GestureDescription.StrokeDescription stroke : strokes) {
            builder.addStroke(stroke);
        }
        mService.dispatchGesture(builder.build(), null, null);
    }

    private void quitLoop() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quit();
        }
    }

    private void prepareLooperIfNeeded() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }

    public boolean click(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return press(x, y, ViewConfiguration.getTapTimeout() + 50);
        } else {
            Vog.INSTANCE.d(this, "需SDK版本->N");
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean press(int x, int y, int delay) {
        return gesture(0, delay, new int[]{x, y});
    }

    public boolean longClick(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return gesture(0, ViewConfiguration.getLongPressTimeout() + 200, new int[]{x, y});
        } else {
            Vog.INSTANCE.d(this, "需SDK版本->N");
            return false;
        }
    }

    private int scaleX(int x) {
        if (mScreenMetrics == null)
            return x;
        return mScreenMetrics.scaleX(x);
    }

    private int scaleY(int y) {
        if (mScreenMetrics == null)
            return y;
        return mScreenMetrics.scaleY(y);
    }

    public boolean swipe(int x1, int y1, int x2, int y2, int delay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return gesture(0, delay, new int[]{x1, y1}, new int[]{x2, y2});
        } else {
            Vog.INSTANCE.d(this, "需SDK版本->N");
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean scrollUp() {
        return swipe(550, 1600, 550, 300, 400);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean scrollDown() {
        return swipe(550, 300, 550, 1600, 400);
    }

}

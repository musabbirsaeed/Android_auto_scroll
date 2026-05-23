package com.example.tiktokautoscroll;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TikTokAccessibilityService extends AccessibilityService {
    private static final String TIKTOK_PACKAGE_LEGACY = "com.zhiliaoapp.musically";
    private static final String TIKTOK_PACKAGE_US = "com.ss.android.ugc.trill";
    private static final String PREF_ENABLED = "enabled";
    private static final String PREF_DIRECTION = "direction"; // up | down

    private WindowManager windowManager;
    private View overlayView;
    private SharedPreferences prefs;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int lastRemainingSeconds = -1;
    private long lastSwipeAt = 0L;

    private final Runnable watchdog = new Runnable() {
        @Override
        public void run() {
            if (isEnabled()) {
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root != null) {
                    evaluateVideoEnd(root);
                    root.recycle();
                }
            }
            handler.postDelayed(this, 700);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        showOverlay();
        handler.post(watchdog);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isTikTokPackage(event.getPackageName())) {
            return;
        }
        if (!isEnabled()) {
            return;
        }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            evaluateVideoEnd(root);
            root.recycle();
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        handler.removeCallbacks(watchdog);
        hideOverlay();
        super.onDestroy();
    }

    private void evaluateVideoEnd(AccessibilityNodeInfo root) {
        int remaining = extractRemainingTimeSeconds(root);
        if (remaining < 0) {
            return;
        }

        long now = System.currentTimeMillis();
        boolean justEnded = lastRemainingSeconds > 2 && remaining <= 1;
        boolean cooldownOver = now - lastSwipeAt > 1500;

        if (justEnded && cooldownOver) {
            performSwipe();
            lastSwipeAt = now;
        }
        lastRemainingSeconds = remaining;
    }

    private int extractRemainingTimeSeconds(AccessibilityNodeInfo root) {
        List<String> texts = new ArrayList<>();
        collectTexts(root, texts);

        Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
        int smallestSeconds = Integer.MAX_VALUE;

        for (String t : texts) {
            Matcher m = timePattern.matcher(t);
            while (m.find()) {
                int min = Integer.parseInt(m.group(1));
                int sec = Integer.parseInt(m.group(2));
                int total = min * 60 + sec;
                if (total < smallestSeconds) {
                    smallestSeconds = total;
                }
            }
        }

        return smallestSeconds == Integer.MAX_VALUE ? -1 : smallestSeconds;
    }

    private void collectTexts(AccessibilityNodeInfo node, List<String> output) {
        if (node == null) return;
        CharSequence text = node.getText();
        if (text != null) {
            output.add(text.toString());
        }
        CharSequence desc = node.getContentDescription();
        if (desc != null) {
            output.add(desc.toString());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectTexts(child, output);
                child.recycle();
            }
        }
    }

    private boolean isEnabled() {
        return prefs.getBoolean(PREF_ENABLED, false);
    }

    private boolean isTikTokPackage(CharSequence packageName) {
        if (packageName == null) {
            return false;
        }
        return TIKTOK_PACKAGE_LEGACY.contentEquals(packageName)
                || TIKTOK_PACKAGE_US.contentEquals(packageName);
    }

    private void performSwipe() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }
                int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        float x = width / 2f;
        boolean swipeUp = "up".equals(prefs.getString(PREF_DIRECTION, "up"));

        float startY = swipeUp ? height * 0.78f : height * 0.22f;
        float endY = swipeUp ? height * 0.22f : height * 0.78f;

        Path path = new Path();
        path.moveTo(x, startY);
        path.lineTo(x, endY);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 180);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();
        dispatchGesture(gesture, null, null);
    }

    private void showOverlay() {
        if (overlayView != null) {
            return;
        }

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_controls, null);
        TextView modeText = overlayView.findViewById(R.id.modeText);
        Button toggleButton = overlayView.findViewById(R.id.toggleButton);
        Button directionButton = overlayView.findViewById(R.id.directionButton);

        refreshOverlayText(modeText, toggleButton, directionButton);

        toggleButton.setOnClickListener(v -> {
            prefs.edit().putBoolean(PREF_ENABLED, !isEnabled()).apply();
            refreshOverlayText(modeText, toggleButton, directionButton);
        });

        directionButton.setOnClickListener(v -> {
            String current = prefs.getString(PREF_DIRECTION, "up");
            prefs.edit().putString(PREF_DIRECTION, "up".equals(current) ? "down" : "up").apply();
            refreshOverlayText(modeText, toggleButton, directionButton);
        });

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 30;
        params.y = 180;

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            private float touchX;
            private float touchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        params.x = startX + (int) (event.getRawX() - touchX);
                        params.y = startY + (int) (event.getRawY() - touchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                    default:
                        return false;
                }
            }
        });

        windowManager.addView(overlayView, params);
    }

    private void refreshOverlayText(TextView modeText, Button toggleButton, Button directionButton) {
        boolean enabled = isEnabled();
        String direction = prefs.getString(PREF_DIRECTION, "up");
        modeText.setText(String.format(Locale.US, "Auto: %s | Direction: %s", enabled ? "ON" : "OFF", direction.toUpperCase(Locale.US)));
        toggleButton.setText(enabled ? "Pause" : "Start");
        directionButton.setText("Switch Direction");
    }

    private void hideOverlay() {
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }
}

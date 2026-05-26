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
import android.provider.Settings;
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
    private static final String YOUTUBE_PACKAGE = "com.google.android.youtube";
    private static final String TIKTOK_PACKAGE_LEGACY = "com.zhiliaoapp.musically";
    private static final String TIKTOK_PACKAGE_US = "com.ss.android.ugc.trill";
    private static final String PREF_ENABLED = "enabled";
    private static final long POLL_INTERVAL_MS = 700;
    private static final long SWIPE_COOLDOWN_MS = 1800;

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
                    if (isYouTubePackage(root.getPackageName())) {
                        evaluateVideoEnd(root);
                    }
                    root.recycle();
                }
            }
            handler.postDelayed(this, POLL_INTERVAL_MS);
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
        if (!isEnabled() || !isYouTubePackage(event.getPackageName())) return;
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

    private boolean isEnabled() {
        return prefs != null && prefs.getBoolean(PREF_ENABLED, false);
    }

    private boolean isYouTubePackage(CharSequence packageName) {
        return packageName != null && YOUTUBE_PACKAGE.contentEquals(packageName);
    }

    private void evaluateVideoEnd(AccessibilityNodeInfo root) {
        int remaining = extractRemainingTimeSeconds(root);
        if (remaining < 0) return;

        long now = System.currentTimeMillis();
        boolean justEnded = lastRemainingSeconds > 2 && remaining <= 1;
        boolean cooldownOver = now - lastSwipeAt > SWIPE_COOLDOWN_MS;

        if (justEnded && cooldownOver) {
            performSwipeUp();
            lastSwipeAt = now;
        }
        lastRemainingSeconds = remaining;
    }

    private int extractRemainingTimeSeconds(AccessibilityNodeInfo root) {
        List<String> texts = new ArrayList<>();
        collectTexts(root, texts);

        Pattern mmss = Pattern.compile("(\d{1,2}):(\d{2})");
        Pattern secOnly = Pattern.compile("(?:^|\s)(\d{1,3})s(?:\s|$)", Pattern.CASE_INSENSITIVE);

        int smallestSeconds = Integer.MAX_VALUE;
        for (String t : texts) {
            Matcher m1 = mmss.matcher(t);
            while (m1.find()) {
                int min = Integer.parseInt(m1.group(1));
                int sec = Integer.parseInt(m1.group(2));
                smallestSeconds = Math.min(smallestSeconds, min * 60 + sec);
            }
            Matcher m2 = secOnly.matcher(t);
            while (m2.find()) {
                int sec = Integer.parseInt(m2.group(1));
                smallestSeconds = Math.min(smallestSeconds, sec);
            }
        }
        return smallestSeconds == Integer.MAX_VALUE ? -1 : smallestSeconds;
    }

    private void collectTexts(AccessibilityNodeInfo node, List<String> output) {
        if (node == null) return;
        CharSequence text = node.getText();
        if (text != null) output.add(text.toString());
        CharSequence desc = node.getContentDescription();
        if (desc != null) output.add(desc.toString());
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectTexts(child, output);
                child.recycle();
            }
        }
    }

    private void performSwipeUp() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        int width = getResources().getDisplayMetrics().widthPixels;
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

        Path path = new Path();
        path.moveTo(width / 2f, height * 0.78f);
        path.lineTo(width / 2f, height * 0.22f);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 180);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();
        dispatchGesture(gesture, null, null);
    }

    private void showOverlay() {
        if (overlayView != null) return;
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_controls, null);
        TextView modeText = overlayView.findViewById(R.id.modeText);
        Button toggleButton = overlayView.findViewById(R.id.toggleButton);
        Button directionButton = overlayView.findViewById(R.id.directionButton);

        directionButton.setVisibility(View.GONE);
        refreshOverlayText(modeText, toggleButton);

        toggleButton.setOnClickListener(v -> {
            if (prefs != null) prefs.edit().putBoolean(PREF_ENABLED, !isEnabled()).apply();
            refreshOverlayText(modeText, toggleButton);
        });

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 30;
        params.y = 180;

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY; private float touchX, touchY;
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: startX=params.x; startY=params.y; touchX=event.getRawX(); touchY=event.getRawY(); return false;
                    case MotionEvent.ACTION_MOVE: params.x=startX+(int)(event.getRawX()-touchX); params.y=startY+(int)(event.getRawY()-touchY); windowManager.updateViewLayout(overlayView, params); return true;
                    default: return false;
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) return;
        try { windowManager.addView(overlayView, params); } catch (SecurityException | IllegalStateException ignored) { overlayView = null; }
    }

    private void refreshOverlayText(TextView modeText, Button toggleButton) {
        boolean enabled = isEnabled();
        modeText.setText(String.format(Locale.US, "Auto: %s | Direction: UP", enabled ? "ON" : "OFF"));
        toggleButton.setText(enabled ? "Pause" : "Start");
    }

    private void hideOverlay() {
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }
}

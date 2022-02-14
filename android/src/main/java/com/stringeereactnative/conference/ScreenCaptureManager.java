package com.stringeereactnative.conference;

import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.stringee.video.StringeeScreenCapture;

public class ScreenCaptureManager {
    private static ScreenCaptureManager instance;
    private ActivityEventListener listener;
    private StringeeScreenCapture screenCapture;


    private ScreenCaptureManager(ReactApplicationContext context) {
        context.addActivityEventListener(new ActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (listener != null) {
                    listener.onActivityResult(activity, requestCode, resultCode, data);
                }
            }

            @Override
            public void onNewIntent(Intent intent) {

            }
        });

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            screenCapture = new StringeeScreenCapture.Builder().buildWithActivity(context.getCurrentActivity());
        }
    }

    public static ScreenCaptureManager getInstance(ReactApplicationContext context) {
        if (instance == null) {
            instance = new ScreenCaptureManager(context);
        }
        return instance;
    }

    public void getActivityResult(ActivityEventListener listener) {
        this.listener = listener;
    }

    public StringeeScreenCapture getScreenCapture() {
        return screenCapture;
    }
}

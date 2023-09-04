package com.android.sun2meg.vol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.widget.Toast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.widget.Toast;

public class VolumeButtonReceiver extends BroadcastReceiver {

    private PowerManager.WakeLock mWakeLock;
    private AudioManager mAudioManager;
    private boolean mIsLongPress = false;
    private Runnable mRunnable;
    private static final int VOLUME_UP_BUTTON = KeyEvent.KEYCODE_VOLUME_UP;
    private static final int VOLUME_UP_BUTTON_LONG_PRESS_TIMEOUT = 6000; // 3 seconds
    private Handler mHandler = new Handler();
    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mContext = context;
        if (action != null && action.equals(Intent.ACTION_SCREEN_OFF)) {
            // Acquire WakeLock
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLockTag");
            mWakeLock.acquire();
        } else if (action != null && action.equals(Intent.ACTION_SCREEN_ON)) {
            // Release WakeLock
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
                mWakeLock = null;
            }
        } else if (action != null && action.equals(Intent.ACTION_USER_PRESENT)) {
            // User unlocked the device
        } else if (action != null && action.equals(Intent.ACTION_MEDIA_BUTTON)) {
            // Handle media button events
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getKeyCode() == VOLUME_UP_BUTTON) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Volume up button pressed
                    startTrackingLongPress();
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    // Volume up button released
                    stopTrackingLongPress(context);
                }
            }
        }
    }

    private void startTrackingLongPress() {
        mIsLongPress = false;
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mIsLongPress = true;
                // Volume up button long press detected
                Toast.makeText(mContext, "Volume up button long press detected!", Toast.LENGTH_SHORT).show();
            }
        };
        mHandler.postDelayed(mRunnable, VOLUME_UP_BUTTON_LONG_PRESS_TIMEOUT);
    }

    private void stopTrackingLongPress(Context context) {
        mHandler.removeCallbacks(mRunnable);
        if (!mIsLongPress) {
            // Volume up button short press detected
            Toast.makeText(context, "Volume up button short press detected!", Toast.LENGTH_SHORT).show();
        }
    }
}

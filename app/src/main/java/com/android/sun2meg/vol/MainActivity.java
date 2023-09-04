package com.android.sun2meg.vol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    private MediaRecorder mMediaRecorder;
    private boolean mRecording = false;
    private long mLastPressTime = 0;


    private static final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 202;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 201;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 123;
    private GestureDetector mDetector;
    private Handler mHandler = new Handler();
    private static final long LONG_PRESS_TIMEOUT = 1000; // 1 second
    private static final int VOLUME_UP_BUTTON = KeyEvent.KEYCODE_VOLUME_UP;


    private boolean mIsLongPress = false;
    private Runnable mRunnable = null;
    private static final int VOLUME_UP_BUTTON_EVENT = 1;
    private static final int VOLUME_UP_BUTTON_LONG_PRESS_TIMEOUT = 3000; // 3 seconds
    private static final int VOLUME_UP_BUTTON_DEACTIVATE_TIMEOUT = 2000; // 2 seconds
    private boolean mIsVolumeUpButtonPressed = false;
    Timer mTimer;
    private PowerManager.WakeLock mWakeLock;

    private boolean mIsRecording = false;
    private VolumeButtonReceiver mVolumeButtonReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        mWakeLock.acquire();



        // Initialize the GestureDetector
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                // Volume up button long press detected
                if (!mIsRecording) {
                    /////////////////////////////////////
//                    startRecord();
                    mIsRecording = true;
                    Toast.makeText(MainActivity.this, "gest Recording started!", Toast.LENGTH_SHORT).show();
                } else {
                    //////////////////////////////////
//                    stopRecording();
                    mIsRecording = false;
                    Toast.makeText(MainActivity.this, "Recording stopped!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize AudioManager
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Set up a runnable to handle the long press event
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // Volume up button long press detected after 3 seconds
                mHandler.removeCallbacks(mRunnable);
                mIsLongPress = true;
                // Wait for 3 seconds
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!mIsRecording) {
                    ///////////////////////////////////////////////

//                    startRecord();
                    mIsRecording = true;
                    Toast.makeText(MainActivity.this, "Recording started!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Recording stopped!", Toast.LENGTH_SHORT).show();
                    mIsRecording = false;
                }
            }
        };

        // Create and register the VolumeButtonReceiver


        mVolumeButtonReceiver = new VolumeButtonReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mVolumeButtonReceiver, filter);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Start tracking the duration of the volume up button press
            mHandler.postDelayed(mRunnable, 5000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Stop tracking the duration of the volume up button press
            mHandler.removeCallbacks(mRunnable);

            // Check if the volume up button was pressed for a long duration
            if (!mIsLongPress) {
                // Volume up button short press detected
                Toast.makeText(MainActivity.this, "Volume up button short press detected!", Toast.LENGTH_SHORT).show();
            }

            // Reset the long press flag
            mIsLongPress = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass touch events to the GestureDetector
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the wakelock
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }


    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            // Check if the long press occurred on the volume up button
            if (event.getAction() == MotionEvent.ACTION_DOWN &&
                    event.getEventTime() - event.getDownTime() > ViewConfiguration.getLongPressTimeout() &&
                    KeyEvent.KEYCODE_VOLUME_UP == VOLUME_UP_BUTTON ) {
                // Volume up button long press detected
                Toast.makeText(MainActivity.this, "Volume up button long press detected!", Toast.LENGTH_SHORT).show();
            }


        }
    }

    private void setupGestureDetector() {
        mGestureDetector = new GestureDetector(this, new MyGestureListener());

    }
///////////////////////////good
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // Pass touch events to the GestureDetector
//        mGestureDetector.onTouchEvent(event);
//        return super.onTouchEvent(event);
//    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            // create a MotionEvent from the KeyEvent
//            MotionEvent motionEvent = MotionEvent.obtain(
//                    SystemClock.uptimeMillis(),
//                    SystemClock.uptimeMillis(),
//                    MotionEvent.ACTION_DOWN,
//                    0,
//                    0,
//                    0
//            );
//            // pass the MotionEvent to the GestureDetector
//            mGestureDetector.onTouchEvent(motionEvent);
//            motionEvent.recycle(); // recycle the MotionEvent to save memory
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            // create a MotionEvent from the KeyEvent
//            MotionEvent motionEvent = MotionEvent.obtain(
//                    SystemClock.uptimeMillis(),
//                    SystemClock.uptimeMillis(),
//                    MotionEvent.ACTION_UP,
//                    0,
//                    0,
//                    0
//            );
//            // pass the MotionEvent to the GestureDetector
//            mGestureDetector.onTouchEvent(motionEvent);
//            motionEvent.recycle(); // recycle the MotionEvent to save memory
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }
//
//
//    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public void onLongPress(MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_UP && event.getEventTime() - event.getDownTime() >= LONG_PRESS_TIMEOUT) {
//                // Volume up button long press detected
//                Toast.makeText(MainActivity.this, "Volume up button long press detected", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }
//
//    private void setupGestureDetector() {
//        mGestureDetector = new GestureDetector(this, new MyGestureListener());
//    }
//
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // Pass touch events to the GestureDetector
//        mGestureDetector.onTouchEvent(event);
//        return super.onTouchEvent(event);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            mHandler.postDelayed(mVolumeLongPress, ViewConfiguration.getLongPressTimeout());
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.getAction() == KeyEvent.ACTION_UP) {
            mHandler.removeCallbacks(mVolumeLongPress);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }




    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, initialize audio recorder
//                    initializeRecorder();
                } else {
                    // permission denied, show a message and disable audio recording
                    Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show();
//                    mRecordButton.setEnabled(false);
                }
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, nothing to do here
                } else {
                    // permission denied, show a message
                    Toast.makeText(this, "External storage permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }



    @Override
    public boolean onDown(MotionEvent event) {
        // called when the first finger is pressed down
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        // called when the user has pressed down and not yet released the finger
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        // called when the user has tapped the screen and released the finger
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // called when the user has dragged their finger across the screen
        return false;
    }

//    @Override
//    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            mDetector.onLongPress(event);
//            return true;
//        }
//        return super.onKeyLongPress(keyCode, event);
//    }


    private Runnable mVolumeLongPress = new Runnable() {
        @Override
        public void run() {
            // long press on volume up detected
            // do something here, like start recording
        }
    };



    @Override
    public void onLongPress(MotionEvent event) {
        // called when the user has pressed and held down on the screen for a long time
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        long pressTime = System.currentTimeMillis();
        if (pressTime - mLastPressTime < 2000) {
            // Don't start recording if a volume up button long press was recently detected
            return;
        }

        if (mAudioManager != null) {
            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume == 0) {
                // Do nothing if volume is already zero
                return;
            }
        }

        if (!mRecording) {
            // Start audio recording here
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setOutputFile(getOutputFile());

            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
                mRecording = true;
                mLastPressTime = pressTime;
                ToneGenerator tone2 = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                Toast.makeText(this, "Started audio recording", Toast.LENGTH_SHORT).show();
                acquireWakeLock();
            } catch (IOException e) {
                Log.e("MainActivity", "Error starting audio recording", e);
            }
        } else {
            // Stop audio recording here
            if (pressTime - mLastPressTime >= 2000) {
                // Only stop recording if the same volume up button has been long pressed for at least 2 seconds
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mRecording = false;
                mLastPressTime = pressTime;
                Toast.makeText(this, "Stopped audio recording", Toast.LENGTH_SHORT).show();
                releaseWakeLock();
            } else {
                Toast.makeText(this, "Hold volume up for 2 seconds to stop recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AudioRecorderWakeLock");
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AudioRecorderWakeLock");
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // called when the user has swiped their finger across the screen with enough velocity
        return false;
    }



    private String getOutputFile() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, "recording.3gp");
        return file.getAbsolutePath();
    }


    public void onOpenDirectoryButtonClick(View view) {
        // Get the directory where recorded files are stored
        File directory = getExternalFilesDir(null);

        // Create an intent to open the directory
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(directory), "resource/folder");

        // Verify that there is an app available to handle the intent
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            // Start the activity to open the directory
            startActivity(intent);
        } else {
            // Display an error message if no app is available to handle the intent
            Toast.makeText(this, "No app available to open directory", Toast.LENGTH_SHORT).show();
        }
    }


}


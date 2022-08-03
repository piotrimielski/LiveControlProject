package com.givevision.livecontrolproject;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.givevision.livecontrolproject.databinding.ActivityFullscreenBinding;
import com.givevision.livecontrolproject.log.Pojo;
import com.givevision.livecontrolproject.methodes.wifi.WifiEvent;
import com.givevision.livecontrolproject.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    private static final String TAG = "FullscreenActivity";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;

    private TcpServerService mService;
    private boolean mBound = false;
    public BroadcastReceiver tcpReceiver;

    private boolean isConnected0;
    private boolean isConnected1;
    private boolean isConnected2;
    private boolean isConnected3;
    private boolean isConnected4;
    private boolean isConnected5;
    private boolean isConnected6;
    private boolean isConnected7;
    private boolean isConnected8;
    private boolean isConnected9;

    public class TcpReceiver extends BroadcastReceiver {
        public TcpReceiver() {
            LogManagement.Log_d(TAG, "TcpReceiver BroadcastReceiver started");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            LogManagement.Log_d(TAG, "BroadcastReceiver onReceive= "
                    +" extra= "+intent.getStringExtra("pojo"));
            Pojo pojo= new Pojo();
            if(pojo.setPojo(intent.getStringExtra("pojo"))){
                if(pojo.getAction().equals(Constants.ACTION_OK) &&
                        pojo.getType().equals(Constants.ACTION_TYPE_PING)){
                    pingOKCode(pojo);
                }else if(pojo.getAction().equals(Constants.ACTION_ERROR) &&
                        pojo.getType().equals(Constants.ACTION_TYPE_PING)){
                    pingCode(pojo);
                }else if(pojo.getAction().equals(Constants.ACTION_ERROR)){
                    errorCode(pojo);
                }else if(pojo.getMessage().equals(Constants.MSG_SIGNAL_QUALITY_OK)){
                    okCode(pojo);
                }else if(pojo.getMessage().contains("ERROR")){
                    warningCode(pojo);
                }
            }else{
                throw new UnsupportedOperationException("received wrong format value");
            }
        }

    }



    private void configureReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.givevision.livecontrolproject");
        tcpReceiver = new TcpReceiver();
        registerReceiver(tcpReceiver, filter);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
            final Window win = getWindow();
            int ui = getWindow().getDecorView().getSystemUiVisibility();
            win.getDecorView().setSystemUiVisibility(ui);
            win.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            win.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            win.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            |WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityFullscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManagement.Log_d(TAG, "onCreate started ");

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.kit1;
        mContentView = binding.kit2;
        mContentView = binding.kit3;
        mContentView = binding.kit4;
        mContentView = binding.kit5;
        mContentView = binding.kit6;
        mContentView = binding.kit7;
        mContentView = binding.kit8;
        mContentView = binding.kit9;
        mContentView = binding.kit10;
        mContentView = binding.wifirouter;
        mContentView = binding.encoder;

        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);

        configureReceiver();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        LogManagement.Log_d(TAG, "onPostCreate started ");
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

    }

    @Override
    protected void onStart() {
        super.onStart();
        LogManagement.Log_d(TAG, "onStart started ");
        // Bind to Your Service
        Intent intent = new Intent(this, TcpServerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        LogManagement.Log_d(TAG, "onResume started ");


    }

    @Override
    protected void onPause() {
        super.onPause();
        LogManagement.Log_d(TAG, "onPause started ");

    }

    @Override
    protected void onStop() {
        super.onStop();
        LogManagement.Log_d(TAG, "onStop started");
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(tcpReceiver);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to the running Service, cast the IBinder and get instance
            LogManagement.Log_d(TAG, "onServiceConnected");
            TcpServerService.LocalBinder binder = (TcpServerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            LogManagement.Log_d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    private void errorCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10")){
            binding.kit1.setBackgroundColor(Color.RED);
            isConnected0=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            binding.kit2.setBackgroundColor(Color.RED);
            isConnected1=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            binding.kit3.setBackgroundColor(Color.RED);
            isConnected2=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            binding.kit4.setBackgroundColor(Color.RED);
            isConnected3=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            binding.kit5.setBackgroundColor(Color.RED);
            isConnected4=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            binding.kit6.setBackgroundColor(Color.RED);
            isConnected5=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            binding.kit7.setBackgroundColor(Color.RED);
            isConnected6=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            binding.kit8.setBackgroundColor(Color.RED);
            isConnected7=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            binding.kit9.setBackgroundColor(Color.RED);
            isConnected8=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            binding.kit10.setBackgroundColor(Color.RED);
            isConnected9=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            binding.wifirouter.setBackgroundColor(Color.RED);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            binding.encoder.setBackgroundColor(Color.RED);
        }
    }

    private void okCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10")){
            binding.kit1.setBackgroundColor(Color.GREEN);
            isConnected0=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            binding.kit2.setBackgroundColor(Color.GREEN);
            isConnected1=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            binding.kit3.setBackgroundColor(Color.GREEN);
            isConnected2=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            binding.kit4.setBackgroundColor(Color.GREEN);
            isConnected3=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            binding.kit5.setBackgroundColor(Color.GREEN);
            isConnected4=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            binding.kit6.setBackgroundColor(Color.GREEN);
            isConnected5=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            binding.kit7.setBackgroundColor(Color.GREEN);
            isConnected6=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            binding.kit8.setBackgroundColor(Color.GREEN);
            isConnected7=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            binding.kit9.setBackgroundColor(Color.GREEN);
            isConnected8=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            binding.kit10.setBackgroundColor(Color.GREEN);
            isConnected9=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            binding.wifirouter.setBackgroundColor(Color.GREEN);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            binding.encoder.setBackgroundColor(Color.GREEN);
        }
    }

    private void warningCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10")){
            binding.kit1.setBackgroundColor(Color.YELLOW);
            isConnected0=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            binding.kit2.setBackgroundColor(Color.YELLOW);
            isConnected1=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            binding.kit3.setBackgroundColor(Color.YELLOW);
            isConnected2=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            binding.kit4.setBackgroundColor(Color.YELLOW);
            isConnected3=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            binding.kit5.setBackgroundColor(Color.YELLOW);
            isConnected4=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            binding.kit6.setBackgroundColor(Color.YELLOW);
            isConnected5=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            binding.kit7.setBackgroundColor(Color.YELLOW);
            isConnected6=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            binding.kit8.setBackgroundColor(Color.YELLOW);
            isConnected7=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            binding.kit9.setBackgroundColor(Color.YELLOW);
            isConnected8=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            binding.kit10.setBackgroundColor(Color.YELLOW);
            isConnected9=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            binding.wifirouter.setBackgroundColor(Color.YELLOW);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            binding.encoder.setBackgroundColor(Color.YELLOW);
        }
    }
    private void pingCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10")){
            binding.kit1.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            binding.kit2.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            binding.kit3.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            binding.kit4.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            binding.kit5.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            binding.kit6.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            binding.kit7.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            binding.kit8.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            binding.kit9.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            binding.kit10.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            binding.wifirouter.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            binding.encoder.setBackgroundColor(Color.BLACK);
        }
    }
    private void pingOKCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10") && !isConnected0){
            binding.kit1.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.11") && !isConnected1){
            binding.kit2.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.12") && !isConnected2){
            binding.kit3.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.13") && !isConnected3){
            binding.kit4.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.14") && !isConnected4){
            binding.kit5.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.15") && !isConnected5){
            binding.kit6.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.16") && !isConnected6){
            binding.kit7.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.17") && !isConnected7){
            binding.kit8.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.18") && !isConnected8){
            binding.kit9.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.19") && !isConnected9){
            binding.kit10.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            binding.wifirouter.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            binding.encoder.setBackgroundColor(Color.CYAN);
        }
    }
}


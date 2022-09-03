package com.givevision.livecontrolproject;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;

import com.givevision.livecontrolproject.databinding.ActivityFullscreenBinding;
import com.givevision.livecontrolproject.log.Pojo;
import com.givevision.livecontrolproject.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    private ActivityFullscreenBinding binding;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    private View mContent1View;
    private View mContent2View;
    private View mContent3View;
    private View mContent4View;
    private View mContent5View;
    private View mContent6View;
    private View mContent7View;
    private View mContent8View;
    private View mContent9View;
    private View mContent10View;
//    private final Handler mHideHandler1 = new Handler(Looper.myLooper());
    private View mControlsKitView;
    private boolean mKitVisible;

    private TcpServerService mService;
    private boolean mBound = false;
    public BroadcastReceiver tcpReceiver;


    private boolean isConnected1;
    private boolean isConnected2;
    private boolean isConnected3;
    private boolean isConnected4;
    private boolean isConnected5;
    private boolean isConnected6;
    private boolean isConnected7;
    private boolean isConnected8;
    private boolean isConnected9;
    private boolean isConnected10;

    private List<StatusOfKit> statusKits=new ArrayList<>();
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    public class StatusOfKit{
        private String ipAddress;
        private String state; // camera, video, reset
        private String rssi; // rssi dBm
        private String battery;
        private String temperature;
        private String mode;

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getRssi() {
            return rssi;
        }

        public void setRssi(String rssi) {
            this.rssi = rssi;
        }

        public String getBattery() {
            return battery;
        }

        public void setBattery(String battery) {
            this.battery = battery;
        }

        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

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
                    pingErrorCode(pojo);
                }else if(pojo.getAction().equals(Constants.ACTION_ERROR)){
                    errorCode(pojo);
                }else if(pojo.getMessage().equals(Constants.MSG_SIGNAL_QUALITY_OK)){
                    okCode(pojo);
                }else if(pojo.getMessage().contains("ERROR") ||
                        pojo.getMessage().equals(Constants.MSG_SIGNAL_NOT_STRONG)){
                    warningCode(pojo);
                }if(pojo.getMessage().contains(Constants.MSG_SIGNAL_RSSI)){
                    rssiCode(pojo);
                }else if(pojo.getAction().equals(Constants.ACTION_ON_APP)){
                    if(pojo.getMessage().contains(Constants.MSG_STATE)) {
                        stateCode(pojo);
                    }else if(pojo.getMessage().contains(Constants.MSG_MODE)) {
                        modeCode(pojo);
                    }else if(pojo.getMessage().contains(Constants.MSG_BATTERY)) {
                        batteryCode(pojo);
                    }else if(pojo.getMessage().contains(Constants.MSG_TEMPERATURE)) {
                        temperatureCode(pojo);
                    }
                }
                stateKits();
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
            mControlsView.setVisibility(View.INVISIBLE);
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

    private final Runnable mHidePart2Runnable1 = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mControlsKitView.setVisibility(View.INVISIBLE);
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

    private final Runnable mShowPart2Runnable1 = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsKitView.setVisibility(View.VISIBLE);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mHideRunnable1 = new Runnable() {
        @Override
        public void run() {
            hideContent();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchResetListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    LogManagement.Log_d(TAG, "mDelayHideTouchListener button tag="+view.getTag());
                    buttonReset((String) view.getTag());
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchModeListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    LogManagement.Log_d(TAG, "mDelayHideTouchListener button tag="+view.getTag());
                    buttonMode((String) view.getTag());
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private void buttonReset(String tag) {
        LogManagement.Log_d(TAG, "buttonReset kit="+tag+ " mBound="+mBound);
        if(tag==null)
            return;
        if(tag.equals("all")){
            if(mBound){
                for (int i=0; i<statusKits.size(); i++) {
                    String ipAddr= statusKits.get(i).getIpAddress();
                    String state=statusKits.get(i).getState();
                    if(state.equals(Constants.ACTION_VIDEO)){
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                    }
                }
            }
        }else{
            if(mBound){
                for (int i=0; i<statusKits.size(); i++) {
                    String ipAddr= statusKits.get(i).getIpAddress();
                    String state=statusKits.get(i).getState();
                    if(ipAddr.equals("192.168.1.10")&& tag.equals("1") && state.equals(Constants.ACTION_VIDEO)){
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.11")&& tag.equals("2") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.12")&& tag.equals("3") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.13")&& tag.equals("4") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.14")&& tag.equals("5") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.15")&& tag.equals("6") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.16")&& tag.equals("7") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.17")&& tag.equals("8") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.18")&& tag.equals("9") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.19")&& tag.equals("10") && state.equals(Constants.ACTION_VIDEO)) {
                        LogManagement.Log_d(TAG, "buttonReset resetKit="+ipAddr);
                        statusKits.get(i).setState(Constants.ACTION_RESET);
                        stateKits();
                        mService.resetKit(ipAddr);
                        break;
                    }
                }
            }
        }
    }

    private void buttonMode(String tag) {
        LogManagement.Log_d(TAG, "buttonMode kit="+tag+ " mBound="+mBound);
        if(tag==null)
            return;
        if(tag.equals("all")){
            if(mBound){
                for (int i=0; i<statusKits.size(); i++) {
                    String ipAddr= statusKits.get(i).getIpAddress();
                    String state=statusKits.get(i).getState();
                    if(state.equals(Constants.ACTION_VIDEO)){
                        LogManagement.Log_d(TAG, "buttonMode resetKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                    }
                }
            }
        }else{
            if(mBound){
                for (int i=0; i<statusKits.size(); i++) {
                    String ipAddr= statusKits.get(i).getIpAddress();
                    String state=statusKits.get(i).getState();
                    if(ipAddr.equals("192.168.1.10")&& tag.equals("1")){
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.11")&& tag.equals("2")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.12")&& tag.equals("3")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.13")&& tag.equals("4")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.14")&& tag.equals("5")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.15")&& tag.equals("6")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.16")&& tag.equals("7")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.17")&& tag.equals("8")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.18")&& tag.equals("9")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }else if(ipAddr.equals("192.168.1.19")&& tag.equals("10")) {
                        LogManagement.Log_d(TAG, "buttonMode modeKit="+ipAddr);
                        statusKits.get(i).setMode(Constants.ACTION_MODE);
                        stateKits();
                        mService.modeKit(ipAddr);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManagement.Log_d(TAG, "onCreate started ");
        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        for( int i=10;i<20; i++){
            StatusOfKit statusOfKit=new StatusOfKit();
            statusOfKit.setIpAddress("192.168.1."+i);
            statusOfKit.setState("");
            statusOfKit.setRssi("");
            statusOfKit.setMode("switch mode");
            statusKits.add(statusOfKit);
            LogManagement.Log_d(TAG, "onCreate add statusKits::"+
                    " adrr="+statusKits.get(statusKits.size()-1).getIpAddress()+
                    " state="+statusKits.get(statusKits.size()-1).getState()+
                    " signal="+statusKits.get(statusKits.size()-1).getRssi());
        }
        LogManagement.Log_d(TAG, "onCreate statusKits::"+ " sizes="+statusKits.size());


        mVisible = true;
        mKitVisible =true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.encoder;
        mContent1View = binding.kit1;
        mContent2View = binding.kit2;
        mContent3View = binding.kit3;
        mContent4View = binding.kit4;
        mContent5View = binding.kit5;
        mContent6View = binding.kit6;
        mContent7View = binding.kit7;
        mContent8View = binding.kit8;
        mContent9View = binding.kit9;
        mContent10View = binding.kit10;
        mControlsKitView = binding.contentKit;

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick restart ");
                toggle();
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick restart ");
                toggle();
            }
        });


        mContent1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 1 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(1);
                }
                showStatusKit("192.168.1.10");
            }
        });
//        mContent1View.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                LogManagement.Log_d(TAG, "setOnTouchListener 1 ");
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN: {
//                        v.getBackground().setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
//                        v.invalidate();
//                        break;
//                    }
//                    case MotionEvent.ACTION_UP: {
//                        v.getBackground().clearColorFilter();
//                        v.invalidate();
//                        break;
//                    }
//                }
//                return false;
//            }
//        });

        mContent2View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 2 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(2);
                }
                showStatusKit("192.168.1.11");
            }
        });

        mContent3View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 3 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(3);
                }
                showStatusKit("192.168.1.12");
            }
        });

        mContent4View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 4 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(4);
                }
                showStatusKit("192.168.1.13");
            }
        });

        mContent5View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 5 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(5);
                }
                showStatusKit("192.168.1.14");
            }
        });

        mContent6View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 6 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(6);
                }
                showStatusKit("192.168.1.15");
            }
        });

        mContent7View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 7 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(7);
                }
                showStatusKit("192.168.1.16");
            }
        });

        mContent8View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 8 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(8);
                }
                showStatusKit("192.168.1.17");
            }
        });

        mContent9View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 9 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(9);
                }
                showStatusKit("192.168.1.18");
            }
        });

        mContent10View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManagement.Log_d(TAG, "setOnClickListener onClick 10 ");
                view.startAnimation(buttonClick);
                if(mBound){
                    mService.checkKit(10);
                }
                showStatusKit("192.168.1.19");
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.restartButton.setOnTouchListener(mDelayHideTouchResetListener);
        binding.restartKitButton.setOnTouchListener(mDelayHideTouchResetListener);
        binding.modeButton.setOnTouchListener(mDelayHideTouchModeListener);
        binding.modeKitButton.setOnTouchListener(mDelayHideTouchModeListener);
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
        delayedHide1(100);
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
        LogManagement.Log_d(TAG, "toggle mVisible="+mVisible
        + " mContentKitVisible="+mKitVisible);
        if(mKitVisible){
            hideContent();
            mKitVisible=false;
        }
        if (mVisible) {
            hide();
        } else {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            show();
        }
    }

    private void toggleContent(String kit, String state, String strength, String battery, String temperature, String mode) {
        LogManagement.Log_d(TAG, "toggleContent mVisible="+mVisible
                + " mContentKitVisible="+mKitVisible);

        if(mVisible){
            hide();
            mVisible=false;
        }
        if (mKitVisible) {
            hideContent();
        } else {
            if (AUTO_HIDE) {
                delayedHide1(AUTO_HIDE_DELAY_MILLIS);
            }
            showContent(kit, state, strength, battery, temperature,mode );
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void hideContent() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mKitVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable1);
        mHideHandler.postDelayed(mHidePart2Runnable1, UI_ANIMATION_DELAY);
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


    private void showContent(String kit, String state, String strength, String battery, String temperature, String mode) {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mControlsKitView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mControlsKitView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mKitVisible = true;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        binding.kitContent.setText(kit);
        binding.stateContent.setText(state);
        binding.strengthContent.setText(strength);
        binding.batteryContent.setText(battery);
        binding.temperatureContent.setText(temperature);
        binding.modeKitButton.setText(mode);
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable1);
        mHideHandler.postDelayed(mShowPart2Runnable1, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void delayedHide1(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable1);
        mHideHandler.postDelayed(mHideRunnable1, delayMillis);
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
            LogManagement.Log_e(TAG, "errorCode kit1");
            binding.kit1.setBackgroundColor(Color.RED);
            isConnected1=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            LogManagement.Log_e(TAG, "errorCode kit2");
            binding.kit2.setBackgroundColor(Color.RED);
            isConnected2=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            LogManagement.Log_e(TAG, "errorCode kit3");
            binding.kit3.setBackgroundColor(Color.RED);
            isConnected3=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            LogManagement.Log_e(TAG, "errorCode kit4");
            binding.kit4.setBackgroundColor(Color.RED);
            isConnected4=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            LogManagement.Log_e(TAG, "errorCode kit5");
            binding.kit5.setBackgroundColor(Color.RED);
            isConnected5=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            LogManagement.Log_e(TAG, "errorCode kit6");
            binding.kit6.setBackgroundColor(Color.RED);
            isConnected6=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            LogManagement.Log_e(TAG, "errorCode kit7");
            binding.kit7.setBackgroundColor(Color.RED);
            isConnected7=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            LogManagement.Log_e(TAG, "errorCode kit8");
            binding.kit8.setBackgroundColor(Color.RED);
            isConnected8=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            LogManagement.Log_e(TAG, "errorCode kit9");
            binding.kit9.setBackgroundColor(Color.RED);
            isConnected9=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            LogManagement.Log_e(TAG, "errorCode kit10");
            binding.kit10.setBackgroundColor(Color.RED);
            isConnected10=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            LogManagement.Log_e(TAG, "errorCode router");
            binding.wifirouter.setBackgroundColor(Color.RED);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            LogManagement.Log_e(TAG, "errorCode encoder");
            binding.encoder.setBackgroundColor(Color.RED);
        }
    }

    private void okCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10")){
            LogManagement.Log_d(TAG, "okCode kit1");
            binding.kit1.setBackgroundColor(Color.GREEN);
            isConnected1=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            LogManagement.Log_d(TAG, "okCode kit2");
            binding.kit2.setBackgroundColor(Color.GREEN);
            isConnected2=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            LogManagement.Log_d(TAG, "okCode kit3");
            binding.kit3.setBackgroundColor(Color.GREEN);
            isConnected3=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            LogManagement.Log_d(TAG, "okCode kit4");
            binding.kit4.setBackgroundColor(Color.GREEN);
            isConnected4=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            LogManagement.Log_d(TAG, "okCode kit5");
            binding.kit5.setBackgroundColor(Color.GREEN);
            isConnected5=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            LogManagement.Log_d(TAG, "okCode kit6");
            binding.kit6.setBackgroundColor(Color.GREEN);
            isConnected6=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            LogManagement.Log_d(TAG, "okCode kit7");
            binding.kit7.setBackgroundColor(Color.GREEN);
            isConnected7=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            LogManagement.Log_d(TAG, "okCode kit8");
            binding.kit8.setBackgroundColor(Color.GREEN);
            isConnected8=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            LogManagement.Log_d(TAG, "okCode kit9");
            binding.kit9.setBackgroundColor(Color.GREEN);
            isConnected9=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            LogManagement.Log_d(TAG, "okCode kit10");
            binding.kit10.setBackgroundColor(Color.GREEN);
            isConnected10=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            LogManagement.Log_d(TAG, "okCode router");
            binding.wifirouter.setBackgroundColor(Color.GREEN);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            LogManagement.Log_d(TAG, "okCode encoder");
            binding.encoder.setBackgroundColor(Color.GREEN);
        }
    }

    private void warningCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10")){
            LogManagement.Log_d(TAG, "okCode kit1");
            binding.kit1.setBackgroundColor(Color.YELLOW);
            isConnected1=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            LogManagement.Log_d(TAG, "okCode kit2");
            binding.kit2.setBackgroundColor(Color.YELLOW);
            isConnected2=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            LogManagement.Log_d(TAG, "okCode kit3");
            binding.kit3.setBackgroundColor(Color.YELLOW);
            isConnected3=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            LogManagement.Log_d(TAG, "okCode kit4");
            binding.kit4.setBackgroundColor(Color.YELLOW);
            isConnected4=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            LogManagement.Log_d(TAG, "okCode kit5");
            binding.kit5.setBackgroundColor(Color.YELLOW);
            isConnected5=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            LogManagement.Log_d(TAG, "okCode kit6");
            binding.kit6.setBackgroundColor(Color.YELLOW);
            isConnected6=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            LogManagement.Log_d(TAG, "okCode kit7");
            binding.kit7.setBackgroundColor(Color.YELLOW);
            isConnected7=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            LogManagement.Log_d(TAG, "okCode kit8");
            binding.kit8.setBackgroundColor(Color.YELLOW);
            isConnected8=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            LogManagement.Log_d(TAG, "okCode kit9");
            binding.kit9.setBackgroundColor(Color.YELLOW);
            isConnected9=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            LogManagement.Log_d(TAG, "okCode kit10");
            binding.kit10.setBackgroundColor(Color.YELLOW);
            isConnected10=true;
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            LogManagement.Log_d(TAG, "okCode router");
            binding.wifirouter.setBackgroundColor(Color.YELLOW);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            LogManagement.Log_d(TAG, "okCode encoder");
            binding.encoder.setBackgroundColor(Color.YELLOW);
        }
    }
    private void pingErrorCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10")){
            LogManagement.Log_d(TAG, "okCode kit1");
            binding.kit1.setBackgroundColor(Color.BLACK);
            isConnected1=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.11")){
            LogManagement.Log_d(TAG, "okCode kit2");
            binding.kit2.setBackgroundColor(Color.BLACK);
            isConnected2=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.12")){
            LogManagement.Log_d(TAG, "okCode kit3");
            binding.kit3.setBackgroundColor(Color.BLACK);
            isConnected3=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.13")){
            LogManagement.Log_d(TAG, "okCode kit4");
            binding.kit4.setBackgroundColor(Color.BLACK);
            isConnected4=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.14")){
            LogManagement.Log_d(TAG, "okCode kit5");
            binding.kit5.setBackgroundColor(Color.BLACK);
            isConnected5=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.15")){
            LogManagement.Log_d(TAG, "okCode kit6");
            binding.kit6.setBackgroundColor(Color.BLACK);
            isConnected6=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.16")){
            LogManagement.Log_d(TAG, "okCode kit7");
            binding.kit7.setBackgroundColor(Color.BLACK);
            isConnected7=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.17")){
            LogManagement.Log_d(TAG, "okCode kit8");
            binding.kit8.setBackgroundColor(Color.BLACK);
            isConnected8=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.18")){
            LogManagement.Log_d(TAG, "okCode kit9");
            binding.kit9.setBackgroundColor(Color.BLACK);
            isConnected9=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.19")){
            LogManagement.Log_d(TAG, "okCode kit10");
            binding.kit10.setBackgroundColor(Color.BLACK);
            isConnected10=false;
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            LogManagement.Log_d(TAG, "okCode router");
            binding.wifirouter.setBackgroundColor(Color.BLACK);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            LogManagement.Log_d(TAG, "okCode encoder");
            binding.encoder.setBackgroundColor(Color.BLACK);
        }
    }
    private void pingOKCode(Pojo pojo) {
        if(pojo.getIpAddress().equals("192.168.1.10") && !isConnected1){
            LogManagement.Log_d(TAG, "okCode kit1");
            binding.kit1.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.11") && !isConnected2){
            LogManagement.Log_d(TAG, "okCode kit2");
            binding.kit2.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.12") && !isConnected3){
            LogManagement.Log_d(TAG, "okCode kit3");
            binding.kit3.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.13") && !isConnected4){
            LogManagement.Log_d(TAG, "okCode kit4");
            binding.kit4.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.14") && !isConnected5){
            LogManagement.Log_d(TAG, "okCode kit5");
            binding.kit5.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.15") && !isConnected6){
            LogManagement.Log_d(TAG, "okCode kit6");
            binding.kit6.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.16") && !isConnected7){
            LogManagement.Log_d(TAG, "okCode kit7");
            binding.kit7.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.17") && !isConnected8){
            LogManagement.Log_d(TAG, "okCode kit8");
            binding.kit8.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.18") && !isConnected9){
            LogManagement.Log_d(TAG, "okCode kit9");
            binding.kit9.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.19") && !isConnected10){
            LogManagement.Log_d(TAG, "okCode kit10");
            binding.kit10.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.1")){
            LogManagement.Log_d(TAG, "okCode router");
            binding.wifirouter.setBackgroundColor(Color.CYAN);
        }
        if(pojo.getIpAddress().equals("192.168.1.168")){
            LogManagement.Log_d(TAG, "okCode encoder");
            binding.encoder.setBackgroundColor(Color.CYAN);
        }
    }

    private void stateCode(Pojo pojo) {
        String state = "";
        String[] parts = pojo.getMessage().split("=");
        if(parts.length==2){
            state= parts[1];
        }
        String ipAddr= pojo.getIpAddress();
        for (int i=0; i<statusKits.size(); i++) {
            if(statusKits.get(i).getIpAddress().equals(ipAddr)){
                statusKits.get(i).setState(state);
                LogManagement.Log_d(TAG, "stateCode statusKits::"+
                        " adrr="+statusKits.get(i).getIpAddress()+
                        " state="+statusKits.get(i).getState()+
                        " mode="+statusKits.get(i).getMode()+
                        " signal="+statusKits.get(i).getRssi()+
                        " battery="+statusKits.get(i).getBattery()+
                        " temperature="+statusKits.get(i).getTemperature());
                break;
            }
        }
    }

    private void modeCode(Pojo pojo) {
        String mode = "";
        String[] parts = pojo.getMessage().split("=");
        if(parts.length==2){
            mode= parts[1];
        }
        String ipAddr= pojo.getIpAddress();
        for (int i=0; i<statusKits.size(); i++) {
            if(statusKits.get(i).getIpAddress().equals(ipAddr)){
                statusKits.get(i).setMode(mode);
                LogManagement.Log_d(TAG, "stateCode statusKits::"+
                        " adrr="+statusKits.get(i).getIpAddress()+
                        " state="+statusKits.get(i).getState()+
                        " mode="+statusKits.get(i).getMode()+
                        " signal="+statusKits.get(i).getRssi()+
                        " battery="+statusKits.get(i).getBattery()+
                        " temperature="+statusKits.get(i).getTemperature());
                break;
            }
        }
    }

    private void temperatureCode(Pojo pojo) {
        String temperature = "";
        String[] parts = pojo.getMessage().split("=");
        if(parts.length==2){
            temperature= parts[1];
        }
        String ipAddr= pojo.getIpAddress();
        for (int i=0; i<statusKits.size(); i++) {
            if(statusKits.get(i).getIpAddress().equals(ipAddr)){
                statusKits.get(i).setTemperature(temperature);
                LogManagement.Log_d(TAG, "stateCode statusKits::"+
                        " adrr="+statusKits.get(i).getIpAddress()+
                        " state="+statusKits.get(i).getState()+
                        " mode="+statusKits.get(i).getMode()+
                        " signal="+statusKits.get(i).getRssi()+
                        " battery="+statusKits.get(i).getBattery()+
                        " temperature="+statusKits.get(i).getTemperature());
                break;
            }
        }
    }

    private void batteryCode(Pojo pojo) {
        String battery = "";
        String[] parts = pojo.getMessage().split("=");
        if(parts.length==2){
            battery= parts[1];
        }
        String ipAddr= pojo.getIpAddress();
        for (int i=0; i<statusKits.size(); i++) {
            if(statusKits.get(i).getIpAddress().equals(ipAddr)){
                statusKits.get(i).setBattery(battery);
                LogManagement.Log_d(TAG, "stateCode statusKits::"+
                        " adrr="+statusKits.get(i).getIpAddress()+
                        " state="+statusKits.get(i).getState()+
                        " mode="+statusKits.get(i).getMode()+
                        " signal="+statusKits.get(i).getRssi()+
                        " battery="+statusKits.get(i).getBattery()+
                        " temperature="+statusKits.get(i).getTemperature());
                break;
            }
        }
    }

    private void rssiCode(Pojo pojo) {
        String[] parts = pojo.getMessage().split("=");
        String strength="dBm";
        if(parts.length==2){
            strength= parts[1];
        }
        String ipAddr= pojo.getIpAddress();
            for (int i=0; i<statusKits.size(); i++) {
                if(statusKits.get(i).getIpAddress().equals(ipAddr)){
                    statusKits.get(i).setRssi(strength);

                    LogManagement.Log_d(TAG, "rssiCode statusKits::"+
                            " adrr="+statusKits.get(i).getIpAddress()+
                            " state="+statusKits.get(i).getState()+
                            " mode="+statusKits.get(i).getMode()+
                            " signal="+statusKits.get(i).getRssi()+
                            " battery="+statusKits.get(i).getBattery()+
                            " temperature="+statusKits.get(i).getTemperature());

                    if(Integer.parseInt(strength)>=-60){
                        okCode(pojo);
                    }else{
                        warningCode(pojo);
                    }
                    if(pojo.getIpAddress().equals("192.168.1.10")){
                        isConnected1=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.11")){
                        isConnected2=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.12")){
                        isConnected3=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.13")){
                        isConnected4=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.14")){
                        isConnected5=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.15")){
                        isConnected6=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.16")){
                        isConnected7=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.17")){
                        isConnected8=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.18")){
                        isConnected9=true;
                    }else if(pojo.getIpAddress().equals("192.168.1.19")){
                        isConnected10=true;
                    }
                    break;
                }
            }
    }

    private void stateKits(){
        for (int i=0; i<statusKits.size(); i++) {
            String ipAddr= statusKits.get(i).getIpAddress();
            String state=statusKits.get(i).getState();
            if(ipAddr.equals("192.168.1.10")){
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit1.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit1.setTextColor(Color.BLACK);
                }else{
                    binding.kit1.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.11")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit2.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit2.setTextColor(Color.BLACK);
                }else{
                    binding.kit2.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.12")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit3.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit3.setTextColor(Color.BLACK);
                }else{
                    binding.kit3.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.13")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit4.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit4.setTextColor(Color.BLACK);
                }else{
                    binding.kit4.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.14")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit5.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit5.setTextColor(Color.BLACK);
                }else{
                    binding.kit5.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.15")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit6.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit6.setTextColor(Color.BLACK);
                }else{
                    binding.kit6.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.16")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit7.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit7.setTextColor(Color.BLACK);
                }else{
                    binding.kit7.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.17")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit8.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit8.setTextColor(Color.BLACK);
                }else{
                    binding.kit8.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.18")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit9.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit9.setTextColor(Color.BLACK);
                }else{
                    binding.kit9.setTextColor(Color.WHITE);
                }
                break;
            }else if(ipAddr.equals("192.168.1.19")) {
                if(state.equals(Constants.ACTION_CAMERA)){
                    binding.kit10.setTextColor(Color.YELLOW);
                }else if(state.equals(Constants.ACTION_RESET)){
                    binding.kit10.setTextColor(Color.BLACK);
                }else{
                    binding.kit10.setTextColor(Color.WHITE);
                }
                break;
            }
        }
    }

    private void showStatusKit(String k) {
        LogManagement.Log_d(TAG, "showStatus ipAdrr ="+k+" statusKits sizes="+statusKits.size());
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                String kit="";
                String strength= "dBm";
                String state="";
                String battery="%";
                String temperature="℃";
                String mode="mode";
                for (int i=0; i<statusKits.size(); i++) {
                    String ipAddr= statusKits.get(i).getIpAddress();
                    strength= statusKits.get(i).getRssi()+"dBm";
                    state=statusKits.get(i).getState();
                    battery=statusKits.get(i).getBattery()+"%";
                    temperature=statusKits.get(i).getTemperature();
                    mode=statusKits.get(i).getMode();
                    if(ipAddr.equals("192.168.1.10") && ipAddr.equals(k)){
                        kit="kit:1";
                        binding.restartKitButton.setTag("1");
                        binding.modeKitButton.setTag("1");
                        break;
                    }else if(ipAddr.equals("192.168.1.11") && ipAddr.equals(k)) {
                        kit = "kit:2";
                        binding.restartKitButton.setTag("2");
                        binding.modeKitButton.setTag("2");
                        break;
                    }else if(ipAddr.equals("192.168.1.12") && ipAddr.equals(k)) {
                        kit = "kit:3";
                        binding.restartKitButton.setTag("3");
                        binding.modeKitButton.setTag("3");
                        break;
                    }else if(ipAddr.equals("192.168.1.13") && ipAddr.equals(k)) {
                        kit = "kit:4";
                        binding.restartKitButton.setTag("4");
                        binding.modeKitButton.setTag("4");
                        break;
                    }else if(ipAddr.equals("192.168.1.14") && ipAddr.equals(k)) {
                        kit = "kit:5";
                        binding.restartKitButton.setTag("5");
                        binding.modeKitButton.setTag("5");
                        break;
                    }else if(ipAddr.equals("192.168.1.15") && ipAddr.equals(k)) {
                        kit = "kit:6";
                        binding.restartKitButton.setTag("6");
                        binding.modeKitButton.setTag("6");
                        break;
                    }else if(ipAddr.equals("192.168.1.16") && ipAddr.equals(k)) {
                        kit = "kit:7";
                        binding.restartKitButton.setTag("7");
                        binding.modeKitButton.setTag("7");
                        break;
                    }else if(ipAddr.equals("192.168.1.17") && ipAddr.equals(k)) {
                        kit = "kit:8";
                        binding.restartKitButton.setTag("8");
                        binding.modeKitButton.setTag("8");
                        break;
                    }else if(ipAddr.equals("192.168.1.18") && ipAddr.equals(k)) {
                        kit = "kit:9";
                        binding.restartKitButton.setTag("9");
                        binding.modeKitButton.setTag("9");
                        break;
                    }else if(ipAddr.equals("192.168.1.19") && ipAddr.equals(k)) {
                        kit = "kit:10";
                        binding.restartKitButton.setTag("10");
                        binding.modeKitButton.setTag("10");
                        break;
                    }else{
                        binding.restartKitButton.setTag("0");
                        binding.modeKitButton.setTag("0");
                    }

                }
                mKitVisible=false;
                LogManagement.Log_d(TAG, "showStatus kit ="+kit+" state="+state+" strength="+strength);
                toggleContent(kit, state,strength , battery, temperature,mode);
            }
        }, 500);

    }
}


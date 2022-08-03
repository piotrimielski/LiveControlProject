package com.givevision.livecontrolproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.givevision.livecontrolproject.util.Constants;

public class StartAtBootServiceReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private static final String TAG = "StartAtBootServiceReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // BOOT_COMPLETED” start Service
        if (intent.getAction().equals(ACTION)) {
            LogManagement.Log_d(TAG, "onReceive start service");
            //Service
            Intent serviceIntent = new Intent(context, TcpServerService.class);
            context.startForegroundService(serviceIntent);
            LogManagement.Log_d(TAG, "onReceive start activity");
            Intent activityIntent = new Intent(context, FullscreenActivity.class);//MyActivity can be anything which you want to start on bootup...
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtra(Constants.ACTION_START, true);
            context.startActivity(activityIntent);
        }
    }
}

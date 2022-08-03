package com.givevision.livecontrolproject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Calendar;

public class TcpServerService extends Service {
    private static final String TAG = "TcpServerService";

    /** interface for clients that bind */
//    IBinder mBinder;
    private final IBinder mBinder = new LocalBinder();
    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    private TcpServerSocket serverSocket;


    /** Called when the service is being created. */
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        LogManagement.Log_d(TAG, "Service onCreate");
        startForeground(1,new Notification());
        serverSocket=new TcpServerSocket(this.getBaseContext());
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManagement.Log_d(TAG, "Service onStartCommand");
        serverSocket.start();
        return TcpServerService.START_STICKY;
    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        LogManagement.Log_d(TAG, "Service onBind");
        return mBinder;
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        LogManagement.Log_d(TAG, "Service onUnbind");
        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {
        LogManagement.Log_d(TAG, "Service onRebind");
    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LogManagement.Log_d(TAG, "Service onDestroy");
//        serverSocket.stop();
//        Looper.myLooper().quitSafely();
//		   Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    public boolean check(String serv){
        LogManagement.Log_d(TAG, "Service check");
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))  {
            if (serv.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        TcpServerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TcpServerService.this;
        }
    }
}
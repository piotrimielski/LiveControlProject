package com.givevision.livecontrolproject.db.wifiLogs;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.givevision.livecontrolproject.LogManagement;
import com.givevision.livecontrolproject.db.GiveVisionDatabase;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class WifiLogsRepository {
    public final static String TAG = "WifiLogsRepository";

    private String DB_NAME = "db_givevision";
    private GiveVisionDatabase gvDatabase;

    public WifiLogsRepository(Context context) {
        LogManagement.Log_d(TAG, "PictureRepository:: create database");
        gvDatabase = Room.databaseBuilder(context, GiveVisionDatabase.class, DB_NAME).build();
    }

    public void insertWifiLogs(String msg,Date currentTime) {
        WifiLogs wifiLogs = new WifiLogs();
        wifiLogs.setMsg(msg);
        wifiLogs.setSaved(false);
        wifiLogs.setCreatedAt(currentTime);
        wifiLogs.setModifiedAt(currentTime);
        insertWifiLogs(wifiLogs);
    }

    private void insertWifiLogs(final WifiLogs wifiLogs) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                gvDatabase.wifiLogsDao().insertwifiLog(wifiLogs);
                LogManagement.Log_d(TAG, "WifiLogsRepository:: insertWifiLogs");
                return null;
            }
        }.execute();
    }

    private void updateWifiLogs(final WifiLogs wifiLogs) {
        Date currentTime = new Date(Calendar.getInstance().getTimeInMillis());
        wifiLogs.setModifiedAt(currentTime);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                gvDatabase.wifiLogsDao().updateWifiLogs(wifiLogs);
                LogManagement.Log_d(TAG, "WifiLogsRepository:: updateWifiLogs");
                return null;
            }
        }.execute();
    }

    public void deleteWifiLogs(final int id) {
        final WifiLogs wifiLogs = getWifiLogsById(id);
        if (wifiLogs != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    gvDatabase.wifiLogsDao().deleteWifiLogs(wifiLogs);
                    LogManagement.Log_d(TAG, "WifiLogsRepository:: deleteWifiLogs");
                    return null;
                }
            }.execute();
        }
    }

    private void deleteWifiLogs(final WifiLogs wifiLogs) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                gvDatabase.wifiLogsDao().deleteWifiLogs(wifiLogs);
                LogManagement.Log_d(TAG, "WifiLogsRepository:: deleteWifiLogs");
                return null;
            }
        }.execute();
    }

    public void deleteWifiLogsTable() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                gvDatabase.wifiLogsDao().deleteWifiLogsTable();
                LogManagement.Log_d(TAG, "WifiLogsRepository:: deleteWifiLogsTable");
                return null;
            }
        }.execute();
    }

    public List<WifiLogs> getAllWifiLogs() {
        LogManagement.Log_d(TAG, "WifiLogsRepository:: getAllWifiLogs");
        return gvDatabase.wifiLogsDao().getAllWifiLogs();
    }

    public List<WifiLogs> loadWifiLogsByIds(int[] ids) {
        LogManagement.Log_d(TAG, "WifiLogsRepository:: loadWifiLogsByIds");
        return gvDatabase.wifiLogsDao().loadWifiLogsByIds(ids);
    }

    public List<WifiLogs> loadWifiLogsBySaved(boolean saved) {
        LogManagement.Log_d(TAG, "PictureRepository:: loadWifiLogsBySaved");
        return gvDatabase.wifiLogsDao().loadWifiLogsBySaved(saved);
    }

    public WifiLogs getWifiLogsByMsg(String msg) {
        LogManagement.Log_d(TAG, "WifiLogsRepository:: getWifiLogsByMsg");
        return gvDatabase.wifiLogsDao().findWifiLogsByMsg(msg);
    }

    public WifiLogs getWifiLogsById(int id) {
        LogManagement.Log_d(TAG, "WifiLogsRepository:: getWifiLogsById");
        return gvDatabase.wifiLogsDao().getWifiLogsById(id);
    }

    public void newInstallation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // new installation
                List<WifiLogs> wifiLogs = getAllWifiLogs();
                if(!wifiLogs.isEmpty()){
                    for(WifiLogs wifiLog : wifiLogs) {
                        if(wifiLog!=null){
                            LogManagement.Log_d(TAG, "newInstallation:: onStart picture id= "+wifiLog.getId());
                            LogManagement.Log_d(TAG, "newInstallation:: onStart picture title= "+wifiLog.getMsg());
                            LogManagement.Log_d(TAG, "newInstallation:: onStart picture active= "+wifiLog.isSaved());
                        }else{
                            LogManagement.Log_e(TAG, "newInstallation:: onStart picture empty");
                        }
                    }
                }else{
                    LogManagement.Log_e(TAG, "newInstallation:: onStart pictures empty");
                }
            }
        } ).start();
        deleteWifiLogsTable();
        LogManagement.Log_d(TAG, "newInstallation:: onStart delete database");
    }


}

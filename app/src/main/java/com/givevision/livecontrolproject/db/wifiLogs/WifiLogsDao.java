package com.givevision.livecontrolproject.db.wifiLogs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * @author Piotr
 */
@Dao
public interface WifiLogsDao {
    @Insert
    void insertwifiLog(WifiLogs wifiLogs);

    @Query("SELECT * FROM wifiLogs ORDER BY id")
    List<WifiLogs> getAllWifiLogs();

    @Query("SELECT * FROM wifiLogs WHERE id IN (:wifiLogsIds)")
    List<WifiLogs> loadWifiLogsByIds(int[] wifiLogsIds);

    @Query("SELECT * FROM wifiLogs WHERE saved = :wifiLogsSaved")
    List<WifiLogs> loadWifiLogsBySaved(boolean wifiLogsSaved);

    @Query("SELECT * FROM wifiLogs WHERE msg LIKE :wifiLogsMsg LIMIT 1")
    WifiLogs findWifiLogsByMsg(String wifiLogsMsg);

    @Query("SELECT * FROM wifiLogs WHERE id =:wifiLogsId")
    WifiLogs getWifiLogsById(int wifiLogsId);

    @Update
    void updateWifiLogs(WifiLogs wifiLogs);

    @Delete
    void deleteWifiLogs(WifiLogs wifiLogs);

    @Query("DELETE FROM wifiLogs")
    public void deleteWifiLogsTable();

}

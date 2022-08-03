package com.givevision.livecontrolproject.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.givevision.livecontrolproject.db.wifiLogs.WifiLogs;
import com.givevision.livecontrolproject.db.wifiLogs.WifiLogsDao;

/**
 * @author Piotr
 */
@Database(entities = {WifiLogs.class}, version = 1, exportSchema = false)
public abstract class GiveVisionDatabase extends RoomDatabase {
    public abstract WifiLogsDao wifiLogsDao();
}

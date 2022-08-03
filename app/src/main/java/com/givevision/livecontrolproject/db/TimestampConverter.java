package com.givevision.livecontrolproject.db;

import androidx.room.TypeConverter;

import com.givevision.livecontrolproject.util.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimestampConverter {

    private static DateFormat df = new SimpleDateFormat(Constants.formats[3], Locale.UK);

    @TypeConverter
    public static Date fromTimestamp(String value) {
        if (value != null) {
            try {
                TimeZone timeZone = TimeZone.getDefault() ;
                df.setTimeZone(timeZone);
                return df.parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }


    @TypeConverter
    public static String dateToTimestamp(Date value) {
        TimeZone timeZone = TimeZone.getDefault() ;
        df.setTimeZone(timeZone);
        return value == null ? null : df.format(value);
    }
}
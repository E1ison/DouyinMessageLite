package com.example.douyinmessagelite.utils;
import android.text.format.DateFormat;

import java.util.Calendar;
public class TimeUtils {
    public static String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long oneMinute = 60 * 1000L;
        long oneHour = 60 * oneMinute;
        long oneDay = 24 * oneHour;

        if (diff < oneMinute) {
            return "刚刚";
        } else if (diff < oneHour) {
            long minutes = diff / oneMinute;
            return minutes + " 分钟前";
        }

        Calendar msgCal = Calendar.getInstance();
        msgCal.setTimeInMillis(timestamp);
        Calendar nowCal = Calendar.getInstance();

        boolean sameDay =
                msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                        msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR);

        if (sameDay) {
            return DateFormat.format("HH:mm", timestamp).toString();
        }

        // 昨天
        nowCal.add(Calendar.DAY_OF_YEAR, -1);
        boolean yesterday =
                msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                        msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR);

        if (yesterday) {
            return "昨天 " + DateFormat.format("HH:mm", timestamp);
        }

        // 7 天内
        long days = diff / oneDay;
        if (days <= 7) {
            return days + " 天前";
        }

        // 其他：MM-dd
        return DateFormat.format("MM-dd", timestamp).toString();
    }
}

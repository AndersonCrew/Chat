package com.dazone.crewchatoff.utils;

import android.content.Context;
import android.util.Log;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static int KEY_FROM_SERVER = 200;

    public static String showTimeWithoutTimeZone(long date, String defaultPattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(defaultPattern, Locale.getDefault());
        return simpleDateFormat.format(new Date(date));
    }

    public static String getTimezoneOffsetInMinutes() {
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getRawOffset() / 60000;
        String sign = "";
        if (offsetMinutes < 0) {
            sign = "-";
            offsetMinutes = -offsetMinutes;
        }
        return sign + "" + offsetMinutes;
    }

    /**
     * Convert Time Device To Time Server
     */
    public static String convertTimeDeviceToTimeServerDefault(String regDate) {
        return "/Date(" + getTime(regDate) + ")/";
    }

    /**
     * @param regDate with format "/Date(1450746095000)/"
     * @return Today hh:mm aa || yesterday hh:mm aa || yyyy-MM-dd hh:mm aa
     */
    public static boolean checkDateIsToday(String regDate) {
        try {
            Date date = new Date(getTime(regDate));
            Date currentDate = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat(Statics.DATE_FORMAT_YYYY_MM_DD, Locale.getDefault());
            return formatter.format(date).equals(formatter.format(currentDate));
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean checkDateIsYesterday(String regDate) {
        try {
            Date date = new Date(getTime(regDate));
            Date currentDate = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat(Statics.DATE_FORMAT_YYYY_MM_DD, Locale.getDefault());
            SimpleDateFormat formatterDay = new SimpleDateFormat(Statics.DATE_FORMAT_DD, Locale.getDefault());
            return formatter.format(date).equals(formatter.format(currentDate)) && Integer.parseInt(formatterDay.format(date)) - Integer.parseInt(formatterDay.format(currentDate)) == 1;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean checkBetweenDate(String regDate1, String regDate2) {
        try {
            Date date1 = new Date(getTime(regDate1));
            Date date2 = new Date(getTime(regDate2));
            SimpleDateFormat formatter = new SimpleDateFormat(Statics.DATE_FORMAT_YYYY_MM_DD, Locale.getDefault());
            return formatter.format(date1).equals(formatter.format(date2));
        } catch (Exception ex) {
            return false;
        }
    }


    /**
     * @param context    application context
     * @param timeString with format "/Date(1450746095000)/"
     * @return Today hh:mm aa || yesterday hh:mm aa || yyyy-MM-dd hh:mm aa
     */


    public static String displayTimeWithoutOffset(Context context, String timeString, int task, int key) {
        try {
            return displayTimeWithoutOffset(context, getTime(timeString), task);
        } catch (Exception e) {
            return "";
        }
    }

    public static String displayTimeWithoutOffset(String timeString, String locale) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(locale, Locale.getDefault());
            return formatter.format(new Date(getTime(timeString)));
        } catch (Exception e) {
            return "";
        }
    }

    public static String displayTimeWithoutOffset(String timeString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(Statics.DATE_FORMAT_YYYY_MM_DD, Locale.getDefault());
            return formatter.format(new Date(getTime(timeString)));
        } catch (Exception e) {
            return "";
        }
    }

    public static Date convertStringToDate(String timeString) {
        try {
            return new Date(getTime(timeString));
        } catch (Exception e) {
            return new Date();
        }
    }

    public static long getTime(String timeString) {
        try {
            long time;
            if (timeString.contains("(")) {
                timeString = timeString.replace("/Date(", "");
                int plusIndex = timeString.indexOf("+");
                int minusIndex = timeString.indexOf("-");
                if (plusIndex != -1) {
                    time = Long.valueOf(timeString.substring(0, plusIndex));
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(time);
                    cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(timeString.substring(plusIndex + 1, plusIndex + 3)));
                    cal.add(Calendar.MINUTE, Integer.parseInt(timeString.substring(plusIndex + 3, plusIndex + 5)));
                    Calendar tCal = Calendar.getInstance();
                    tCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                    time = tCal.getTimeInMillis();
                } else if (minusIndex != -1) {
                    time = Long.valueOf(timeString.substring(0, minusIndex));
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(time);
                    cal.setTimeZone(TimeZone.getDefault());
                    cal.add(Calendar.HOUR_OF_DAY, -Integer.parseInt(timeString.substring(minusIndex + 1, minusIndex + 3)));
                    cal.add(Calendar.MINUTE, -Integer.parseInt(timeString.substring(minusIndex + 3, minusIndex + 5)));
                    Calendar tCal = Calendar.getInstance();
                    tCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                    time = tCal.getTimeInMillis();
                } else {
                    time = Long.valueOf(timeString.substring(0, timeString.indexOf(")")));
                }
            } else {
                time = Long.valueOf(timeString);
            }

            return time;
        } catch (Exception e) {
            Log.d("lchTest", e.toString());
            return 0;
        }
    }

    /**
     * format time
     *
     * @param context application context
     * @param time    long in milliseconds
     * @return Today hh:mm aa || yesterday hh:mm aa || yyyy-MM-dd hh:mm aa
     */
    //task - 0: EN
    //task - 1: KO
    public static String displayTimeWithoutOffset(Context context, long time, int task) {
        SimpleDateFormat formatter;

        if (task == 0) {
            formatter = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        } else {
            formatter = new SimpleDateFormat("aa hh:mm", Locale.getDefault());
        }

        int type = (int) getTimeForMail(time);

        String dateString;
        switch (type) {
            case -2:
                dateString = formatter.format(new Date(time)).toLowerCase();
                break;
            case -3:
                dateString = context.getString(R.string.yesterday) + " " + formatter.format(new Date(time)).toLowerCase();
                break;
            default:
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                calendar.setTime(new Date(time));
                int year = calendar.get(Calendar.YEAR);
                if (task == 0) {
                    if (currentYear == year) {
                        formatter.applyLocalizedPattern("MM-dd hh:mm aa");
                    } else {
                        formatter.applyLocalizedPattern("yyyy-MM-dd hh:mm aa");
                    }
                } else {
                    if (currentYear == year) {
                        formatter.applyLocalizedPattern("MM-dd aa hh:mm");
                    } else {
                        formatter.applyLocalizedPattern("yyyy-MM-dd aa hh:mm");
                    }
                }
                dateString = formatter.format(new Date(time)).toLowerCase();
                break;
        }
        return dateString;
    }

    //-2: today
    //-3: Yesterday
    //-4: this month
    //-5: last Month
    //-1: default

    public static long getTimeForMail(long time) {
        int date = -1;
        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time);

        if (cal.get(Calendar.YEAR) == cal1.get(Calendar.YEAR)) {
            if (cal.get(Calendar.MONTH) == cal1.get(Calendar.MONTH)) {
                int temp = cal.get(Calendar.DAY_OF_MONTH) - cal1.get(Calendar.DAY_OF_MONTH);
                if (cal.get(Calendar.DAY_OF_MONTH) == cal1.get(Calendar.DAY_OF_MONTH)) {
                    date = -2;
                } else if (temp == 1) {
                    date = -3;
                } else {
                    date = -4;
                }
            } else if (cal.get(Calendar.MONTH) - 1 == cal1.get(Calendar.MONTH)) {
                date = -5;
            }
        } else if (cal.get(Calendar.YEAR) == cal1.get(Calendar.YEAR) + 1) {
            if (cal.get(Calendar.MONTH) == 0 && cal1.get(Calendar.MONTH) == 11) {
                date = -5;
            }
        }

        return date;
    }

    public static long getStttimeMessage(long time, long lasttime) {
        long date = -1;
        int day_time = getDateNote(time);
        int month_time = getMonthNote(time);
        int year_time = getYearNote(time);
        if (year_time == getYearNote(lasttime)) {
            if (month_time == getMonthNote(lasttime)) {
                int temp = day_time - getDateNote(time);
                if (day_time == getDateNote(lasttime)) {
                    date = -2;
                } else if (temp == -1) {
                    date = -3;
                } else {
                    date = -4;
                }

            } else if (month_time - 1 == getMonthNote(lasttime)) {
                date = -5;
            }

        } else if (year_time == getYearNote(lasttime) + 1) {
            if (month_time == 0 && getMonthNote(time) == 11) {
                date = -5;
            }
        }


        return date;
    }

    //1: today
    //2: Yesterday
    //0: default
    public static int getYearNote(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getMonthNote(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        return cal.get(Calendar.MONTH);
    }

    public static int getDateNote(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean compareTime(long date1, long date2) {
        SimpleDateFormat formatter = new SimpleDateFormat(Statics.DATE_FORMAT_YY_MM_DD);
        String date = formatter.format(new Date(date1));
        String dateTemp = formatter.format(new Date(date2));
        return date.equalsIgnoreCase(dateTemp);
    }

    // Notification time convert to string
    public static String timeToStringNotAMPM(int hourOfDay, int minute) {
        String text = "";
        String minutes = "";
        if (minute < 10) {
            minutes = "0" + minute;
        } else {
            minutes = String.valueOf(minute);
        }
        if ((hourOfDay == 12 && minute > 0) || hourOfDay > 12) {// PM
            text += hourOfDay + ":" + minutes;
        } else { // AM
            if (hourOfDay < 10) {
                text += "0";
            }
            text += hourOfDay + ":" + minutes;
        }
        return text;
    }

    public static String timeToString(int hourOfDay, int minute) {
        String text = "";
        String minutes = "";
        if (minute < 10) {
            minutes = "0" + minute;
        } else {
            minutes = String.valueOf(minute);
        }
        if ((hourOfDay == 12 && minute > 0) || hourOfDay > 12) {// PM
            text = "PM ";
            text += hourOfDay + ":" + minutes;
        } else { // AM
            text = "AM ";
            if (hourOfDay < 10) {
                text += "0";
            }
            text += hourOfDay + ":" + minutes;
        }
        return text;
    }
}
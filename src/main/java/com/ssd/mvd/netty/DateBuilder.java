package com.ssd.mvd.netty;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;

public class DateBuilder {
    private final Calendar calendar;

    public DateBuilder() {
        this(TimeZone.getTimeZone("UTC"));
    }

    public DateBuilder(Date time) {
        this(time, TimeZone.getTimeZone("UTC"));
    }

    public DateBuilder(TimeZone timeZone) {
        this(new Date(0), timeZone);
    }

    public DateBuilder(Date time, TimeZone timeZone) {
        calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        calendar.setTimeInMillis(time.getTime());
    }

    public DateBuilder setYear(int year) {
        if (year < 100) {
            year += 2000;
        }
        calendar.set(Calendar.YEAR, year);
        return this;
    }

    public DateBuilder setMonth(int month) {
        calendar.set(Calendar.MONTH, month - 1);
        return this;
    }

    public DateBuilder setDay(int day) {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return this;
    }

    public DateBuilder setDate(int year, int month, int day) {
        return setYear(year).setMonth(month).setDay(day);
    }

    public DateBuilder setHour(int hour) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return this;
    }

    public DateBuilder setMinute(int minute) {
        calendar.set(Calendar.MINUTE, minute);
        return this;
    }

    public DateBuilder setSecond(int second) {
        calendar.set(Calendar.SECOND, second);
        return this;
    }

    public DateBuilder setMillis(int millis) {
        calendar.set(Calendar.MILLISECOND, millis);
        return this;
    }

    public DateBuilder setTime(int hour, int minute, int second, int millis) {
        return setHour(hour).setMinute(minute).setSecond(second).setMillis(millis);
    }

    public Date getDate() {
        return calendar.getTime();
    }

}

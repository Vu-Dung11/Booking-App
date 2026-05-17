package com.example.bookingapp.core.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Formatter {
    private static final Locale VN = new Locale("vi", "VN");
    private static final SimpleDateFormat API = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat API_UTC = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static { API_UTC.setTimeZone(TimeZone.getTimeZone("UTC")); }
    private static final SimpleDateFormat UI = new SimpleDateFormat("dd/MM/yyyy", VN);
    private static final NumberFormat NF = NumberFormat.getInstance(VN);

    private Formatter() {}

    public static String currency(BigDecimal value) {
        if (value == null) return "0 đ";
        return NF.format(value) + " đ";
    }

    public static String currency(double value) {
        return NF.format(value) + " đ";
    }

    public static String displayDate(String apiDate) {
        if (apiDate == null) return "";
        try {
            Date d = API.parse(apiDate);
            return d != null ? UI.format(d) : apiDate;
        } catch (ParseException e) {
            return apiDate;
        }
    }

    public static String toApiDate(long millis) {
        // MaterialDatePicker trả về millis ở UTC midnight → format ở UTC để không bị lệch ngày.
        return API_UTC.format(new Date(millis));
    }

    /** Parse "yyyy-MM-dd" → millis ở UTC midnight (khớp convention MaterialDatePicker). */
    public static long fromApiDate(String apiDate) {
        if (apiDate == null) return 0L;
        try {
            Date d = API_UTC.parse(apiDate);
            return d != null ? d.getTime() : 0L;
        } catch (ParseException e) {
            return 0L;
        }
    }
}

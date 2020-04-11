package pt.ulisboa.tecnico.socialsoftware.tutor.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateHandler {
    final static private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static DateTimeFormatter getFormatter() {
        return formatter;
    }

    public static String format(LocalDateTime date) {
        if (date == null) return "";
        return date.format(formatter);
    }

    public static LocalDateTime parse(String date) {
        return LocalDateTime.parse(date, formatter);
    }

    public static String formatFromRequest(String date) {
        if (date != null && !date.matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2})")) {
            return LocalDateTime.parse(date.replaceAll(".$", ""), DateTimeFormatter.ISO_DATE_TIME).format(formatter);
        }
        return date;
    }
}

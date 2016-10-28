package name.javalex.apijson.app.urlshortener.helpers;

import java.text.DateFormat;
import java.util.Date;

public class DateAndTime {
    public static String getCurrentDateAndTime() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }
}

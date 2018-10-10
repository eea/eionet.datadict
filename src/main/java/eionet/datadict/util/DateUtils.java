
package eionet.datadict.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class DateUtils {
    
    public static final String UTC_TIME_ZONE="UTC";
    
    public static String formatDateInUTC(Long date,String DateFormat){
        TimeZone timeZone = TimeZone.getTimeZone(UTC_TIME_ZONE);
        DateFormat dateFormat = new SimpleDateFormat(DateFormat);
        dateFormat.setTimeZone(timeZone);
        Date formattedDate = new Date(date);
        return dateFormat.format(formattedDate);
    }
    
}

package com.juno.datapojo;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import org.springframework.format.datetime.DateFormatter;

public class DateInterval {

    // format 24hre ex. 12:12 , 17:15
//    private static String  HOUR_FORMAT = "HH:mm";
    

    private DateInterval() {    }

    public static String getCurrentHour() {
        Calendar cal = Calendar.getInstance();
//        SimpleDateFormat sdfHour = new SimpleDateFormat(HOUR_FORMAT);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String hour = sdf.format(cal.getTime());
        return hour;
    }

    /**
     * @param  target  hour to check
     * @param  start   interval start
     * @param  end     interval end
     * @return true    true if the given hour is between
     */
    public static boolean isHourInInterval(String target, String start, String end) {
    	/*System.out.println("S " + target.compareTo(start));
    	System.out.println("E " + target.compareTo(start));*/
        return ((target.compareTo(start) >= 0)
                && (target.compareTo(end) <= 0));
    }
    
    public static boolean isDayInInterval(String target, String start, String end) {
        return ((target.compareTo(start) >= 0)
                && (target.compareTo(end) <= 0));
    }

    /**
     * @param  start   interval start
     * @param  end     interval end
     * @return true    true if the current hour is between
     */
    public static boolean isNowInInterval(String start, String end) {
        return DateInterval.isHourInInterval
            (DateInterval.getCurrentHour(), start, end);
    }

    //    TEST
    public static void main (String[] args) {
    	LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixMinutesBehind = now.plusDays(2);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     //System.out.println(sixMinutesBehind.toLocalDate());
        Duration duration = Duration.between(now, sixMinutesBehind);
        //System.out.println(duration);
//        long diff = Math.abs(duration.);
     
//		String now = "2019-05-22 07:00:00";
//      String start = "2019-05-24 08:00:00";
//      String end   = "2019-05-22 23:59:00";
//      LocalDateTime now = LocalDateTime.now();
//      System. out.println(cdate + " between " + now + "-" + end + "?");
//      System. out.println(DateInterval.isHourInInterval(cdate,now,end));
//      System.out.println(diff);
      
      /*
       * output example :
       *   21:01 between 14:00-14:26?
       *   false
       *
       */
    }

}





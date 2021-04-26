package com.juno.datapojo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.MDC;

import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;

public class SmsInterval {

	public static int getInterval(String validity, String prod_val){
		Logging.getLogger().info(validity);
		MDC.get("Robi-UNIQUE-ID");
		int result = 0;
		try{	
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
			
			Calendar start = Calendar.getInstance();
			
			if(prod_val.equalsIgnoreCase("W1") || prod_val.equalsIgnoreCase("M1")) {
				start.add(Calendar.DATE, -1);
			}
			start.set(Calendar.HOUR_OF_DAY, 1);
			start.set(Calendar.MINUTE, 00);
			start.set(Calendar.SECOND, 00);
			String startdate = sdf.format(start.getTime());
			
			Calendar end = Calendar.getInstance(); 
			if(prod_val.equalsIgnoreCase("W1") || prod_val.equalsIgnoreCase("M1")) {
				end.add(Calendar.DATE, -1);
			}
			end.set(Calendar.HOUR_OF_DAY, 23);
			end.set(Calendar.MINUTE,59);
			end.set(Calendar.SECOND,59);
			String enddate = sdf.format(end.getTime());
			
			Logging.getLogger().info("Calculating SMS reminder Interval --> startdate : "+startdate+", enddate : "+enddate);
			if(((validity.compareTo(startdate) >= 0)) && (validity.compareTo(enddate) <= 0)){
				result = 1;
				return result;
			}else{
				result =0 ;
				return result;
			}
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in calculate sms reminder Interval - "+e.getMessage());
		}
		return result;
	}

	public static boolean isHourInInterval(String target, String start, String end) {
		return ((target.compareTo(start) >= 0)
				&& (target.compareTo(end) <= 0));
	}

	public static boolean isNowInInterval(String start, String end) {
		return DateInterval.isHourInInterval
				(DateInterval.getCurrentHour(), start, end);
	}
}

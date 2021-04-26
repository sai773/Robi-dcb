package com.juno.datapojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetryResponse {
	private int r_Count;
	private String r_ChrgStatus;
	private String r_time;
	private int r_days;
	
	public RetryResponse(Integer count,String status,String time,Integer day){
		r_Count = count;
		r_ChrgStatus = status;
		r_time = time;
		r_days = day;
	}
}

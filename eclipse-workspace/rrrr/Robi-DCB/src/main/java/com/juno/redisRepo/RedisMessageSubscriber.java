package com.juno.redisRepo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.juno.controller.PurchaseController;
import com.juno.datapojo.PurchaseDetailForExpiry;
import com.juno.datapojo.PurchaseRequestDetail;
import com.juno.logs.CDRLogs;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.util.CommonHelper;
import com.juno.util.Constants;

@Component
public class RedisMessageSubscriber implements MessageListener, Constants{

	public static List<PurchaseDetailForExpiry> messageList = new CopyOnWriteArrayList<PurchaseDetailForExpiry>();

	@Value("${CdrSessionTimeout}")
	int sessiontimeout;
	
	@Autowired
	CommonHelper commonhelper;
	
	@Autowired
	PurchaseController purContrl;
	
	@Autowired
	PurchaseDetailValidationRepositoryImpl purchaseDetailValidationRepositoryImpl;

	@Override	
	public void onMessage(final Message message, final byte[] pattern) {
		try{
			//System.out.println("$$$$$$$$ RedisMessageSubscriber, Message received from queue : "+message.toString()+", msg_body : "+message.getBody());

			if (message != null) {
				String orig_msg = new String(message.getBody());
				String optxn = orig_msg.substring(orig_msg.indexOf("ASHIELD")+7, orig_msg.lastIndexOf("#"));
				String reqTime = orig_msg.substring(orig_msg.lastIndexOf("#") + 1);
				
				PurchaseDetailForExpiry expiryDetail = new PurchaseDetailForExpiry(); 
				expiryDetail.setOptxn(optxn);
				expiryDetail.setRequestTime(reqTime);
				messageList.add(expiryDetail);
			}
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in RedisMessageSubscriber-onMessage : ", e);
		}
	}

	//@Scheduled(fixedRate = 100000)
	@Scheduled(fixedDelay = 5000)
	public void SessionTimeoutTaskConsumerFromRedis() throws ParseException {
		//System.out.println("Data from Subscribe-OnMessage, data stored in LIST --> "+messageList.toString());
		try {
			if(messageList!=null || !messageList.isEmpty()){
				for(PurchaseDetailForExpiry sesDetails : messageList){
					
					//System.out.println("$$$$$$$$ FOR LOOP extracting data from list --> optxn = "+sesDetails.getOptxn()+", time = "+sesDetails.getRequestTime());
					//System.out.println("$$$$$$$$ Getting Object from image Expiry : "+sesDetails!=null?sesDetails.toString():null);
					if(sesDetails!=null){
						Date reqDateTime = CommonHelper.getDateFromString(sesDetails.getRequestTime()); 
						Date expiryDateTime = DateUtils.addMinutes(reqDateTime, sessiontimeout);
						String nowDate = commonhelper.getStrDate();
						Date rightNowTime = CommonHelper.getDateFromString(nowDate); 
						//System.out.println("$$$$$$$ org_reqTime : "+reqDateTime+", expiryTime : "+expiryDateTime+", nowTime : "+rightNowTime);
						PurchaseRequestDetail validationDetail =  purchaseDetailValidationRepositoryImpl.getPurchaseRequestDetail(sesDetails.getOptxn());
						if(validationDetail!=null){
							if (!expiryDateTime.after(rightNowTime)) {
								Logging.getLogger().info("Purchase Transaction timed out (Redis Pub/Sub) : " + sesDetails.getOptxn());
								purchaseDetailValidationRepositoryImpl.deletePurchaseRequestDetail(sesDetails.getOptxn());
								validationDetail = purContrl.XReqWithAnalysisFromDB(validationDetail);
								CDRLogs.getCDRWriter().logCDR(validationDetail, SESSION_TIMEOUT);
								messageList.remove(sesDetails);
							}
						} else {
							//System.out.println("$$$$$$$$ Image processed via check image api, so remove from imager timer thread, optxn : "+sesDetails.getOptxn());
							messageList.remove(sesDetails);
						}
					} 
				}
			}
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in SessionTimeoutTaskConsumerFromRedis : ", e);
		}
	}
}

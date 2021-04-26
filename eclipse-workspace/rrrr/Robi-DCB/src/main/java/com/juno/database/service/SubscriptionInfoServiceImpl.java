package com.juno.database.service;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.juno.database.model.AppRefererDetails;
import com.juno.database.model.CpCallbacks;
import com.juno.database.model.CpCredentials;
import com.juno.database.model.CpDetails;
import com.juno.database.model.LoginDetails;
import com.juno.database.model.ProductInfo;
import com.juno.database.model.Purchases;
import com.juno.database.model.Subscription;
import com.juno.datapojo.PurchaseRequestDetail;
import com.juno.datapojo.RetryResponse;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.util.CommonHelper;
import com.juno.util.Constants;

@Repository
public class SubscriptionInfoServiceImpl  {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	PurchaseRequestDetail purDetail;

	public static int graceCount;
	public static int parkCount;
	public static int graceRetryPerDay;
	public static int parkRetryPerDay;
	public static int callbackCount;

	@Value("${GraceRetryPerDay}")
	public void setgraceRetryPerDay(int gracePerDayCount) {
		graceRetryPerDay = gracePerDayCount;
	}

	@Value("${ParkRetryPerDay}")
	public void setparkRetryPerDay(int parkPerDayCount) {
		parkRetryPerDay = parkPerDayCount;
	}

	@Value("${GraceDays}")
	public void setgraceDays(int graceDays) {
		graceCount = graceDays;
	}

	@Value("${ParkDays}")
	public void setparkDays(int parkDays) {
		parkCount = parkDays;
	}

	@Value("${CallbackCount}")
	public void setcallbackcount(int cbCount) {
		callbackCount = cbCount;
	}

	private int rnwcount = 0, cdrcount = 0, retrycount = 0, cbcount = 0;


	public ProductInfo findByServiceName(String serviceName) {
		ProductInfo pInfo = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(serviceName != null) {
				query = session.createQuery("FROM ProductInfo where ServiceName = :serName");
				query.setString("serName", serviceName);
			} 

			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				pInfo = (ProductInfo) result.get(0);
			}
			else 
				req = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getProductInfo : ",e);
		}finally{
			session.close();
		}
		return pInfo;

	}

	public ProductInfo findByValidity(String validity, String spid) {
		ProductInfo pInfo = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(validity != null) {
				query = session.createQuery("FROM ProductInfo Where Validity = :val AND SpId = :spId");
				query.setString("val", validity);
				query.setString("spId", spid);
			} 

			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				pInfo = (ProductInfo) result.get(0);
			}
			else 
				req = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getProductInfo : ",e);
		}finally{
			session.close();
		}
		return pInfo;

	}


	public String insertNewActivation(Purchases pur) {
		Session session= null;
		Transaction tx = null;
		Serializable purchaseId = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		boolean oops;
		int retries = 3;
		MDC.get("Robi-UNIQUE-ID");
		do{
			oops = false;
			try {
				SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
				session = sessionFactory.openSession();
				tx = session.beginTransaction();
				purchaseId = session.save(pur);
				tx.commit();
				return "SUCCESS";
			} catch (TransactionException | LockAcquisitionException  sqlex) {
				oops = true;
				ErrorLogger.getLogger().error("Exception with purchaseid=" + pur.getPurchaseId(),sqlex);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ErrorLogger.getLogger().error("Exception with purchaseid=" + pur.getPurchaseId(),e);
				} 
			} catch (org.hibernate.exception.ConstraintViolationException cve){
				Logging.getLogger().info("CVE during purchase insert, allow success flow: ");
				return "SUCCESS";
			}catch (Exception e) {
				if (tx!=null) tx.rollback();
				ErrorLogger.getLogger().error("Exception in insertPurchaseRequest : ",e);
			}finally{
				session.close();
			}
		}while (oops == true && retries-- > 0);
		return "FAILURE";
	}

	public Purchases getUserPurchaseInfoFrompurchID(String purchaseId)
	{
		Session session = null;
		Purchases req = null;
		Transaction tx = null;
		MDC.get("Robi-UNIQUE-ID");
		try {

			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			Query query = session.createQuery("FROM Purchases WHERE PurchaseID = :purchaseid  ORDER BY RequestTime DESC");
			query.setString("purchaseid", purchaseId);
			query.setMaxResults(1);
			List result = query.list();

			if(!result.isEmpty()){
				req = (Purchases) result.get(0);
			}
			else
				return null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getUserPurchaseInfoFrompurchID :",e);
		}finally{
			session.close();
		}
		return req;
	}



	public CpDetails getAllurl(String spid) {
		CpDetails cpDetails = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(spid != null) {
				query = session.createQuery("FROM CpDetails WHERE SpId= :spid");
				query.setString("spid", spid);
			}
			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				cpDetails = (CpDetails) result.get(0);
			}
			else 
				req = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getCPDetails : ",e);
		}finally{
			session.close();
		}
		return cpDetails;
	}



	public String insertSubscription (Subscription pur){
		Session session = null;
		Subscription req = null;
		Transaction tx = null;
		Serializable subsid = null;
		boolean oops;
		int retries = 3;
		MDC.get("Robi-UNIQUE-ID");
		do{
			oops = false;
			try {
				SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
				session = sessionFactory.openSession();
				tx = session.beginTransaction();
				subsid = session.save(pur);
				tx.commit();
				return "SUCCESS";

			} catch (TransactionException | LockAcquisitionException  sqlex) {
				oops = true;
				ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+pur.getPurchaseId(),sqlex);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+pur.getPurchaseId(),e);
				} // short delay before retry
			} catch (Exception e) {
				if (tx!=null) tx.rollback();
				ErrorLogger.getLogger().error("Exception in insertSubscription :",e);
			}finally{
				session.close();
			}
		}while (oops == true && retries-- > 0);
		return "FAILURE";
	}

	public  String UpdateActivateRequest (String validity, PurchaseRequestDetail purReq,RetryResponse resp, Subscription sub, String respCode){
		Session session = null;
		Subscription req = null;
		Transaction tx = null;
		Query query = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MDC.get("DIA-UNIQUE-ID");
		String endDate = "";
		Calendar c = Calendar.getInstance();
		if(sub!=null){
			if(purReq.getAction().equalsIgnoreCase("act") || purReq.getAction().equalsIgnoreCase("rnw") || 
					purReq.getAction().equalsIgnoreCase("act_park") || purReq.getAction().equalsIgnoreCase("act_grace")) {
				endDate = respCode.equalsIgnoreCase("J201") ? CommonHelper.getEndDate(validity, purReq.getAction()):sdf.format(sub.getEndTime());
			} else if (purReq.getAction().equalsIgnoreCase("dct")) {
				endDate = sdf.format(c.getTime());
			}
		}
		boolean oops;
		int retries = 3;
		do{
			oops = false;
			try {
				SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
				session = sessionFactory.openSession();
				tx = session.beginTransaction();
				if(sub==null){
					query = session.createQuery("UPDATE Subscription set "
							+"'PurchaseID' = :reqId, Price = :amt, Validity = :val, Status = :status, "
							+ "StartTime = :startime, EndTime = :endTime, "
							+" RenewStatus = \'0\', "
							+ "RetryCount = :rtycnt, ChargeStatus = :rtystat, RetryDays = :rtyday, RetryTime = :rtytime "    
							+"where Msisdn = :msisdn "
							+"AND 'ServiceProvName' = :pid AND 'SpId' = :cpid AND Status = \'dct\'");

					query.setString("reqId", purReq.getPurchaseId());
					query.setString("amt", purReq.getPrice()); 
					query.setString("val", purReq.getValidity());
					query.setString("status", "act");
					query.setTimestamp("startime", sdf.parse(CommonHelper.getStrDate()));
					query.setString("endTime", CommonHelper.getEndDate(validity, purReq.getAction()));
					query.setInteger("rtycnt", resp.getR_Count());
					query.setString("rtystat", resp.getR_ChrgStatus());
					query.setInteger("rtyday", resp.getR_days());
					query.setString("rtytime", resp.getR_time());
					query.setString("msisdn", purReq.getMsisdn());
					query.setString("pid", purReq.getSerProvName());
					query.setString("cpid", purReq.getSpid());
				} 
				else if (sub!=null && respCode.equalsIgnoreCase("J201")){
					if(sub.getAction().equalsIgnoreCase("dct")){
						query = session.createQuery("UPDATE Subscription set "
								+"RequestId = :reqId, Price = :amt, Validity = :val, "
								+"EndTime = :endTime,  "
								+" RenewStatus = \'0\', SubProdid = :prodid, Status = :action, "
								+" RetryCount = :rtycnt, ChargeStatus = :rtystat, RetryDays = :rtyday, RetryTime = :rtytime "
								+"where RequestId = :old_reqid AND "
								+"Msisdn = :msisdn AND Partner = :cpid "
								+"AND ProductId = :pid ");
						query.setString("action", "act");
					} else {
						query = session.createQuery("UPDATE Subscription set "
								+"RequestId = :reqId, Price = :amt, Validity = :val, "
								+"EndTime = :endTime, "
								+" RenewStatus = \'0\', SubProdid = :prodid, "
								+" RetryCount = :rtycnt, ChargeStatus = :rtystat, RetryDays = :rtyday, RetryTime = :rtytime "
								+"where RequestId = :old_reqid AND "
								+"Msisdn = :msisdn AND Partner = :cpid "
								+"AND ProductId = :pid ");
					}

					query.setString("reqId", purReq.getPurchaseId());
					query.setString("amt", purReq.getPrice()); 
					query.setString("val", purReq.getValidity());
					query.setString("status", "act");
					query.setTimestamp("startime", sdf.parse(CommonHelper.getStrDate()));
					query.setString("endTime", CommonHelper.getEndDate(validity, purReq.getAction()));
					query.setInteger("rtycnt", resp.getR_Count());
					query.setString("rtystat", resp.getR_ChrgStatus());
					query.setInteger("rtyday", resp.getR_days());
					query.setString("rtytime", resp.getR_time());
					query.setString("msisdn", purReq.getMsisdn());
					query.setString("pid", purReq.getSerProvName());
					query.setString("cpid", purReq.getSpid());
				} 
				else if(sub!=null && !respCode.equalsIgnoreCase("J201")){
					query = session.createQuery("UPDATE Subscription set "
							+"RequestId = :reqId,  "
							+"EndTime = :endTime, "
							+" RetryCount = :rtycnt, ChargeStatus = :rtystat, RetryDays = :rtyday, RetryTime = :rtytime "
							+"where RequestId = :old_reqid AND "
							+"Msisdn = :msisdn AND Partner = :cpid "
							+"AND ProductId = :pid ");

					query.setString("reqId", purReq.getPurchaseId());
					query.setString("amt", purReq.getPrice()); 
					query.setString("val", purReq.getValidity());
					query.setString("status", "act");
					query.setTimestamp("startime", sdf.parse(CommonHelper.getStrDate()));
					query.setString("endTime", CommonHelper.getEndDate(validity, purReq.getAction()));
					query.setInteger("rtycnt", resp.getR_Count());
					query.setString("rtystat", resp.getR_ChrgStatus());
					query.setInteger("rtyday", resp.getR_days());
					query.setString("rtytime", resp.getR_time());
					query.setString("msisdn", purReq.getMsisdn());
					query.setString("pid", purReq.getSerProvName());
					query.setString("cpid", purReq.getSpid());
				}
				query.setMaxResults(1);
				int result = query.executeUpdate();
				tx.commit();
				Logging.getLogger().info(query.getQueryString());

				if(result > 0) 
					return "SUCCESS";

			} catch (TransactionException | LockAcquisitionException  sqlex) {
				oops = true;
				ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+purReq.getPurchaseId(),sqlex);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+purReq.getPurchaseId(),e);
				} // short delay before retry
			} catch (Exception e) {
				if (tx!=null) tx.rollback();
				ErrorLogger.getLogger().error("Exception in UpdateActivateRequest : ",e);
			}finally{
				session.close();
			}
		}while (oops == true && retries-- > 0);
		return "FAILURE";
	}


	public  String UpdateRenewalActRequest (String status, String reqId, String msisdn,
			String validity, Integer rnwStatus, String new_reqId, RetryResponse resp, String endTime,
			String action){

		Session session = null;
		Subscription req = null;
		Transaction tx = null;
		Query query = null;
		MDC.get("Robi-UNIQUE-ID");
		String flagstatus = (status.equalsIgnoreCase("J201"))?"1":"0";
		String lastdate = (status.equalsIgnoreCase("J201")) ? CommonHelper.getEndDate(validity, "act") :endTime;

		Integer new_rnwstatus = 0;
		if(flagstatus.equalsIgnoreCase("1")) {
			new_rnwstatus = ++rnwStatus;
		} else {
			new_rnwstatus = rnwStatus;
		}
		boolean oops;
		int retries = 3;
		do{
			oops = false;
			try {
				SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
				session = sessionFactory.openSession();
				tx = session.beginTransaction();
				query = session.createQuery("UPDATE Subscription set "
						+"EndTime = :date, "
						+"RenewStatus = :rnw, "
						//+"PurchaseID = :new_reqid, "
						+"Action = :actn,  " 
						+" RetryCount = :rtycnt, ChargeStatus = :rtystat, RetryDays = :rtyday, RetryTime = :rtytime "
						+"where PurchaseID = :old_reqid AND "
						+"Msisdn = :msisdn ");

				query.setString("date", lastdate);
				query.setInteger("rnw", new_rnwstatus);
				query.setString("actn", action);
				//query.setString("new_reqid", new_reqId);
				query.setInteger("rtycnt", resp.getR_Count());
				query.setString("rtystat", resp.getR_ChrgStatus());
				query.setInteger("rtyday", resp.getR_days());
				query.setString("rtytime", resp.getR_time());
				query.setString("old_reqid", reqId);
				query.setString("msisdn", msisdn);


				query.setMaxResults(1);

				int result = query.executeUpdate();
				tx.commit();
				Logging.getLogger().info(query.getQueryString());

				if(result > 0) 
					return "SUCCESS";

			} 
			catch (TransactionException | LockAcquisitionException  sqlex) {
				oops = true;
				ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+reqId,sqlex);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+reqId,e);
				} // short delay before retry
			} catch (Exception e) {
				if (tx!=null) tx.rollback();
				ErrorLogger.getLogger().error("Exception in UpdateRenewalActRequest : ",e);
			}finally{
				session.close();
			}
		}while (oops == true && retries-- > 0);
		return "FAILURE";
	}

	public  Subscription getSubscriptionData(String msisdn, String spid, String serviceName, String action) {
		Session session = null;
		Subscription resp = null;
		Transaction tx = null;
		Query query = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(action.equalsIgnoreCase("act")) {
				query = session.createQuery("FROM Subscription "
						+"WHERE Msisdn = :msisdn "
						+"AND SpId = :spid "
						//+"AND ChargeStatus = 'Y' "
						//+"AND ServiceName like :serName"
						+ "ORDER BY StartTime DESC");

				query.setString("msisdn", msisdn);
				query.setString("spid", spid);
				//query.setString("serName", serviceName);
			}else {
				query = session.createQuery("FROM Subscription "
						+"WHERE Msisdn = :msisdn "
						+"AND SpId = :spid "
						+"AND ServiceName like :pid "
						//+ "AND Action <> \'dct\' "
						//+"AND ChargeStatus = \'Y\' "
						+"ORDER BY StartTime DESC");

				query.setString("msisdn", msisdn);
				query.setString("spid", spid);
				query.setString("pid", serviceName.substring(0, serviceName.lastIndexOf(" "))+"%");
			}
			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());
			if(!result.isEmpty()){
				resp = (Subscription) result.get(0);
				Logging.getLogger().info("Selected Subscription Data for SMS-DCT/ACT Request --> PurchaseID: " + resp.getPurchaseId() +
						" ,SID: " + resp.getSpId() + 
						" ,msisdn: " + resp.getMsisdn() +
						" ,price: " + resp.getAmount() +
						" ,validity: " + resp.getValidity() +
						" ,status: "+ resp.getJunoStatus() +
						" ,startTime: "+ resp.getStartTime() +
						" ,endTime: "+ resp.getEndTime() +
						" ,renewalStatus: "+ resp.getRenewStatus()+
						", ChargeStatus:"+ resp.getCharged()+
						", RetryCount:"+ resp.getRetryCount()+
						", RetryDay:"+ resp.getRetryDays()+
						", RetryTime:"+ resp.getRetryTime());
			}
			else 
				resp = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getSubscriptionData : ",e);
		}finally{
			session.close();
		}
		return  resp;
	}

	public  Purchases getUserPurchaseInfo(String MSISDN, String purchaseId, String serviceName){
		Session session = null;
		Purchases req = null;
		Transaction tx = null;
		Query query = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			if(MSISDN != null && serviceName !=null && purchaseId != null){
				query = session.createQuery("FROM Purchases "
						+"WHERE PurchaseID = :purchaseid "
						+"and MSISDN = :msisdn " 
						+"and ServiceName = :srvid "
						+"ORDER BY RequestTime DESC");
				query.setString("purchaseid", purchaseId);
				query.setString("msisdn", MSISDN);
				query.setString("srvid", serviceName);
			} else {
				query = session.createQuery("FROM Purchases "
						+"WHERE MSISDN = :msisdn " 
						+"and ServiceName = :srvid "
						+"ORDER BY RequestTime DESC");
				query.setString("msisdn", MSISDN);
				query.setString("srvid", serviceName);
			}

			query.setMaxResults(1);

			List result = query.list();

			if(!result.isEmpty()){
				req = (Purchases) result.get(0);

			}
			else
				return null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getUserPurchaseInfo :",e);
		}finally{
			session.close();
		}
		return req;
	}

	public  List<Subscription> SubscriptionFailedRetry() {
		Session session = null;
		List<Subscription> req = null;
		Transaction tx = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			Query query = session.createQuery("From Subscription where "
					+ "((Action=\'act\' and RetryDays <= :park) or (Action=\'rnw\' and RetryDays <= :grace)) "
					+ "and RetryTime is not null and ChargeStatus = \'P\' and (RetryCount >= 0 and RetryCount<= :count) "
					+ "and (TIMESTAMPDIFF(MINUTE,RetryTime,ADDTIME(UTC_TIMESTAMP, '06:00:00')) > 10) "
					+ "and (TIMESTAMPDIFF(MINUTE,RetryTime,ADDTIME(UTC_TIMESTAMP, '06:00:00')) < 240) "
					+ "ORDER BY RetryTime ASC"); //DESC
			//			Query query = session.createQuery("From Subscription where "
			//					+ "((Action='act' and RetryDays <= :park) or (Action='rnw' and RetryDays <= :grace)) "
			//					+ "and RetryTime is not null and ChargeStatus = 'P' and (RetryCount >= 0 and RetryCount <= :count) "
			//					+ "and RetryTime like '%2019-05-23%' ");

			query.setInteger("park", parkCount);
			query.setInteger("grace", graceCount); 
			query.setInteger("count", graceRetryPerDay);
			query.setMaxResults(1000);
			//List result = query.list();

			retrycount++;
			if(retrycount == 200) {
				Logging.getLogger().info(query.getQueryString());
				retrycount = 0;
			}
			req = query.list();
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in SubscriptionFailedRetry : ",e);
		}finally{
			session.close();
		}
		return req;
	}

	public String updateDeactivateStatusinSubscription(String Msisdn, String serName, String action) {
		Session session = null;
		Subscription resp = null;
		Transaction tx = null;
		Query query = null;
		String status = "Failure";
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(serName != null) {
				query = session.createQuery("Update Subscription set Action= :act, ChargeStatus=\'N\' "
						+"WHERE Msisdn = :msisdn "
						+"AND ServiceName = :serName");

				query.setString("msisdn", Msisdn);
				query.setString("act", action);
				query.setString("serName", serName);

			}
			query.setMaxResults(1);
			int result = query.executeUpdate();
			tx.commit();
			Logging.getLogger().info(query.getQueryString());

			if (result > 0) // number of rows updated
				status =  "SUCCESS";
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in updateDeactivateStatusinSubscription : ",e);
		}finally{
			session.close();
		}
		return status;
	}

	public String updateDeactivateStatusinPurchases(String Msisdn, String serName, String action) {
		Session session = null;
		Purchases resp = null;
		Transaction tx = null;
		Query query = null;
		String status = "FAILURE";
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(Msisdn != null) {	
				query = session.createQuery("Update Purchases set Action= :act, Charged='Unsubscribed' "
						+"WHERE Msisdn = :msisdn "
						+"AND ServiceName = :ServiceName");

				query.setString("msisdn", Msisdn);
				query.setString("act", action);
				query.setString("ServiceName", serName);

			}
			query.setMaxResults(10);
			int result = query.executeUpdate();
			tx.commit();
			Logging.getLogger().info(query.getQueryString());

			if (result > 0) // number of rows updated
				status =  "SUCCESS";
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in updateDeactivateStatusinPurchases : ",e);
		}finally{
			session.close();
		}
		return status;
	}

	public  List<Subscription> SubRenewalSMS(String SpId, String srvName) {
		Session session = null;
		List<Subscription> req = null;
		Transaction tx = null;
		int retrycount=0;
		Query query = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(SpId!=null && srvName==null){
				ArrayList<Long> ids = new ArrayList<Long>();
				for (String i : SpId.split(",")){
					ids.add(Long.parseLong(i));
				}

				query = session.createQuery("From Subscription where (Action=\'act\' OR Action=\'rnw\') AND"
						+ " ChargeStatus = \'Y\' AND "
						+ "SpId IN :spid "
						+ "ORDER BY EndTime DESC"); 
				query.setParameterList("spid", ids);
			} 
			else if(srvName!=null && SpId==null){
				ArrayList<String> sName = new ArrayList<String>();
				for (String i : srvName.split(",")){
					sName.add(i);
				}
				query = session.createQuery("From Subscription where (Action=\'act\' OR Action=\'rnw\') AND"
						+ " ChargeStatus = \'Y\' AND "
						+ "ServiceName IN :srv "
						+ "ORDER BY EndTime DESC"); 
				query.setParameterList("srv", sName);
			}
			query.setMaxResults(50);
			retrycount++;
			if(retrycount == 50) {
				Logging.getLogger().info(query.getQueryString());
				retrycount = 0;
			}
			req = query.list();
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in SubRenewalSMS : ",e);
		}finally{
			session.close();
		}
		return req;
	}


	public String updateSmsCount(String Msisdn, String purchaseId, String count) {
		Session session = null;
		Subscription resp = null;
		Transaction tx = null;
		Query query = null;
		String status = "Failure";
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(Msisdn != null) {	
				query = session.createQuery("Update Subscription set SmsCount= :count "
						+"WHERE Msisdn = :msisdn "
						+"AND PurchaseID = :purchaseId");

				query.setString("msisdn", Msisdn);
				query.setString("count", count);
				query.setString("purchaseId", purchaseId);
			}
			query.setMaxResults(1);
			int result = query.executeUpdate();
			tx.commit();
			Logging.getLogger().info(query.getQueryString());

			if (result > 0) // number of rows updated
				status =  "SUCCESS";
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in : ",e);
		}finally{
			session.close();
		}
		return status;
	}

	public List<Subscription> getSubRenewalData() {
		Session session = null;
		List<Subscription> req = null;
		Transaction tx = null;
		Query query = null;
		int rnwcount = 0, cdrcount = 0, retrycount = 0, cbcount = 0;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			query = session.createQuery("FROM Subscription "
					+"WHERE ((Action = \'act\') OR (Action = \'rnw\')) "
					+ " AND (ChargeStatus = \'Y\') AND "
					+ " DATE(EndTime) like CURDATE()");
			/*+ "TIMESTAMPDIFF(MINUTE,EndTime,ADDTIME(UTC_TIMESTAMP, '06:00:00')) > 5 "
					+"AND TIMESTAMPDIFF(MINUTE,EndTime,ADDTIME(UTC_TIMESTAMP, '06:00:00')) < 240");*/

			query.setMaxResults(1000);

			List result = query.list();

			rnwcount++;
			if(rnwcount == 50) {
				Logging.getLogger().info(query.getQueryString());
				rnwcount = 0;
			}
			req = query.list();
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getSubRenewalData :",e);
		}finally{
			session.close();
		}
		return req;
	}

	public synchronized String insertCpdeliveryString(PurchaseRequestDetail pur, String url, String cpdeliveryStr, String status) throws ParseException  {
		Session session = null;
		Transaction tx = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		CpCallbacks cpCallBckReq = new CpCallbacks();
		cpCallBckReq.setPurchaseId(pur.getPurchaseId());
		cpCallBckReq.setRequestTime(sdf.parse(CommonHelper.getStrDate()));
		cpCallBckReq.setCpdeliveryStr(cpdeliveryStr);
		cpCallBckReq.setCallbackTime(sdf.parse(CommonHelper.getStrDate()));
		cpCallBckReq.setCallbackStatus(status);
		cpCallBckReq.setSpId(pur.getSpid());
		cpCallBckReq.setRetryCount(0);
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			session.save(cpCallBckReq);
			tx.commit();
			Logging.getLogger().info("Inserting Cp Delivery String id " + cpCallBckReq.getPurchaseId());
			return "SUCCESS";

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			Logging.getLogger().error("Exception", e);
		} finally {
			session.close();
		}
		return "FAILURE";
	}

	public List<CpCallbacks> getCPCallbackDetails(String status){
		Session session = null;
		Transaction tx = null;
		Query query = null;
		List<CpCallbacks>  result = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(status.equalsIgnoreCase("P")){
				query = session.createQuery("FROM CpCallbacks  WHERE CallbackStatus = :cstatus "
						+ "and RetryCount> :val and RetryCount<= :Ccount "
						+ " ORDER BY RequestTime DESC");

				query.setString("cstatus", "P");
				query.setInteger("val", 0);
				query.setInteger("Ccount", callbackCount);
				query.setMaxResults(1000);
			}
			else{
				query = session.createQuery("FROM CpCallbacks WHERE CallbackStatus = :cstatus "
						+ "and RetryCount= :val "
						+ " ORDER BY RequestTime ASC");

				query.setString("cstatus", "P");
				query.setInteger("val", 0);
				query.setMaxResults(1000);
			}

			cbcount++;
			if(cbcount == 250) {
				Logging.getLogger().info(query.getQueryString());
				cbcount = 0;
			}

			result = query.list();
			return result;
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in updateCallbackStatus :",e);
		}finally{
			session.close();
		}
		return result;
	}

	public  String updateCallbackStatus(String status,String purchaseID){
		Session session = null;
		Transaction tx = null;
		Query query = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		boolean oops;
		int retries = 3;
		MDC.get("DIA-UNIQUE-ID");
		do{
			oops = false;
			try {
				SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
				session = sessionFactory.openSession();
				tx = session.beginTransaction();
				query = session.createQuery("UPDATE CpCallbacks  set "
						+"CallbackStatus = :cstatus "
						+", RetryCount = RetryCount + :cnt "
						+", CallbackTime= :cbtime "
						+" where PurchaseID = :purID ");

				query.setString("cstatus",status);
				query.setString("purID",purchaseID);
				query.setInteger("cnt",1);
				query.setTimestamp("cbtime", sdf.parse(CommonHelper.getStrDate()));
				query.setMaxResults(1);
				int result = query.executeUpdate();
				tx.commit();
				Logging.getLogger().info(query.getQueryString());

				if(result > 0){	
					return "SUCCESS";
				}
			} catch (TransactionException | LockAcquisitionException  sqlex) {
				oops = true;
				ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+purchaseID,sqlex);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ErrorLogger.getLogger().error("DBManger Exception with purchaseid="+purchaseID,e);
				} // short delay before retry
			}catch (Exception e) {
				ErrorLogger.getLogger().error("Exception",e);
			}finally{
				session.close();
			}
		}while (oops == true && retries-- > 0);
		return "FAILURE";
	}

	public CpCredentials getTokenCredentials(String spid) {
		CpCredentials cpCredentials = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(spid != null) {
				query = session.createQuery("FROM CpCredentials WHERE SpId= :spid");
				query.setString("spid", spid);
			}
			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				cpCredentials = (CpCredentials) result.get(0);
			}
			else 
				req = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getTokenCredentials : ",e);
		}finally{
			session.close();
		}
		return cpCredentials;
	}

	public CpCredentials getASsecretKey(String spid) {
		Session session = null;
		CpCredentials resp = null;
		Transaction tx = null;
		Query query = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			query = session.createQuery("FROM CpCredentials "
					+"where SpId = :spid");
			query.setString("spid", spid);
			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());
			if(!result.isEmpty()){
				resp = (CpCredentials) result.get(0);
				Logging.getLogger().info("Selected CpCredentials Data for SMS-DCT/ACT Request --> SpId: " + resp.getSpId()+
						" ,User: " + resp.getUsrName() + 
						" ,Pwd: " + resp.getPwd() +
						" ,ConsumerKey: " + resp.getConsumerKey() +
						" ,ConsumerSecret: " + resp.getConsumerSecret() +
						" ,ASsecretKey: "+ resp.getAsSecretKey());
			}
			else 
				resp = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getASsecretKey : ",e);
		}finally{
			session.close();
		}
		return  resp;
	}

	public String findShortCodeBySpId(String spid) {
		String smsCode = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			query = session.createQuery("SELECT p.smsShortCode FROM ProductInfo p where p.spId = :spId");
			query.setString("spId", spid);
			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				smsCode = (String) result.get(0);
			}
			else 
				smsCode = null;
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getShortCodeFromProductInfo : ",e);
		}finally{
			session.close();
		}
		return smsCode;
	}

	public String findShortCodeBySpIdAndSrvName(String spid, String srvName) {
		String smsCode = null;
		Integer rnwConfig = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			query = session.createQuery("SELECT p.smsShortCode FROM ProductInfo p where p.spId = :spId and p.serviceName = :sName and p.rnwSms = :enable");
			query.setString("sName", srvName);
			query.setString("spId", spid);
			query.setInteger("enable", 1);

			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				smsCode = (String) result.get(0);
			}
			else 
				smsCode = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in findShortCodeBySpIdAndSrvName : ",e);
		}finally{
			session.close();
		}
		return smsCode;
	}

	public String findSPreSmsSpId() {
		String presmsSpid = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			query = session.createQuery("SELECT DISTINCT(p.spId) FROM ProductInfo p where p.preSmsNotify = :id");
			query.setString("id", "1");
			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){
				presmsSpid = StringUtils.join(result, ",");
			}
			else 
				presmsSpid = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in findSPreSmsSpId : ",e);
		}finally{
			session.close();
		}
		return presmsSpid;
	}

	public String findSubPreSmsServiceName() {
		String presmsSpid = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			query = session.createQuery("SELECT DISTINCT(p.serviceName) FROM ProductInfo p where p.preSmsNotify = :id");
			query.setString("id", "1");
			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){
				presmsSpid = StringUtils.join(result, ",");
			}
			else 
				presmsSpid = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in findSubPreSmsServiceName : ",e);
		}finally{
			session.close();
		}
		return presmsSpid;
	}
	
	public LoginDetails findLoginDetailsByUsr(String usr) {
		LoginDetails lInfo = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(usr != null) {
				query = session.createQuery("FROM LoginDetails where Username = :user");
				query.setString("user", usr);
			} 

			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				lInfo = (LoginDetails) result.get(0);
				return lInfo;
			}
			else 
				lInfo = null;
			
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getLoginDetails : ",e);
		}finally{
			session.close();
		}
		return lInfo;
	}
	
	public Subscription checkSubMsisdn(String mdn) {
		Subscription sub = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(mdn != null) {
				query = session.createQuery("FROM Subscription where Msisdn = :msisdn");
				query.setString("msisdn", mdn);
			} 

			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				sub = (Subscription) result.get(0);
			}
			else 
				req = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getLoginDetails : ",e);
		}finally{
			session.close();
		}
		return sub;
	}
	
	//updared from Endtime to Starttime on 08/04/2020 @Swe
	public List<Subscription> getMsisdnRecords(String msisdn,String spid) {
		Session session = null;
		Transaction tx = null;
		Query query = null;
		List<Subscription> sub = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(spid.equalsIgnoreCase("all")) {
				//query = session.createQuery("FROM Subscription where Msisdn = :mdn ORDER BY EndTime DESC ");
				query = session.createQuery("FROM Subscription where Msisdn = :mdn ORDER BY StartTime DESC ");
				query.setString("mdn", msisdn);
			} else {
				//query = session.createQuery("FROM Subscription where Msisdn = :mdn and SpId = :sid ORDER BY EndTime DESC ");
				query = session.createQuery("FROM Subscription where Msisdn = :mdn and SpId = :sid ORDER BY StartTime DESC ");
				query.setString("mdn", msisdn);
				query.setString("sid", spid);
			}
			
			query.setMaxResults(10);
			Logging.getLogger().info("CC-Deactivation QueryString : " + query.getQueryString());
			List<Subscription> result = query.list();
			return result;
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in getMsisdnRecords :", e);
		} finally {
			session.close();
		}
		return sub;
	}
	
	public String UpdateTollFreeDeactivateResponse(String srvName, String msisdn, String status, String loginUserId) {
		Session session = null;
		Transaction tx = null;
		Query query = null;
		MDC.get("ROBI-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			
				query = session.createQuery("UPDATE Subscription set Status = :status, "
						+ "LoginId = :lid where Msisdn = :mdn and ServiceName = :srvname");
				query.setString("mdn", msisdn);
				query.setString("status", status);
				query.setString("lid", loginUserId);
				query.setString("srvname", srvName);
				query.setMaxResults(1);
				
				int result = query.executeUpdate();
				tx.commit();
				Logging.getLogger().info(query.getQueryString());

				if(result > 0) 
					return Constants.SUCCESS_RESPONSE;
			} catch (Exception e) {
				ErrorLogger.getLogger().error("Exception in UpdateTollFreeDeactivateResponse :", e);
			} finally {
				session.close();
			}
			return Constants.FAILURE_RESPONSE;
		}
		
	public Subscription findSubDataByPurId(String purId) {
		Subscription sub = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("Robi-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(purId != null) {
				query = session.createQuery("FROM Subscription where PurchaseID = :pid");
				query.setString("pid", purId);
			} 

			query.setMaxResults(1);

			List result = query.list();
			Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				sub = (Subscription) result.get(0);
			}
			else 
				req = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in findSubDataByPurId : ",e);
		}finally{
			session.close();
		}
		return sub;
	}
	
	public com.juno.database.model.AppRefererDetails findAppReferer(String xreqWith) {
		AppRefererDetails appRef = null;
		Session session = null;
		Transaction tx = null;
		Query query = null;
		String req = null;
		MDC.get("ImiCG-UNIQUE-ID");
		try {
			SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if(xreqWith != null) {
				query = session.createQuery("FROM AppRefererDetails where Package = :appId");
				query.setString("appId", xreqWith);
			} 
			query.setMaxResults(1);
			List result = query.list();
			//Logging.getLogger().info(query.getQueryString());

			if(!result.isEmpty()){			
				appRef = (AppRefererDetails) result.get(0);
			}
			else 
				appRef = null;

		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in findAppReferer : ",e);
		}finally{
			session.close();
		}
		return appRef;
	}

	public String insertAppPkgName(ArrayList<String> applist) {
		Session session= null;
		Transaction tx = null;
		Serializable purchaseId = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		boolean oops;
		int retries = 3;
		MDC.get("ImiCG-UNIQUE-ID");
		do{
			oops = false;
			try {
				SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
				session = sessionFactory.openSession();
				tx = session.beginTransaction();

				//System.out.println("list size : "+applist.size()+", values : "+applist.toString());
				for ( int i=0; i<applist.size(); i++ ) {
					String[] data = applist.get(i).split("-");
					
					//System.out.println("appname data inside loop : "+applist.get(i)+", splitted data --> package : "+data[0]+", status : "+data[1]);
					Logging.getLogger().info("Inserting AppName to DB in loop : "+applist.get(i)+", AppPackageName : "+applist.get(i).substring(0, applist.get(i).lastIndexOf("-"))+", Status : "+data[data.length-1]);
					
					//extract app list data and process requests as 
					AppRefererDetails appDetails = new AppRefererDetails();
					
					appDetails.setAppPackage(applist.get(i).substring(0, applist.get(i).lastIndexOf("-")));
					appDetails.setAppStatus(data[data.length-1]);
					
					session.save(appDetails);
					if ( i > 0 && i % 50 == 0 ) { //20, same as the JDBC batch size
						//flush a batch of inserts and release memory:
						session.flush();
						session.clear();
					}
				}
				tx.commit();
				return Constants.SUCCESS_RESPONSE;
			} catch (TransactionException | LockAcquisitionException  sqlex) {
				oops = true;
				ErrorLogger.getLogger().error("Exception in insertAppPkgName : " ,sqlex);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ErrorLogger.getLogger().error("Exception in insertAppPkgName : " ,e);
				} 
			} catch (org.hibernate.exception.ConstraintViolationException cve){
				Logging.getLogger().info("CVE during App Package Name insert, allow success flow: ");
				return Constants.SUCCESS_RESPONSE;
			}catch (Exception e) {
				if (tx!=null) tx.rollback();
				ErrorLogger.getLogger().error("Exception in insertAppPkgName : ",e);
			}finally{
				session.close();
			}
		}while (oops == true && retries-- > 0);
		return Constants.FAILURE_RESPONSE;
	}
}






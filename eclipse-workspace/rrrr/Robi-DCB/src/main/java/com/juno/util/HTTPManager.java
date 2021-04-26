package com.juno.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.MDC;

import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;

public class HTTPManager {
	private static MultiThreadedHttpConnectionManager connectionMgr = null;
	static{
		connectionMgr = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setConnectionTimeout(30);
		params.setMaxTotalConnections(1000);
		connectionMgr.setParams(params);
	}

	public static synchronized HttpClient getConnection(){
		return new HttpClient(connectionMgr);
	}

	public static String callHTTPs(String URL, String urlParameters,String method, boolean secure, boolean param_Inbody) {
		StringBuffer urlResponse = new StringBuffer("");
		int responseCode = 0;
		MDC.get("Robi-UNIQUE-ID");
		try {
			if (secure) { // HTTPS - Create a trust manager that does not validate certificate chains
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs,
							String authType) {
					}

					public void checkServerTrusted(X509Certificate[] certs,
							String authType) {
					}

					@Override
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] arg0,
							String arg1) throws CertificateException {
						// TODO Auto-generated method stub
					}

					@Override
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] arg0,
							String arg1) throws CertificateException {
						// TODO Auto-generated method stub
					}
				} };

				// Install the all-trusting trust manager
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc
						.getSocketFactory());

				// Create all-trusting host name verifier
				HostnameVerifier allHostsValid = new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				};
				// Install the all-trusting host verifier
				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			}// end HTTPS

			URL url = null;
			if(param_Inbody==true){
				url = new URL(URL);
			} else {
				url = new URL(URL+"?"+urlParameters); //params in query string
			}
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(20000);

			if (method.equalsIgnoreCase("GET")) {
				conn.setRequestMethod(method);
			}else if (method.equalsIgnoreCase("POST")) {
				conn.setRequestMethod(method);
				conn.setDoOutput(true);
				OutputStream os = conn.getOutputStream();
				if(param_Inbody==true){
					if (urlParameters != null && !urlParameters.equals("")) {
						os.write(urlParameters.getBytes()); // params in post body
					}
					os.flush();
				}
			}

			responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));
				String output = "";
				while ((output = br.readLine()) != null) {
					urlResponse.append(output.trim());
				}
			} else {
				urlResponse.append("[ERROR>]");
				//if(conn!=null)
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(
							(conn.getErrorStream())));
					String output = "";
					while ((output = br.readLine()) != null) {
						urlResponse.append(output);
					}
				}
			}

			Logging.getLogger().info("[URL] " + URL + "\n[URLPARAMETERS] " +
			urlParameters + "\n[RESPONSE_CODE] " + responseCode);
			 //+ "\n[URL_RESPONSE] " + urlResponse);

			conn.disconnect();
		} catch (MalformedURLException e) {
			ErrorLogger.getLogger().error(" Got error at: MalformedURLException: "+e.getMessage());
		} catch (IOException e) {
			ErrorLogger.getLogger().error(" Got error at: IOException: " +e.getMessage());
		} catch (Exception e) {
			ErrorLogger.getLogger().error(" Got error at: Exception: " +e.getMessage());
		}
		//return urlResponse.toString();
		return String.valueOf(responseCode);
	}

	public static String callHTTP(String URL,String param, String method) {
		MDC.get("Robi-UNIQUE-ID");
		String urlResponse = "";
		try {
			URL url = new URL(URL+param);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(20000);
			int responseCode = conn.getResponseCode();

			if (responseCode == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				String output = "";
				while ((output = br.readLine()) != null) {
					urlResponse += output;
				}

			} else {
				urlResponse = "[ERROR>]";
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
				String output = "";
				while ((output = br.readLine()) != null) {
					urlResponse += output;
				}
			}
			Logging.getLogger().info("CallHTTP response : "+ urlResponse);
			conn.disconnect();
		} catch (MalformedURLException e) {
			ErrorLogger.getLogger().error(" Got error at: MalformedURLException: " + e.getMessage());
		} catch (IOException e) {
			ErrorLogger.getLogger().error(" Got error at: IOException: " + e.getMessage());
		} catch (Exception e) {
			ErrorLogger.getLogger().error(" Got error at: Exception: " + e.getMessage());
		}
		return urlResponse;
	}
}

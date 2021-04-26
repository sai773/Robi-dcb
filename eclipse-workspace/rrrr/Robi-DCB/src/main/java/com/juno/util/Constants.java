package com.juno.util;


public interface Constants {

	public static final String REDIRECT = "redirect";
	public static final String SERVER = "serverHit";
	public static final String SUCCESS = "J201";
	public static final String OP_VALIDATION_FAILED = "J106";
	public static final String MISSING_PARAMETER = "J101";
	public static final String INVALID_CREDENTIALS = "J111";
	public static final String BAD_REQUEST = "J104";
	public static final String DUPLICATE_REQUEST = "J103";
	public static final String ACTIVATION_WITHIN_30DAYS = "J216";
	public static final String MSISDN_NOT_ACTIVE = "J217";
	public static final String PRICEPOINT_NOT_CONFIGURED = "J110";
	public static final String USER_CANCELLED_THE_REQUEST = "J209";
	public static final String USER_EXCEEDED_QUOTA = "J207";
	public static final String SESSION_TIMEOUT = "J205";
	public static final String MSISDN_BLOCKED = "J214";
	public static final String USER_ALREADY_SUBSCRIBED = "J206";
	public static final String SERVER_ERROR = "J105";
	public static final String LOW_BAL = "J202";
	public static final String NETWORK_ERROR = "J107";
	public static final String AUTHENTICATION_FAILED = "J108";
	public static final String NOACTION = "HE-NoAction";
	public static final String SUSPECT = "HE-Suspect";
	public static final String BLOCK = "HE-Block";
	public static final String FAILURE_RESPONSE = "FAILURE";
	public static final String SUCCESS_RESPONSE = "SUCCESS";

	//APP PACKAGE NAME validation responses
	public static final String PLAY_STORE_APP = "PA";
	public static final String NON_PLAY_STORE_APP = "NPA";
	public static final String INVALID_PLAY_STORE_APP = "IAPN";
	public static final String MALWARE_APP = "MAP";
}

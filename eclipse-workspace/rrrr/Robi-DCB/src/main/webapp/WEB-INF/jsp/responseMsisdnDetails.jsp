<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.juno.logs.ErrorLogger"%>
<%@ page import="com.juno.util.CommonHelper"%>
<%@ page import="com.juno.database.model.Subscription"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script src="js/jquery.js"></script>
<script src="https://code.jquery.com/jquery-1.12.0.min.js"></script>
<script src="js/jquery.validate.min.js"></script>
<!-- <script src="js/disableFunctionalKeys.js"></script> 
 --><style type="text/css">
#tablecontent {
	height: auto;
	overflow-y: auto;
	width: auto;
}
#searchtable {
	border-radius: 6px;
	color: black;
	text-align: center;
	margin-bottom: 11px;
	margin-left: 2px;
	margin-right: 2%;
}
#searchtable thead th {
	/* background-color: rgb(81, 130, 187); */
	background-color: rgba(31, 116, 162, 1);
	color: #fff;
	font-family: Arial;
	font-size: 11px;
	font-weight: 700;
	border-bottom-width: 0;
}
/* Heading and Column Style */
 #searchtable tr, #searchtable th {
	border-top-width: 1px;
	border-top-style: solid;
	border-width: 1px;
	border-style: solid;
	border-color: rgb(81, 130, 187);
	
} 

/* Padding and font style */
#searchtable td {
font-family: sans-serif;
font-size: 12px;
color: #000;
font-weight:400;
text-align: center;
height:25px;
}
#searchtable th{
	padding: 5px 10px;
	font-size: 11px;
	font-weight: 700;
	font-family: Arial;
	color: rgb(177, 106, 104); 
	
	
}
#searchtable tr:nth-child(even) {
	background: rgb(231, 242, 250)
}
#searchtable tr:nth-child(odd) {
	background: #FFF
}
b{
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  font-weight:bold;
}
</style>
</head>
<body>
<%!
List<Subscription> data;
Subscription subPurReq = null;
String Status=null;
String purchaseId=null;
String ProductId=null;
String planType=null;
Date startDate=null;
Date endDate=null;
String MSISDN=null;
%>
	<%
	data=(List<Subscription>)request.getAttribute("tableData"); 
	String userid = (String) session.getAttribute("uname"); 
	String reqtime = (String) request.getAttribute("reqTime");
	String net = (String) request.getAttribute("network");
	String cgcon = (String) request.getAttribute("consent");
	String addSrc = (String) request.getAttribute("adsrc");
	String bua = (String) request.getAttribute("bua");
	String srcAp = (String) request.getAttribute("srcapp");
	String devOs = (String) request.getAttribute("devos")!=null?(String) request.getAttribute("devos"):"null";
	String devModel = (String) request.getAttribute("devmodel")!=null?(String) request.getAttribute("devmodel"):"null";
	%>

	<%if(!reqtime.equalsIgnoreCase("null")){%>
		   <p style="margin-left:10em">Request Datetime : <%=reqtime%></p>
	<%}%>
	<%if(!net.equalsIgnoreCase("null")){%>
		    <p style="margin-left:10em">Network : <%=net%></p>
	<%}%>
	<%if(!cgcon.equalsIgnoreCase("null")){%>
		    <p style="margin-left:10em">Consent : <%=cgcon%></p>
	<%}%>
	<%if(!addSrc.equalsIgnoreCase("null")){%>
		    <p style="margin-left:10em">Ad Source : <%=addSrc%></p>
	<%}%>
	<%if(!bua.equalsIgnoreCase("null")){%>
		    <p style="margin-left:10em">Browser : <%=bua%></p>
	<%}%>
	<%if(!srcAp.equalsIgnoreCase("null")){%>
		    <p style="margin-left:10em">Source APP : <%=srcAp%></p>
	<%}%>
	<%if(!devOs.equalsIgnoreCase("null")){%>
		    <p style="margin-left:10em">Device OS : <%=devOs%></p>
	<%}%>
	<%if(!devModel.equalsIgnoreCase("null")){%>
		    <p style="margin-left:10em">Device Model : <%=devModel%></p>
	<%}%>
	
	<%--   <div><h5 style="margin-left:10%;">Request DateTime : <%=reqtime%></h5></div>
	<div><h5 style="margin-left:10%;">Network : <%=net%></h5></div>
	<div><h5 style="margin-left:10%;">Consent : <%=cgcon%></h5></div> --%>
	
	<!-- <span id="process"  style="height: 50px;display: inherit;margin-left: 107px;margin-top: 10px;"></span> -->
	<div id="finalResp" style="margin-left:40%;font-family:'Open Sans', sans-serif"></div>
	<div><h5 style="margin-left:33%;color: rgba(20, 131, 192, 1);font-size:13px; font-family:'Open Sans', sans-serif;font-variant: small-caps; ">Click on 'unsubscribe' button of service you would like to unsubscribe</h5></div>
	<div id="tablecontent" style="margin-bottom:25px;">
	<fieldset style="border-radius: 10px;border-color: rgb(81, 130, 187);">
			<legend><label style="color: rgba(20, 131, 192, 1);border: 3px solid #5182BB;border-radius: 15px;  padding: 0 25px;font-variant: small-caps;font-weight: 600">MSISDN Details</label></legend>
			<table id="searchtable"  rules="all" style="color:#333333;width:100%;margin-left:1px; margin-bottom:1%;" border="1" cellpadding="1" cellspacing="1" style="">
			<thead>
			<th><b>Purchase ID</b></th> 
			<th><b>Product Name</b></th>
			<th><b>Plan Type</b></th>
			<th><b>Start Date</b></th>
			<th><b>End Date</b></th>
			<th id="stat"><b>Status</b></th>
			<th><b>Action</b></th>
			</thead>
			<%
			try {
			 if(data!=null && data.size()!=0)
	           {
	         Iterator<Subscription> it = data.iterator();
				while(it.hasNext()){
				subPurReq = it.next();
				purchaseId=subPurReq.getPurchaseId();
				ProductId=subPurReq.getServiceName();
				planType=subPurReq.getValidity();
				planType=CommonHelper.getDaysCount(planType);
				startDate=subPurReq.getStartTime();
				endDate=subPurReq.getEndTime();
				MSISDN=subPurReq.getMsisdn();
				Status=subPurReq.getAction();
				if(Status.equalsIgnoreCase("dct"))
					 Status="Unsubscribed";
				else
					 Status="Active"; 
			 %> 
		 	 <tr>
		      <td style="width:13%;"><%=purchaseId%></td> 
		     <td style="width:13%;"><%=ProductId%></td>
		     <td style="width:13%;"><%=planType%></td>
		     <td style="width:13%;"><%=startDate%></td>
		     <td style="width:13%;"><%=endDate%></td>
		     <td style="width:13%;" id="status"><%=Status%></td>
		     <%
		     if(Status.equalsIgnoreCase("Unsubscribed")){
		     %>
		     <td style="width:5%;"><%-- <%="Unsubscribe"%> --%>
			<%}
		     else{%>
		    	   <td style="width:5%;"><input type=button class="clickonce" name="deact" id='<%=purchaseId%>'
							value="Unsubscribe" onclick="return init(this.id,'<%=ProductId%>','<%=startDate%>','<%=userid %>')" /></td>
		   <%}
			%>
		 </tr> 
		<% } %>
	<%}
		else{
	%>
			<tr>
				<td colspan='6' align="center" style="background-color: #fff"><p style="margin-top: 5px;font-family: initial;">No
						Records Found..</p></td>
			</tr>
			
			<%}

} catch (Exception e)
{
	ErrorLogger.getLogger().error("Exception in MsisdnDetails : ",e);
}
%>
</table>
</fieldset>
</div>
<script>
  function init(id,id1,id2,id3){
	var r = confirm("You have Chosen to unsubscribe "+"''"+id1+"''"+"\nService activated on  "+"''"+id2+"''"+"\nPlease press OK to unsubscribe the service");
	if (r == true) {
	   call(id,id3);
	} else {
	   x = "You pressed Cancel!";
	}
	}  
	function call(id,id3){
	var id=id;
	var proccessObj=$("#process");
	proccessObj.addClass("process");
	$.ajax({
	type : "post",
	url :"RobiDeactivateRequest",
	async : true,
	data : {
	deactivateId:id,
	userid:id3
	},
	success : function(response) {
	var deact=$(".clickonce").attr('id');
	if(response=="J201"){
	$('form').find('input[type="submit"]').trigger('click');
    $("#"+id).css("display", "none");
	$('#finalResp').css({'display': 'block','color':'green',}).html("Success. Deactivation is Successful.").fadeOut(8000);
	}else if(response=="J211"){
		$('form').find('input[type="submit"]').trigger('click');
		$("#"+id).css("display", "none");
		$('#finalResp').css({'display': 'block','color':'green',}).html("User Already deactivate on this service.").fadeOut(8000);
	}else if(response=="J107"){
		$('#finalResp').css({'display': 'block','color':'red',}).html("Failure. Unexpected Error, Please retry later.").fadeOut(8000);
	}
	},complete:function(){
		proccessObj.removeClass("process");
	}
	});
	return false;
	} 
</script>
</body>
</html>
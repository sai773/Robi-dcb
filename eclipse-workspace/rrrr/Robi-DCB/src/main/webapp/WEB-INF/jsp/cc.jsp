<!DOCTYPE html>
<html lang="en"> 
<head>
<meta charset="utf-8">
<title>Robi</title>
<!-- <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon"> -->
 <link rel="shortcut icon" href="images/favicon.ico">
<link rel="stylesheet" type="text/css" href="css/style.css" />
<script src="js/jquery.js"></script>
<script src="https://code.jquery.com/jquery-1.12.0.min.js"></script>
<script src="js/jquery.validate.min.js"></script>
<script src="js/msisdnScript.js"></script>
<link rel="stylesheet" type="text/css" href="css/msdropdown/dd.css" />
<script src="js/msdropdown/jquery.dd.min.js"></script>
<!-- <script src="js/disableFunctionalKeys.js"></script> 
 --><link rel="stylesheet" type="text/css" href="css/msdropdown/flags.css" />
 <script type="text/javascript">
$(document).ready(function(){
	 searchFromSubmit();
	 $("#countryname").msDropdown(); 
 });
 </script>
 <style>
.process{
	background-image: url('css/ajax-loader.gif');
	background-repeat: no-repeat;
	padding-left: 20px;
}
 p{
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  font-weight:bold;
 }

</style>
<script>
function fromReset(){
	$('#searchResult').css("display", "none");
}
 function OnCountryChange(x) {
	var ddl = document.getElementById('countryname');
	var txt = document.getElementById('msisdn');
	var opr=document.getElementById('operatorType');
	if (ddl.value == "Select Country") {
	txt.value = '';
	txt.readOnly = true;
	}
	else {
	txt.value = x;
	txt.readOnly = false; 
	}
	if(x==""){
		$('#test').remove();
		$('#operatorType').append("<option value='Robi'>Robi-Telecom</option>");
		}
	}
function doCheck(e) {
	var keycode;
	var len;
	var msdlen;
	len=$('#countryname').val().length;
	msdlen=$('#msisdn').val().length;
	if(len==msdlen){
		if (e.which === 8) {
		e.preventDefault();
		}
	}
	}  
</script>
</head>
<%!
String cid=null;
%>
<body>
<div class="logged" style="margin-top:20px;">
<p style="margin-top:2px;">
<label style="float:left; margin-left:35%;color: black" class="log">Welcome  : &nbsp&nbsp <%=session.getAttribute("uname")%>
		<%! String user=null; %>
		 <%
				String userid = (String) session.getAttribute("uname");
			 	if (userid == null) {
					response.sendRedirect("login.jsp");
					return;
				}
			%> 
</label>

<label style="float:center; margin-left: 13%;" class="log">
<a href="login" style="text-decoration: none; color: #060613;"><button class="exit-btn" style="background-color:#60AAD2;color: white;border: 1px solid #60AAD2;" >Logout</button></a>
</label>
</p>
</div> 
<%-- 
<%
String uname=(String)session.getAttribute("uname");
if (uname == null) {
	response.sendRedirect("login.jsp");
	return;
}
%> --%>

<%
cid=(String)session.getAttribute("cid");
%>

<div class="serachForm"  style="margin-top:3%;margin-left:35%;border: 1px;">
	<h3 style="margin-left: 1%;color:rgba(20, 131, 192, 1);font-size:19px;font-family: Ahem!, sans-serif;font-variant: small-caps;">AShield - Customer Care - Un-Subscription</h3>
	<div style="margin-left:14%"><img src="Robi_Axiata_logo.png" id="square-image" alt=" " height="100px" width="18%"></img></div>
	<form id="dataSerachForm" name="dataSerachForm"  style="width: 40%" method="post">
	<fieldset>
			<legend><label style="color:rgba(20, 131, 192, 1);font-size: 16px;font-variant: small-caps;font-weight:800; ">Search Details</label></legend>
			<div id="errodiv" style="margin-left:20%;color:red"><label id="errorMsg"></label><br/></div>
			<p>Please Enter the mobile number that you would like to unsubscribe the service for</p>
	<table>
	<tr><td></td>&nbsp&nbsp&nbsp<td  style="color: #3D90F8;"></td></tr>
	<tr>
					
	<td>
		<input type="text" placeholder="Enter your number" style="width:140px;height:26px;" class="form-control" id="msisdn" name="msisdn" maxlength="29" onKeyDown="doCheck(event)"/>
	</td>
	
	<td><input type="reset" class="form-control" name="reset" value="Clear"
							style="width: 100px;" onclick="return fromReset()" /></td>
	<td><input type="submit" class="form-control" value="Submit"
							style="width: 100px;" onsubmit="return searchFromSubmit()" /></td>
					
	<tr hidden="true"><td><input type="text" name="textid" id="textid" value="<%=cid%>"/></td></tr>
	</table>
	</fieldset>
 	<span id="process"  style="height: 50px;display: inherit;margin-left: 107px;margin-top: 10px; "></span>
 </form>
</div>
<!-- <img id = "loading" src = "css/ajax-loader.gif" alt = "Loading i
 -->
<div id="searchResult" style="margin-left: 1%;margin-right: 1%;margin-top:1%;"></div>
 <!-- <div style="background-color: #AFDAC3"><h5 style="margin-left:43%;">Powered by JunoTele</h5></div> --> 
</body>
</html>
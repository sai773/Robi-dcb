<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/gv-2.0.min.js"></script>
<script src="js/disableFunctionalKeys.js"></script>
<script src="js/aes.js"></script>
<script src="js/pbkdf2.js"></script>
<script>
		
$(document).ready(function(){
	sessiontout();
	var cnt = 0;
	var param5="";  
	var c = document.getElementById("usertapp");
	$("#jSecureImgId").bind('click',function(ev){
		cnt = cnt+1;
		var otpFlowCheck= $("#checkForOtpFlow").val();
		if(otpFlowCheck === "false"){
			cnt=4;
		}
		var ctx = c.getContext("2d");
        ctx.fillStyle="red";
        ctx.font = "30px Verdana";
		var $div = $(ev.target); 
		var optxn=$("#optxnid").val();
		var captchaFlag=$("#captchaFlag").val();
		var jSecureDiv = $('#jSecureImgId'); 
		var offset = $div.offset(); 
		var bottom = jSecureDiv.offset().top + jSecureDiv.height(); 
		var x=Math.round(ev.pageX - offset.left); 
		var y=Math.round(ev.pageY - offset.top); 
		ctx.fillText("*",x-9,y+18);
		
		if (cnt <= 4){
        	if (cnt == 4){
                $('#jSecureImgId').off('click');
                $("#jsec").css("background-color","white");
                $("#share1").attr("src", "");
                $("#share2").attr("src", "");
                $("div.toshow").css("color", "red");
                $("div.toshow").css('font-weight', 'bold');
                $("div.toshow").show();
                param5=param5+gv(optxn,x,y);
                $("#param5").val(param5);
                var iv = CryptoJS.lib.WordArray.random(128/8).toString(CryptoJS.enc.Hex);
                var salt = CryptoJS.lib.WordArray.random(128/8).toString(CryptoJS.enc.Hex);
                var key = CryptoJS.PBKDF2(optxn,CryptoJS.enc.Hex.parse(salt),{keySize: 128/32, iterations: 100});
                var str= encodeURIComponent(navigator.platform+"*"+screen.width+"-"+screen.height+"*"+navigator.userAgent+"*"+navigator.oscpu);
                var encrypted = CryptoJS.AES.encrypt(str,key,{ iv: CryptoJS.enc.Hex.parse(iv) });
                ciphertext = encrypted.ciphertext.toString(CryptoJS.enc.Base64);       
                var en = (iv+"::"+salt+"::"+encodeURIComponent(ciphertext));
                $("#en").val(en);
                document.getElementById("junoform").submit();
                ctx.clearRect(0,0,c.width,c.height);		
        	}
       		else{
                param5=param5+gv(optxn,x,y)+"$";
            }
        }
		});
	function disableBack() { window.history.forward() }
    window.onload = disableBack();
    window.onpageshow = function(evt) { if (evt.persisted) disableBack() }
});
</script>

<script>
function sessiontout()
{
var count = '<%=request.getAttribute("t")%>';
var counter = count*60;
 myVar= setInterval(function(){
  if(counter==0){
        window.location="http://jewelfive.junohub.com/Robi/sessTOut?txn="+optxnid.value;
	 }
   counter--;
 }, 1000)
}

window.history.forward();
function noBack() {
	window.history.forward();
}
</script>
 
<%
String optxnid = (String)request.getAttribute("optxn");
String img1 = (String)request.getAttribute("img1");
String img2 = (String)request.getAttribute("img2");
String pshare = (String)request.getAttribute("pshare");
String mimgURL = (String)request.getAttribute("mimgURL");
String price = (String)request.getAttribute("pr");
String validity = (String)request.getAttribute("val");
String merchant = (String)request.getAttribute("merchant");
String srvName = (String)request.getAttribute("srvName");
String pimage = (String) request.getAttribute("pimg");
String checkForOtpFlow= (String) request.getAttribute("checkforOtpFlow");
String Days = "";
if(validity.equalsIgnoreCase("M1")){
	Days = "30";
}else if(validity.equalsIgnoreCase("W1")){
	Days = "7";
}else if(validity.equalsIgnoreCase("D1")){
	Days = "1";
}else{
	Days = "0";
}
%>
<style type="text/css">
.jSecureOverlay {
	position: absolute;
}

#jSecureImgContainer {
	position: relative;
	text-align: center;
}

#jSecureImgContainer img {
	position: absolute;
	margin: auto;
	top: 0;
	left: 0;
	right: 0;
}
p{
font-size:12px;
font-align:center;
font-family:arial;

}
header{
font-size:13px;
font-align:center;
}
#price{
	margin-left:0px;
	font-style:bold;
	font-align:center;
}
.footerb{
border-top:1px solid #565656;
background:#dddddd;
font-size:12px;
margin:1px 0 0 0;
}
</style>

<meta content="application/xhtml+xml; charset=ISO-8859-1"
	http-equiv="Content-Type" />
<meta name="viewport" content="width=device-width,minimum-scale=1.0">

<title>WAP Consent Gateway</title>
</head>
<body class="body">
	<div class="w3-container" align="center">
  	<div id="header" align="center"><img src= "Robi_Axiata_logo.png" height="60" width="60"><span style="color:#cc0000;font-size:27px;font-family:serif;margin-left:0px">Robi Billing</span></div>
  	<div class="w3-card-4" style="width:100%;margin-top:7px;">
    <header class="w3-container">
      <div align="center"><%= srvName %></div>
    </header>
	<div id="price" align="center"><span style="font-weight:bold;font-size:17px;font-family:arial;"><%=price%> (+VAT+SC+SD)</span></div>
    <header class="w3-container">
    <div align="center"><%= merchant %></div>
    </header>
    <div class="w3-container" style="margin-bottom:-5px;font-family:arial;" align="center">
      <% if((Days.equalsIgnoreCase("0"))) {
    	  %>
    	  <p>This is a on-demand based content and one time will be charge. Upon confirmation, your
     purchase will be charged to your Robi Account. Would you like to pay now?</p>
     <% 
      }else {
     %>
    	  <p>This is a subscription based content and recurring charge will be incurred every <%= Days%> days. Upon confirmation, your
    			     purchase will be charged to your Robi Account. Would you like to pay now?</p> 
   <% 
      }     
      %>   
    </div>
    <div>
    	<%-- <p>Click <img src="data:image/png;base64,<%=pimage%>" id="pshare" > to confirm</p> --%> 
    	<div align="center"><span style="font-weight:bold;font-size:17px;font-family:arial;">Click <img src="data:image/png;base64,<%=pimage%>" id="pshare" > to confirm</span></div>
    </div>
	<table id="jsec" style="width: 240px; height: 100px;" align="center" cellspacing="0" cellpadding="0">
		<tr align="left">
 		<td align="center" valign="top" width="100%" style="text-align: center;">
 		<input type="hidden" name="checkForOtpFlow"	id="checkForOtpFlow" value="<%=checkForOtpFlow%>" />
 		<input type="hidden" name="cgtrxId" id="optxnid" value="<%=optxnid%>" />
		<div id="jSecureImgId" align="center">
		<div id="jSecureImgContainer" align="center">
		<div class="toshow" style="display: none; font-family=arial; font-align=center;margin-top:30px">Please Wait...</div>
		<canvas id = "usertapp" height="100px" width="240px" ></canvas>
		<img src="data:image/png;base64,<%=img1%>" id="share1" class="jSecureOverlay"> 
		<img src="data:image/png;base64,<%=img2%>" id="share2" class="jSecureOverlay">
		</div>
		</div><br>
		</table>
		<form name="junoform" id="junoform" action="/Robi/chkImg" method="post">
			<input type="hidden" name="cgtrxId" value="<%=optxnid%>" />
			<input type="hidden" name="param5" id="param5" />
			<input type="hidden" name="en" id="en" />
		</form>
	 <footer class="w3-container">
 	
 	<p>Please turn OFF data saver on your internet browser settings for better experience.</p>
 	  <div id="i" align=center style="margin-bottom:4px;"><img src= "<%=mimgURL%>" alt=" " class="square-image" width="200" height="200"></div>
      <div class="footerb" align="center">Powered by AShield</div>
    </footer>
  </div>
</div>
</body>
</html>
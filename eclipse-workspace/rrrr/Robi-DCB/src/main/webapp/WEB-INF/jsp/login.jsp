<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"> 
<head>
<meta charset="utf-8">
<title>Robi</title>
<!-- <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">  -->
<link rel="shortcut icon" href="images/favicon.ico">
<link rel="stylesheet" type="text/css" href="css/login.css" />
<script src="js/jquery.js"></script>
<script src="https://code.jquery.com/jquery-1.12.0.min.js"></script>
<script src="js/jquery.validate.min.js"></script>
<script src="js/disableFunctionalKeys.js"></script> 
</head>
<body>
<div class="container">
	<section id="content">
		<form name="loginForm" method="post" action="adminLoginForm">
			<h1>Login Form</h1>
	    	<div id="loginMsg" style="color: #FF0000;">${errorMessage}</div>
			<div>
				<input type="text" placeholder="Username" required="" name="username" id="username" />
			</div>
			<div>
				<input type="password" placeholder="Password" required="" name="password" id="password" />
			</div>
			<div>
				<input type="submit" value="Log in" onclick="return loginForm()" />
				<a href="#">Lost your password?</a>
			</div>
		</form>
	</section>
</div>
</body>
</html>
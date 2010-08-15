<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Message</title>

<script type="text/javascript" src="js/ajax.js"></script>

<style>
html, body{
margin:0;
padding:0;
min-width:100%;
height:100%;
font-family:Arial, Helvetica, sans-serif;
}

</style>
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>

<script type="text/javascript">
function show(msg){
	$('#msg').html(msg);
	
}

</script>

</head>

<body>
<%=request.getAttribute("t") %>
<form name="form1" action="msg.jsp" method="get">
	<input type="text" id="t" name="t"/>
	<textarea id="msg" name="msg" rows="10" cols="100">
		<%=request.getAttribute("msg") %>
	</textarea>
	<input type="submit"></input>
	
</form>
					
</body>

</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Delete File</title>


<style>
html, body{
margin:0;
padding:0;
min-width:100%;
height:100%;
font-family:Arial, Helvetica, sans-serif;
}

</style>

<script type="text/javascript">

function del_multiple_file(){
	$("waitDiv").hide();
	$("delForm").show();
	window.parent.del_multiple_file();	
}





</script>

</head>

<body>
<div id="delForm">
	<p>Are you sure to delete the selected files or folders!(the operation can not be recovered!)</p>
	<input type="button" value="OK" onclick="del_multiple_file()"/>
	<input type="button" value="Cancel" onclick="window.parent.close_dialog()"/>
</div>

<div id="waitDiv" style="display:none">
	<p>Please Wait...</p>
</div>
					
</body>

</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Delete File</title>

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

<script type="text/javascript">

function delFile(){
	var uri="delFile.action";

	xmlrequest.open("POST",uri,true);

	xmlrequest.setRequestHeader("Content-Type","application/x-www-form-urlencoded");

	xmlrequest.onreadystatechange=processResponse;

	var id = document.getElementById("folder.id");

	var str="folder.id="+id.value;

	xmlrequest.send(str);

	document.getElementById("delForm").style.display="none";
	document.getElementById("waitDiv").style.display="";
	
}

function processResponse()
{
	if(xmlrequest.readyState==4){
		if(xmlrequest.status==200){
			if(xmlrequest.responseText.indexOf("Successed")!=-1){
				window.parent.delFile(document.getElementById("folder.id").value);
			}
			else{
				var waitDiv = document.getElementById("waitDiv");
				while(waitDiv.hasChildNodes()){
					waitDiv.removeChild(waitDiv.firstChild);
				}
								
				var p = document.createElement("p");
				p.innerHTML = xmlrequest.responseText;
				waitDiv.appendChild(p);
			}
				
		}
	}
}



</script>

</head>

<body>
<div id="delForm">
	<p>Are you sure to delete the file or folder:${path} (the operation can not be recovered!)</p>
	<input type="hidden" id="folder.id" name="folder.id" value="${folder.id }"/>
	<input type="button" value="OK" onclick="delFile()"/>
	<input type="button" value="Cancel" onclick="window.parent.close_dialog()"/>
</div>

<div id="waitDiv" style="display:none">
	<p>Please Wait...</p>
</div>
					
</body>

</html>
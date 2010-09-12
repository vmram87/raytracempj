<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>New Folder</title>

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

function renameFile(){
	var uri="renameFile.action";

	xmlrequest.open("POST",uri,true);

	xmlrequest.setRequestHeader("Content-Type","application/x-www-form-urlencoded");

	xmlrequest.onreadystatechange=processResponse;

	var folderName=document.getElementById("folderName");
	var id = document.getElementById("folder.id");

	var str="folderName="+folderName.value+"&folder.id="+id.value;

	xmlrequest.send(str);

	document.getElementById("addForm").style.display="none";
	document.getElementById("waitDiv").style.display="";
	
}

function processResponse()
{
	if(xmlrequest.readyState==4){
		if(xmlrequest.status==200){
			if(xmlrequest.responseText.indexOf("Successed")!=-1){
				var folderName=document.getElementById("folderName");
				var id = document.getElementById("folder.id");
				window.parent.rename_file_for_tree(id.value,folderName.value);
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
<div id="addForm">
	<p>Please enter a new name for the folder or file:</p>
	<input id="folderName" name="folderName" type="text" value="<s:if test='%{folderName!=null}'>${floderName }</s:if><s:else>New Folder</s:else>"/>
	<p>Destination Folder:${path}</p>
	<input type="hidden" id="folder.id" name="folder.id" value="${folder.id }"/>
	<input type="button" value="OK" onclick="renameFile()"/>
	<input type="button" value="Cancel" onclick="window.parent.close_dialog()"/>
</div>

<div id="waitDiv" style="display:none">
	<p>Please Wait...</p>
</div>
					
</body>

</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>New Folder</title>

<script type="text/javascript" src="js/ajax.js"></script>
<link href="css/upload_page.css" rel="stylesheet" type="text/css" />

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
var select_folder_id = null;

function click_node(node){
	select_folder_id = node.attr("id");
}

function moveFile(){
	var uri="moveFile.action";

	xmlrequest.open("POST",uri,true);

	xmlrequest.setRequestHeader("Content-Type","application/x-www-form-urlencoded");

	xmlrequest.onreadystatechange=processResponse;

	var folderName=document.getElementById("folderName");
	var id = document.getElementById("folder.id");

	var str="directoryId="+select_folder_id+"&folder.id="+id.value;

	xmlrequest.send(str);

	document.getElementById("addForm").style.display="none";
	document.getElementById("waitDiv").style.display="";
	
}

function processResponse()
{
	if(xmlrequest.readyState==4){
		if(xmlrequest.status==200){
			if(xmlrequest.responseText.indexOf("Successed")!=-1){
				window.parent.move_file(select_folder_id,document.getElementById("folder.id").value);
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

<style type="text/css">
#choose_folder_area{
	width:265px;
	height:400px;
	z-index:100;
	background-color:#FFF;
	
}
#choose_folder_area iframe{
margin-bottom: 10px;
	border:1px solid #DDD;
}

</style>

</head>

<body>
<div id="addForm">
	<div id="choose_folder_area">
		<iframe id="fileFrame" name="fileFrame" border="0" frameBorder="0"  scrolling="auto"  width="100%" height="280px" src="fileTree.action"></iframe>
		<input type="hidden" id="folder.id" name="folder.id" value="${folder.id }"/>
		<input type="button" value="Move To This Folder" onclick="moveFile()"/>
		<input type="button" value="Cancel" onclick="window.parent.close_dialog()"/>
	</div>
</div>

<div id="waitDiv" style="display:none">
	<p>Please Wait...</p>
</div>
					
</body>

</html>
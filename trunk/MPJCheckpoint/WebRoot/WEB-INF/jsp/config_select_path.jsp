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
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>

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

String.prototype.Trim = function(){
	return this.replace(/(^\s*)|(\s*$)/g,"");
};

function selectPath(){	

	document.getElementById("addForm").style.display="none";
	document.getElementById("waitDiv").style.display="";

	$.get("getDirectory.action?folder.id="+select_folder_id, {},
		function(responseText){
			window.parent.setDirectory(responseText.Trim());
		}
	);
	
}

function click_node(node){
	select_folder_id = node.attr("id");
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
		<input type="button" value="Select Directory" onclick="selectPath()"/>
		<input type="button" value="Cancel" onclick="window.parent.close_dialog()"/>
	</div>
</div>

<div id="waitDiv" style="display:none">
	<p>Please Wait...</p>
</div>
					
</body>

</html>
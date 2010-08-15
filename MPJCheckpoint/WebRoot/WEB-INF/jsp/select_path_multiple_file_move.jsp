<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Select folder</title>

<script type="text/javascript" src="js/ajax.js"></script>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/map.js"></script>
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

function move_file(){
	if(select_folder_id == null){
		alert("Please first select the destination folder first");
		return;
	}
	$("#addForm").hide();
	$("#waitDiv").show();
	var ids= new Array();
	for(var i=0;i<window.parent.select_file_map.size();i++){
		ids.push(window.parent.select_file_map.item(i).value);
	}
	$.post("moveMultipleFile.action",{"directoryId":select_folder_id,"selectFileIds":ids},
			function(responseText){		
				if(responseText.indexOf("Successed")!=-1){
					window.parent.select_file_map.clear();
				}
				else{
					$("#waitDiv").html(responseText);
				}

				window.parent.close_dialog();
				window.parent.open_view("fileList");
			}
	);
	
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
		<input type="button" value="Move To This Folder" onclick="move_file()"/>
		<input type="button" value="Cancel" onclick="window.parent.close_dialog()"/>
	</div>
</div>

<div id="waitDiv" style="display:none">
	<p>Please Wait...</p>
</div>
					
</body>

</html>
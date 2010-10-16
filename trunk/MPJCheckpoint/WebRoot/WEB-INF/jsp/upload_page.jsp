<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
    double perMaxSize = 3.0;//max file size 
    String sizeUnit = "MB";//perMaxSize
    String ext = "*.*";//allow type
    //sumbit location
	StringBuffer uploadUrl = new StringBuffer("http://");
	uploadUrl.append(request.getHeader("Host"));
	uploadUrl.append(request.getContextPath());
	uploadUrl.append("/swfUpload.action");
	Object  id =  request.getAttribute("folder.id");
	if(id != null){
		uploadUrl.append("?folder.id=" + id);
	}	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>File List</title>

<style>
html, body{
margin:0;
padding:0;
width:100%;
height:100%;
font-family:Arial, Helvetica, sans-serif;
}
</style>


<script language="javascript" type="text/javascript" src="js/jquery.js"></script>

<script language="javascript" type="text/javascript">
$(function() {


	$().mouseup(function(){
		if(window.parent.isMouseDown){
			window.parent.doHandBarMouseUp();
		}
		if(window.parent.isListMouseDown){
			window.parent.doListHandleMouseUp();
		}
	});

	$().mousemove(function(e){
		if(window.parent.isMouseDown){
			window.parent.doHandBarMouseMoveInListFrame(e);
		}
		if(window.parent.isListMouseDown){
			if(e.pageX >  26 ){
				$(".name_col").width(e.pageX  - 26);
				$(".date_col").css('left',e.pageX + 10);
			}
			window.parent.doListHandleMouseMoveInListFrame(e);
		}
	});


});
</script>

<link href="css/layout.css" rel="stylesheet" type="text/css" />
<link href="css/upload_page.css" rel="stylesheet" type="text/css" />

<script type="text/javascript" src="js/swfupload.js"></script>
<script type="text/javascript" src="js/swfupload.swfobject.js"></script>
<script type="text/javascript" src="js/swfupload.queue.js"></script>
<script type="text/javascript" src="js/fileprogress.js"></script>
<script type="text/javascript" src="js/handlers.js"></script>

<script type="text/javascript">
function beforeUploadStart(){
	//after click start upload button
	swfu.addPostParam("folder.id",select_folder_id);
	
	return true;
}

var swfu;
SWFUpload.onload = function () {
	var settings = {
		flash_url : "js/swfupload.swf",
		upload_url: "<%=uploadUrl.toString()%>",
		post_params: {
			"user_id" : "stephen830",
			"pass_id" : "123456"
		},
		file_size_limit : "<%=perMaxSize%> <%=sizeUnit%>",
		file_types : "<%=ext%>",
		file_types_description : "<%=ext%>",
		file_upload_limit : 0,
		file_queue_limit : 0,
		custom_settings : {
			progressTarget : "fsUploadProgress",
			cancelButtonId : "btnCancel",
			uploadButtonId : "btnUpload",
			myFileListTarget : "idFileList"
		},
		debug: false,
		auto_upload:false,

		// Button Settings
		button_image_url : "images/layout/XPButtonUploadText_61x22.png",	// Relative to the SWF file
		button_placeholder_id : "spanButtonPlaceholder",
		button_width: 61,
		button_height: 22,

		// The event handler functions are defined in handlers.js
		swfupload_loaded_handler : swfUploadLoaded,
		file_queued_handler : fileQueued,
		file_queue_error_handler : fileQueueError,
		file_dialog_complete_handler : fileDialogComplete,
		before_upload_start_handler : beforeUploadStart,
		upload_start_handler : uploadStart,
		upload_progress_handler : uploadProgress,
		upload_error_handler : uploadError,
		upload_success_handler : uploadSuccess,
		upload_complete_handler : uploadComplete,
		queue_complete_handler : queueComplete,	// Queue plugin event
		
		// SWFObject settings
		minimum_flash_version : "9.0.28",
		swfupload_pre_load_handler : swfUploadPreLoad,
		swfupload_load_failed_handler : swfUploadLoadFailed
	};

	swfu = new SWFUpload(settings);
}



</script>

<script type="text/javascript">
function overButton(b){
	if(b.hasChildNodes()){
		var x = b.childNodes;
		for(i=0;i<x.length;i++){
			if(x[i].nodeType == 1){
				x[i].style.borderColor="#555555";
			}
		}
	}
}

function mouseOutButton(b){
	if(b.hasChildNodes()){
		var x = b.childNodes;
		for(i=0;i<x.length;i++){
			if(x[i].nodeType == 1){
				x[i].style.borderColor="#bbbbbb";
			}
		}
	}
}

var isDstBtnDown = false;

function mouseDownButton(b){
	if(isDstBtnDown == false){
		isDstBtnDown = true;
		if(b.hasChildNodes()){
			var x = b.childNodes;
			for(i=0;i<x.length;i++){
				if(x[i].className == "button_inner_border"){
					x[i].style.backgroundPosition="left -19px";				
				}
				
			}
			
		}

		$("#choose_folder_area").show();
	}
	else{
		isDstBtnDown = false;
		if(b.hasChildNodes()){
			var x = b.childNodes;
			for(i=0;i<x.length;i++){
				if(x[i].className == "button_inner_border"){
					x[i].style.backgroundPosition="left top";
				}
			}
		}
		
		$("#choose_folder_area").hide();
	}
}


var select_folder_id = null;
var temp_select_id = null;
var temp_select_name = null;
function click_node(node){
	temp_select_id = select_folder_id;
	select_folder_id = node.attr("id");
	temp_select_name = $(">span",node).html();
}

function click_select_ok(){
	$("#dest_folder_name").html(temp_select_name);
	var p = $("#dest_folder_name").parent();
	p.css("background-position","left top");

	$("#choose_folder_area").hide();
}

function click_select_cancel(){
	select_folder_id = temp_select_id;
	var p = $("#dest_folder_name").parent();
	p.css("background-position","left top");
	$("#choose_folder_area").hide();
}

</script>

</head>

<body bgcolor="#FCFCFC" >
<table width="100%"  cellspacing="4" cellpadding="4" border="0" bgcolor="#FCFCFC">
	<tr> 
	<td class="DH1">
	<table width="100%" cellspacing="4" cellpadding="4" border="0" bgcolor="#FCFCFC">
	<tr>
	<td class="DH2">
	<STRONG>Multiple File Upload (Max Single File Size ：<%=perMaxSize%> <%=sizeUnit%>）</STRONG> 
	</td><td class="DH2" align="right"></td>
	</tr>
	</table>
<div id="content">
	<form id="form1" action="UploadFileExampleSubmit.jsp" method="post" enctype="multipart/form-data">
		<table width="100%" cellspacing="0" cellpadding="0" border="0"><tr><td>
		<span id="spanButtonPlaceholder"></span>
		<input id="btnUpload" type="button" value="Start Upload" class="btn" />
		<input id="btnCancel" type="button" value="Cancel All" disabled="disabled" class="btn" />
		
		<div id="button_destination_folder" class="button_frame" onclick="choose_folder()">
			<div class="button_item" onmouseover="overButton(this)" onmousedown="mouseDownButton(this)"  onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
				<div class="button_left_border"></div>	
				<div class="button_inner_border">
						<div class="folder_icon"></div>
						<span id="dest_folder_name">Destination Folder</span>
						<div class="down_triangle"></div>						
				</div>	
				<div class="button_right_border"></div>
				<div class="clear"></div>				
			</div>
				
			<div id="choose_folder_area" style="display:none">
				<iframe id="fileFrame" name="fileFrame" border="0" frameBorder="0"  scrolling="auto"  width="100%" height="210px" src="fileTree.action"></iframe>
				<input type="button" value="OK" onclick="click_select_ok()"/>
				<input type="button" value="Cancel" onclick="click_select_cancel()"/>
			</div>
		</div>
		
		</td>		
		</tr></table>
		
		<div id="listTable">
			<table id="idFileList" class="uploadFileList"><tr class="uploadTitle"><td><B>File Name</B></td><td><B>File Size</B></td><td width=100px><B>Status</B></td><td width=35px>&nbsp;</td></tr></table>
		</div>
		
		File Waiting:<span id="idFileListCount">0</span> , File Upload Successed: <span id="idFileListSuccessUploadCount">0</span>
		<div id="divSWFUploadUI" style="visibility: hidden;"></div>
		<noscript style="display: block; margin: 10px 25px; padding: 10px 15px;">
			Sorry, the upload page can not be loaded, please set the browser to support Javascript.
		</noscript>
		<div id="divLoadingContent" class="content" style="background-color: #FFFF66; border-top: solid 4px #FF9966; border-bottom: solid 4px #FF9966; margin: 10px 25px; padding: 10px 15px; display: none;">
			The upload page is loading, please wait...
		</div>
		<div id="divLongLoading" class="content" style="background-color: #FFFF66; border-top: solid 4px #FF9966; border-bottom: solid 4px #FF9966; margin: 10px 25px; padding: 10px 15px; display: none;">
			Sorry, the upload page can not be loaded, please set the browser to support Javascript and the Flash plugin has been installed.
		</div>
		<div id="divAlternateContent" class="content" style="background-color: #FFFF66; border-top: solid 4px #FF9966; border-bottom: solid 4px #FF9966; margin: 10px 25px; padding: 10px 15px; display: none;">
			Sorry, the upload page can not be loaded, please install or update the Flash plugin for the browser.
			Please refer to： <a href="http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash" target="_blank">Adobe Website</a> for the latest Flash plugin.
		</div>
	</form>
</div>
</td></tr></table>

</body>
</html>
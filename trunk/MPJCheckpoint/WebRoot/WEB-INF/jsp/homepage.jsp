<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Fault Tolerance Java Parrallel Computing System</title>

<link href="css/layout.css" type="text/css" rel="stylesheet"/>
<link href="css/tree.css" type="text/css" rel="stylesheet"/>
<link href="css/list.css" type="text/css" rel="stylesheet"/>
<link href="css/upload_page.css" rel="stylesheet" type="text/css" />
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.simple.tree.js"></script>
<script type="text/javascript" src="js/file_operation.js"></script>
<script type="text/javascript" src="js/map.js"></script>
<script type="text/javascript" src="js/run.js"></script>
<script type="text/javascript" src="js/update_file_list.js"></script>

<link rel="stylesheet" type="text/css" href="css/dialog.css"/>
<script language="javascript" type="text/javascript" src="js/dialog.js" ></script>

<script language="javascript" type="text/javascript">
$(function() {
	doResize();
	$(window).resize(function() {
	   doResize();
	});
	
	$("#handle_bar").mousedown=function(){return false;}; 
	$("#handle_bar").mousemove=function(){return false;}; 
	
	$("#handle_container").mousedown(function(e){
		doHandBarMouseDown(e);
	});

	$().mouseup(function(){
		if(isMouseDown){
			doHandBarMouseUp();
		}
		if(isListMouseDown){
			doListHandleMouseUp();
		}
	});

	$().mousemove(function(e){
		if(isMouseDown){
			doHandBarMouseMove(e);
		}
		if(isListMouseDown){
			doListHandleMouseMove(e)
		}
	});
	
	$(".movable_divider").mousedown(function(e){
		doListHandleMouseDown(e);
	});

	



});

var doResize = function() {
$("#right_bar").width($("body").width()-$("#left_bar").width()-$("#handle_container").width());
$("#fileFrame").height($("#container").height()-$("#top_line").height()-$("#top_title").height()-$("#left_button_area").height()-$("#left_option_area").height()-14);
$("#listFrame").height($("body").height()-$("#top_line").height()-$("#top_title").height()-$("#right_nav").height()-$("#right_option_area").height()-$("#list_head").height()-39);

}

var isMouseDown = false;
var isListMouseDown = false;

function doHandBarMouseDown(e){
	 if($("#handle_container").setCapture){  
		 $("#handle_container").setCapture();  
	 }else if(window.captureEvents){  
	  	window.captureEvents(Event.MOUSEMOVE|Event.MOUSEUP);  
	 }  
 
	 if(e.preventDefault)e.preventDefault();
 	 else e.returnvalue=false;
	isMouseDown = true;
}

function doHandBarMouseUp(){
	 if($("#handle_container").releaseCapture){  
		 $("#handle_container").releaseCapture();  
	 }else if(window.captureEvents){  
		window.captureEvents(Event.MOUSEMOVE|Event.MOUSEUP);  
	 } 
	isMouseDown = false;
}

function doHandBarMouseMove(e){
	$("#left_bar").width(e.pageX);
	doResize();
}


function doHandBarMouseMoveInListFrame(e){
	$("#left_bar").width($("#handle_container").position().left+$("#handle_container").width()+e.pageX);
	doResize();
}



function doListHandleMouseDown(e){
	if($("#movable_divider").setCapture){  
		 $("#movable_divider").setCapture();  
	 }else if(window.captureEvents){  
	  	window.captureEvents(Event.MOUSEMOVE|Event.MOUSEUP);  
	 }  
 
	 if(e.preventDefault)e.preventDefault();
 	 else e.returnvalue=false;
	isListMouseDown = true;
}

function doListHandleMouseUp(){
	 if($("#movable_divider").releaseCapture){  
		 $("#movable_divider").releaseCapture();  
	 }else if(window.captureEvents){  
		window.captureEvents(Event.MOUSEMOVE|Event.MOUSEUP);  
	 } 
	isListMouseDown = false;
}

function doListHandleMouseMove(e){
	if(e.pageX > $("#list_head").position().left + 26 ){
		$(".name_col").width(e.pageX - $("#list_head").position().left - 26);
		$(".date_col").css('left',e.pageX-$("#list_head").position().left+$(".movable_divider").width());
		window.frames["listFrame"].doListHandleMouseMoveInListFrame(e.pageX - $("#list_head").position().left);
	}
}

function doListHandleMouseMoveInListFrame(e){
	if(e.pageX >  26 ){
		$(".name_col").width(e.pageX  - 26);
		$(".date_col").css('left',e.pageX + $(".movable_divider").width());
	}
}



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
				if(x[i].className == "button_inner_border" && b.parentNode.id != "button_select_option"){
					x[i].style.backgroundPosition="left top";
				}
			}
		}
	}
}

function mouseDownButton(b){
	if(b.hasChildNodes()){
		var x = b.childNodes;
		for(i=0;i<x.length;i++){
			if(x[i].className == "button_inner_border"){
				x[i].style.backgroundPosition="left -19px";				
			}
			
		}
		
	}
}

function mouseUpButton(b){
	if(b.hasChildNodes()){
		var x = b.childNodes;
		for(i=0;i<x.length;i++){
			if(x[i].className == "button_inner_border"){
				x[i].style.backgroundPosition="left top";
			}
		}
	}
}

function selectOption(viewMode){
	var p = document.getElementById("left_option_area");
	var childs = p.childNodes;
	for(i=0;i<childs.length;i++){
		if(childs[i].nodeType == 1){
			childs[i].className="option_item";
		}
	}
	document.getElementById(viewMode+"_option").className="option_item_selected";
 
	open_view(viewMode);
	
}

var view_mode;
if(window.location.hash == "")
	view_mode = "fileList";
else{
	view_mode = window.location.hash.substring(1);
}

$(document).ready(init_document);

function init_document(){
	if(view_mode != "fileList")
		$("#file_list_option").hide();

	open_view(view_mode);
	$("#"+view_mode+"_option").attr("class","option_item_selected");
}

function open_view(viewMode){
	if(view_mode == "fileList"){
		$("#list_head").hide();
		$("#file_list_option").hide();
	}

	if(viewMode == "fileList"){
		$("#list_head").show();
		$("#file_list_option").show();
	}

	if(viewMode == "graphicView"){
		$("#run_option_area").show();
	}
	else{
		$("#run_option_area").hide();
	}

	var path = viewMode +".action";	
	if(viewMode == "fileList" && select_folder_id != null){
		path = path + "?folder.id=" + select_folder_id + "&includeFiles=true";
	}
	document.getElementById("listFrame").src = path;	
	view_mode = viewMode;
	window.location.hash = view_mode;
}

function open_upload_page(){
	if(view_mode != "uploadPage"){
		open_view('uploadPage');
	}	
}

function click_node(node){
	select_folder_id = node.attr("id");
	selectOption("fileList");
}

var select_file_map = new Map();
$(document).ready(init);

function init(){
	select_file_map.clear();
}
// update file list operation is in update_file_list.js
</script>


</head>

<body onload="doResize();">
<div id="container">
	<div id="top_line"></div>
	<div id="top_title"><p><b>Fault Tolerance Java Parrallel Computing System</b></p></div>
	
	<div id="content">
		<div id="left_bar">
			<div id="left_button_area">		
				<div id="button_new_folder" class="button_frame" onclick="create_new_folder()">
					<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
						<div class="button_left_border"></div>	
						<div class="button_inner_border">
								<span>New Folder</span>						
						</div>	
						<div class="button_right_border"></div>
						<div class="clear"></div>				
					</div>
				</div>
				
				<div id="button_upload" class="button_frame" onclick="open_upload_page()">
					<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
						<div class="button_left_border"></div>	
						<div class="button_inner_border">
								<span>Upload...</span>						
						</div>	
						<div class="button_right_border"></div>
						<div class="clear"></div>				
					</div>
				</div>
				
			</div><!-- end of left button_area-->
			
			<div id="left_option_area">
				<div id="graphicView_option" class="option_item" onclick="selectOption('graphicView')">Nodes Status View</div>
				<div id="fileList_option" class="option_item" onclick="selectOption('fileList')">Files View</div>
				<div id="sysConfig_option" class="option_item" onclick="selectOption('sysConfig')">System Configuration</div>	
				<div id="userGuide_option" class="option_item" onclick="selectOption('userGuide')">User Guide</div>				
			</div><!-- end of left_option_area-->
			
			<div id="file_tree">
				<iframe id="fileFrame" name="fileFrame" border="0" frameBorder="0"  scrolling="auto"  width="100%"  onload="doResize()"  src="fileTree.action"></iframe>
			


			</div><!-- end of file_tree-->
			
		</div><!-- end of left bar-->
		
		<div id="handle_container">
			<div id="handle_bar">
			
			</div><!-- end of handle bar-->
		</div>
	
		<div id="right_bar">
			<div id="right_nav"></div>
			<div id="right_option_area">
				<div id="file_list_option">
				
					<div id="button_select_option" class="button_frame">
						<div id="select_option_button_item" class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="selectOptionButtonDown(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Select</span><div class="down_triangle"></div>						
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>
						
						<div id="select_option_area" style="display:none">
							<div id="select_all" class="select_option_item" onclick="select_all()">Select All</div>
							<div id="select_none" class="select_option_item" onclick="select_none()">Select None</div>
						</div>
					</div>
				
					<div id="button_movto_folder" class="button_frame" onclick="move_multiple_file_page()">
						<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Move to</span>						
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>
						
					</div>
					
					<div id="button_del_files" class="button_frame" onclick="del_multiple_file_page()">
						<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Delete</span>			
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>

					</div>
					
					
					
					<div id="button_rename_file" class="button_frame" onclick="rename_file_for_list_page()">
						<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Rename</span>						
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>
					</div>
					
					<div class="clear"></div>
				</div><!-- file_list_option -->
				
				<div id="run_option_area" style="display:none">
					<div id="button_run_program" class="button_frame" onclick="run_program()">
						<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Run</span>						
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>
					</div>
					
					<!--  
					<div id="button_checkpoint_program" class="button_frame" onclick="checkpoint()">
						<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Checkpoint</span>						
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>
					</div>
					
					-->
					
					<div id="button_restart_program" class="button_frame" onclick="restart()">
						<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Restart</span>						
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>
					</div>
					
					<div id="button_stop_program" class="button_frame" onclick="stop_program()">
						<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
							<div class="button_left_border"></div>	
							<div class="button_inner_border">
									<span>Stop</span>						
							</div>	
							<div class="button_right_border"></div>
							<div class="clear"></div>				
						</div>
					</div>
					
					<div class="clear"></div>
				</div>
				
			</div><!-- end of right_option_area -->
			
			<div id="right_context">
				
				<script type="text/javascript">
					if(view_mode == "fileList"){
						document.write('<div id="list_head"><div id="select_col_head" class="select_col"></div><div id="name_col_head" class="name_col">Name</div><div class="movable_divider"></div><div id="date_col_head" class="date_col">Date</div><div class="clear"></div></div>')
					}
				</script>

				<script type="text/javascript">
					document.write('<iframe id="listFrame" name="listFrame" border="0" frameBorder="0"  scrolling="auto"  width="100%"    onload="doResize()"  src="' + view_mode + '.action"></iframe>')
				</script>

				
				 
				
			</div><!--end of right_context-->
		</div><!-- end of right bar -->
		
		<div class="clear"></div>
	</div>
	<!-- end of content-->
	
</div><!-- end of container-->

<iframe id="DivShim" src="javascript:false;" scrolling="no" frameborder="0" width="0" height="0" style="position:absolute; top:0px; left:0px;opacity:0">
</iframe>

</body>

</html>
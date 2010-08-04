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
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.simple.tree.js"></script>

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
$("#fileFrame").height($("#container").height()-$("#top_line").height()-$("#top_title").height()-$("#left_button_area").height()-$("#left_option_area").height()-37);
$("#listFrame").height($("body").height()-$("#top_line").height()-$("#top_title").height()-$("#right_nav").height()-$("#right_option_area").height()-$("#list_head").height()-63);

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
				if(x[i].className == "button_inner_border"){
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

function selectOption(opt){
	var p = opt.parentNode;
	var childs = p.childNodes;
	for(i=0;i<childs.length;i++){
		if(childs[i].nodeType == 1){
			childs[i].className="option_item"
		}
	}
	opt.className="option_item_selected";
}



</script>


</head>

<body onload="doResize();">
<div id="container">
	<div id="top_line"></div>
	<div id="top_title"><p><b>Fault Tolerance Java Parrallel Computing System</b></p></div>
	
	<div id="content">
		<div id="left_bar">
			<div id="left_button_area">		
				<div id="button_new_folder" class="button_frame">
					<div class="button_item" onselectstart="return false;"  onmouseover="overButton(this)" onmousedown="mouseDownButton(this)" onmouseup="mouseUpButton(this)" onmouseout="mouseOutButton(this)" onselectstart ="function(){return false;}">
						<div class="button_left_border"></div>	
						<div class="button_inner_border">
								<span>New Folder</span>						
						</div>	
						<div class="button_right_border"></div>
						<div class="clear"></div>				
					</div>
				</div>
				
				<div id="button_upload" class="button_frame">
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
				<div class="option_item" onclick="selectOption(this)">Nodes Status View</div>
				<div class="option_item" onclick="selectOption(this)">Files View</div>
				<div class="option_item" onclick="selectOption(this)">System Configuration</div>	
				<div class="option_item" onclick="selectOption(this)">User Guide</div>				
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
			<div id="right_option_area"></div>
			
			<div id="right_context">
				
				
				<div id="list_head">
					<div id="select_col_head" class="select_col"></div>
					<div id="name_col_head" class="name_col">Name</div>
					<div class="movable_divider"></div>
					<div id="date_col_head" class="date_col">Date</div>
					<div class="clear"></div>
				</div>

				<iframe id="listFrame" name="listFrame" border="0" frameBorder="0"  scrolling="auto"  width="100%"    onload="doResize()"  src="fileList.action"></iframe>
				
				
				<!--  
				<iframe id="listFrame" name="listFrame" border="0" frameBorder="0"  scrolling="auto"  width="100%"    onload="doResize()"  src="graphicView.action"></iframe>
				-->
			</div><!--end of right_context-->
		</div><!-- end of right bar -->
		
		<div class="clear"></div>
	</div>
	<!-- end of content-->
	
</div><!-- end of container-->
</body>

</html>
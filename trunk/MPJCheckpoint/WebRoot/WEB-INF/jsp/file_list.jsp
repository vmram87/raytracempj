<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
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

<link href="css/list.css" type="text/css" rel="stylesheet"/>

<script language="javascript" type="text/javascript" src="js/jquery.js"></script>
<script language="javascript" type="text/javascript" src="js/map.js"></script>

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

<script>
function doListHandleMouseMoveInListFrame(x){
	if(x >  26 ){
		$(".name_col").width(x  - 26);
		$(".date_col").css('left',x + 10);
	}
}

function click_folder(folder_icon){
	if(folder_icon.style.backgroundPosition == "" || folder_icon.style.backgroundPosition == "-385px 4px")
		open_folder(folder_icon);
	else
		close_folder(folder_icon);
}

function open_folder(folder_icon){
	folder_icon.style.backgroundPosition="-400px 4px";
	var list_item = folder_icon.parentNode.parentNode;
	var next_list_item = get_nextSibling(list_item);
	var new_blank_item = document.createElement("div");
	new_blank_item.className = "list_content_blank_item";
	document.getElementById("list_content").insertBefore(new_blank_item, next_list_item);
}

function close_folder(folder_icon){
	folder_icon.style.backgroundPosition="-385px 4px";
	var list_item = folder_icon.parentNode.parentNode;
	var next_list_item = get_nextSibling(list_item);
	while(next_list_item != null){
		if(next_list_item.className != "list_content_item")
			next_list_item.parentNode.removeChild(next_list_item); 
		
		else{
			break;
		}	
		next_list_item = get_nextSibling(list_item);
	}
}

function get_nextSibling(n)
{
	if (n.nextSibling){
		y=n.nextSibling;
		while (y.nodeType!=1)
		{
			if(y.nextSibling)
		  		y=y.nextSibling;
			else
				return null;
		}
		return y;
	}
	else
		return null;
}

function click_folder(id){
	window.location="fileList.action?folder.id=" + id + "&includeFiles=true";
}

function select_file(select){
	if(select.checked){
		select.parentNode.parentNode.className="list_content_item_selected";
		if(!window.parent.select_file_map.containsKey(select.value))
			window.parent.select_file_map.put(select.value,select.value);
	}
	else{
		select.parentNode.parentNode.className="list_content_item";
		window.parent.select_file_map.remove(select.value);
	}

}

function select_all(){
	var selectInputs = document.selectForm.select_check_box;
	if(selectInputs.length == undefined){
		selectInputs.checked = true;
		selectInputs.parentNode.parentNode.className="list_content_item_selected";
		window.parent.select_file_map.put(selectInputs.value,selectInputs.value);
		return;
	}
	
	for(i=0;i<selectInputs.length;i++){
		selectInputs[i].checked = true;
		selectInputs[i].parentNode.parentNode.className="list_content_item_selected";
		window.parent.select_file_map.put(selectInputs[i].value,selectInputs[i].value);
	}
}

function select_none(){
	var selectInputs = document.selectForm.select_check_box;
	if(selectInputs.length == undefined){
		selectInputs.checked = false;
		selectInputs.parentNode.parentNode.className="list_content_item";
		window.parent.select_file_map.clear();
		return;
	}
	
	for(i=0;i<selectInputs.length;i++){
		selectInputs[i].checked = false;
		selectInputs[i].parentNode.parentNode.className="list_content_item";
		
	}
	window.parent.select_file_map.clear();
}

$(document).ready(init);

function init(){
	window.parent.select_file_map.clear();
}
</script>

</head>

<body>

<div id="list_content">
<form id="selectForm" name="selectForm">
	<s:iterator value="fileList" id="file">
		
		
			<div class="list_content_item" >
				<div id="select_col_box"  class="select_col">
					<input type="checkbox" id="select_check_box" name="select_check_box" value="${file.id }" onchange="select_file(this)"/>
				</div>
				
				<s:if test="%{#file.isDirectory==false}">
					<a href="filedownload.action?file.id=${file.id }">
				</s:if>
				
				<div id="name_col_content" <s:if test="%{#file.isDirectory}">onclick="click_folder(${file.id })"</s:if> class="name_col">
					<s:if test="%{#file.isDirectory}">
						<div class="folder_icon"></div>
					</s:if>
					<s:else>
						<div class="file_icon"></div>
					</s:else>
					
					<span>${file.fileName}</span>
				</div>
				
				<s:if test="%{#file.isDirectory==false}">
					</a>
				</s:if>
				
				<div id="date_col_content" class="date_col">
					<span>${file.updateTime}</span>
				</div>
			</div><!-- end of list_content_item -->
			
		
	</s:iterator>

</form>
</div><!-- end of list_content -->

</body>
</html>
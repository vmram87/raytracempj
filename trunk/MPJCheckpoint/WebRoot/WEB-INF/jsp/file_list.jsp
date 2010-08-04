<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
</script>

</head>

<body>

<div id="list_content">
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			<div class="list_folder_close" onclick="click_folder(this)"></div>
			<span>aaa</span>
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			aaa
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->
	
	<div class="list_content_item">
		<div id="select_col_box" class="select_col">
			<input type="checkbox" />
		</div>
		<div id="name_col_content" class="name_col">
			bbb
		</div>
		<div id="date_col_content" class="date_col">
			July 27th
		</div>
	</div><!-- end of list_content_item -->

</div><!-- end of list_content -->




</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>File Tree</title>

<link href="css/tree.css" type="text/css" rel="stylesheet"/>
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.simple.tree.js"></script>
<script type="text/javascript" src="js/update_file_tree.js"></script>
<script type="text/javascript" src="js/jquery.contextmenu.r2.packed.js"></script>
<script type="text/javascript" src="js/map.js"></script>

<script language="javascript" type="text/javascript">
$(function() {


	$().mouseup(function(){
		if(window.parent.isMouseDown){
			window.parent.doHandBarMouseUp();
		}
	});

	$().mousemove(function(e){
		if(window.parent.isMouseDown){			
			window.parent.doHandBarMouseMove(e);
		}
		
	});

	ajax_update_tree();

});
</script>

<script type="text/javascript">
var simpleTreeCollection;
$(document).ready(init_simple_tree);

function init_simple_tree(){
	simpleTreeCollection = $('.simpleTree').simpleTree({
		autoclose: true,
		afterClick:function(node){
			window.parent.click_node(node);
			
		},
		afterDblClick:function(node){
			//alert("text-"+$('span:first',node).text());
		},
		afterMove:function(destination, source, pos){
			//alert("destination-"+destination.attr('id')+" source-"+source.attr('id')+" pos-"+pos);
		},
		afterAjax:function()
		{
			//alert('Loaded');
		},
		afterContextMenu:function(node)
		{
		},
		animate:true
		//,docToFolderConvert:true
	});
}
</script>

<script type="text/javascript">
var open_directory_ids = new Map();
function ajax_update_tree(){
	update_open_ids();
	$.post("updateTree.action",{"openDirectoryIds":open_directory_ids.values()},
		function(responseTest){		
			$('.simpleTree').html(responseTest);			
			init_simple_tree();
			if(window.parent.select_folder_id != null){
				var li = document.getElementById( window.parent.select_folder_id);
				$('.active',simpleTreeCollection[0]).attr('class','text');
				$('>span', li).attr("class","active");
				
			}
			setTimeout("ajax_update_tree()",10000);
		}
	);	
}

function update_open_ids(){
	open_directory_ids.clear();
	
	for(var i=0;i<$(".folder-open").size();i++){
		var id = $(".folder-open").get(i).id;
		open_directory_ids.put(id,id);
	}

	for(var i=0;i<$(".folder-open-last").size();i++){
		var id = $(".folder-open-last").get(i).id;
		open_directory_ids.put(id,id);
	}
}


	
</script>

<style>
html, body{
margin:0;
padding:0;
min-width:100%;
height:100%;
font-family:Arial, Helvetica, sans-serif;
}

</style>

</head>

<body>
<ul class="simpleTree">
<s:include value="tree_main.jsp"/>
</ul>


 <div class="contextMenu" id="myMenu1">
  <ul>
    <li id="new_folder">New Folder</li>
    <li id="rename">Rename</li>
    <li id="delete">Delete</li>
    <li id="move_to">Move To</li>
  </ul>
</div>


					
</body>

</html>
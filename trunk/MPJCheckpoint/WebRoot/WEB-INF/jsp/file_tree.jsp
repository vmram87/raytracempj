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


});
</script>

<script type="text/javascript">
var simpleTreeCollection;
$(document).ready(function(){
	simpleTreeCollection = $('.simpleTree').simpleTree({
		autoclose: true,
		afterClick:function(node){
			window.parent.select_folder_id = node.attr("id");
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
});
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

<s:include value="tree_main.jsp"/>

 <div class="contextMenu" id="myMenu1">
  <ul>
    <li id="new_folder"><img src="folder.png" /> New Folder</li>
    <li id="rename"><img src="email.png" /> Rename</li>
    <li id="delete"><img src="disk.png" /> Delete</li>
    <li id="move_to"><img src="cross.png" /> Move To</li>
  </ul>
</div>

					
</body>

</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Graphic View</title>

<style>
html, body{
margin:0;
padding:0;
width:100%;
height:100%;
font-family:Arial, Helvetica, sans-serif;
}
</style>

<link href="css/graphic_view.css" type="text/css" rel="stylesheet"/>

<script language="javascript" type="text/javascript" src="js/jquery.js"></script>

<script language="javascript" type="text/javascript">
$(function() {


	$().mouseup(function(){
		if(window.parent.isMouseDown){
			window.parent.doHandBarMouseUp();
		}
	});

	$().mousemove(function(e){
		if(window.parent.isMouseDown){
			window.parent.doHandBarMouseMoveInListFrame(e);
		}
	});


});

$(document).ready(update_view);
function update_view(){
	var itv = Math.floor($(document).width() / 220);
	$.get("nodesInfo.action",{interval: itv},
			function(responseText){		
				$("#nodes").html(responseText);
				setTimeout("update_view()",2000);
			}
	);
}
</script>

</head>

<body>

<div id="nodes">

	<s:iterator value="nodeList" id="node" status="st">
		<div class="machine_node">
			<div class="machine_id">
				<span>${node.name}</span>
			</div>
			
			<div class="machine_frame">
				<div class="node_info">
					<p>${st.index}</p>
					<p>node status: ${node.daemonStatus }</p>
					<p>no. of processes: ${fn:length(node.process)}</p>
					<p>Process Rank:</p>
					<table>					
						<s:iterator value="#node.process" id="rank">
							<tr>
								<td>Rank</td><td>${rank }</td>
							</tr>
						</s:iterator>
	
					</table>
				</div><!-- end of node_info -->
			</div><!-- end of machine_frame -->
		</div><!-- end of machine_node -->
		
		
		
	</s:iterator>
	
</div>

</body>
</html>
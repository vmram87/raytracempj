<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
</script>

</head>

<body>

<div id="nodes">

	<div class="machine_node">
		<div class="machine_id">
			<span>pc01</span>
		</div>
		
		<div class="machine_frame">
			<div class="node_info">
				<p>node status: </p>
				<p>no. of processes: </p>
				<table>
					<tr>
						<th>Rank</th><th>Status</th>
					</tr>
					<tr>
						<td>0</td><td>Running</td>
					</tr>
					<tr>
						<td>1</td><td>Running</td>
					</tr>
				</table>
			</div>
		</div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
	<div class="machine_node">
		<div class="machine_id"></div>
		
		<div class="machine_frame"></div>
	</div><!-- end of machine_node -->
	
</div>

</body>
</html>
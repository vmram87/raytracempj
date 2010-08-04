<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>File Tree</title>

<link href="css/tree.css" type="text/css" rel="stylesheet"/>
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.simple.tree.js"></script>

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
			//alert("text-"+$('span:first',node).text());
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
		animate:true
		//,docToFolderConvert:true
	});
});
</script>

<style>
html, body{
margin:0;
padding:0;
width:100%;
height:100%;
font-family:Arial, Helvetica, sans-serif;
}

</style>
</head>

<body>

<ul class="simpleTree">
	<li class="root" id='1'><span>Tree Root 1</span>
		<ul>
			
			<li class="open" id='2'><span>Tree Node 1</span>
			</li>
			
			<li id='5'><span>Tree Node 2</span>
				<ul>
					
					<li id='6'><span>Tree Node 2-1</span>
						<ul>
							
							<li id='7'><span>Tree Node 2-1-1</span></li>
							
							<li id='8'><span>Tree Node 2-1-2</span></li>
							
							<li id='9'><span>Tree Node 2-1-3</span></li>
							
							<li id='10'><span>Tree Node 2-1-4</span>
								<ul class="ajax">
									<li id='11'>{url:loadTree.php?tree_id=1}</li>
								</ul>
							</li>
							
						</ul>
					</li>
					
					<li id='12'><span>Tree Node 2-2</span>
						<ul>
							
							<li id='13'><span>Tree Node 2-2-1</span></li>
							
						</ul>
					</li>
					
					
					<li id='14'><span>Tree Node 2-3</span>
						<ul>
							
							<li id='15'><span>Tree Node 2-3-1</span>
									<ul>
										
										<li id='16'><span>Tree Node 2-3-1-1</span></li>
										
										<li id='17'><span>Tree Node 2-3-1-2</span></li>
										
										<li id='18'><span>Tree Node 2-3-1-3</span>
											<ul>
												
												<li id='19'><span>Tree Node 2-3-1-3-1</span></li>
												
											</ul>
										</li>
										
										<li id='20'><span>Tree Node 2-3-1-4</span></li>
										
										<li id='21'><span>Tree Node 2-3-1-5</span></li>
										
										<li id='22'><span>Tree Node 2-3-1-6</span>
											<ul>
												
												<li id='23'><span>Tree Node 2-3-1-6-1</span></li>
												
											</ul>
										</li>
										
										<li id='24'><span>Tree Node 2-3-1-7</span></li>
										
										<li id='25'><span>Tree Node 2-3-1-8</span></li>
										
										<li id='26'><span>Tree Node 2-3-1-9</span>
											<ul>
												
												<li id='27'><span>Tree Node 2-3-1-9-1</span></li>
												
											</ul>
										</li>
										
									</ul>
							</li>
							
						</ul>
					</li>
					
				</ul>
			</li>
			
		</ul>
	</li>
</ul>
</body>

</html>
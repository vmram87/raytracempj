<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>System Configuration</title>

<script type="text/javascript" src="js/ajax.js"></script>
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>
<script language="javascript" type="text/javascript" src="js/timer.js"></script>
<link rel="stylesheet" type="text/css" href="css/dialog.css"/>
<script language="javascript" type="text/javascript" src="js/dialog.js" ></script>

<style>
html, body{
margin:0;
padding:0;
min-width:100%;
height:100%;
font-family:Arial, Helvetica, sans-serif;
text-align:center;
}

#center_area{
	font-size:13px;
	margin-left:auto;
	margin-right:auto;
	margin-top:30px;
	width:550px;
}


table td{
	text-align:left;
}
</style>

<script type="text/javascript">
function selectDirectory(){
	dialog("Select Directory","iframe:selectDirectoryPage.action","300px","360px","iframe");
}

function setDirectory(directory){
	$("#outputFile").attr("value",directory);
	close_dialog();
}

function removeOutputFile(fileNode){
	fileNode.parentNode.removeChild(fileNode);
}




function addOutpFile(){

	var e = $("<div class='output_file_name'> \
				<input id='outputFile' name='outputFile' type='text'>\
				<input type='button' value='Select Directory' onclick='selectDirectory()'></input>\
				<input type='button' value='Remove' onclick='removeOutputFile(this.parentNode)'></input>\
			</div>");

	$("#output_file_collection").append(e);
}

$(document).ready(initial);

function initial(){
	$.timer(3000,function(timer){
		$("#tip").hide();
		timer.stop();
	});
}

</script>

</head>

<body>

<div id="center_area">
<font id="tip" color="#FF0000">${tip }</font>
	<form id="configForm" action="saveConfig.action" method="post">
		<table align="left">
			<tr>
				<td width="120px">Run Java Type:</td>
				<td>
					<input type="radio" id="runType" name="runType" value="Class" <s:if test="%{runType=='Class'}">checked</s:if>  >Class</input>
					<input type="radio" id="runType" name="runType" value="Jar" <s:if test="%{runType=='Jar'}">checked</s:if> >Jar</input>
				</td>
			</tr>
			<tr>
				<td>Run File:</td>
				<td>
					<input id="runFile" name="runFile" type="text" value="${runFile }"/>				
				</td>
			</tr>
			<tr>
				<td>Number of Process:</td>
				<td><input id="nproc" name="nproc" type="text" value="${nproc }"></input></td>
			</tr>
			<tr>
				<td valign="top">Output File:</td>
				<td>
					<div id="output_file_area">					
							
							<div id="output_file_collection" class="output_file_item">
								<s:iterator value="outputFile" id="file" status="st">
									<div class="output_file_name">
										<input id="outputFile" name="outputFile" type="text" value="${file}"/>
										<input type="button" value="Select Directory" onclick="selectDirectory()"></input>
										<input type="button" value="Remove" onclick="removeOutputFile(this.parentNode)"></input>
									</div>									
								</s:iterator>
							</div>						
						
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<input type="button" value="Add Output File" onclick="addOutpFile()"></input>
				</td>
			</tr>
			
			<tr>
				<td colspan="2" align="center" style="text-align:center">
					<p>Notice: The output file path configuration is to automatically add the output file to the file system when your program ends. The output file path must under "My_Class" directory, if your output file path is "My_Class/XXX.bmp", then in your java application, your path should be "XXX.bmp", and output file path of "My_Class/yourFolderName/XXX.bmp" should be "yourFolderName/XXX.bmp" in your application.</p>
				</td>
			</tr>
			
			<tr>
				<td colspan="2" align="center" style="text-align:center">
					<br/>
					<input type="submit" value="Save"/>
					<input type="reset" value="Reset"/>
				</td>
			</tr>
		</table>
		
		
	</form>

</div>
					
</body>

</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>System Configuration</title>

<script type="text/javascript" src="js/ajax.js"></script>

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


</script>

</head>

<body>
<div id="center_area">

	<form id="configForm" action="saveConfig.action" method="post">
		<table align="left">
			<tr>
				<td>Run Java Type:</td>
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
				<td>Output File:</td>
				<td>
					<div id="output_file_area">
						<div class="output_file_item">
							<div class="output_file_name">
								File Path:
								<input id="outputFile" name="outputFile" type="text" value="${outputFile[0] }"/>
								<input type="button" value="Select Directory"></input>
							</div>
						</div>
					</div>
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
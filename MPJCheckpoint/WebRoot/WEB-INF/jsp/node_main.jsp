<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<%@ page pageEncoding="UTF-8" %>

<s:iterator value="nodeList" id="node" status="st">
	<div class="machine_node">
		<div class="machine_id">
			<span>${node.name}</span>
		</div>
		
		<div class="machine_frame">
			<div class="node_info">
				<p>node status: 
					<font color="<s:if test="%{#node.daemonStatus=='Checkpointing'}">#00FF00</s:if>
								<s:elseif test="%{#node.daemonStatus=='Disconnected'}">#FF0000</s:elseif>
								<s:elseif test="%{#node.daemonStatus=='Restarting'}">#0000FF</s:elseif>">						 
						${node.daemonStatus }</p>
					</font>					
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
	<s:if test="%{interval!=null &&  #st.index%3==(interval-1)}">
		<div class="clear"></div>
	</s:if>
</s:iterator>


<s:if test="%{outputFile!=null}">
	Output Files:
	<s:iterator value="outputFile" id="file" status="st">
		<div>
			File <a href="${file }" target="_blank"><s:property value="%{#file.substring(11)}"/></a> : <br/>
			<a href="${file }" target="_blank"><img border="0" width="600px" src="${file }"/></a>
		</div>
	</s:iterator>
</s:if>

					

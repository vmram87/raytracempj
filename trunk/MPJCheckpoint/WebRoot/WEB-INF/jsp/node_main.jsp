<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<%@ page pageEncoding="UTF-8" %>

<s:iterator value="nodeList" id="node">
	<div class="machine_node">
		<div class="machine_id">
			<span>${node.name}</span>
		</div>
		
		<div class="machine_frame">
			<div class="node_info">
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


					

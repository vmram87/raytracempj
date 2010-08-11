<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>


<s:iterator value="fileList" id="file" >
	<li id="${file.id}"><span class="text">${file.fileName }</span>
		<s:if test="%{#file.isDirectory}">
			<ul class="ajax">
				<li id="${-file.id }">{url:folderList.action?folder.id=${file.id}&includeFiles=false}</li>
			</ul>
		</s:if>
	</li>
</s:iterator>
					

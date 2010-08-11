<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<ul class="simpleTree">
	<li class="root" id='-1'><span>My Folder</span>
		<ul>
			
			<li id='${codeFolder.id}'><span>${codeFolder.fileName}</span>
				<ul class="ajax">
					<li id='${-codeFolder.id}'>{url:folderList.action?folder.id=${codeFolder.id}&includeFiles=false}</li>
				</ul>
			</li>
			
			<li id='${libFolder.id}'><span>${libFolder.fileName}</span>
				<ul class="ajax">
					<li id='${-libFolder.id}'>{url:folderList.action?folder.id=${libFolder.id}&includeFiles=false}</li>
				</ul>
			</li>
		</ul>
	</li>
</ul>
					

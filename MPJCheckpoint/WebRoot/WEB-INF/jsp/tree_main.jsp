<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>


	<li class="root" id='-1'><span>My Folder</span>
		<ul>
			
			<li <s:if test="%{tip!=null&&tip!=''}">class="open"</s:if> id='${codeFolder.id}'><span>${codeFolder.fileName}</span>
				<s:if test="%{tip!=null&&tip!=''}">
					<ul>
						${tip }
					</ul>
				</s:if>
				
				<s:else>
					<ul class="ajax">					
						<li id='${-codeFolder.id}'>{url:folderList.action?folder.id=${codeFolder.id}&includeFiles=false}</li>
					</ul>
				</s:else>
				
			</li>
			
			<li <s:if test="%{libList!=null&&libList.size>0}">class="open"</s:if>  id='${libFolder.id}'><span>${libFolder.fileName}</span>
				<s:if test="%{libList!=null&&libList.size>0}">
					<ul>
						
					</ul>
				</s:if>
				<s:else>
					<ul>
						
					</ul>
				</s:else>				
			</li>
		</ul>
	</li>
	
	<script type="text/javascript">
		if(window.parent.select_folder_id  == null)
			window.parent.select_folder_id =${codeFolder.id};
	</script>

					

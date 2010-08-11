<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>

<s:if test="%{tip==null}">
	Successed
</s:if>
<s:else>
	${tip }
</s:else>

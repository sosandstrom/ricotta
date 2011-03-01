<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/">Home</a> | 
<a href="/proj/">Projects</a> | 
<a href="/proj/<c:out value='${projName}' />/branch/<c:out value='${branchName}' />/"><c:out value="${projName}" /></a>
<c:if test="${null != viewContext}" >
 | <a href="/proj/<c:out value='${projName}' />/lang/<c:out value='${langCode}' />/"><c:out value="${langCode}" /></a>  
	<div class="pageHeading">Translations for context <c:out value="${viewContext.name}" /></div>
	<b>Description: </b><c:out value="${viewContext.description}" /><br />
	<img src="/screenshot?blobKey=<c:out value='${viewContext.blobKey.keyString}' />" />
</c:if>
<div class="pageHeading">Translations - <c:out value="${langCode}" /></div>
<form id="translations" name="translations" action="<c:out value='${action}' />" method="post" >
<input type="submit" value="Save" />
<table>
		<thead>
		<tr>
			<th width="15%">Name</th>
			<th width="15%">Description</th>
			<th width="40%">Value</th>
			<th width="30%">Default</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${translations}" var="t">
		<c:set var="even" scope="page" value="${!even}" />
		<tr id="<c:out value='${t.token.keyString}' />" class="evenRow<c:out value='${even}' />" title="<c:out value='${t.key.kind}' />">
			<td><c:out value="${t.token.name}" /></td>
			<td><input id="description.<c:out value='${t.token.keyString}' />" name="description.<c:out value='${t.token.keyString}' />" 
				type="text" value="<c:out value='${t.token.description}' />" /></td>
			<td><textarea id="<c:out value='${t.keyString}' />"	name="<c:out value='${t.keyString}' />"
				cols="80" rows="2" ><c:out value='${t.local.local}' /></textarea></td>
			<td><c:out value="${t.parent.local}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<input type="submit" value="Save" />
</form>
<h3>Import</h3>
<a href="/proj/<c:out value="${projName}"/>/lang/<c:out value="${langCode}" />/import.html">Import tokens and translations</a>
</body>
</html>
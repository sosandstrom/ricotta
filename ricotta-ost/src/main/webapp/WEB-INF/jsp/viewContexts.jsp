<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> | <a href="/projects/index.html">Projects</a> | <a href="/projects/<c:out value='${project.name}' />/index.html"><c:out value="${project.name}" /></a>
<div class="pageHeading">Select Context</div>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
			<th>Screenshot</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${viewContexts}" var="c">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><a href="/projects/<c:out value='${project.name}'/>/viewContexts/<c:out value='${c.name}' />/"><c:out value="${c.name}" /></a></td>
			<td><c:out value="${c.description}" /></td>
			<td><img src="/screenshot?blobKey=<c:out value='${c.blobKey.keyString}' />" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
</body>
</html>
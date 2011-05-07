<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> I <a href="/exportOld.xml">.</a>
<div class="pageHeading">Projects</div>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Owner</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${projs}" var="p">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><a href="<c:out value="${p.name}" />/branch/trunk/"><c:out value="${p.name}" /></a></td>
			<td><c:out value="${p.owner}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="create.html">Create project...</a>
</body>
</html>
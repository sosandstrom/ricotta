<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a>
<div class="pageHeading">Templates</div>
<table>
		<thead>
		<tr>
			<th>Key</th>
			<th>Name</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${malls}" var="m">
		<tr>
			<td><c:out value="${m.key}" /></td>
			<td><a href="/templates/<c:out value='${m.name}' />/"><c:out value="${m.name}" /></a></td>
			<td><c:out value="${m.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="/templates/create.html">Create template...</a>
</body>
</html>
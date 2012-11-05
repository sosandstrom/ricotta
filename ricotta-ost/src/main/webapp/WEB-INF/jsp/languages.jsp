<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/classic/index.html">Classic Home</a>
<div class="pageHeading">Languages</div>
<table>
		<thead>
		<tr>
			<th>Code</th>
			<th>Name</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${languages}" var="language">
		<tr>
			<td><c:out value="${language.code}" /></td>
			<td><c:out value="${language.name}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="create.html">Create language...</a>
</body>
</html>
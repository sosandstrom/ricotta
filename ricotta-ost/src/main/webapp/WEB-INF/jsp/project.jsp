<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> | <a href="/projects/index.html">Projects</a>
<div class="pageHeading">Project Details</div>
<table>
		<thead>
		<tr>
			<th>Key</th>
			<th>Name</th>
			<th>Owner</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td><c:out value="${project.key}" /></td>
			<td><c:out value="${project.name}" /></td>
			<td><c:out value="${project.owner}" /></td>
		</tr>
	</tbody>
</table>
<h3>Project Languages</h3>
<table>
		<thead>
		<tr>
			<th>Code</th>
			<th>Name</th>
			<th>Default</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${languages}" var="plm">
		<tr>
			<td><c:out value="${plm.language.code}" /></td>
			<td><a href="languages/<c:out value='${plm.language.code}' />/translations/" ><c:out value="${plm.language.name}" /></a></td>
			<td><c:out value="${plm.parentName}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="/projects/<c:out value="${project.name}"/>/languages/create.html">Add language</a>
<h3>Project Artifacts</h3>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${artifacts}" var="a">
		<tr>
			<td><c:out value="${a.name}" /></td>
			<td><c:out value="${a.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="/projects/<c:out value="${project.name}"/>/artifacts/create.html">Create artifact</a>
<h3>Project Tokens</h3>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${tokens}" var="token">
		<tr>
			<td><c:out value="${token.name}" /></td>
			<td><c:out value="${token.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="/projects/<c:out value="${project.name}"/>/tokens/create.html">Create token</a>
</body>
</html>
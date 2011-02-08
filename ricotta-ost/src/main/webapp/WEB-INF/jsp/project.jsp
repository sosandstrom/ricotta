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
			<th>Name</th>
			<th>Owner</th>
		</tr>
	</thead>
	<tbody>
		<tr>
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
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${languages}" var="plm">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
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
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${artifacts}" var="a">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><a href="/projects/<c:out value='${project.name}'/>/tokens/"><c:out value="${a.name}" /></a></td>
			<td><c:out value="${a.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="/projects/<c:out value="${project.name}"/>/artifacts/create.html">Create artifact</a>
<form action="versions/deleteVersions.html" method="post" name="deleteVersions" id="deleteVersions">
<h3>Project Versions</h3>
<table>
		<thead>
		<tr>
			<th>Delete</th>
			<th>Name</th>
			<th>Description</th>
			<th>Date</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td></td>
			<td><a href="?" ><c:out value="${HEAD.name}" /></a></td>
			<td><c:out value="${HEAD.description}" /></td>
			<td><c:out value="${HEAD.datum}" /></td>
		</tr>
	<c:forEach items="${versions}" var="v">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><input type="checkbox" name="versions" id="versions" value="<c:out value='${v.keyString}' />" /></td>
			<td><a href="?version=<c:out value='${v.name}' />"><c:out value="${v.name}" /></a></td>
			<td><c:out value="${v.description}" /></td>
			<td><c:out value="${v.datum}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<c:if test="${pageContext.request.userPrincipal.name == project.owner}">
	<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected versions" />
</c:if>
</form>
<a href="/projects/<c:out value="${project.name}"/>/versions/create.html">Create version</a>
<form action="" method="post" name="deleteForm" id="deleteForm">
<h3>Project Users</h3>
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users and tokens" />
<table>
		<thead>
		<tr>
			<th>Delete</th>
			<th>Email</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${users}" var="u">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><input type="checkbox" name="users" id="users" value="<c:out value='${u.keyString}' />" /></td>
			<td><c:out value="${u.user}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users and tokens" /><br />
<a href="/projects/<c:out value="${project.name}"/>/users/create.html">Add user</a>
<h3>Project Tokens</h3>
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users and tokens" />
<table>
		<thead>
		<tr>
			<th>Delete</th>
			<th>Name</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${tokens}" var="token">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><input type="checkbox" id="tokens" name="tokens" value="<c:out value='${token.keyString}' />" /></td>
			<td><c:out value="${token.name}" /></td>
			<td><c:out value="${token.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users and tokens" />
</form>
<a href="/projects/<c:out value="${project.name}"/>/tokens/create.html">Create token</a>
</body>
</html>
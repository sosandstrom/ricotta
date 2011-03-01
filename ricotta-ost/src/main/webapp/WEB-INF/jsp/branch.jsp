<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/">Home</a> | <a href="/proj/">Projects</a>
<div class="pageHeading">Project: <c:out value="${projName}" /> (<c:out value="${branchName}" />)</div>
<p />
<div class="pageHeading">Languages</div>
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
			<td><c:out value="${plm.lang.code}" /></td>
			<td><a href="lang/<c:out value='${plm.lang.code}' />/" ><c:out value="${plm.lang.name}" /></a></td>
			<td><c:out value="${plm.defaultCode}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="lang/create.html">Add language</a>

<h3>Project Tokens</h3>
<a href="tokn/">Edit tokens...</a>

<h3>Project contexts</h3>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${viewContexts}" var="c">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><c:out value="${c.name}" /></td>
			<td><c:out value="${c.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="uploadContext.html">Upload context</a>

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
			<td><c:out value="${a.name}" /></td>
			<td><c:out value="${a.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="variant/create.html">Create artifact</a>

<form action="deleteBranches.html" method="post" name="deleteBranches" id="deleteBranches">
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
			<td><a href="../<c:out value='${v.name}' />/"><c:out value="${v.name}" /></a></td>
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

<h3>Project Users</h3>
<form action="" method="post" name="deleteForm" id="deleteForm">
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users" />
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
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users" /><br />
</form>
<a href="/projects/<c:out value="${project.name}"/>/users/create.html">Add user</a>

</body>
</html>
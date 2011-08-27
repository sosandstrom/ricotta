<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/">Home</a> | <a href="/proj/">Projects</a>
<div class="pageHeading">Project: <c:out value="${projName}" /> (<c:out value="${branchName}" />)</div>
<p />
<h3>Languages</h3>
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
<c:if test="${projUser.manage}">
<a href="lang/create.html">Add language</a>
</c:if>

<c:if test="${projUser.tokn}">
<h3>Tokens</h3>
<a href="tokn/">Edit tokens...</a>
</c:if>

<c:if test="${null != ctxts}">
<h3>Contexts</h3>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${ctxts}" var="c">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><c:out value="${c.name}" /></td>
			<td><c:out value="${c.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="uploadCtxt.html">Create context...</a>
</c:if>


<c:if test="${projUser.manage}">
<h3>Subsets</h3>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${subsets}" var="a">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><c:out value="${a.name}" /></td>
			<td><c:out value="${a.description}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<a href="subset.html">Create Subset...</a>
</c:if>

<c:if test="${null != branches}">
<form action="deleteBranches.html" method="post" name="deleteBranches" id="deleteBranches">
<h3>Branches</h3>
<table>
		<thead>
		<tr>
			<th>Delete</th>
			<th>Name</th>
			<th>Description</th>
			<th>Created</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${branches}" var="v">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td>
<c:if test="${v.name != branchName && v.name != 'trunk' && projUser.destroy}">
				<input type="checkbox" name="versions" id="versions" value="<c:out value='${v.keyString}' />" />
</c:if>
			</td>
			<td>
<c:if test="${v.name != branchName}">
				<a href="../<c:out value='${v.name}' />/">
</c:if>
				<c:out value="${v.name}" />
<c:if test="${v.name != branchName}">
				</a>
</c:if>
			</td>
			<td><c:out value="${v.description}" /></td>
			<td><c:out value="${v.createdDate}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<c:if test="${projUser.destroy}">
	<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected branches" />
</c:if>
</form>
<c:if test="${projUser.manage}">
<a href="create.html">Create branch...</a>
</c:if>
</c:if>

<c:if test="${null != users}">
<h3>Users</h3>
<form action="" method="post" name="deleteForm" id="deleteForm">
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users" />
<input type="submit" id="updateRoles" name="updateRoles" value="Save user roles" />
<table>
		<thead>
		<tr>
			<th>Delete</th>
			<th>Email</th>
			<th>Role</th>
			<th>Added</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${users}" var="u">
<c:if test="${projUser.user != u.user}">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td>
				<input type="checkbox" name="users" id="users" value="<c:out value='${u.keyString}' />" />
			</td>
			<td><c:out value="${u.user}" /></td>
			<td>
<c:if test="${u.role <= projUser.role}">
				<select id="role:<c:out value='${u.user}'/>" name="role:<c:out value='${u.user}'/>">
					<c:forEach items="${roles}" var="r">
					<option value="<c:out value='${r.key}'/>"
					 	<c:if test="${r.key == u.role}">selected="selected"</c:if>
						><c:out value="${r.value}"/></option>
					</c:forEach>
				</select>				
</c:if>			
<c:if test="${projUser.role < u.role}">
				<c:out value="${roles[u.role]}" />
</c:if>			
			</td>
			<td>${u.createdDate}</td>
		</tr>
</c:if>
	</c:forEach>
	</tbody>
</table>
<input type="submit" id="deleteSelected" name="deleteSelected" value="Delete selected users" />
<input type="submit" id="updateRoles" name="updateRoles" value="Save user roles" />
</form><br />
<a href="user.html">Add user...</a>
</c:if>

</body>
</html>
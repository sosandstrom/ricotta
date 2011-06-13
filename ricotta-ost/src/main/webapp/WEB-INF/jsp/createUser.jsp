<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> | <a href="/projects/index.html">Projects</a> | <a href="/projects/<c:out value='${project.name}' />/index.html"><c:out value="${project.name}" /></a>
<div class="pageHeading">Add user</div>
<form id="projectUser" name="projectUser" action="" method="post">
<table>
	<tr>
		<td>User email:</td>
		<td><input id="user" name="user" type="text" value="" /></td>
	</tr>
	<tr>
		<td>User role:</td>
		<td><select id="role" name="role">
			<c:forEach items="${roles}" var="r">
			<option value="<c:out value='${r.key}'/>"><c:out value="${r.value}"/></option>
			</c:forEach>
		</select></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="create" name="create" type="submit" value="Add user" /></td>
	</tr>
</table>
</form>
</body>
</html>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> | <a href="/projects/index.html">Projects</a> | <a href="/projects/<c:out value='${project.name}' />/index.html"><c:out value="${project.name}" /></a>
<div class="pageHeading">Artifacts - <c:out value="${project.name}" /></div>
<form id="artifact" name="artifact" action="" method="post">
<table>
	<tr>
		<td>Artifact Name:</td>
		<td><input id="name" name="name" type="text" value="" /></td>
	</tr>
	<tr>
		<td>Artifact Description:</td>
		<td><input id="description" name="description" type="text" value="" /></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="create" name="create" type="submit" value="Create artifact" /></td>
	</tr>
</table>
</form>
</body>
</html>
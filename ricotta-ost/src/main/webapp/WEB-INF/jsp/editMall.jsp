<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> | <a href="../index.html">Templates</a>
<div class="pageHeading">Edit template</div>
<form id="mall" name="mall" action="" method="post">
<table>
	<tr>
		<td>Template name:</td>
		<td><c:out value="${mall.name}" /></td>
	</tr>
	<tr>
		<td>Template description:</td>
		<td><input id="description" name="description" type="text" value="<c:out value='${mall.description}' />" /></td>
	</tr>
	<tr>
		<td>Template MIME type:</td>
		<td><input id="mimeType" name="mimeType" type="text" value="<c:out value='${mall.mimeType}' />" /></td>
	</tr>
	<tr>
		<td>Template body:</td>
		<td><textarea id="body" name="body" cols="80" rows="30"><c:out value='${mall.body}' /></textarea></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="save" name="save" type="submit" value="Save template" /></td>
	</tr>
</table>
</form>
</body>
</html>
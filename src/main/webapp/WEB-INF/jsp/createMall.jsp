<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<form id="mall" name="mall" action="" method="post">
<table>
	<tr>
		<td>Template name:</td>
		<td><input id="name" name="name" type="text" value="" /></td>
	</tr>
	<tr>
		<td>Template description:</td>
		<td><input id="description" name="description" type="text" value="" /></td>
	</tr>
	<tr>
		<td>Template MIME type:</td>
		<td><input id="mimeType" name="mimeType" type="text" value="text/plain" /></td>
	</tr>
	<tr>
		<td>Template body:</td>
		<td><textarea id="body" name="body" cols="80" rows="30"></textarea></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="create" name="create" type="submit" value="Create template" /></td>
	</tr>
</table>
</form>
</body>
</html>
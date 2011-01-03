<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Translations</title>
</head>
<body>
<h3>Translations - </h3>
<form id="translations" name="translations" action="" method="post" >
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
			<th>Value</th>
			<th>Default</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${translations}" var="t">
		<tr>
			<td><c:out value="${t.token.name}" /></td>
			<td><c:out value="${t.token.description}" /></td>
			<td><input id="<c:out value='${t.token.keyString}' />" 
				name="<c:out value='${t.token.keyString}' />" 
				type="text" value="<c:out value='${t.local.local}' />" /></td>
			<td><c:out value="${t.parent.local}" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
</form>
</body>
</html>
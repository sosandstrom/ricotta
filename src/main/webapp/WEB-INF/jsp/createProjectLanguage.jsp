<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Projects</title>
</head>
<body>
<form id="project" name="project" action="" method="post">
<table>
	<tr>
		<td>Project Name:</td>
		<td><c:out value="${project.name}" /></td>
	</tr>
	<tr>
		<td>Default language:</td>
		<td><select id="parent" name="parent">
			<c:forEach items="${parentLanguages}" var="parent">
				<option value="<c:out value='${parent.language.keyString}' />"><c:out value="${parent.language.name}" /> (<c:out value="${parent.language.code}" />)</option>
			</c:forEach>
		</select></td>
	</tr>
	<tr>
		<td>Language:</td>
		<td><select id="language" name="language">
			<c:forEach items="${languages}" var="language">
				<option value="<c:out value='${language.keyString}' />"><c:out value="${language.name}" /> (<c:out value="${language.code}" />)</option>
			</c:forEach>
		</select></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="create" name="create" type="submit" value="Add project language" /></td>
	</tr>
</table>
</form>
</body>
</html>
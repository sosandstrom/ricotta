<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<form id="language" name="language" action="" method="post">
<table>
	<tr>
		<td>Language code:</td>
		<td><input id="code" name="code" type="text" value="" /></td>
	</tr>
	<tr>
		<td>Language name:</td>
		<td><input id="name" name="name" type="text" value="" /></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="create" name="create" type="submit" value="Create language" /></td>
	</tr>
</table>
</form>
</body>
</html>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> | <a href="/proj/index.html">Projects</a> | <a href="index.html"><c:out value="${project.name}" /></a>
<div class="pageHeading">Create Context</div>
<form id="ctxt" name="ctxt" action="<c:out value='${action}'/>" method="post" enctype="multipart/form-data">
<table>
	<tr>
		<td>Select screenshot image:</td>
		<td><input id="screenShot" name="screenShot" type="file" /></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="upload" name="upload" type="submit" value="Upload screenshot" /></td>
	</tr>
</table>
</form>
</body>
</html>
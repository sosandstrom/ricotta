<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<form id="projLang" name="projLang" action="" method="post">
<table>
	<h3>Add language to project</h3>
	<tr>
		<td>Default language:</td>
		<td><select id="defaultLang" name="defaultLang">
			<c:forEach items="${parentLanguages}" var="parent">
				<option value="<c:out value='${parent.keyString}' />"><c:out value="${parent.name}" /> (<c:out value="${parent.code}" />)</option>
			</c:forEach>
		</select></td>
	</tr>
	<tr>
		<td>Language:</td>
		<td><select id="langCode" name="langCode">
			<c:forEach items="${languages}" var="language">
				<option value="<c:out value='${language.code}' />"><c:out value="${language.name}" /> (<c:out value="${language.code}" />)</option>
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
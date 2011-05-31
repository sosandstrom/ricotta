<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a> | <a href="/proj/index.html">Projects</a> | <a href="/proj/<c:out value='${projName}' />/index.html"><c:out value="${projName}" /></a>
| <a href="/proj/<c:out value='${projName}' />/lang/<c:out value="${langCode}" />/index.html"><c:out value="${langCode}" /></a>
<div class="pageHeading">Import - <c:out value="${langCode}" /> (<c:out value="${langCode}" />)</div>
<form id="import" name="import" action="" method="post">
<table>
	<tr>
		<td>Regexp:</td>
		<td><select id="regexp" name="regexp">
			<option value="custom">Custom regexp</option>
			 <option value="([^=^\s]+)[\s]*=[\s]*([^\n^\r]*)[\r\n\s]*" >Java properties file</option>
			 <!-- name[\s]*=[\s]*"([^"]+)"[\s]*\>([^\<]*)\</string\> -->
			 <option value='name[\s]*=\s*"(.+?)"\s*\>(.*?)\&lt;/string\&gt;' >Android strings file</option>
			 <option value='([^=^\s]+)\s*=\s*\"([^\"]*)\"\;' >iPhone Localizeable.strings file</option>
			 <option value='name="([^"]+)"[^\&lt;]*\&lt;value\&gt;([^\&lt;]*)\&lt;/value\&gt;' >WM7 XML file</option>
		</td>
	</tr>
	<tr>
		<td>Custom regexp "(tokenName) (languageValue)":</td>
		<td><input id="custom" name="custom" type="text" value="" /></td>
	</tr>
	<tr>
		<td>Paste your body text here:</td>
		<td><textarea id="body" name="body" cols="80" rows="30"></textarea></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="import" name="import" type="submit" value="Import translation" /></td>
	</tr>
</table>
</form>
</body>
</html>

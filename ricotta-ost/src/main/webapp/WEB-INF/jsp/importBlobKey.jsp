<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/index.html">Home</a>
<div class="pageHeading">Import XML Blob (all)</div>
<form id="ctxt" name="ctxt" action="" method="post">
<table>
	<tr>
		<td>Ricotta XML blobKeyString:</td>
		<td><input id="blobKeyString" name="blobKeyString" type="text" /></td>
	</tr>
	<tr>
		<td></td>
		<td><input id="upload" name="upload" type="submit" value="Import XML from blob" /></td>
	</tr>
</table>
</form>
</body>
</html>
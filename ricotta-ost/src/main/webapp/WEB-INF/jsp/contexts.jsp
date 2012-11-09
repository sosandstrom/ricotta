<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<a href="/classic/index.html">Home</a> | 
<a href="/classic/proj/index.html">Projects</a> | 
<a href="/classic/proj/<c:out value='${projName}' />/branch/<c:out value='${branchName}' />/index.html"><c:out value="${projName}" /></a>
 | <a href="/classic/proj/<c:out value='${projName}' />/branch/<c:out value='${branchName}' />/lang/<c:out value='${langCode}' />/index.html"><c:out value="${langCode}" /></a>
   
<div class="pageHeading">Select Context</div>
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
			<th>Screenshot</th>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><a href="ctxt/index.html">All tokens</a></td>
			<td><c:out value="All tokens, with or without context" /></td>
			<td><img src="/screenshot?blobKey=AMIfv96vJ5hngrNuZdTZzG8P0SfT2aDcJwqmVo928rnYBQBeNeWmfGd3HhOZ6xlAVCPchkW7FKhiDg3biDgGD1pz8Oc3JUmX-qeYgm3gRbZ4007L1CT65_JXol1_JMypAyR4aY04EcU0FXRy6dPHfI5Vdvu7gHSIqQ" /></td>
		</tr>
	<c:forEach items="${viewContexts}" var="c">
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><a href="ctxt/<c:out value='${c.name}' />/index.html"><c:out value="${c.name}" /></a></td>
			<td><c:out value="${c.description}" /></td>
			<td><img src="/screenshot?blobKey=<c:out value='${c.blobKey.keyString}' />" /></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
</body>
</html>
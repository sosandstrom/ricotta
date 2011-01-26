<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<c:if test="${null != pageContext.request.userPrincipal}">
	<a href="/logout.html">Logout</a> <c:out value="${pageContext.request.userPrincipal.name}" />
</c:if>
<div class="pageHeading">Home</div>
<ul>
<c:choose>
<c:when test="${null != pageContext.request.userPrincipal}">
	<li><a href="/projects/">Projects</a></li>
	<li><a href="/languages/">Languages</a></li>
	<li><a href="/templates/">Templates</a></li>
</c:when>
<c:otherwise>
	<li><a href="<c:out value='${loginURL}' />">Login</a></li>
</c:otherwise>
</c:choose>
</ul>
<h3>Ricotta Overview</h3>
Ricotta is a translation management tool, with complimentary build tools, to efficiently manage localised resources in your software project.
<div><img src="/static/RicottaOverview.1.png" alt="Ricotta Overview" /></div>
<div>Source repository: <a href="https://bitbucket.org/f94os/ricotta-ost">https://bitbucket.org/f94os/ricotta-ost</a></div>
<div>Contact: <a href="mailto:s.o.sandstrom@gmail.com">s.o.sandstrom@gmail.com</a</div>
<h3>Downloads</h3>
<div><a href="/static/ricotta-maven-plugin-1.0.zip">command line tool</a></div>
<div><a href="/static/ricotta-maven-plugin-1.0.jar">ricotta-maven-plugin</a></div>
<div><a href="/static/pom.xml">Example maven pom.xml</a></div>
</body>
</html>
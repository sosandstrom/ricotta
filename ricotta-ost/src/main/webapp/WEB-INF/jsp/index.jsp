<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<c:if test="${null != pageContext.request.userPrincipal}">
		<a href="/logout.html">
		  <spring:message code="ric_lnk_logout"/>
		</a>
		<c:out value="${pageContext.request.userPrincipal.name}" />
	</c:if>
<div class="pageHeading">
    <spring:message code="ric_lnk_home"/>
</div>
<ul>
<c:choose>
<c:when test="${null != pageContext.request.userPrincipal}">
	<li>
	   <a href="/proj/">
	       <spring:message code="ric_lnk_projects"/>
	   </a>
	</li>
	<li>
	   <a href="/lang/">
	       <spring:message code="ric_lnk_languages"/>
	   </a>
	</li>
	<li>
	   <a href="/templ/">
	       <spring:message code="ric_lnk_templates"/>
	   </a>
	</li>
</c:when>
<c:otherwise>
	<li><a href="<c:out value='${loginURL}' />">
	       <spring:message code="ric_lnk_login"/>
	    </a>
	</li>
</c:otherwise>
</c:choose>
</ul>
<h3><spring:message code="ric_lnk_overviews"/></h3>
<spring:message code="ric_lnk_overviews_txt"/>
<div><iframe src="http://www.facebook.com/plugins/like.php?app_id=202783656429603&amp;href=http%3A%2F%2Fricotta-ost.appspot.com%2F&amp;send=true&amp;layout=standard&amp;width=450&amp;show_faces=true&amp;action=like&amp;colorscheme=light&amp;font&amp;height=80" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:450px; height:80px;" allowTransparency="true"></iframe></div>
<div><img src="/static/RicottaOverview.2.png" alt="Ricotta Overview" /></div>
<div>Source repository: <a href="https://bitbucket.org/f94os/ricotta-ost">https://bitbucket.org/f94os/ricotta-ost</a></div>
<div>Contact: <a href="mailto:s.o.sandstrom@gmail.com">s.o.sandstrom@gmail.com</a</div>
<h3>Downloads</h3>
<div><a href="/static/ricotta-maven-plugin-7.zip">command line tool</a></div>
<div><a href="/static/ricotta-maven-plugin-15.jar">ricotta-maven-plugin</a></div>
<div><a href="/static/pom.xml">Example maven pom.xml</a></div>
</body>
</html>
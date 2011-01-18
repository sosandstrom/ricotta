<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:include page="header.jsp" />
<body>
<script type="text/javascript">
 
function all_checkboxes(id, checked, artifactKey) {
    var inputs = document.getElementsByTagName("input");
    for(var x=0; x < inputs.length; x++) {
        if (inputs[x].type == 'checkbox' && inputs[x].id == id && -1 < inputs[x].value.indexOf(artifactKey)){
            inputs[x].checked = checked;
        }
    }
}
 
</script>
<a href="/index.html">Home</a> | <a href="/projects/index.html">Projects</a> | <a href="/projects/<c:out value='${project.name}' />/index.html"><c:out value="${project.name}" /></a>
<div class="pageHeading">Project Tokens</div>
<form id="tokens" name="tokens" action="" method="post" >
<input type="submit" value="Save" />
<table>
		<thead>
		<tr>
			<th>Name</th>
			<th>Description</th>
			<th>Context</th>
			<c:forEach items="${artifacts}" var="artifact">
				<th><input type="checkbox" id="<c:out value='${artifact.keyString}' />" name="<c:out value='${artifact.keyString}' />" 
					onclick="all_checkboxes('mappings', this.checked, '<c:out value='${artifact.keyString}' />')"/><c:out value="${artifact.name}" /></th>
			</c:forEach>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${tokens}" var="token">
		<tr>
			<td><c:out value="${token.name}" /></td>
			<td><c:out value="${token.description}" /></td>
			<td>TODO</td>
			<c:forEach items="${artifacts}" var="artifact">
				<td><c:set var="key" scope="page"><c:out value='${token.keyString}' />.<c:out value='${artifact.keyString}' /></c:set>
					<input type="checkbox" id="mappings" name="mappings" value="<c:out value='${key}' />"
						<c:if test="${null != mappings[key]}">checked="checked"</c:if> />
				</td>
			</c:forEach>
		</tr>
	</c:forEach>
	</tbody>
</table>
<input type="submit" value="Save" />
</form>
</body>
</html>
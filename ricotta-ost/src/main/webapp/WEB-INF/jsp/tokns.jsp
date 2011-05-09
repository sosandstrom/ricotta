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
<a href="/index.html">Home</a> | <a href="/proj/index.html">Projects</a> | <a href="../index.html"><c:out value="${projName}" /></a>
<div class="pageHeading">Project Tokens</div>
<form id="tokens" name="tokens" action="" method="post" >
<input type="submit" name="Action" value="Delete selected tokens" />
<input type="submit" value="Save" />
<a href="create.html">Create token...</a>
<table>
		<thead>
		<tr>
			<th>Delete</th>
			<th>Name</th>
			<th>Description</th>
			<th>Context</th>
			<c:forEach items="${subsets}" var="artifact">
				<th><input type="checkbox" id="<c:out value='${artifact.keyString}' />" name="<c:out value='${artifact.keyString}' />" 
					onclick="all_checkboxes('mappings', this.checked, '<c:out value='${artifact.keyString}' />')"/><c:out value="${artifact.name}" /></th>
			</c:forEach>
		</tr>
	</thead>
	<tbody>
	<c:set var="even" scope="page" value="${true}" />
	<c:forEach items="${tokens}" var="token">
		<input type="hidden" id="id" name="id" value="<c:out value='${token.toknId}'/>" />
		<c:set var="even" scope="page" value="${!even}" />
		<tr class="evenRow<c:out value='${even}' />">
			<td><input type="checkbox" id="delete" name="delete" value="<c:out value='${token.keyString}' />" /></td>
			<td><input type="text" id="name" name="name" value="<c:out value='${token.name}'/>" /></td>
			<td><input type="text" id="description" name="description" value="<c:out value='${token.description}'/>" /></td>
			<td><select name="ctxt" id="ctxt">
				<c:forEach items="${viewContexts}" var="c">
					<option value="<c:out value='${c.key}'/>" <c:if test="${token.viewContext == c.value.primaryKey}"> selected="selected" </c:if> >
						<c:out value="${c.value.name}" /></option>
				</c:forEach>
			</select></td>
			<c:forEach items="${token.subsets}" var="subset">
				<td><input type="checkbox" id="mappings" name="mappings" value="<c:out value='${subset.keyString}' />" title="<c:out value='${token.name}' />:<c:out value='${subset.subset.name}' />"
						<c:if test="${null != mappings[subset.keyString]}">checked="checked"</c:if> />
				</td>
			</c:forEach>
		</tr>
	</c:forEach>
	</tbody>
</table>
<input type="submit" name="Action" value="Delete selected tokens" />
<input type="submit" value="Save" />
<a href="create.html">Create token...</a>
</form>
</body>
</html>
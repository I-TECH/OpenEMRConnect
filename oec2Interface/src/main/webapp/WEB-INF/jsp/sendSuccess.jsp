<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CDA Query Result</title>
</head>
<body>
    <c:if test="${not empty errors}">${errors[0]}</c:if>
    <p><a href="sentPatientId">New search</a></p>
    <ul>
        <c:forEach var="cda" items="${cdaList}">
            <li><a href="viewCda/${cda.key}">${cda.key}</a></li>
        </c:forEach>
    </ul>
</body>
</html>
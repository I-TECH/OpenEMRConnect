<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty errors}">${errors[0]}</c:if>
<c:if test="${not empty params}">${params[0]}</c:if>
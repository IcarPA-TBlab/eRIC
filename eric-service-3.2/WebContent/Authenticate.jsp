<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<html>
<head> 
</head>
<body bgcolor='#ffffff'>
    <jsp:useBean id="registryBrowser" class="it.cnr.icar.eric.client.ui.thin.RegistryBrowser" scope="session"/>
    <% registryBrowser.doClientCertAuthentication(); %>
    <c:set var="isAuthenticated" value="${registryBrowser.authenticated}"/>
    <c:choose>
      <c:when test="${isAuthenticated == false}">
        <jsp:forward page="/registry/thin/ErrorPage.jsp" />
      </c:when>
      <c:when test="${isAuthenticated == true}">
        <c:set var="redirectPage" value="${registryBrowser.redirectPage}"/>
        <jsp:forward page="${redirectPage}">
            <jsp:param name="info"  value="test" />
        </jsp:forward>
      </c:when>
      <c:otherwise>
        <jsp:forward page="/registry/thin/ErrorPage.jsp" />
      </c:otherwise>
    </c:choose>
</body>
</html>

    

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="logoView">
<%
    if (request.getParameter("info") !=null) {
%>
<h:panelGrid cellspacing="0" cellpadding="0" styleClass="MstDiv"
            width="100%" id="fillerPanel">
            <h:graphicImage url="images/a.gif" height="24" width="1"
            id="fillerLogoImg"/>
</h:panelGrid>
<%
    }
%>
<h:panelGrid id="logoPanel" style="height: 50px;" cellspacing="0" styleClass="MstDiv" cellpadding="0">
    <c:choose>
    <c:when test="${registryBrowser.logoFileDisplayed}">
        <h:graphicImage url="#{registryBrowser.logoFile}" height="75 px" width="200"
            alt="A logo image file" id="logoImg"/>
    </c:when>
    <c:otherwise>
        <h:graphicImage id="emptyLogoImg" url="/images/a.gif" height="75 px" 
            alt="A logo image file"/>
    </c:otherwise>
    </c:choose>
</h:panelGrid>
</f:subview>

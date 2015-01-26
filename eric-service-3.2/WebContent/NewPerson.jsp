<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<jsp:useBean id="userPreferencesBean" class="it.cnr.icar.eric.client.ui.thin.UserPreferencesBean" 
    scope="session" />
<f:subview id="newPersonView">

    <h:panelGrid id="newPersonPanel" columns="1" headerClass="header" 
               rowClasses="list-row-default,"
               styleClass="list-background">
        <c:if test="${userPreferencesBean.nameBeforeAddress}">
            <c:import url="/PersonName.jsp" />
        </c:if>
        <c:if test="${userPreferencesBean.nameBeforeAddress == false}">
            <c:import url="/PersonName_east.jsp" />
        </c:if>               
        <c:import url="/PostalAddresses.jsp" />
        <c:import url="/TelephoneNumbers.jsp" />
        <c:import url="/EmailAddresses.jsp" />
 
    </h:panelGrid>
    
</f:subview>

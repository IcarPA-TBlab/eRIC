<!-- This is the generic JSP for displaying a details of a drilldown page.  
     It takes RegistryObjectName as a parameter. The JSP will include a 
     specific details page using this parameter as the specific details page 
     name.  
-->

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/components" prefix="d" %>
<f:view locale="#{userPreferencesBean.uiLocale}">

<html>
  <head> 
    <title>Details Panel</title> 
    <link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/ebxml.css" %>'>
    <META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
    <META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
  </head>

    <f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
    <jsp:useBean id="roCollection" 
            class="it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean" 
            scope="session" />

    <% roCollection.setCurrentRelatedDrilldownRegistryObjectBean(); %> 

    <c:set var="result" scope="request" value="${roCollection.currentRegistryObjectBean.fields}"/>

  <body>
    <h:form id="detailWrapperForm">
      <h:panelGrid cellpadding="3" cellspacing="0" width="100%" styleClass="tabPage"
           id="topPanel">
        <h:panelGrid id="innerPanel" cellpadding="0" cellspacing="0" width="100%">
            <h:outputText id="details" value="#{bundle.detailsPanel}" styleClass="h2"/>
        </h:panelGrid>     
        <h:panelGrid id="detailWrapperStatusPanel" columns="1" width="100%" columnClasses="leftAlign">
            <h:messages id="detailWrapperMessage" globalOnly="true" />  
        </h:panelGrid>
        <c:import url="/${roCollection.currentRegistryObjectBean.detailsPageName}.jsp"/>
        <f:verbatim><br><hr></f:verbatim>
        <h:panelGrid id="detailsWrapperToolbarPanel" columns="2">
        <c:choose>
            <c:when test="${roCollection.readWritePermission == 'read'}">
                <h:commandButton id="relatedObjectClose" 
                         value="#{bundle.close}"
                         onclick="window.close()" 
                         styleClass="Btn2Mni" 
                        />
            </c:when>
            <c:otherwise>
                <h:commandButton id="relatedObjectSave" 
                                 value="#{bundle.saveButtonText}"
                                 action="#{roCollection.doSaveOnCurrentComposedROB}" 
                                 styleClass="Btn2Mni" 
                                />
                <h:commandButton id="relatedObjectClose1" 
                                 value="#{bundle.cancel}" 
                                 action="#{roCollection.doCancelSaveOnCurrentComposedROB}"
                                 styleClass="Btn2Mni" 
                                />

            </c:otherwise>
         </c:choose>   
        </h:panelGrid>
      </h:panelGrid>
    </h:form>
  </body>
</f:view> 
</html>


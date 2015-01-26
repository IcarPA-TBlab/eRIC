<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="d" uri="/components" %>

<f:subview id="publishView">
<jsp:useBean id="roCollection" 
    class="it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean" 
    scope="session" />
<f:verbatim><br></f:verbatim>
<h:form id="newROHelpForm" target="_new">
  <h:panelGrid id="newROHelpPanel" columns="2" width="100%" columnClasses="leftAlign, rightAlign">
        <h:outputText id="newROPublishOut" value="#{bundle.createNewRegistryObject}" styleClass="h2"/>
    <h:outputLink id="newROHelpLink"
        value="#{registryBrowser.publishHelp}" target="_new">
        <h:outputText id="newROHelpOut" value="#{bundle.createNewRegistryObjectHelp}"/>
    </h:outputLink>
  </h:panelGrid>
</h:form>
<p>
<c:choose>
<c:when test="${registryBrowser.sessionExpired}">
  <h:panelGrid id="pubSessionTimeoutPanel">
    <h:form id="pubSessionTimeoutForm">
      <h:outputText id="pubSessionTimoutClearResults" value="#{bundle.clearResults}"/>
      <f:verbatim><br><br></f:verbatim>
      <h:outputText id="pubSessionTimeout" value="#{bundle.sessionTimeout}"/>
    </h:form>
  </h:panelGrid>
</c:when>
<c:otherwise>
 <h:panelGrid id="toolbarDisplay">
    <h:form id="addNewRO">
        <h:panelGrid id="addNewROPanel">
        <h:outputText id="addNewROSteps" value="#{bundle.createNewRoSteps}"/>       
        <f:subview id="addNewROStepsView">
            <f:verbatim><ol></f:verbatim>
            <f:verbatim><li></f:verbatim>
            <h:outputText id="addNewROSteps1" value="#{bundle.createNewRoStep1}"/>
            <f:verbatim><li></f:verbatim>
            <h:outputText id="addNewROSteps2" value="#{bundle.createNewRoStep2}"/>
            <f:verbatim><li></f:verbatim>
            <h:outputText id="addNewROSteps3" value="#{bundle.clickApplyButtonText}"/>
            <f:verbatim></ol><p></f:verbatim>
        </f:subview>
        <c:import url="/AddNewObjectToolbar.jsp"/>
        <f:verbatim><br></f:verbatim>
        <h:panelGrid id="publishMessagePanel" columns="1" width="100%" columnClasses="leftAlign">
        <h:messages id="publishMessages" globalOnly="true" />
        <h:outputText escape="false" id="publisDirtyObjectsOut" value="#{roCollection.dirtyObjectsMessage}" />
        </h:panelGrid>
        </h:panelGrid>
    </h:form>
  </h:panelGrid>
</c:otherwise>
</c:choose>
<f:verbatim><br></f:verbatim>
<h:panelGrid id="publishDetailsPane" width="100%">
  <c:if test="${roCollection.currentDrilldownRegistryObjectBean != null}">
    <c:import url="/Details.jsp"/>
  </c:if>
</h:panelGrid>

</f:subview>

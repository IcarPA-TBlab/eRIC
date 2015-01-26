<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="classSchemeSubview">

  <h:panelGrid id="classSchemeWrapperPanel">
  
    <c:import url="RegistryObject.jsp" />
    
    <%-- 
      External checkbox. 
    --%>
    <h:panelGrid id="externalPanelWrapper" columns="2"
                 columnClasses="leftAlign160,list-row-default">
      <h:outputLabel id="externalCheckLabel" styleClass="h3" for="externalCheck" 
          value="#{bundle.isExternalClassificationSchemeLabel}#{bundle.colon}"/>
      <h:panelGrid id="externalPanel" columns="2">
        <h:selectBooleanCheckbox id="externalCheckbox" value="#{roCollection.currentRegistryObjectBean.fields.external}" disabled="true"/>
        <h:outputLabel id="externalLabel" for="externalCheckbox">
          <h:outputText id="externalOut2" value="#{bundle.isExternalClassificationSchemeLabel}"/>
        </h:outputLabel>
      </h:panelGrid>
    <%-- 
      Value Type menu
    --%>
      <h:outputLabel id="valueTypeMenuLabel" styleClass="h3" for="valueTypeMenu" value="#{bundle.valueTypeLabel}"/>
      <h:selectOneMenu id="valueTypeMenu" value="#{roCollection.currentRegistryObjectBean.fields.valueType}" disabled="false">
        <f:selectItems value="#{roCollection.valueTypes}" id="valueTypesItems"/>
      </h:selectOneMenu>
    
  </h:panelGrid>
  </h:panelGrid>

</f:subview>

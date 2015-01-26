<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:subview id="audittrailSubView">
  <!-- AuditTail attributes -->
  <h:panelGrid id="auditTrailPanel" columns="1"
               rowClasses="h3,list-row-default"
               styleClass="list-background">
    <h:outputText id="auditTrailOut" value="#{bundle.auditTrail}#{bundle.colon}"/>
    <h:dataTable columnClasses=""
                 rowClasses="list-row-default"
                 styleClass="list-background"
                 id="auditableEvents"
                 rows="20"
                 value="#{roCollection.currentRegistryObjectBean.fields.auditTrail}"
                 var="auditableEvent">
      <h:column id="auditTrailEventCol">
        <f:facet name="header">
          <h:outputLabel id="auditTrailEventMenuLabel" for="auditTrailEventMenu" 
                value="#{bundle.auditEventType}" />
        </f:facet>
        <h:selectOneMenu id="auditTrailEventMenu" 
          value="#{auditableEvent.eventType1}" disabled="true" title="#{bundle.auditEventType}">
          <f:selectItems id="auditTrailItems" value="#{roCollection.auditableEventType_SelectItems}"/>
        </h:selectOneMenu>
      </h:column>
      <h:column id="auditTrailTimeCol">
        <f:facet name="header">
          <h:outputText id="auditTrailTimeOut" value="#{bundle.auditTimestamp}"/>
        </f:facet>
        <h:outputText id="auditTrailTimeOut2" value="#{auditableEvent.timestamp}">
            <f:convertDateTime dateStyle="full" timeStyle="full"/>
        </h:outputText>
      </h:column>
      <h:column id="auditTrailUserCol">
        <f:facet name="header">
          <h:outputText id="auditTrailUserOut" value="#{bundle.auditUser}"/>
        </f:facet>
        <h:outputText id="auditTrailUserOut2" value="#{auditableEvent.user.name}"/>
      </h:column>
    </h:dataTable>
  </h:panelGrid>
</f:subview>

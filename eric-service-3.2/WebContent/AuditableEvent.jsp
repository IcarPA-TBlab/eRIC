<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:subview id="auditEvtSubview">

  <h:panelGrid id="auditEvtPanel" columns="1" headerClass="tableHeader"
               rowClasses="h3,list-row-default" styleClass="list-background">
    <h:outputText id="auditEvtIdHeaderOut" value="#{bundle.uniqueIdentifier}"/>
    <h:outputLink value="#{facesContext.externalContext.request.contextPath}/registry/http?interface=QueryManager&method=getRegistryObject&param-id=#{roCollection.currentRegistryObjectBean.fields.url_encoded_id}" 
        id="auditEvtIdLink" target="_new">
      <h:outputText id="auditEvtIdOut" value="#{roCollection.currentRegistryObjectBean.fields.id}"/>
    </h:outputLink>
    
    <h:outputLabel id="auditEvtLidInLabel" for="auditEvtLidIn" 
                value="#{bundle.logicalUniqueIdentifier}#{bundle.colon}" />
    <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.lid}" 
         id="auditEvtLidIn" disabled="true" size="70"/>
    
        <h:outputLabel id="auditEvtNameInLabel" for="auditEvtNameIn" value="#{bundle.name} (#{userPreferencesBean.contentLocaleCode})#{bundle.colon}" />
        <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.name}" 
            id="auditEvtNameIn" size="70"/>
        <h:outputLabel id="auditEvtDescInLabel" for="auditEvtDescIn" value="#{bundle.description} (#{userPreferencesBean.contentLocaleCode})#{bundle.colon}" />
        <h:inputTextarea value="#{roCollection.currentRegistryObjectBean.fields.Description}" 
            id="auditEvtDescIn" disabled="true" rows="4" cols="70" />
  </h:panelGrid>
  <f:verbatim><br></f:verbatim>
  <h:panelGrid id="aeConVerPanel" columns="2" headerClass="tableHeader"
               columnClasses="h3,list-row-default">
      <h:outputText id="auditEvtStatusCodeOut" value="#{bundle.statusLabel}#{bundle.colon}" />
      <h:outputText id="auditEvtStatusCodeValOut" 
         value="#{roCollection.currentRegistryObjectBean.fields.statusAsString}" />
      <h:outputText id="auditEvtVerOut" value="#{bundle.version}#{bundle.colon}" />
      <h:outputText value="#{roCollection.currentRegistryObjectBean.fields.versionName}" 
         id="auditEvtVerIn" />
      <h:outputText id="auditEvtComOut" value="#{bundle.versionComment}#{bundle.colon}" />
      <h:outputText value="#{roCollection.currentRegistryObjectBean.fields.comment}" 
         id="auditEvtComIn" />
         
      <h:outputText id="eventType" value="#{bundle.auditEventType}#{bundle.colon}" />
      <h:selectOneMenu id="auditTrailEvent" 
          value="#{auditableEvent.eventType}" disabled="true" title="#{bundle.auditEventType}">
          <f:selectItems id="auditTrailItem" value="#{roCollection.auditableEventTypes}"/>
      </h:selectOneMenu>
      <!--TODO: AfterServer implementation for requestId need to implement this 
        field on WebUI-->
      <h:outputText id="timestamp" value="#{bundle.auditTimestamp}#{bundle.colon}" />
      <h:inputText id="auditTrailTimestamp" value="#{roCollection.currentRegistryObjectBean.fields.timestamp}"
            disabled="true" size="60">
            <f:convertDateTime dateStyle="full" timeStyle="full"/>
      </h:inputText>
      
      <h:outputText id="user" value="#{bundle.auditUser}#{bundle.colon}" />
      <h:inputText id="auditTrailUserNameOut2" value="#{roCollection.currentRegistryObjectBean.fields.user.name}" 
            disabled="true" size="40"/>
      
      <h:outputLink id="primaryUser1"
        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
       <f:param id="idParam1" name="idValue" value="#{roCollection.currentRegistryObjectBean.fields.id}"/>
       <f:param id="drillIdParam1" name="drilldownIdValue" value="#{roCollection.currentRegistryObjectBean.fields.user.key.id}"/>
       <f:param id="readWritePermissionParam" name="readWritePermissionParam" value="read" />
       <h:outputText id="auditUserDetailsOut1" value="#{bundle.contactDetails}"/>
     </h:outputLink> 
    </h:panelGrid>
    
  <f:verbatim><br></f:verbatim>

</f:subview>

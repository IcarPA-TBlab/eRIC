<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!-- This is Registryobject JSP for displaying a details of a drilldown page
     for Registryobject.
-->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:subview id="roSubview">

  <h:panelGrid id="roIdPanelWrapper" columns="1" headerClass="tableHeader"
               rowClasses="h3,list-row-default,h3,list-row-default,h3,list-row-default,
               h3,list-row-default" styleClass="list-background">
      <h:outputLabel id="idHeaderOut" for="roIdIn" 
                value="#{bundle.uniqueIdentifier}"/>
      <h:panelGrid id="idLinkWrapper" columns="2">
        <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.id}" 
          id="roIdIn" disabled="false" rendered="#{roCollection.currentRegistryObjectBean.new}" size="70"/>

        <h:outputLink value="#{facesContext.externalContext.request.contextPath}/registry/http?interface=QueryManager&method=getRegistryObject&param-id=#{roCollection.currentRegistryObjectBean.fields.url_encoded_id}" 
        id="idLink" target="_new"  rendered="#{! roCollection.currentRegistryObjectBean.new}">
        <h:outputText id="idOut" value="#{roCollection.currentRegistryObjectBean.fields.id}"/>
        </h:outputLink>
      </h:panelGrid>
      <h:outputLabel id="roLidInLabel" for="roLidIn" 
                value="#{bundle.logicalUniqueIdentifier}#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.lid}" 
         id="roLidIn" disabled="false" size="70"/>
  
      <h:outputLabel id="nameInLabel" for="nameIn" 
                value="#{bundle.name} (#{roCollection.currentRegistryObjectBean.nameContentLocale})#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.name}" 
            id="nameIn" disabled="false" size="70"/>
      <h:outputLabel id="descInLabel" for="descIn" 
                value="#{bundle.description} (#{roCollection.currentRegistryObjectBean.descriptionContentLocale})#{bundle.colon}" />
      <h:inputTextarea value="#{roCollection.currentRegistryObjectBean.description}" 
            id="descIn" disabled="false" rows="4" cols="70" />
  </h:panelGrid>
  <f:verbatim><br></f:verbatim>
  <h:panelGrid id="roConVerPanel" columns="2" headerClass="header" 
               columnClasses="leftAlign160,list-row-default">
      <h:outputText id="statusCodeOut" styleClass="h3" value="#{bundle.statusLabel}#{bundle.colon}" />
      <h:outputText id="statusCodeValOut" 
         value="#{roCollection.currentRegistryObjectBean.fields.statusAsString}" />
      <h:outputLabel id="roConVerInLabel" styleClass="h3" for="roConVerIn" 
                value="#{bundle.version}#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.versionName}" 
         id="roConVerIn" disabled="true" size="5"/>
      <h:outputLabel id="roConComInLabel" styleClass="h3" for="roConComIn" 
                value="#{bundle.versionComment}#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.versionInfo.comment}" 
         id="roConComIn" disabled="false" size="70"/>
  </h:panelGrid>
    
  <f:verbatim><br></f:verbatim>

</f:subview>




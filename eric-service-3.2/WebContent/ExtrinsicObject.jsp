<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="extrinsicObjectSubview">

<c:import url="RegistryObject.jsp" />

  <!-- ExtrinsicObject attributes -->
    <c:set var="hasItem" value="${roCollection.currentRegistryObjectBean.fields.repositoryItemPresent && !roCollection.currentRegistryObjectBean.repositoryItemRemoved}"/>
    <c:if test="${hasItem}">
        <h:panelGrid id="exObjConVerPanel" columns="2" headerClass="tableHeader"
                   columnClasses="leftAlign160,list-row-default">
          <h:outputLabel id="exObjConVerInLabel" styleClass="h3" for="exObjConVerIn" 
                    value="#{bundle.contentVersion}#{bundle.colon}" />
          <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.contentVersionName}" 
             id="exObjConVerIn" disabled="true" size="5"/>
          <h:outputLabel id="exObjConComInLabel" styleClass="h3" for="exObjConComIn" 
                    value="#{bundle.contentVersionComment}#{bundle.colon}" />
          <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.contentVersionInfoComment}" 
             id="exObjConComIn" disabled="false" size="70"/>
        </h:panelGrid>
    </c:if>
    
    <h:panelGrid id="exObjPropsPanel" columns="2" headerClass="tableHeader"
               columnClasses="leftALign160,list-row-default">
      <h:outputLabel id="exObjMimeInLabel" styleClass="h3" for="exObjMimeIn" 
                value="#{bundle.mimeTypeLabel}#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.mimeType}" 
         id="exObjMimeIn" disabled="false" size="20"/>
      <h:outputLabel id="exObjOpaqueCheckLabel" styleClass="h3" for="exObjOpaqueCheck" 
                value="#{bundle.isOpaqueLabel}" />
      <h:selectBooleanCheckbox value="#{roCollection.currentRegistryObjectBean.fields.opaque}" 
        id="exObjOpaqueCheck" disabled="false" />

      <h:outputLabel id="exObjTypeInLabel" styleClass="h3" for="exObjTypeIn" 
                value="#{bundle.objectTypeLabel}#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.objectType.value}" 
          id="exObjTypeIn" disabled="true" size="20"/>
      <c:if test="${hasItem && !roCollection.currentRegistryObjectBean.newUpload}">
      <h:outputLabel id="exObjContentLinkLabel" styleClass="h3" for="exObjContentLink" 
                value="#{bundle.viewContent}#{bundle.colon}" />
        <h:outputLink id="exObjContentLink" value="#{facesContext.externalContext.request.contextPath}/registry/http?interface=QueryManager&method=getRepositoryItem&param-id=#{roCollection.currentRegistryObjectBean.fields.url_encoded_id}&param-lid=#{roCollection.currentRegistryObjectBean.fields.url_encoded_lid}&param-versionName=#{roCollection.currentRegistryObjectBean.fields.url_encoded_versionName}" target="_new">
              <h:outputText id="exObjContentOut" value="#{bundle.viewRepositoryItemContent}"/>
        </h:outputLink> 
      </c:if>
    </h:panelGrid>
    <f:verbatim><br></f:verbatim>
    <h:panelGrid id="exObjConceptPanel" columns="3" headerClass="tableHeader"
               rowClasses="list-row-default" styleClass="tabPage">
        <h:commandButton id="extObjClassNodeButton" 
                         value="#{bundle.selectConceptForObjectType}" 
                  immediate="true"  
                 styleClass="Btn2Mni"  
                   onclick="window.open('ExtrinsicObjectClassNodeSelector.jsp', 'ExtrinsiscObjectClassNodeSelector','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800'); return false;"/>

        <h:commandButton id="classNodeFileSelect" 
                      value="#{bundle.chooseRepositoryItemFile}"
                  immediate="true" 
                 styleClass="Btn2Mni"  
                    onclick="window.open('FileUpload.jsp','my_new_window','toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width=760, height=190, left=300,top=400'); return false;"/>
        <c:if test="${hasItem && !roCollection.currentRegistryObjectBean.newUpload}">
            <h:commandButton id="classNodeDelete" 
                         action="#{roCollection.doRemoveRepositoryItem}" 
                     styleClass="Btn2Mni"  
                          value="#{bundle.removeRepositoryItem}"/>        
        </c:if>
    </h:panelGrid>
</f:subview>

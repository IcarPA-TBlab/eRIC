<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="externalLinkSubview">

<c:import url="/RegistryObject.jsp" /> 
   
    <h:panelGrid id="exLinkPropsPanel" columns="1" headerClass="tableHeader"
               rowClasses="h3,list-row-default"
               styleClass="list-background">
        <h:outputLabel id="exLinkTypeInLabel" for="exLinkObjTypeIn" 
            value="#{bundle.objectTypeLabel}#{bundle.colon}" />
        <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.objectType.value}" 
            id="exLinkObjTypeIn" disabled="true" size="20"/>
        <h:outputLabel id="extLinkClassNodeLabel" for="extLinkClassNodeButton" 
            value="#{bundle.selectConceptForObjectType}" />
        <h:commandButton id="extLinkClassNodeButton" 
                      value="#{bundle.selectConceptForObjectType}" 
                  immediate="true"  
                 styleClass="Btn2Mni"  
                    onclick="window.open('ExtrinsicObjectClassNodeSelector.jsp', 'ExtrinsicObjectClassNodeSelector','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"/>
        <h:outputLabel id="extLinkUriInLabel" for="extLinkUriIn" 
                value="#{bundle.externalURI}#{bundle.colon}" />
        <h:inputText id="extLinkUriIn" value="#{roCollection.currentRegistryObjectBean.externalURI}" 
            disabled="false" size="100"/>
   </h:panelGrid>
        <c:if test="${roCollection.currentRegistryObjectBean.fields.externalURIPresent}">
            <f:subview id="displayContent">
                <h:outputLink id="extLinkUriLink" value="#{roCollection.currentRegistryObjectBean.externalURI}" target="_new">
                    <h:outputText id="extLinkValOut" value="#{bundle.displayContent}"/>
                </h:outputLink>
            </f:subview>
        </c:if>
</f:subview>

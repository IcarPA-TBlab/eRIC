<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    <h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
        id="memberObjectPanel">
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="0" cellspacing="0"
        id="memberObjectToolbarPanel">
        <h:commandButton value="#{bundle.addToRegistryPackage}" 
                         immediate="true"
                         id="memberObjectAddButton"
                         action="#{roCollection.cacheCurrentROId}" 
                         onclick="window.open('MemberObject.jsp', 'MemberObject','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"
                         styleClass="Btn2Mni" 
                        />
        <h:commandButton value="#{bundle.removeFromRegistryPackage}" 
                         id="memberObjectRemoveButton"
                         action="#{roCollection.doRemoveRoFromRegistryPackage}" 
                         styleClass="Btn2Mni" 
                        />
    </h:panelGrid>
    <h:dataTable id="memberObjectDataTable"
                 columnClasses=""                 
                 headerClass="list-row-first"
                 rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                 styleClass="list-background"
                 rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                 value="#{roCollection.relatedRegistryObjectBeans}"
                 var="result">
        <h:column id="memberObjectPickCol">
            <h:column id="memberObjectPickInnerCol">
                <f:facet name="header">
                    <h:outputLabel id="memberObjectCheckboxLabel" for="memberObjectPickCheckbox" 
                        value="#{bundle.pick}"/>
                </f:facet>
                <h:selectBooleanCheckbox id="memberObjectPickCheckbox" title="#{bundle.pick}"
                    value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
            </h:column>
         </h:column>
         <h:column id="memberObjectDetailsCol">
                <f:facet name="header">
                     <h:outputText id="memberObjectDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                </f:facet>
                <h:outputLink id="memberObjectDetailsLink"
                    value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                    <f:param id="memberObjectDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                    <f:param id="memberObjectIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                    <h:outputText id="memberObjectDetailsOut" value="#{bundle.details}"/>
                </h:outputLink>

        </h:column>
        <h:column id="memberObjectTypeCol">
            <f:facet name="header">
                 <h:outputText id="memberObjectTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
             </f:facet>
            <h:outputText id="memberObjectTypeOut" escape="false" value="#{result.value}"/>
        </h:column>
        <h:column id="memberObjectNameCol">
            <f:facet name="header">
                 <h:outputText id="memberObjectNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
             </f:facet>
            <h:outputText id="memberObjectNameOut" escape="false" value="#{result.value}"/>
        </h:column>
        <h:column id="memberObjectDescCol">
            <f:facet name="header">
                 <h:outputText id="memberObjectDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
             </f:facet>
            <h:outputText id="memberObjectDescOut" escape="false" value="#{result.value}"/>
        </h:column>
    </h:dataTable>
    </h:panelGrid>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
    id="postalAddressCollPanel">    
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2"
        id="postalAddressCollToolbarPanel">
        <h:commandButton value="#{bundle.addButtonText}" 
                         immediate="true"
                         onclick="window.open('NewComposedObject.jsp')"
                         id="postalAddressAddButton" 
                         styleClass="Btn2Mni" 
                         />
        <h:commandButton value="#{bundle.deleteButtonText}" 
                         action="#{roCollection.doDeleteOnCurrentComposedROB}" 
                         id="postalAddressDeleteButton" 
                         styleClass="Btn2Mni" 
                         />
    </h:panelGrid>
    <h:panelGrid id="postalAddressPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">
        <h:dataTable id="postalAddressCollDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
            <h:column id="postalAddressCollPickCol">
                <h:column id="postalAddressCollPickInnerCol">
                    <f:facet name="header">
                        <h:outputLabel id="postalAddressCollCheckboxLabel" for="postalAddressCollPickCheckbox" 
                            value="#{bundle.pick}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox id="postalAddressCollPickCheckbox" title="#{bundle.pick}"
                        value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
                </h:column>
             </h:column>
             <h:column id="postalAddressCollDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="postalAddressCollDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="postalAddressCollDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="postalAddressCollDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="postalAddressCollIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="postalAddressCollDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="postalAddressCollTypeCol">
                <f:facet name="header">
                     <h:outputText id="postalAddressCollTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="postalAddressCollTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="postalAddressCollNameCol">
                <f:facet name="header">
                     <h:outputText id="postalAddressCollNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="postalAddressCollNameOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="postalAddressCollDescCol">
                <f:facet name="header">
                     <h:outputText id="postalAddressCollDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="postalAddressCollDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</h:panelGrid>

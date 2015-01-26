<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
    id="telephoneNumberCollPanel">    
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2"
        id="telephoneNumberCollToolbarPanel">
        <h:commandButton value="#{bundle.addButtonText}" 
                         immediate="true"
                         onclick="window.open('NewComposedObject.jsp')"
                         id="telephoneNumberAddButton" 
                         styleClass="Btn2Mni" 
                         />
        <h:commandButton value="#{bundle.deleteButtonText}" 
                         action="#{roCollection.doDeleteOnCurrentComposedROB}" 
                         id="telephoneNumberDeleteButton" 
                         styleClass="Btn2Mni" 
                         />
    </h:panelGrid>
    <h:panelGrid id="telephoneNumberPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">        
        <h:dataTable id="telephoneNumberCollDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
            <h:column id="telephoneNumberCollPickCol">
                <h:column id="telephoneNumberCollPickInnerCol">
                    <f:facet name="header">
                        <h:outputLabel id="telephoneNumberCollCheckboxLabel" for="telephoneNumberCollPickCheckbox" 
                            value="#{bundle.pick}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox id="telephoneNumberCollPickCheckbox" title="#{bundle.pick}"
                        value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
                </h:column>
             </h:column>
             <h:column id="telephoneNumberCollDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="telephoneNumberCollDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="telephoneNumberCollDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="telephoneNumberCollDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="telephoneNumberCollIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="telephoneNumberCollDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="telephoneNumberCollTypeCol">
                <f:facet name="header">
                     <h:outputText id="telephoneNumberCollTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="telephoneNumberCollTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="telephoneNumberCollNameCol">
                <f:facet name="header">
                     <h:outputText id="telephoneNumberCollNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="telephoneNumberCollNameOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="telephoneNumberCollDescCol">
                <f:facet name="header">
                     <h:outputText id="telephoneNumberCollDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="telephoneNumberCollDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</h:panelGrid>

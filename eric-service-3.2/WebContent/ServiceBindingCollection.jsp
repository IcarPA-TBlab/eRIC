<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
    id="serviceBindingCollPanel">    
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2"
        id="serviceBindingCollToolbarPanel">
        <h:commandButton value="#{bundle.addButtonText}" 
                         immediate="true"
                         onclick="window.open('NewComposedObject.jsp')"
                         id="serviceBindingCollAddButton" 
                         styleClass="Btn2Mni" 
                         />
        <h:commandButton value="#{bundle.deleteButtonText}" 
                         action="#{roCollection.doDeleteOnCurrentComposedROB}" 
                         id="serviceBindingCollDeleteButton" 
                         styleClass="Btn2Mni" 
                         />
    </h:panelGrid>
    <h:panelGrid id="serviceBindingPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">
        <h:dataTable id="serviceBindingCollDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
            <h:column id="serviceBindingCollPickCol">
                <h:column id="serviceBindingCollPickInnerCol">
                    <f:facet name="header">
                        <h:outputLabel id="serviceBindingCollCheckboxLabel" for="serviceBindingCollPickCheckbox" 
                            value="#{bundle.pick}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox id="serviceBindingCollPickCheckbox" title="#{bundle.pick}"
                        value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
                </h:column>
             </h:column>
             <h:column id="serviceBindingCollDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="serviceBindingCollDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="serviceBindingCollDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="serviceBindingCollDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="serviceBindingCollIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="serviceBindingCollDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="serviceBindingCollTypeCol">
                <f:facet name="header">
                     <h:outputText id="serviceBindingCollTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="serviceBindingCollTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="serviceBindingCollNameCol">
                <f:facet name="header">
                     <h:outputText id="serviceBindingCollNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="serviceBindingCollNameOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="serviceBindingCollDescCol">
                <f:facet name="header">
                     <h:outputText id="serviceBindingCollDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="serviceBindingCollDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</h:panelGrid>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
    id="conceptPanel">    
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2"
        id="conceptToolbarPanel">
        <h:commandButton value="#{bundle.addButtonText}" 
                         immediate="true"
                         onclick="window.open('NewComposedObject.jsp')"
                         id="conceptAddButton" 
                         styleClass="Btn2Mni" 
                         />
        <h:commandButton value="#{bundle.deleteButtonText}" 
                         action="#{roCollection.doDeleteOnCurrentComposedROB}" 
                         id="conceptDeleteButton" 
                         styleClass="Btn2Mni" 
                        />
    </h:panelGrid>
    <h:panelGrid id="conceptPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">
        <h:dataTable id="conceptDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
            <h:column id="conceptPickCol">
                <h:column id="conceptPickInnerCol">
                    <f:facet name="header">
                        <h:outputLabel id="conceptCheckboxLabel" for="conceptPickCheckbox" 
                            value="#{bundle.pick}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox id="conceptPickCheckbox" title="#{bundle.pick}"
                        value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
                </h:column>
             </h:column>
             <h:column id="conceptDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="conceptDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="conceptDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="conceptDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="conceptIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="conceptDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="conceptTypeCol">
                <f:facet name="header">
                     <h:outputText id="conceptTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="conceptTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="conceptNameCol">
                <f:facet name="header">
                     <h:outputText id="conceptNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="conceptNameOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="conceptDescCol">
                <f:facet name="header">
                     <h:outputText id="conceptDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="conceptDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</h:panelGrid>

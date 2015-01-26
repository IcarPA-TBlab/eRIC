<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
    id="classificationCollPanel">
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2"
        id="classificationCollToolbarPanel">
        <h:commandButton value="#{bundle.addButtonText}" 
                         immediate="true"
                         onclick="window.open('NewComposedObject.jsp')"
                         id="classificationCollAddButton" 
                         styleClass="Btn2Mni" 
                         />
        <h:commandButton value="#{bundle.deleteButtonText}" 
                         action="#{roCollection.doDeleteOnCurrentComposedROB}" 
                         id="classificationCollDeleteButton" 
                         styleClass="Btn2Mni" 
                         />
    </h:panelGrid>
    <h:panelGrid id="classificationPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">
        <h:dataTable id="classificationCollDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
            <h:column id="classificationCollPickCol">
                <h:column id="classificationCollPickInnerCol">
                    <f:facet name="header">
                        <h:outputLabel id="classificationCollCheckboxLabel" for="classificationCollPickCheckbox" 
                            value="#{bundle.pick}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox id="classificationCollPickCheckbox" title="#{bundle.pick}"
                        value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
                </h:column>
             </h:column>
             <h:column id="classificationCollDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="classificationCollDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="classificationCollDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="classificationCollDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="classificationCollIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="classificationCollDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="classificationCollTypeCol">
                <f:facet name="header">
                     <h:outputText id="classificationCollTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="classificationCollTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="classificationCollNameCol">
                <f:facet name="header">
                     <h:outputText id="classificationCollNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="classificationCollNameOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="classificationCollDescCol">
                <f:facet name="header">
                     <h:outputText id="classificationCollDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="classificationCollDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</h:panelGrid>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
    id="externalIdentiferCollPanel">    
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2"
        id="externalIdentiferCollToolbarPanel">
        <h:commandButton value="#{bundle.addButtonText}" 
                         immediate="true"
                         onclick="window.open('NewComposedObject.jsp')"
                         id="externalIdentiferCollAddButton" 
                         styleClass="Btn2Mni" 
                         />
        <h:commandButton value="#{bundle.deleteButtonText}" 
                        action="#{roCollection.doDeleteOnCurrentComposedROB}" 
                         id="externalIdentiferCollDeleteButton" 
                         styleClass="Btn2Mni" 
                         />
    </h:panelGrid>
    <h:panelGrid id="externalIdentiferPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">
        <h:dataTable id="externalIdentiferCollDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
            <h:column id="externalIdentiferCollPickCol">
                <h:column id="externalIdentiferCollPickInnerCol">
                    <f:facet name="header">
                        <h:outputLabel id="externalIdentiferCollCheckboxLabel" for="externalIdentiferCollPickCheckbox" 
                            value="#{bundle.pick}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox id="externalIdentiferCollPickCheckbox" title="#{bundle.pick}"
                        value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
                </h:column>
             </h:column>
             <h:column id="externalIdentiferCollDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="externalIdentiferCollDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="externalIdentiferCollDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="externalIdentiferCollDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="externalIdentiferCollIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="externalIdentiferCollDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="externalIdentiferCollTypeCol">
                <f:facet name="header">
                     <h:outputText id="externalIdentiferCollTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="externalIdentiferCollTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="externalIdentiferCollNameCol">
                <f:facet name="header">
                     <h:outputText id="externalIdentiferCollNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="externalIdentiferCollNameOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="externalIdentiferCollDescCol">
                <f:facet name="header">
                     <h:outputText id="externalIdentiferCollDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="externalIdentiferCollDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</h:panelGrid>

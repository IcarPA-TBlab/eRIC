<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
        id="assocCollPanel">
    <h:panelGrid id="assocCollToolbarPanel" columnClasses="rightAlign" cellpadding="0" cellspacing="0">
        <h:commandButton id="assocCollRemoveButton" value="#{bundle.removeButtonText}" 
            action="#{roCollection.doDeleteOnCurrentComposedROB}" 
            styleClass="Btn2Mni" 
            />
    </h:panelGrid>
    <h:panelGrid id="assocPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">    
    <h:dataTable id="assocCollDataTable"
                 columnClasses=""                 
                 headerClass="list-row-first"
                 rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                 styleClass="list-background"
                 rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                 value="#{roCollection.relatedRegistryObjectBeans}"
                 var="result">
        <h:column id="assocCollPickColumn">
            <h:column id="assocCollPickInnerCol">
                <f:facet name="header">
                    <h:outputLabel id="assocCollCheckboxLabel" for="assocCollCheckbox" 
                        value="#{bundle.pick}"/>
                </f:facet>
                <h:selectBooleanCheckbox id="assocCollCheckbox" title="#{bundle.pick}"
                    value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>         
            </h:column>
         </h:column>
         <h:column id="assocCollDetailsCol">
                <f:facet name="header">
                    <h:outputText id="assocCollDetailsOutHeader" value="Details"/>
                </f:facet>
                <h:outputLink id="assocCollDetailsLink" value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                    <f:param id="accodDetailDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                    <f:param id="assocCollDetailIdParam" name="idValue" 
                        value="#{roCollection.currentRegistryObjectBean.id}"/>
                    <h:outputText id="assocCollDetailsOut" value="#{bundle.details}"/>
                </h:outputLink>
        </h:column>
        <h:column id="assocCollSourceCol">
            <f:facet name="header">
                 <h:outputText id="AssocCollourceOutHeader" value="#{bundle.sourceObject}"/>
            </f:facet>
            <h:outputLink id="AssocCollSourceLink" value="DetailsWrapper.jsp" target="_new">
                <h:outputText id="AssocCollSourceOut1" value="#{result.fields.sourceObject.objectType}" styleClass="h2" rendered="#{result.fields.sourceObject.key.id == roCollection.currentRegistryObjectBean.id}"/>
                <h:outputText id="AssocCollSourceOut2" value="#{result.fields.sourceObject.objectType}" rendered="#{result.fields.sourceObject.key.id != roCollection.currentRegistryObjectBean.id}"/>
                <f:param id="AssocCollSourceDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                <f:param id="AssocCollSourceIdParam" name="idValue" 
                    value="#{roCollection.currentRegistryObjectBean.id}"/>
            </h:outputLink>
        </h:column>
        <h:column id="assocCollSourceNameCol">
            <f:facet name="header">
                 <h:outputText id="AssocCollSourceNameOutHeader" value="#{bundle.name}"/>
            </f:facet>
            <h:outputText id="assocCollRelatedSourceNameObjValue" escape="false" value="#{result.fields.sourceObject.name.value}"/>
        </h:column>


        <h:column id="assocCollRelatedObjCol">
            <f:facet name="header">
                <h:outputText id="assocCollRelatedObjHeader" value="#{bundle.type}"/>
            </f:facet>
            <h:outputText id="assocCollRelatedObjValue" escape="false" value="#{result.fields.associationType}"/>
        </h:column>

        <h:column id="assocCollTargetCol">
            <f:facet name="header">
                <h:outputText id="AssocCollTargetOutHeader" value="#{bundle.targetObject}"/>
            </f:facet>
            <h:outputLink id="AssocCollTargetLink" value="DetailsWrapper.jsp" target="_new">
                <h:outputText id="AssocCollTargetOut1" value="#{result.fields.targetObject.objectType}" styleClass="h2" rendered="#{result.fields.targetObject.key.id == roCollection.currentRegistryObjectBean.id}"/>
                <h:outputText id="AssocCollTargetOut2" value="#{result.fields.targetObject.objectType}" rendered="#{result.fields.targetObject.key.id != roCollection.currentRegistryObjectBean.id}"/>
                <f:param id="AssocCTargetDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                <f:param id="AssocCTargetIdParam" name="idValue" 
                    value="#{roCollection.currentRegistryObjectBean.id}"/>
            </h:outputLink>
        </h:column>
        <h:column id="assocCollTargetNameCol">
            <f:facet name="header">
                 <h:outputText id="AssocCollTargetNameOutHeader" value="#{bundle.name}"/>
            </f:facet>
            <h:outputText id="assocCollRelatedTargetNameObjValue" escape="false" value="#{result.fields.targetObject.name.value}"/>
        </h:column>
    </h:dataTable>
    </h:panelGrid>
</h:panelGrid>



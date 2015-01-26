<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    <h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
        id="emailAddressCollPanel">    
    <h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2"
        id="emailAddressCollToolbarPanel">
        <h:commandButton value="#{bundle.addButtonText}" 
                         immediate="true"
                         onclick="window.open('NewComposedObject.jsp')"
                         id="emailAddressCollAddButton" 
                         styleClass="Btn2Mni" 
                         />
        <h:commandButton value="#{bundle.deleteButtonText}" 
                         action="#{roCollection.doDeleteOnCurrentComposedROB}" 
                         id="emailAddressCollDeleteButton" 
                         styleClass="Btn2Mni" 
                         />
    </h:panelGrid>
    <h:panelGrid id="emailAddressPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">
        <h:dataTable id="emailAddressCollDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
            <h:column id="emailAddressCollPickCol">
                <h:column id="emailAddressCollPickInnerCol">
                    <f:facet name="header">
                        <h:outputLabel id="emailAddressCollCheckboxLabel" for="emailAddressCollPickCheckbox" 
                            value="#{bundle.pick}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox id="emailAddressCollPickCheckbox" title="#{bundle.pick}"
                        value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>       
                </h:column>
             </h:column>
             <h:column id="emailAddressCollDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="emailAddressCollDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="emailAddressCollDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="emailAddressCollDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="emailAddressCollIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="emailAddressCollDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="emailAddressCollTypeCol">
                <f:facet name="header">
                     <h:outputText id="emailAddressCollTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="emailAddressCollTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="emailAddressCollNameCol">
                <f:facet name="header">
                     <h:outputText id="emailAddressCollNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="emailAddressCollNameOut" escape="false" value="#{bundle[result.value]}"/>
            </h:column>
            <h:column id="emailAddressCollDescCol">
                <f:facet name="header">
                     <h:outputText id="emailAddressCollDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="emailAddressCollDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</h:panelGrid>

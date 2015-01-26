<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    <h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage"
        id="affectedObjectsCollPanel">    
        <h:dataTable id="affectedObjectsCollDataTable"
                     columnClasses=""                 
                     headerClass="list-row-first"
                     rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                     styleClass="list-background"
                     rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                     value="#{roCollection.relatedRegistryObjectBeans}"
                     var="result">
             <h:column id="affectedObjectsCollDetailsCol">
                    <f:facet name="header">
                         <h:outputText id="affectedObjectsCollDetailsHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                    </f:facet>
                    <h:outputLink id="affectedObjectsCollDetailsLink"
                        value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                        <f:param id="affectedObjectsCollDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                        <f:param id="affectedObjectsCollIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                        <h:outputText id="affectedObjectsCollDetailsOut" value="#{bundle.details}"/>
                    </h:outputLink>
            </h:column>
            <h:column id="affectedObjectsCollTypeCol">
                <f:facet name="header">
                     <h:outputText id="affectedObjectsCollTypeHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="affectedObjectsCollTypeOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="affectedObjectsCollNameCol">
                <f:facet name="header">
                     <h:outputText id="affectedObjectsCollNameHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="affectedObjectsCollNameOut" escape="false" value="#{result.value}"/>
            </h:column>
            <h:column id="affectedObjectsCollDescCol">
                <f:facet name="header">
                     <h:outputText id="affectedObjectsCollDescHeaderOut" value="#{roCollection.relatedObjectHeader}"/>
                 </f:facet>
                <h:outputText id="affectedObjectsCollDescOut" escape="false" value="#{result.value}"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>

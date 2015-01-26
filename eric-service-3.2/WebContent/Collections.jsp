<!-- This is the generic JSP for displaying a colleation of drilldown pages.  
     It will show the the collections in a table for the selected tab. 
-->
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    <c:import url="ComposedObjectToolbar.jsp" />
    <h:dataTable columnClasses=""                 
                 headerClass="list-row-first"
                 rowClasses="list-row,list-row,list-row,list-row,list-row,list-row,"
                 styleClass="list-background"
                 rows="#{roCollection.numberRelatedRegistryObjectBeans}"
                 value="#{roCollection.relatedRegistryObjectBeans}"
                 var="result">
        <h:column>                      
            <h:column>
                <f:facet name="header">
                    <h:outputLabel id="composedCheckboxLabel" for="composedCheckbox" 
                        value="#{bundle.pick}"/>
                </f:facet>
                <h:selectBooleanCheckbox id="composedCheckbox" title="#{bundle.pick}"
                    value="#{roCollection.registryObjectLookup[result.id].relatedSelected}"/>
            </h:column>            
        </h:column>                 
         <h:column>
                <f:facet name="header">
                     <h:outputText value="#{roCollection.relatedObjectHeader}"/>
                </f:facet>
                <h:outputLink id="composedLink"
                    value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                    <f:param id="collDrillIdParam" name="drilldownIdValue" value="#{result.value}"/>
                    <f:param id="collIdParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.id}"/>
                    <h:outputText value="#{bundle.details}"/>
                </h:outputLink>

        </h:column>
        <h:column>
            <f:facet name="header">
                 <h:outputText value="#{roCollection.relatedObjectHeader}"/>
             </f:facet>
            <h:outputText id="composedTypeOut" escape="false" value="#{result.value}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                 <h:outputText value="#{roCollection.relatedObjectHeader}"/>
             </f:facet>
            <h:outputText id="composedNameOut" escape="false" value="#{result.value}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                 <h:outputText value="#{roCollection.relatedObjectHeader}"/>
             </f:facet>
            <h:outputText id="composedDescOut" escape="false" value="#{result.value}"/>
        </h:column>
    </h:dataTable>    

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="externalIdSubview">

<c:import url="RegistryObject.jsp" />

    <h:panelGrid id="extIdPanel" columns="2" headerClass="header" 
               columnClasses="leftAlign160,list-row-default">    
        <h:outputLabel id="extIdInLabel" styleClass="h3" for="extIdClassIn" 
                value="#{bundle.classificationScheme}#{bundle.colon}" />
       <h:inputText id="extIdClassIn" value="#{roCollection.currentRegistryObjectBean.fields.identificationScheme.name}" 
        size="42" disabled="true"/>
        <h:outputLabel id="extIdValInLabel" styleClass="h3" for="extIdClassValIn" 
                value="#{bundle.value}" />

         <c:choose>
            <c:when test="${roCollection.currentRegistryObjectBean.fields.value != null}">
                <h:inputText id="extIdClassValIn" value="#{roCollection.currentRegistryObjectBean.fields.value}" size="42" />
            </c:when>
            <c:otherwise>
                <h:inputText id="extId2ClassValIn" value="#{roCollection.currentRegistryObjectBean.fields.value}" size="42" />
            </c:otherwise>
        </c:choose>            
    </h:panelGrid> 
    <h:panelGrid id="extIdValButtonPanel">
        <h:commandButton id="extIdClassSchemeButton" 
                      value="#{bundle.selectClassificationScheme}" 
                     immediate="true" 
                 styleClass="Btn2Mni"  
                    onclick="window.open('ClassSchemeSelector.jsp', 'ClassSchemeSelector','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"/>
    </h:panelGrid>
</f:subview>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    <h:panelGrid id="newObjectPanel" bgcolor="#ffffff" width="100%" columns="3" cellspacing="0" 
        cellpadding="0">
        <h:outputLabel id="objectTypeMenuLabel" for="objectTypeMenu" 
            value="#{bundle.objectTypeLabel}: "/>
        <h:selectOneMenu 
            id="objectTypeMenu"
            value="#{roCollection.newObjectType}">
            <f:selectItems value="#{roCollection.submittableRegistryObjects}"/>
        </h:selectOneMenu>
        <h:commandButton 
            id="addButton"
            value="#{bundle.addButtonText}"
            action="#{roCollection.doAdd}"
            styleClass="Btn2Mni" 
        />
    </h:panelGrid>
 

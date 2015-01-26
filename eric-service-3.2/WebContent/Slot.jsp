<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="SlotView">
    <h:panelGrid id="slotName" columns="2" headerClass="header"
           columnClasses="rightAlign,list-row-default">
        <h:outputLabel id="slotTypeNameInLabel" for="slotTypeNameIn" value="#{bundle.name}#{bundle.colon}"/>
        <h:inputText id="slotTypeNameIn" value="#{roCollection.currentRegistryObjectBean.fields.name}" 
             size="100"/>
        <h:outputLabel id="slotTypeInLabel" for="slotTypeIn" value="#{bundle.slotType}#{bundle.colon}"/>
        <h:inputText id="slotTypeIn" value="#{roCollection.currentRegistryObjectBean.fields.slotType}" 
             size="100"/>
        <h:outputLabel id="slotValInLabel" for="slotValIn" value="#{bundle.values}"/>
        <h:inputText id="slotValIn" value="#{roCollection.currentRegistryObjectBean.slotValues}" 
                     size="100" />
    </h:panelGrid>
</f:subview>

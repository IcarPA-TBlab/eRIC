<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:subview id="associationsubview">
    <c:import url="RegistryObject.jsp" />
    <h:panelGrid id="assocPanel" columns="2" headerClass="header" columnClasses="leftAlign160,list-row-default"
                styleClass="list-background">

        <h:outputLabel styleClass="h3" id="sourceIdLabel" for="sourceIn" 
            value="#{bundle.sourceObject}" />

        <h:outputLink id="sourceLink" value="DetailsWrapper.jsp" target="_new">
            <h:outputText id="sourceOut" value="#{roCollection.currentRegistryObjectBean.fields.sourceObject.name} (#{roCollection.currentRegistryObjectBean.fields.sourceObject.objectType})"/>
            <f:param id="AssocSourceIdValParam" name="drilldownIdValue" value="#{roCollection.currentRegistryObjectBean.fields.sourceObject.key.id}"/>
            <f:param id="AssocSourceTypeParam" name="objectType" value="#{roCollection.currentRegistryObjectBean.fields.sourceObject.objectType}"/>
        </h:outputLink>
        <h:panelGrid id="confirmSourcePanel" columns="2" headerClass="header">
            <h:selectBooleanCheckbox id="confirmSourceCheck" title="#{bundle.confirmedBySourceOwner}" disabled="true"
                value="#{roCollection.currentRegistryObjectBean.fields.isConfirmedBySourceOwner}">
            </h:selectBooleanCheckbox>
            <h:outputLabel id="confirmSourceCheckLabel" for="confirmSourceCheck" 
                value="#{bundle.confirmedBySourceOwner}" />
        </h:panelGrid>
    </h:panelGrid>

      <f:verbatim><br></f:verbatim>

    <h:panelGrid id="assocPanel1" columns="2" headerClass="header" columnClasses="leftAlign160,list-row-default"
               styleClass="list-background">
        <h:outputLabel styleClass="h3" id="targetIdLabel" for="targetIn" 
            value="#{bundle.targetObject}" />

        <h:outputLink id="targetLink" value="DetailsWrapper.jsp" target="_new">
            <h:outputText id="targetOut" value="#{roCollection.currentRegistryObjectBean.fields.targetObject.name} (#{roCollection.currentRegistryObjectBean.fields.targetObject.objectType})"/>
            <f:param id="AssocTargetIdParam" name="drilldownIdValue" value="#{roCollection.currentRegistryObjectBean.fields.targetObject.key.id}"/>
            <f:param id="AssocTargetTypeParam" name="objectType" value="#{roCollection.currentRegistryObjectBean.fields.targetObject.objectType}"/>
        </h:outputLink>
        <h:panelGrid id="confirmTargetPanel" columns="2" headerClass="header">
            <h:selectBooleanCheckbox id="confirmTargetCheck" title="#{bundle.confirmedByTargetOwner}" disabled="true"
                value="#{roCollection.currentRegistryObjectBean.fields.isConfirmedByTargetOwner}">
            </h:selectBooleanCheckbox>

      
            <h:outputLabel id="confirmTargetCheckLabel" for="confirmTargetCheck" 
                value="#{bundle.confirmedByTargetOwner}" />
        </h:panelGrid>
    </h:panelGrid>

    <f:verbatim><br></f:verbatim>

    <h:panelGrid id="assocTypePanel" columns="2" headerClass="header" 
               columnClasses="leftAlign160,list-row-default">
            <h:outputLabel id="assocTypeValMenuLabel"  styleClass="h3" for="assocTypeValMenu" 
                value="#{bundle.associationType}" />
            <h:selectOneMenu id="assocTypeValMenu" value="#{roCollection.currentRegistryObjectBean.associationType}" disabled="false">
                <f:selectItems value="#{roCollection.associationTypes}" id="assocTypesItems" />
            </h:selectOneMenu>

            <h:outputLabel id="extramuralValCheckLabel"  styleClass="h3" for="extramuralValCheck" 
                value="#{bundle.extramural}" />
            <h:panelGrid id="checkBoxTable" columns="2">
            <h:selectBooleanCheckbox id="extramuralValCheck" 
                    title="#{bundle.isExtramural}" disabled="true"
                    value="#{roCollection.currentRegistryObjectBean.fields.isExtramural}">
            </h:selectBooleanCheckbox>
            <h:outputText id="extramuralValCheckOut" value="#{bundle.isExtramural}"/>
            </h:panelGrid>
    </h:panelGrid>
</f:subview>

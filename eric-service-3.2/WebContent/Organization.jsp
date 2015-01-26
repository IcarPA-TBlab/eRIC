<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="organizationSubview">
    <h:panelGrid id="registryObjectPanel" columns="1" headerClass="header" 
           rowClasses="list-row-default,"
           styleClass="list-background">

        <c:import url="RegistryObject.jsp" />
    </h:panelGrid>

<!--jsp:useBean id="roCollection" class="it.cnr.icar.eric.client.ui.thin.SearchPanelBean" scope="session" /-->
    <h:panelGrid id="orgPanel" columns="1" headerClass="header" 
           rowClasses="list-row-default,"
           styleClass="list-background">

        <c:import url="/PostalAddresses.jsp" />
        <c:import url="/TelephoneNumbers.jsp" />
        <c:import url="/EmailAddresses.jsp" />

        <h:panelGrid id="orgContactPanel" columns="1"
                     rowClasses="h3"
                     styleClass="list-background">
        <h:outputLabel id="orgContactValInLabel" for="orgContactValIn" 
                value="#{bundle.primaryContact}" />
        </h:panelGrid> 
        <h:panelGrid id="orgContactValPanel" columns="1" 
               rowClasses="list-row-default,list-row-default,"
               styleClass="list-background">
            <c:if test="${roCollection.currentRegistryObjectBean.fields.parentOrganization.key.id != null}">        
                <h:outputLabel id="organizationParentInLabel" styleClass="h3" for="organizationParentIn" 
                                value="#{bundle.parentOrganization}#{bundle.colon}" />
                <h:outputLink id="organizationParentLink" value="DetailsWrapper.jsp" target="_new">
                    <h:outputText id="organizationParentOut" value="#{roCollection.currentRegistryObjectBean.fields.id}"/>
                    <f:param id="organizationParentIdValParam" name="drilldownIdValue" value="#{roCollection.currentRegistryObjectBean.fields.parentOrganization.key.id}"/>
                    <f:param id="organizationParentTypeParam" name="objectType" value="Organization"/>
                </h:outputLink>
            </c:if>
            <c:if test="${roCollection.currentRegistryObjectBean.fields.primaryContact != null}">
                <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.primaryContact.name}" 
                               id="orgContactValIn" disabled="true" size="50"/>
                <h:outputLink id="primaryUser"
                            value="#{facesContext.externalContext.request.contextPath}/registry/thin/DetailsWrapper.jsp" target="_new">
                     <f:param id="idParam" name="idValue" value="#{roCollection.currentRegistryObjectBean.fields.id}"/>
                     <f:param id="drillIdParam" name="drilldownIdValue" value="#{roCollection.currentRegistryObjectBean.fields.primaryContact.key.id}"/>
                     <h:outputText id="orgUserDetailsOut" value="#{bundle.contactDetails}"/>
                 </h:outputLink>
            </c:if>
        </h:panelGrid>
    </h:panelGrid>
</f:subview>

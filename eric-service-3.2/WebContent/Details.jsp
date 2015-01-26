<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="d" uri="/components" %>

    <f:subview id="detailsSubview">
        <jsp:useBean id="roCollection" 
            class="it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean" 
            scope="session" />
        <% roCollection.resetToEditMode(); %> 
      <h:form id="detailsHelpForm" target="_new">
        <f:verbatim><hr><a name="detailsHere"></a></f:verbatim>
        <h:panelGrid id="detailsHelpPanel" columns="2" cellpadding="10"  width="100%"
                   rowClasses="list-row-first-default"
                   styleClass="list-backgroud"
                   columnClasses="leftAlign, rightAlign">
          <h:outputText id="detailsHelpOut" value="#{bundle.details} : #{roCollection.currentDrilldownRegistryObjectBean.fields.Name}" />
          <h:outputLink id="detailsHelpLink"
              value="#{registryBrowser.detailsHelp}" target="_new">
              <h:outputText id="detailsHelpOut2" value="#{bundle.detailsHelp}"/>
          </h:outputLink>
        </h:panelGrid>
      </h:form>
      
      <h:panelGrid cellpadding="8" cellspacing="0" width="100%" styleClass="padFrame"
        id="detailsWrapperPanel">
        <h:form id="panelForm">
              <h:panelGrid id="detailToolbar" cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage">
                <c:import url="/DetailsToolbar.jsp"/>
              </h:panelGrid>
              <h:graphicImage id="whiteSpaceImage" height="2" width="0" url="images/nothing.gif" alt=""/> 
          <d:pane_tabbed id="tabcontrol1"
                         paneClass="tabbed-pane"
                         contentClass="tabbed-content"
                         selectedClass="tabbed-selected"
                         unselectedClass="tabbed-unselected"
                         supportsROB="true">

            <d:pane_tab id="detailPane" registryObjectId="#{roCollection.currentDrilldownRegistryObjectBean.fields.key.id}"
                firstTab="true">
              <f:facet name="label">
                <d:pane_tablabel id="detailPaneLabel" label="#{roCollection.currentDrilldownRegistryObjectBean.fields.objectType} #{bundle.detail}"/>
              </f:facet>

              <h:panelGrid id="detailPanel" cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage">
                <c:import url="/${roCollection.currentDrilldownRegistryObjectBean.detailsPageName}.jsp"/>
              </h:panelGrid>
            </d:pane_tab>
           
            <d:pane_tab id="slotsPane" relationshipName="Slots"> 
                <f:facet name="label">
                  <d:pane_tablabel id="slotsPaneLabel" label="#{bundle.Slots}" />
                </f:facet>
                <c:import url="/SlotCollection.jsp" />
            </d:pane_tab>
            <d:pane_tab id="classificationsPane" relationshipName="Classifications"> 
                <f:facet name="label">
                  <d:pane_tablabel id="classificationsPaneLabel" label="#{bundle.Classifications}" />
                </f:facet>
                <c:import url="/ClassificationCollection.jsp" />
            </d:pane_tab>        
            <d:pane_tab id="externalIdsPane" relationshipName="ExternalIdentifiers"> 
                <f:facet name="label">
                  <d:pane_tablabel id="externalIdsPaneLabel" label="#{bundle.ExternalIdentifiers}" />
                </f:facet>
                <c:import url="/ExternalIdentifierCollection.jsp" />
            </d:pane_tab>
            <d:pane_tab id="associationsPane" relationshipName="Associations"> 
                <f:facet name="label">
                  <d:pane_tablabel id="associationsPaneLabel" label="#{bundle.Associations}" />
                </f:facet>
                <c:import url="/AssociationCollection.jsp" />
            </d:pane_tab>        
            <d:pane_tab id="externalLinksPane" relationshipName="ExternalLinks"> 
                <f:facet name="label">
                  <d:pane_tablabel id="externalLinksPaneLabel" label="#{bundle.ExternalLinks}" />
                </f:facet>
                <c:import url="/ExternalLinkCollection.jsp" />
            </d:pane_tab>
            <c:set var="objectType" scope="request" value="${roCollection.currentDrilldownRegistryObjectBean.registryObject.objectType.value}"/>
            <c:if test="${objectType == 'RegistryPackage'}">
                <d:pane_tab id="registryObjectsPane" relationshipName="RegistryObjects">
                    <f:facet name="label">
                      <d:pane_tablabel id="registryObjectsPaneLabel" label="#{bundle.Members}" />
                    </f:facet>
                    <c:import url="/MemberObjectCollection.jsp" />
                </d:pane_tab>
            </c:if>
            <c:if test="${objectType == 'Organization' || objectType == 'User'}">
                <d:pane_tab id="postalAddressesPane" relationshipName="PostalAddresses">
                    <f:facet name="label">
                      <d:pane_tablabel id="postalAddressesPaneLabel" label="#{bundle.PostalAddresses}" />
                    </f:facet>
                    <c:import url="/PostalAddressCollection.jsp" />
                </d:pane_tab>
                <d:pane_tab id="TelephoneNumbersPane" relationshipName="TelephoneNumbers">
                    <f:facet name="label">
                      <d:pane_tablabel id="TelephoneNumbersPaneLabel" label="#{bundle.TelephoneNumbers}" />
                    </f:facet>
                    <c:import url="/TelephoneNumberCollection.jsp" />
                </d:pane_tab>
                <d:pane_tab id="EmailAddressesPane" relationshipName="EmailAddresses">
                    <f:facet name="label">
                      <d:pane_tablabel id="EmailAddressesPaneLabel" label="#{bundle.EmailAddresses}" />
                    </f:facet>
                    <c:import url="/EmailAddressCollection.jsp" />
                </d:pane_tab>
            </c:if>
            <c:if test="${objectType == 'Organization'}">
                <d:pane_tab id="UsersPane" relationshipName="Users">
                    <f:facet name="label">
                      <d:pane_tablabel id="UsersPaneLabel" label="#{bundle.Users}" />
                    </f:facet>
                    <c:import url="/UserCollection.jsp" />
                </d:pane_tab>
                <d:pane_tab id="OrganizationsPane" relationshipName="Organizations">
                    <f:facet name="label">
                      <d:pane_tablabel id="OrganizationsPaneLabel" label="#{bundle.ChildOrganizations}" />
                    </f:facet>
                    <c:import url="/OrganizationCollection.jsp" />
                </d:pane_tab>
            </c:if>
            <c:if test="${objectType == 'Service'}">
                <d:pane_tab id="ServiceBindingsPane" relationshipName="ServiceBindings">
                    <f:facet name="label">
                      <d:pane_tablabel id="ServiceBindingsPaneLabel" label="#{bundle.ServiceBindings}" />
                    </f:facet>
                    <c:import url="/ServiceBindingCollection.jsp" />
                </d:pane_tab>
            </c:if>
            <c:if test="${objectType == 'ServiceBinding'}">
                <d:pane_tab id="SpecificationLinksPane" relationshipName="SpecificationLinks">
                    <f:facet name="label">
                      <d:pane_tablabel id="SpecificationLinksPaneLabel" label="#{bundle.SpecificationLinks}" />
                    </f:facet>
                    <c:import url="/SpecificationLinkCollection.jsp" />
                </d:pane_tab>
            </c:if>
            <c:if test="${objectType == 'ClassificationScheme' || objectType == 'ClassificationNode'}">
                <d:pane_tab id="ConceptsPane" relationshipName="Concepts">
                    <f:facet name="label">
                      <d:pane_tablabel id="ConceptsPaneLabel" label="#{bundle.Concepts}" />
                    </f:facet>
                    <c:import url="/ConceptCollection.jsp" />
                </d:pane_tab>
            </c:if>
            <c:if test="${objectType == 'AuditableEvent'}">
                <d:pane_tab id="AffectedObjectsPane" relationshipName="AffectedObjects">
                    <f:facet name="label">
                      <d:pane_tablabel id="AffectedObjectsPaneLabel" label="#{bundle.AffectedObjects}" />
                    </f:facet>
                    <c:import url="/AffectedObjectsCollection.jsp" />
                </d:pane_tab>
            </c:if>
            <d:pane_tab id="auditPane" registryObjectId="#{roCollection.currentDrilldownRegistryObjectBean.fields.key.id}">
              <f:facet name="label">
                <d:pane_tablabel id="auditPaneLabel" label="#{bundle.auditTrail}"/>
              </f:facet>

              <h:panelGrid id="auditPanel" cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage">
                <c:import url="/AuditTrail.jsp"/>
              </h:panelGrid>
            </d:pane_tab>

          </d:pane_tabbed>
      </h:form>
    </h:panelGrid>
  </f:subview> 


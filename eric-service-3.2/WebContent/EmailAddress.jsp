<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="EmailPanel">
    <h:panelGrid id="emailPanelWrapper" columns="1" headerClass="header" 
                    rowClasses="h3,list-row-default"
                    styleClass="list-background">
          <h:outputText id="emailAddressOut" value="#{bundle.emailAddress}#{bundle.colon}" />
          <h:panelGrid id="emailPanel" columns="2" columnClasses="rightAlign160, list-row-default">
              <h:outputLabel id="emailTypeMenuLabel" for="emailTypeMenu" 
                  value="#{bundle.emailAddressTypeLabel}"/>
              <h:selectOneMenu id="emailTypeMenu"
                              value="#{roCollection.currentRegistryObjectBean.fields.type}">
                  <f:selectItems id="emailTypeMenuId" value="#{roCollection.emailTypes}"/>
              </h:selectOneMenu>
              <h:outputLabel id="emailAddressInLabel" for="emailAddressIn" 
                value="#{bundle.emailAddressLabel}" />
              <h:inputText id="emailAddressIn" value="#{roCollection.currentRegistryObjectBean.fields.address}" size="80" 
                disabled="false"/>
          </h:panelGrid>
          <f:verbatim><br></f:verbatim>
    </h:panelGrid>
</f:subview>
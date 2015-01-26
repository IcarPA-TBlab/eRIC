<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PersonNameWestView">
    <h:panelGrid id="personPanelWestWrapper" columns="1" headerClass="header" 
           rowClasses="h3,list-row-default,list-row-default,"
           styleClass="list-background">
          <h:outputText id="personNameWestOut" value="#{bundle.personName}#{bundle.colon}" />
          <h:panelGrid id="personNameWestPanel" columns="2" columnClasses="rightAlign160, list-row-default" styleClass="list-background">
              <h:outputLabel id="personWestFirstNameInLabel" for="personWestFirstNameIn" 
                 value="#{bundle.firstNameLabel}" />
              <h:inputText id="personWestFirstNameIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.firstName}" disabled="false"/>
              <h:outputLabel id="personWestMiddleInLabel" for="personWestMiddleIn" 
                 value="#{bundle.middleNameLabel}" />
              <h:inputText id="personWestMiddleIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.middleName}" disabled="false"/>
              <h:outputLabel id="personWestLastInLabel" for="personWestLastIn" 
                 value="#{bundle.lastNameLabel}" />
              <h:inputText id="personWestLastIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.lastName}" disabled="false"/>
          </h:panelGrid>
      </h:panelGrid>
    </f:subview>

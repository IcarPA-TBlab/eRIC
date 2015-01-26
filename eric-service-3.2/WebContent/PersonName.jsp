<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="PersonView">

    <h:panelGrid id="personPanelWrapper" columns="1" headerClass="header" 
           rowClasses="h3,list-row-default,list-row-default,"
           styleClass="list-background">
        <h:outputText id="personNameOut" value="#{bundle.personName}#{bundle.colon}" />
          <h:panelGrid id="personNamePanel" columns="2" columnClasses="rightAlign160, list-row-default" styleClass="list-background">
                <h:outputLabel id="personFirstNameInLabel" for="personFirstNameIn" 
                   value="*#{bundle.firstNameLabel}" />
              <h:inputText id="personFirstNameIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.firstName}" disabled="false"/>
              <h:outputLabel id="personMiddleInLabel" for="personMiddleIn" 
                 value="#{bundle.middleNameLabel}" />
              <h:inputText id="personMiddleIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.middleName}" disabled="false"/>
                <h:outputLabel id="personLastInLabel" for="personLastIn" 
                   value="*#{bundle.lastNameLabel}" />
              <h:inputText id="personLastIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.lastName}" disabled="false"/>
          </h:panelGrid>
    </h:panelGrid>
</f:subview>

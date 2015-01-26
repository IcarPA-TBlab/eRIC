<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PersonNameEastView">
      <h:panelGrid id="personPanelEastWrapper" columns="1" headerClass="header" 
           rowClasses="h3,list-row-default,list-row-default,"
           styleClass="list-background">
        <h:outputText id="personNameEastOut" value="#{bundle.personName}#{bundle.colon}" />
          <h:panelGrid id="personNameEastPanel" columns="2" columnClasses="rightAlign160, list-row-default">               <h:outputLabel id="personEastLastInLabel" for="personEastLastIn" 
                 value="*#{bundle.lastNameLabel}" />
              <h:inputText id="personEastLastIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.lastName}" disabled="false"/>
              <h:outputLabel id="personFirstEastNameInLabel" for="personEastFirstEastNameIn" 
                 value="*#{bundle.firstNameLabel}" />
              <h:inputText id="personEastFirstEastNameIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.firstName}" disabled="false"/>
              <h:outputLabel id="personEastMiddleInLabel" for="personEastMiddleIn" 
                 value="#{bundle.middleNameLabel}" />
              <h:inputText id="personEastMiddleIn" 
                 value="#{roCollection.currentRegistryObjectBean.fields.personName.middleName}" disabled="false"/>
          </h:panelGrid>
      </h:panelGrid>
    </f:subview>

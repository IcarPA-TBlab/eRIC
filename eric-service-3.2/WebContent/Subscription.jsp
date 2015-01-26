<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="subscriptionSubview">

  <c:import url="RegistryObject.jsp" />
  <!-- ServiceBinding attributes -->
 
     <h:panelGrid id="subActionPanel" columns="1" headerClass="header"
                     rowClasses="h3,list-row-default,list-row-default,list-row-default,list-row-default,list-row-default,"
                     styleClass="list-background">
        <h:outputText id="subActionOut" value="#{bundle.actions}#{bundle.colon}" />
        <h:dataTable id="subActionTable" columnClasses=""
                   rowClasses="list-row-default"
                   styleClass="list-background"
                   rows="20"
                   value="#{roCollection.currentRegistryObjectBean.fields.action}"
                   var="action">
        <h:column id="subEndCol" >
          <f:facet name="header">
              <h:outputLabel id="subEndOutLabel" for="subEndOut" value="#{bundle.endPoint}"/>
          </f:facet>
          <h:inputText id="subEndOut" value="#{action.endPoint}" disabled="true"
             size="42" title="#{bundle.endPoint}"/>
        </h:column>
        <h:column id="subNotCol" >
          <f:facet name="header">
            <h:outputLabel id="subNotOutLabel" for="subNotOut" value="#{bundle.notificationOption}"/>
          </f:facet>
          <h:inputText id="subNotOut" value="#{action.notificationOption}"
              disabled="true" size="42" title="#{bundle.notificationOption}"/>
        </h:column>
      </h:dataTable>    
    </h:panelGrid>
    
    <h:panelGrid id="subTimePanel" columns="2" headerClass="header"
                     rowClasses="h3,list-row-default,list-row-default,list-row-default,list-row-default,list-row-default,"
                     styleClass="list-background">
        <h:outputLabel id="subTimeStartInLabel" for="subTimeStartIn" value="#{bundle.startDate}#{bundle.colon}"/>
        <h:outputLabel id="subTimeEndInLabel" for="subTimeEndIn" value="#{bundle.endDate}#{bundle.colon}"/>
        <h:inputText id="subTimeStartIn" value="#{roCollection.currentRegistryObjectBean.fields.startDate.time}" 
                     disabled="true" size="42">
            <f:convertDateTime dateStyle="full" timeStyle="full"/>
        </h:inputText>
        <h:inputText id="subTimeEndIn" value="#{roCollection.currentRegistryObjectBean.fields.endTime.time}" 
                     disabled="true" size="42">
            <f:convertDateTime dateStyle="full" timeStyle="full"/>
        </h:inputText>
    </h:panelGrid>
    <h:panelGrid id="subNotifyPanel" columns="1" headerClass="header"
                     rowClasses="h3,list-row-default,h3,list-row-default"
                     styleClass="list-background">
        <h:outputLabel id="subNotifySelInLabel" for="subNotifySelIn" value="#{bundle.selector}#{bundle.colon}"/>
        <h:inputText id="subNotifySelIn" size="42"
                     value="#{roCollection.currentRegistryObjectBean.fields.selector}" 
                     disabled="true"/>       
        <h:outputLabel id="subNotifyIntInLabel" for="subNotifyIntIn" value="#{bundle.notificationInterval}#{bundle.colon}"/>
        <h:inputText id="subNotifyIntIn" size="25"
                     value="#{roCollection.currentRegistryObjectBean.fields.notificationInterval}" 
                     disabled="true"/>
    </h:panelGrid>
</f:subview>

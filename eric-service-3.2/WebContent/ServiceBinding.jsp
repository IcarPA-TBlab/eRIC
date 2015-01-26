<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:subview id="servicebindingView">

    <c:import url="RegistryObject.jsp" />



   <h:panelGrid columns="3" headerClass="header"
                 columnClasses="leftAlign160,list-row-default,list-row-default"
                 id="serviceBindingAccessUriLabelPanel">
      <h:outputLabel id="sbAccessUriLabel" styleClass="h3" for="sbAccessUri" 
          value="#{bundle.accessURLLabel}#{bundle.colon}"/>
      <h:inputText id="sbAccessUri" value="#{roCollection.currentRegistryObjectBean.accessURI}" 
          size="70" disabled="false"/>
      <h:outputLink value="#{roCollection.currentRegistryObjectBean.accessURI}" target="_new" 
          id="serviceBindingAccessUriLink"> 
          <h:outputText id="serviceBindingAccessURLOut" value="#{bundle.accessURLLabel}"/>
      </h:outputLink>
   </h:panelGrid>
   <h:panelGrid columns="2" headerClass="header"
                 columnClasses="leftAlign160, list-row-default"
                 id="serviceBindingLabelPanel">

      <h:outputLabel id="sbTargetBindingLabel" styleClass="h3" for="sbTargetBinding" 
          value="#{bundle.targetBindingLabel}#{bundle.colon}"/>
      <h:inputText id="sbTargetBinding" value="#{roCollection.currentRegistryObjectBean.targetBindingForSerBinding}" 
          size="70" disabled="false"/>    
   </h:panelGrid>

</f:subview>

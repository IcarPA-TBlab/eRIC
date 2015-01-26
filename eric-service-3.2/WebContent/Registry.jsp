<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="registrySubview">
<c:import url="RegistryObject.jsp" />
 <h:panelGrid id="registryTypeObjectPanel" columns="2" headerClass="header"
                columnClasses="leftAlign160,list-row-default">
      <h:outputLabel id="organizationOperatorInLabel" styleClass="h3" for="organizationOperatorIn" 
                value="#{bundle.operatorOrganization}#{bundle.colon}" />
      <h:outputLink id="organizationOperatorLink" value="DetailsWrapper.jsp" target="_new">
            <h:outputText id="organizationOperatorOut" value="#{roCollection.currentRegistryObjectBean.operatorForRegistry}"/>
            <f:param id="organizationOperatorIdValParam" name="drilldownIdValue" value="#{roCollection.currentRegistryObjectBean.operatorForRegistry}"/>
            <f:param id="organizationOperatorTypeParam" name="objectType" value="Organization"/>
      </h:outputLink>

      <h:outputLabel id="roConOperatorInLabel" styleClass="h3" for="roConOperatorIn" 
                value="#{bundle.operator}#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.operatorForRegistry}" 
                id="roConOperatorIn" disabled="false" size="70"/>   
      <h:outputLabel id="roConSpecVerInLabel" styleClass="h3" for="roConSpecVerIn" 
                value="#{bundle.specificationVersion}#{bundle.colon}" />
      <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.specificationVersion}" 
                id="roConSpecVerIn" disabled="false" size="30"/>   
 </h:panelGrid>         
</f:subview>

  <!-- Registry attributes -->

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="specificationLinkSubview">

    <c:import url="RegistryObject.jsp" />

    <h:panelGrid id="specLinkPanel" columns="1" 
                 headerClass="header"
                 rowClasses="h3,list-row-default,h3,list-row-default,h3,list-row-default"
                 styleClass="list-background"
                 width="100%">
      <h:outputLabel id="specLinkUsageAreaLabel" for="specLinkUsageArea" value="#{bundle.usageDescription}#{bundle.colon}"/>
      <h:inputTextarea id="specLinkUsageArea" value="#{roCollection.currentRegistryObjectBean.fields.usageDescription}" 
                 disabled="false" rows="4" cols="70"/>
      <h:outputText id="specLinkParamOut" value="#{bundle.usageParameters}#{bundle.colon}"/>
        <h:inputTextarea id="specLinkusageParameters" value="#{roCollection.currentRegistryObjectBean.usageParamString}" 
                     disabled="false" rows="4" cols="70"/>   
        <h:outputLabel id="specificationObjectInLabel" for="specificationObjectIn" value="#{bundle.specificationObject}#{bundle.colon}"/>
    <h:inputText id="specificationObjectIn" value="#{roCollection.currentRegistryObjectBean.specificationObject}" disabled="false" size="70"/>
      </h:panelGrid>
</f:subview>
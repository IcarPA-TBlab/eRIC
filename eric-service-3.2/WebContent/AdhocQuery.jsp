<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="adhocQuerySubview">

  <c:import url="RegistryObject.jsp" />
 
     <h:panelGrid id="adhocQueryPanel" columns="2" headerClass="header"
                     columnClasses="leftAlign160,list-row-default">
        <h:outputLabel id="adhocQueryLabel" for="adhocQueryTypesMenu" styleClass="h3" value="#{bundle.queryType}#{bundle.colon}" />
        <h:selectOneMenu id="adhocQueryTypesMenu" value="#{roCollection.currentRegistryObjectBean.fields.type}" disabled="false">
            <f:selectItems id="adhocQueryTypesItems" value="#{roCollection.queryTypes}"/>
        </h:selectOneMenu>
    </h:panelGrid>
    <f:verbatim><br></f:verbatim>
    <h:panelGrid id="adhocQueryStringPanel" columns="1" headerClass="header"
                     rowClasses="h3,list-row-default,list-row-default,list-row-default,list-row-default,list-row-default,"
                     styleClass="list-background">
        
      <h:outputLabel id="adhocQueryStringAreaLabel" for="adhocQueryStringArea" 
          value="#{bundle.queryString}#{bundle.colon}" />
      <h:inputTextarea id="adhocQueryStringArea" 
          cols="80" rows="10" value="#{roCollection.currentRegistryObjectBean.fields.string}" disabled="false"/>
      <h:outputText id="subQueryTip" style="color: gray" value="#{bundle.subQueryTip}" />
    </h:panelGrid>
</f:subview>

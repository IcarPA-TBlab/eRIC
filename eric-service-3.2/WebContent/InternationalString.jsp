<!-- This is the Service JSP for displaying a details of a drilldown page
     for Service. It includes InternationalString JSP page. 
-->

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="iStringSubview">
  <!-- InternationString attributes -->
    <h:panelGrid id="i18nStringPanel" columns="1" headerClass="header"
       rowClasses="list-row-first-left,list-row-default,list-row-default,list-row-default,list-row-default,list-row-default,"
       styleClass="list-background">
    <h:outputText id="i18nStringOut" value="#{bundle.localizedStrings}" />
    <h:dataTable id="i18nStringTable" columnClasses=""
                 rowClasses="list-row-default"
                 styleClass="list-background"
                 rows="20"
                 value="#{roCollection.currentRegistryObjectBean.fields.param.name}"
                 var="result">
      <h:column id="i18nStringCol">
        <h:outputText id="i18nStringVal" value="#{result}"/>
      </h:column>
    </h:dataTable>

  </h:panelGrid>
</f:subview>

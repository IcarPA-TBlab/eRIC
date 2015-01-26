<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:view>
<HTML>
<HEAD>
    <link href='<%= request.getContextPath() + "/ebxml.css" %>' rel="stylesheet" type="text/css">
</HEAD>
  <BODY bgcolor="#FFFFFF">
  <f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>    
      <h:panelGrid>
        <h:outputText value="#{bundle.errorMessage}"/>
      </h:panelGrid>   
</BODY>
</HTML>
</f:view>

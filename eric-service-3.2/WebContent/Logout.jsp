<%@page contentType="text/html; charset=UTF-8" language="java" %>
<html>
  <head>
    <link href='<%= request.getContextPath() + "/ebxml.css" %>' rel="stylesheet" type="text/css">
  </head>
  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <body> 
    <f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
    <f:view>
      <h:panelGrid>
        <h:outputText value="#{bundle.logoutInstructions}"/>
        <f:verbatim>
          <form action='<%= request.getContextPath() + "/registry/thin/browser.jsp" %>'>
            <input type="submit" value="#{bundle.login}">
          </form>
        </f:verbatim>
      </h:panelGrid>
    </f:view>
  </body>
</html>


<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view locale="#{userPreferencesBean.uiLocale}">
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>

<html>
 <head>
   <meta http-equiv="Refresh" content="0;
         URL=<%= request.getContextPath()%>/registry/thin/Authenticate.jsp">
 </head>
 <body>
 <p>
 <h:outputText value="#{bundle.authenticationRedirect}"/>
 <h:outputLink id="Authenticate"
                     value="#{facesContext.externalContext.request.contextPath}/registry/thin/Authenticate.jsp">
     <h:outputText id="AuthenticateOut" value="#{bundle.clickHere}"/>
 </h:outputLink>
 </p>
 <p>
<h:outputText value="#{bundle.newRegReason}"/> 
<br><br>
<h:outputFormat escape="false" value="#{bundle.newRegRedirect1}"> 
    <f:param value="#{facesContext.externalContext.request.contextPath}/registry/thin/RegisterPage.jsp" />
</h:outputFormat>
 </p>
 <p>
<h:outputFormat escape="false" value="#{bundle.newRegRedirect2}"> 
    <f:param value="#{facesContext.externalContext.request.contextPath}/registry/thin/RegisterPage.jsp" />
</h:outputFormat>
 </p>
  <p>
<h:outputFormat escape="false" value="#{bundle.newRegRedirect3}"> 
    <f:param value="#{registryBrowser.userGuide}" />
</h:outputFormat>
 </p>
  </body>
</html>
</f:view>

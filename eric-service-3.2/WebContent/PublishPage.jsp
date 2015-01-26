<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>

<f:view locale="#{userPreferencesBean.uiLocale}">
<html>
<head> 
<title><h:outputText value="#{registryBrowser.browserTitle}"/></title> 
<link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/" %><h:outputText value="#{registryBrowser.cssFile}"/>'>
<script type='text/javascript'  src='<%= request.getContextPath() + "/browser.js" %>'></script>
<noscript>
    <h2>
        <%=WebUIResourceBundle.getInstance().getString("noscript")%>
    </h2>
</noscript>
<META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
<META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
</head>
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
<body bgcolor='#ffffff' onload="lastCall()">
    <h:panelGrid id="publishPagePanel" bgcolor="#ffffff" columns="2" cellspacing="0" cellpadding="0" 
        columnClasses="leftPanelColumn, rightPanelColumn">
        <h:panelGrid id="publishDiscPanel" bgcolor="#ffffff" width="100%" 
           cellspacing="0" cellpadding="0" rowClasses="MstDiv">
           <c:import url="/Logo.jsp"/>
           <c:import url="/DiscoveryPanel.jsp"/>
        </h:panelGrid>
        <h:panelGrid id="publishToolPanel" bgcolor="#ffffff" width="100%" 
           cellspacing="0" cellpadding="0" >
              <c:import url="/Banner.jsp"/>
              <c:import url="/Toolbar.jsp"/>
              <c:import url="/Publish.jsp"/>
        </h:panelGrid>
    </h:panelGrid>
    <c:import url="/Footer.jsp"/>
</body>
</html>
</f:view> 
    

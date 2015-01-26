<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>

<f:view>
<html>
<head> 
<title>Registry Browser Login</title> 
<link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/ebxml.css" %>'>
<script language='text/javascript'  src='<%= request.getContextPath() + "/browser.js" %>'></script>
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
<body bgcolor='#ffffff'>
    <h:panelGrid bgcolor="#ffffff" cellspacing="0" cellpadding="0" width="100%">
        <h:panelGrid bgcolor="#ffffff" width="100%" columns="2" cellspacing="0" 
            cellpadding="0" columnClasses="leftColumn, rightColumn">           
            <c:import url="/Logo.jsp"/>
            <c:import url="/Banner.jsp"/>
        </h:panelGrid>
        <f:verbatim><br><br><br><br><br><br></f:verbatim>
        
        <h:panelGrid bgcolor="#ffffff" width="100%" columns="1" cellspacing="0" 
            cellpadding="0">
            <h:outputText style="centerColumn" value="#{bundle.loginFailed}"/>
        </h:panelGrid>
        <f:verbatim><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></f:verbatim>

        <f:facet name="footer">
            <c:import url="/Footer.jsp"/>
        </f:facet>
    </h:panelGrid>
</body>
</html>
</f:view>


 

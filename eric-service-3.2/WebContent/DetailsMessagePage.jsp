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
<META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
<META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<script>
    setTimeout('reloadCall()',1000);

    function reloadCall() {
        try {
            var indexVal = opener.window.location.href.indexOf('#');
            var moveLoc = opener.window.location.href.substring(indexVal);
            if (indexVal == -1) {
                opener.window.location.href = opener.window.location.href+"?change="+(Math.random());
            } else {
                opener.window.location.href = opener.window.location.href.substring(0,indexVal)+"?change="+(Math.random())+moveLoc;
            }
        } catch (errorHandler) {
            alert("<%= WebUIResourceBundle.getInstance().getString("refreshRequest") %>");
        }
        window.close();
    }
</script>
<noscript>
    <h2>
        <%=WebUIResourceBundle.getInstance().getString("pageReload")%>        
    </h2>
</noscript>      
</head>
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
<body bgcolor='#ffffff'>
    <f:verbatim><br><br><br><br></f:verbatim>
    <h:panelGrid id="detailsMessagePanel" bgcolor="#ffffff" cellspacing="0" cellpadding="0" 
        width="100%" styleClass="tabPage">
        <f:verbatim><br><br></f:verbatim>
        <h:outputText id="detailsMessageStatus" value="#{bundle.Status}:" styleClass="h2"/>
        <f:verbatim><br></f:verbatim>
        <h:messages id="detailsMessages" globalOnly="true" />
        <f:verbatim><br><br></f:verbatim>
        <h:commandButton id="detailsMessageCancelButton" 
                         value="#{bundle.close}"
                         onclick="reloadCall()" 
                         action="showSearchResultsPage"
                         styleClass="Btn2Mni" 
                        />
    </h:panelGrid>
</body>
</html>
</f:view> 
    

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="d" uri="/components" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>

<f:view locale="#{userPreferencesBean.uiLocale}">
    <html>
        <head> 
            <title>Classification Node Selector</title> 
            <link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/" %><h:outputText value="#{registryBrowser.cssFile}"/>'>
            <script language='javascript'  src='<%= request.getContextPath() + "/browser.js" %>'></script>


            <META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
            <META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
            <META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
            <META HTTP-EQUIV="Pragma" CONTENT="no-cache">

            <SCRIPT LANGUAGE="JavaScript">
            <!--//
                function reloadCall() {
                    var Net = navigator.appName == "Netscape";
                    var IE = navigator.appName == "Microsoft Internet Explorer";
                    if(Net) {    
                        try {
                            if (opener.window.location.href.indexOf('#') == -1) {
                                opener.window.location.href = opener.window.location.href+"?time="+(new Date());
                            } else {
                                opener.window.location.reload();
                            }
                        } catch (errorHandler) {
                            alert("<%= WebUIResourceBundle.getInstance().getString("refreshRequest") %>");
                        }
                        window.close();
                    }

                    if (IE) {
                        try {
                            if (opener.document.location.href.indexOf('#') == -1) {
                                opener.document.location.href = opener.document.location.href+"?time="+(new Date());;
                            } else {
                                opener.document.location.reload();
                            }
                        } catch (errorHandler) {
                            alert("<%= WebUIResourceBundle.getInstance().getString("refreshRequest") %>");
                        }
                        window.close();
                    }
                }
            //-->
            </SCRIPT>
            <noscript>
                <h2>
                    <%=WebUIResourceBundle.getInstance().getString("noscript")%>
                </h2>
            </noscript>
        </head>
        <f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
        <body bgcolor='#ffffff'>
            <h:form id="searchClassSchemeSelectForm" >
                <h:panelGrid id="searchClassSchemeSelectTableWrapper">
                    <f:verbatim><br></f:verbatim>
                    <h:outputText id="searchClassSchemeSelectOut" value="#{bundle.selectClassificationNode}" 
                    styleClass="h2"/>
                    <f:verbatim><br></f:verbatim>
                    <h:outputText id="selectSearchClassSchemeOut" value="#{bundle.selectConceptNotes}" />
                    <f:verbatim><br></f:verbatim>
                    <h:panelGrid id="searchClassSchemeSelectorTable" styleClass="tabPage">
                        <h:inputHidden id="expandTree" value="true"/>
                        <d:searchRegistryGraphMenutree id="searchClassSchemeTreeSelector" 
                        value="#{searchPanel.classSchemeGraphBean.treeGraph}"
                        action="showMessagePage"
                        actionListener="#{searchPanel.classSchemeGraphBean.processGraphEvent}"
                        selectedValues="#{searchPanel.currentQuery.parameters.$classificationPath.listValue}"
                        styleClass="tree-control"
                        selectedClass="tree-control-selected"
                        unselectedClass="tree-control-unselected"
                        treeSelect="CN"
                        immediate="true"/>
                    </h:panelGrid>
                    <h:commandButton id="searchClassSchemeSelectOkClose" 
                                     value="#{bundle.ok}" 
                                     onclick="reloadCall()"
                                     styleClass="Btn2Mni" 
                                    />
                </h:panelGrid>
            </h:form>
        </body>
    </html>
</f:view>

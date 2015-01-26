<%@page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<jsp:useBean id="registryBrowser" class="it.cnr.icar.eric.client.ui.thin.RegistryBrowser" scope="session"/>
<f:subview id="TasksView">

    <h:form id="tasksNewPanel">
        <h:panelGrid id="otherToolsPanel" cellpadding="5"  style="height: 400px; width: 100%" columnClasses="topColumn" styleClass="DicoveryPanel">
            <h:panelGrid id="LinksWrapperPanel" width="100%" styleClass="taskPage">
                <f:verbatim><br></f:verbatim>
                <h:commandLink id="registerLink" action="#{registryBrowser.doRegister}">
                    <h:outputText id="registryOut" value="#{bundle.createUserAccount}"/> 
                </h:commandLink>
                <f:verbatim><br><br></f:verbatim>
                <h:commandLink id="publishLink" action="#{registryBrowser.doPublish}">
                    <h:outputText id="publishOut" value="#{bundle.createNewRegistryObject}"/> 
                </h:commandLink>
                <f:verbatim><br><br></f:verbatim>
                <h:commandLink id="customizeLink" action="#{registryBrowser.doCustomize}">
                    <h:outputText id="customizeOut" value="#{bundle.Customize}"/> 
                </h:commandLink>
            </h:panelGrid>

        </h:panelGrid>
    </h:form>
</f:subview>

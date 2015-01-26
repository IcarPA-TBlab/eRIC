<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="explorePanelSubview">
    <h:form id="exploreNewPanel">
        <h:panelGrid id="explorePanel" cellpadding="5"  style="height: 400px; width: 100%" columnClasses="topColumn" styleClass="DicoveryPanel">
            <h:outputLink id="exploreHelp"
                value="#{registryBrowser.exploreHelp}" target="_new"  style="white-space: nowrap;">
                <h:outputText id="exploreHelpText" value="#{bundle.help}"/>
            </h:outputLink>
            <c:import url="/ExploreSubPanel.jsp" />
            <f:verbatim><br><br><br><br><br><br><br><br><br><br><br><br></f:verbatim>
        </h:panelGrid>
    </h:form>
</f:subview>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="bannerView">
    <h:panelGrid id="bannerPanel"
        columns="2" columnClasses="leftAlign, rightAlign" style="height: 75px;" cellspacing="0" styleClass="MstDiv" cellpadding="0" width="100%" >
<%
    if (request.getParameter("info") !=null) {
%>
            <h:outputText id="redirectionMessage" 
            value="#{registryBrowser.authenticationMessage}"/>
            <f:verbatim> &nbsp; </f:verbatim>
<%
    }
%>        
        <h:outputText id="bannerTitleOut" escape="false" value="#{registryBrowser.title}" styleClass="bannerPanel-applicationTitle"/>
        <h:panelGrid id="currentUserPanel"
            columns="1" columnClasses="rightAlign, rightAlign"  cellspacing="0"  cellpadding="0" width="100%">
            <h:outputText id="bannerUserOut" styleClass="toolbarAndBannerText" value="#{bundle.currentUser}: #{registryBrowser.principalName}" />
          <h:form id="homeForm" style="white-space: nowrap;">
            <h:panelGrid id="linksPanel"
                columns="4" columnClasses="rightAlign, rightAlign"  style="margin-left: auto; margin-right: 0px; color: #ffffff" cellspacing="4"  cellpadding="4">
                    <h:commandLink id="homeLink" action="showWelcomePage"
                        actionListener="#{registryBrowser.clearSelectedTabs}">
                        <h:outputText id="homeLinkOut" value="#{bundle.Home}"/>
                    </h:commandLink>
                <c:if test="${registryBrowser.faqDisplayed}">
                    <h:outputLink id="faqLink"
                        value="#{registryBrowser.faq}" target="_new"  style="white-space: nowrap;">
                        <h:outputText id="faqOut" value="#{bundle.faq}"/>
                    </h:outputLink>
                </c:if>
                <c:if test="${registryBrowser.documentationDisplayed}">
                    <h:outputLink id="docLink" 
                        value="#{registryBrowser.documentation}" target="_new"  style="white-space: nowrap;">
                        <h:outputText id="docOut" value="#{bundle.documentation}"/>
                    </h:outputLink>
                </c:if>
                <c:if test="${registryBrowser.userGuideDisplayed}">
                    <h:outputLink id="userGuideLink" 
                        value="#{registryBrowser.userGuide}" target="_new"  style="white-space: nowrap;">
                        <h:outputText id="userGuideOut" value="#{bundle.userGuide}"/>
                    </h:outputLink>
                </c:if>
            </h:panelGrid>
          </h:form>
        </h:panelGrid>
    </h:panelGrid>
</f:subview>

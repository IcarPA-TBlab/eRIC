<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="toolbarView">
    <h:panelGrid id="toolbarTablewrapper" cellspacing="0" cellpadding="0" 
        styleClass="MstTblEnd" columnClasses="leftAlign" width="100%"
        style="text-align: left; margin-left: auto; margin-right: 0px;">
        <%-- Form for the content locale --%>
        <h:form id="toolbarForm">
            <h:commandButton id="loginButton" 
                          value="#{bundle.login}"
                         action="#{registryBrowser.doLogin}"
                       disabled="#{registryBrowser.login}"
                     styleClass="Btn1Def"
            />
            <f:verbatim>&nbsp;&nbsp;</f:verbatim>   
            <h:commandButton id="localeRefresh" value="#{bundle.resetLocale}"
                         action="#{userPreferencesBean.resetLocale}"
                     styleClass="Btn1Def"         
            />

            <f:verbatim>&nbsp;&nbsp;</f:verbatim>
            <h:commandButton id="endSessionButton" 
                          value="#{registryBrowser.logoutLabel}"
                         action="doEndSession"
                     styleClass="Btn1Def" 
            />
            <f:verbatim>&nbsp;&nbsp;</f:verbatim>
            <h:outputLabel id="versionLabel" for="versionObjectCheckbox" 
                        value="#{bundle.versioningOn}" styleClass="toolbarAndBannerText"/>
            <h:selectBooleanCheckbox id="versionObjectCheckbox" 
                                  value="#{roCollection.objectVersioned}"
                                 title="#{bundle.versioningOn}"
                              onchange="document.forms['toolbarView:toolbarForm'].submit(); return false;"
            />
            <f:verbatim>&nbsp;&nbsp;<br></f:verbatim>            
            <h:outputLabel id="localeLabel" for="contentLocaleMenu" styleClass="toolbarAndBannerText" value="#{bundle.contentLocale}#{bundle.colon}" />
            <h:selectOneMenu id="contentLocaleMenu"
                          value="#{userPreferencesBean.contentLocaleCode}"
            valueChangeListener="#{userPreferencesBean.changeContentLocaleCode}"
                       onchange="document.forms['toolbarView:toolbarForm'].submit(); return true;">
                <f:selectItems value="#{userPreferencesBean.allLocalesSelectItems}" id="allLocalesSelectItems"/>
            </h:selectOneMenu>
        </h:form>
    </h:panelGrid>
</f:subview>

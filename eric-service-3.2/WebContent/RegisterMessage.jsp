<%@ page contentType="text/html; charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <f:subview id="regMessageView">
      <h:panelGrid id="regMessagePanel"
        bgcolor="#FFFFFF" cellspacing="0" cellpadding="5" width="100%">
        <f:verbatim><br></f:verbatim>
        <h:outputText id="regHeadOut" value="#{bundle.registrationHeader}" styleClass="h2"/>
        <h:outputText id="regMsgOut" value="#{bundle.registrationInfo}" styleClass="li"/>
        <h:outputLink id="regHelpLink" value="#{registryBrowser.userRegistrationHelp}" target="_new">
          <h:outputText id="regHelpOut" value="#{bundle.userRegistrationGuide}"/>
        </h:outputLink>

        <h:outputText id="regImportKeyHelpOut" value="#{bundle.registrationBrowserImportKeyHelpText}" styleClass="li"/>
        <h:outputLink id="regHelpFirefoxLink" value="#{registryBrowser.userRegistrationHelp}" target="_new">
          <h:outputText id="regHelpFirefoxOut" value="Firefox"/>
        </h:outputLink>
        <h:outputLink id="regHelpIELink" value="#{registryBrowser.userRegistrationHelp}" target="_new">
          <h:outputText id="regHelpIEOut" value="Microsoft Internet Explorer"/>
        </h:outputLink>
        <h:outputLink id="regHelpMozillaLink" value="#{registryBrowser.userRegistrationHelp}" target="_new">
          <h:outputText id="regHelpMozillaOut" value="Mozilla"/>
        </h:outputLink>

      </h:panelGrid>

      <%--
        Messages
      --%>
      <h:panelGrid id="regWizardMsg" columns="1" cellpadding="5" width="100%"
		   columnClasses="leftAlign">
         <h:messages id="allMsg" globalOnly="true" layout="table"
		     style="color: red"/>
      </h:panelGrid>

      <%--
        Buttons
      --%>
      <h:panelGrid id="regButtonPanel" columns="1" columnClasses="rightAlign">
        <h:commandButton id="startButton"
                      value="#{bundle.startRegistrationWizardButtonText}"
                 styleClass="Btn2Mni"
                     action="#{registrationInfo.doNext}" />
      </h:panelGrid>
    </f:subview>

    <%@ page contentType="text/html; charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <f:subview id="regWizard1">
        <h:panelGrid id="regWizard1Panel"
            bgcolor="#FFFFFF" cellspacing="0" cellpadding="5">
            <f:verbatim><br></f:verbatim>
            <h:outputText id="regHead1Out" value="#{bundle.registrationWizardHeader}" styleClass="h2"/>
            <h:outputText id="regInfo1Out" value="#{bundle.registrationWizardInfo_1}" styleClass="li"/>

            <h:outputText id="regDesc1Out" value="#{bundle.registrationWizardDesc}" styleClass="li"/>

        </h:panelGrid>

        <%--
          Messages
        --%>
        <h:panelGrid id="regWizard1Msg" columns="1" cellpadding="5"
		     width="100%" columnClasses="leftAlign">
           <h:messages id="allMsg1" globalOnly="true" layout="table"
		       style="color: red"/>
        </h:panelGrid>

        <%--
          Buttons
        --%>
        <h:panelGrid  id="regWizard1Buttons" columns="2" columnClasses="leftAlign, rightAlign">
          <h:commandButton id="cancelReg1Button"
                    immediate="true"
                        value="#{bundle.cancel}"
                   styleClass="Btn2Mni"
                       action="#{registrationInfo.doCancel}"/>
          <h:commandButton id="nextReg1Button"
                    immediate="true"
                        value="#{bundle.next}"
                   styleClass="Btn2Mni"
                       action="#{registrationInfo.doNext}" />
        </h:panelGrid>
    </f:subview>

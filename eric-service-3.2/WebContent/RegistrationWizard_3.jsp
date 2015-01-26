    <%@ page contentType="text/html; charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <f:subview id="regWizard3">
        <h:panelGrid id="regWizard3Panel"
            bgcolor="#FFFFFF" cellspacing="0" cellpadding="5">
            <f:verbatim><br></f:verbatim>
            <h:outputText id="regHead3Out" value="#{bundle.registrationWizardHeader}" styleClass="h2"/>
            <h:outputText id="regMsg3Out" value="#{bundle.registrationWizardInfo_3}" styleClass="li"/>
            <h:outputText id="regMsgRequiredInfo_3" value="#{bundle.requiredInfoMsg}" style="font-size: 10px"/>
        </h:panelGrid>

        <h:panelGrid id="regAuthDetails" columns="1">
            <%-- Extension point: give a custom extension of RegistrationAuth. --%>
            <c:import url="/RegistrationAuth.jsp" />
        </h:panelGrid>

        <%--
          Messages
        --%>
        <h:panelGrid id="regWizard3Msg" columns="1" cellpadding="5"
		     width="100%" columnClasses="leftAlign">
           <h:messages id="allMsg3" globalOnly="true" layout="table"
		       style="color: red"/>
        </h:panelGrid>

        <%--
          Buttons
        --%>
        <h:panelGrid id="regWizard3Buttons" columns="2" columnClasses="leftAlign, rightAlign">
          <h:panelGrid id="regWizardCC3Panel" columns="2" columnClasses="leftAlign, leftAlign">
            <h:commandButton id="clearReg3Button"
                          value="#{bundle.clearButtonText}"
                     styleClass="Btn2Mni"
                         action="#{registrationInfo.doClearAuth}" />
            <h:commandButton id="cancelReg3Button"
                      immediate="true"
                          value="#{bundle.cancel}"
                     styleClass="Btn2Mni"
                         action="#{registrationInfo.doCancel}" />
          </h:panelGrid>
          <h:panelGrid id="regWizardPN3Panel" columns="2" columnClasses="rightAlign, rightAlign">
            <h:commandButton id="prevReg3Button"
                      immediate="true"
                          value="#{bundle.previous}"
                     styleClass="Btn2Mni"
                         action="#{registrationInfo.doPrev}" />
            <h:commandButton id="nextReg3Button"
                          value="#{bundle.next}"
                     styleClass="Btn2Mni"
                         action="#{registrationInfo.doRegister}" />
          </h:panelGrid>
        </h:panelGrid>
    </f:subview>

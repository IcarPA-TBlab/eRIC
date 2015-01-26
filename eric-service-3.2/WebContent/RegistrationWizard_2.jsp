    <%@ page contentType="text/html; charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <f:subview id="regWizard2">
        <h:panelGrid id="regWizard2Panel"
            bgcolor="#FFFFFF" cellspacing="0" cellpadding="5">
            <f:verbatim><br></f:verbatim>
            <h:outputText id="regHead2Out" value="#{bundle.registrationWizardHeader}" styleClass="h2"/>
            <h:outputText id="regMsg2Out" value="#{bundle.registrationWizardInfo_2}" styleClass="li"/>
            <h:outputText id="regMsgRequiredInfo_2" value="#{bundle.requiredInfoMsg}" style="font-size: 10px"/> 
        </h:panelGrid>

        <h:panelGrid id="regUserDetails" columns="1">
            <c:import url="/NewUser.jsp" />
        </h:panelGrid>

        <%--
          Messages
        --%>
        <h:panelGrid id="regWizard2Msg" columns="1" cellpadding="5"
		     width="100%" columnClasses="leftAlign">
           <h:messages id="allMsg2" globalOnly="true" layout="table"
		       style="color: red"/>
        </h:panelGrid>

        <%--
          Buttons
        --%>
        <h:panelGrid id="regWizard2Buttons" columns="2" columnClasses="leftAlign, rightAlign">
          <h:panelGrid id="regWizardCC2Panel" columns="2" columnClasses="leftAlign, leftAlign">
            <h:commandButton id="clearReg2Button"
                     styleClass="Btn2Mni"
                          value="#{bundle.clearButtonText}"
                         action="#{registrationInfo.doClearUser}" />
            <h:commandButton id="cancelReg2Button"
                      immediate="true"
                          value="#{bundle.cancel}"
                     styleClass="Btn2Mni"
                         action="#{registrationInfo.doCancel}" />
          </h:panelGrid>
          <h:panelGrid id="regWizardPN2Panel" columns="2" columnClasses="rightAlign, rightAlign">
            <h:commandButton id="prevReg2Button"
                      immediate="true"
                          value="#{bundle.previous}"
                     styleClass="Btn2Mni"
                         action="#{registrationInfo.doPrev}" />
            <h:commandButton id="nextReg2Button"
                          value="#{bundle.next}"
                     styleClass="Btn2Mni"
                         action="#{registrationInfo.doCheckUserDetails}" />
          </h:panelGrid>
       </h:panelGrid>

    </f:subview>

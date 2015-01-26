<%@ page contentType="text/html; charset=UTF-8" language="java" %>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <f:subview id="welcomeMessageView">
        <f:verbatim><br></f:verbatim>
        <h:messages id="welMessages" globalOnly="true" styleClass="h2"/>
        <h:panelGrid id="welcomeMessagePanel" 
            bgcolor="#FFFFFF" cellspacing="0" cellpadding="5" width="100%">
        <f:verbatim><br></f:verbatim>
        <h:outputText id="welMsgOut" value="#{bundle.welcomeMessage}" styleClass="h2"/>
        <f:verbatim><p></f:verbatim>
        <h:outputText id="welMsgInfo1Out" value="#{bundle.registryInfo1}" />
        <f:verbatim><br></f:verbatim>
        <h:outputText id="welMsgObjOut" value="#{bundle.Objectives}" styleClass="h2"/>
        <f:subview id="objectives">
        <f:verbatim><ol></f:verbatim>
        <f:verbatim><li></f:verbatim>
        <h:outputText id="welMsgObj1Out" value="#{bundle.objectives1}" styleClass="li"/> 
        <f:verbatim><li></f:verbatim>
        <h:outputText id="welMsgObj2Out" value="#{bundle.objectives2}" styleClass="li"/>
        <f:verbatim></ol><br></f:verbatim>
        </f:subview>
        <h:outputText id="welMsgStartOut" value="#{bundle.gettingStarted}" styleClass="h2"/>
        <f:verbatim><br></f:verbatim>
        <h:graphicImage alt="A picture of a Registry expert helping a new persion" id="welMsgStartImg" url="images/browsing_user.jpg"/>
        <h:outputText id="welMsgStart1Out" value="#{bundle.gettingStarted1}"/>
        <h:outputText id="welMsgUserGuideOut" value="#{bundle.clickOnUserGuideLink}"/>
        </h:panelGrid>
    </f:subview>

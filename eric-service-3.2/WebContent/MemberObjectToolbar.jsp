<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="0" cellspacing="0"
    id="hasMemberPanel">
    <h:commandButton value="#{bundle.addToRegistryPackage}" 
                     immediate="true"
                     id="hasMemberAddButton"
                     onclick="window.open('MemberObject.jsp', 'MemberObject','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"
                     styleClass="Btn2Mni" 
                    />
    <h:commandButton value="#{bundle.removeFromRegistryPackage}" 
                     id="hasMemberRemoveFromRPButton"
                     action="#{roCollection.doRemoveRoFromRegistryPackage}" 
                     styleClass="Btn2Mni" 
                    />
</h:panelGrid>

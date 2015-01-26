<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    <f:subview id="messageView">
        <f:verbatim><br><br><br><br></f:verbatim>
        <h:panelGrid id="messagePanel" bgcolor="#ffffff" cellspacing="0" cellpadding="0" 
            width="100%">
            <f:verbatim><br><br></f:verbatim>
            <f:verbatim><br></f:verbatim>
            <h:messages id="messageMessages" globalOnly="true" styleClass="h2"/>
        </h:panelGrid>
    </f:subview>
    

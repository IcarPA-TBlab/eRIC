<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="footerView">
  <h:panelGrid id="footerPanel" cellspacing="0" cellpadding="2" 
    columns="1" width="100%" columnClasses="centerColumn,centerColumn" rowClasses="centerColumn">
        <f:verbatim><hr></f:verbatim>

        <h:outputText escape="false" id="footerCopyrightOut" value="#{registryBrowser.companyCopyright}"/>
  </h:panelGrid>
</f:subview>

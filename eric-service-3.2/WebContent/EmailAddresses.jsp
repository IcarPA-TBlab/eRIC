<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="EmailAddressesPanel">

    <h:panelGrid id="emailAddressesPanel" columns="1" headerClass="header" 
           rowClasses="h3,list-row-default,list-row-default,"
           styleClass="list-background">
        <h:outputText id="emailAddressesOut" value="#{bundle.emailAddresses}" />
        <h:dataTable id="emailAddressTable"
            columnClasses=""
            rowClasses="list-row-default"
            styleClass="list-background"
            rows="20"
            value="#{roCollection.currentRegistryObjectBean.fields.emailAddresses}"
            var="result">
            <h:column id="emailAddressCol">
                <c:import url="/EmailAddressRow.jsp"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</f:subview>

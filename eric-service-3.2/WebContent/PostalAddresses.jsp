<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="PostalAddressesView">

    <h:panelGrid id="postalAddresses" columns="1" headerClass="tableHeader" 
           rowClasses="h3,list-row-default,list-row-default,"
           styleClass="list-background">
        <h:outputText id="postalAddressesOut" value="#{bundle.postalAddresses}" />
        <h:dataTable id="postalAddressesTable"
            rowClasses="list-row-default"
            styleClass="list-background"
            rows="20"
            value="#{roCollection.currentRegistryObjectBean.fields.postalAddresses}"
            var="result">
            <h:column id="postalAddressesCol" >
                <c:import url="/PostalAddressRow.jsp"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</f:subview>

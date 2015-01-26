<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>


    <f:subview id="PostalView_es">        
        <h:panelGrid id="PostalAddressWrapper_es" columns="1" headerClass="header" 
            rowClasses="h3,list-row-default"
            styleClass="list-background">
            <h:outputText id="postalAddressOut_es" value="#{bundle.postalAddress}" />

            <h:panelGrid id="PostalAddressPanel_es" columns="2" columnClasses="rightAlign160, list-row-default">
                    <h:outputLabel id="PostalStreetInLabel_es" for="PostalStreetIn_es" value="#{bundle.streetLabel}" />
                    <h:inputText id="PostalStreetIn_es" value="#{roCollection.currentRegistryObjectBean.fields.street}" disabled="false"/>
                    <h:outputLabel id="PostalNumInLabel_es" for="PostalNumIn_es" value="#{bundle.streetNumberLabel}" />
                    <h:inputText id="PostalNumIn_es" value="#{roCollection.currentRegistryObjectBean.fields.streetNumber}" disabled="false"/>
                    <h:outputLabel id="PostalCodeInLabel_es" for="PostalCodeIn_es" value="#{bundle.postalCodeLabel}" />
                    <h:inputText id="PostalCodeIn_es" value="#{roCollection.currentRegistryObjectBean.fields.postalCode}" disabled="false"/>
                    <h:message id="PostalCityIn_es_message" for="PostalCityIn_es" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                    <h:outputLabel id="PostalCityInLabel_es" for="PostalCityIn_es" value="#{bundle.cityLabel}" />
                    <h:inputText id="PostalCityIn_es" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{roCollection.currentRegistryObjectBean.fields.city}" disabled="false" required="false"/>
                    <h:message id="PostalCountryInRow_es_message" for="PostalCountryIn_es" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                <h:outputLabel id="PostalCountryInLabel_es" for="PostalCountryIn_es" value="#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryIn_es" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{roCollection.currentRegistryObjectBean.fields.country}" disabled="false" required="false"/>
            </h:panelGrid>
            <f:verbatim><br></f:verbatim>
        </h:panelGrid>
    </f:subview>

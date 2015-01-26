<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalView_fr">        
        <h:panelGrid id="PostalAddressWrapper_fr" columns="1" headerClass="header" 
            rowClasses="h3,list-row-default"
            styleClass="list-background">
            <h:outputText id="postalAddressOut_fr" value="#{bundle.postalAddress}" />

            <h:panelGrid id="PostalAddressPanel_fr" columns="2" columnClasses="rightAlign160, list-row-default">
                    <h:outputLabel id="PostalNumInLabel_fr" for="PostalNumIn_fr" value="#{bundle.streetNumberLabel}" />
                    <h:inputText id="PostalNumIn_fr" value="#{roCollection.currentRegistryObjectBean.fields.streetNumber}" disabled="false"/>
                    <h:outputLabel id="PostalStreetInLabel_fr" for="PostalStreetIn_fr" value="#{bundle.streetLabel}" />
                    <h:inputText id="PostalStreetIn_fr" value="#{roCollection.currentRegistryObjectBean.fields.street}" disabled="false"/>
                    <h:outputLabel id="PostalCodeInLabel_fr" for="PostalCodeIn_fr" value="#{bundle.postalCodeLabel}" />
                    <h:inputText id="PostalCodeIn_fr" value="#{roCollection.currentRegistryObjectBean.fields.postalCode}" disabled="false"/>
                    <h:message id="PostalCityIn_fr_message" for="PostalCityIn_fr" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                    <h:outputLabel id="PostalCityInLabel_fr" for="PostalCityIn_fr" value="#{bundle.cityLabel}" />
                    <h:inputText id="PostalCityIn_fr" value="#{roCollection.currentRegistryObjectBean.fields.city}" disabled="false" required="false"/>
                    <h:message id="PostalCountryInRow_fr_message" for="PostalCountryIn_fr" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                <h:outputLabel id="PostalCountryInLabel_fr" for="PostalCountryIn_fr" value="#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryIn_fr" value="#{roCollection.currentRegistryObjectBean.fields.country}" disabled="false" required="false"/>
            </h:panelGrid>
            <f:verbatim><br></f:verbatim>
        </h:panelGrid>
    </f:subview>


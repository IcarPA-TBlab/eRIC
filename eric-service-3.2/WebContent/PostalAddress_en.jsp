<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>


    <f:subview id="PostalView_en">        
        <h:panelGrid id="PostalAddressWrapper_en" columns="1" headerClass="header" 
            rowClasses="h3,list-row-default"
            styleClass="list-background">
            <h:outputText id="postalAddressesOut_en" value="#{bundle.postalAddress}" />

            <h:panelGrid id="PostalAddressPanel_en" columns="2" columnClasses="rightAlign160, list-row-default">
                    <h:outputLabel id="PostalNumInLabel_en" for="PostalNumIn_en" value="#{bundle.streetNumberLabel}" />
                    <h:inputText id="PostalNumIn_en" value="#{roCollection.currentRegistryObjectBean.fields.streetNumber}" disabled="false"/>
                    <h:outputLabel id="PostalStreetInLabel_en" for="PostalStreetIn_en" value="#{bundle.streetLabel}" />
                    <h:inputText id="PostalStreetIn_en" value="#{roCollection.currentRegistryObjectBean.fields.street}" disabled="false"/>
                    <h:message id="PostalCityIn_en_message" for="PostalCityIn_en" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                    <h:outputLabel id="PostalCityInLabel_en" for="PostalCityIn_en" value="#{bundle.cityLabel}" />
                    <h:inputText id="PostalCityIn_en" value="#{roCollection.currentRegistryObjectBean.fields.city}" disabled="false" required="false"/>
                    <h:message id="PostalStateIn_en_message" for="PostalStateIn_en" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                    <h:outputLabel id="PostalStateInLabel_en" for="PostalStateIn_en" value="#{bundle.stateOrProvinceLabel}" />
                    <h:inputText id="PostalStateIn_en" value="#{roCollection.currentRegistryObjectBean.fields.stateOrProvince}" disabled="false" required="false"/>
                <h:outputLabel id="PostalCodeInLabel_en" for="PostalCodeIn_en" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeIn_en" value="#{roCollection.currentRegistryObjectBean.fields.postalCode}" disabled="false"/>
                <h:message id="PostalCountryIn_en_message" for="PostalCountryIn_en" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                <h:outputLabel id="PostalCountryInLabel_en" for="PostalCountryIn_en" value="#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryIn_en" value="#{roCollection.currentRegistryObjectBean.fields.country}" disabled="false" required="false"/>
            </h:panelGrid>
            <f:verbatim><br></f:verbatim>
        </h:panelGrid>
    </f:subview>

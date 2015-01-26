<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalView_zh">        
        <h:panelGrid id="PostalAddressWrapper_zh" columns="1" headerClass="header" 
            rowClasses="h3,list-row-default"
            styleClass="list-background">
            <h:outputText id="postalAddressesOut_zh" value="#{bundle.postalAddress}" />
            <h:panelGrid id="PostalCountryPanel_zh" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:message id="PostalCountryIn_zh_message" for="PostalCountryIn_zh" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCountryInLabel_zh" for="PostalCountryIn_zh" value="#{bundle.countryCodeLabel}" />
                    <h:inputText id="PostalCountryIn_zh" value="#{roCollection.currentRegistryObjectBean.fields.country}" disabled="false" required="false"/>
                <h:message id="PostalStateIn_zh_message" for="PostalStateIn_zh" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalStateInLabel_zh" for="PostalStateIn_zh" value="#{bundle.stateOrProvinceLabel}" />
                <h:inputText id="PostalStateIn_zh" value="#{roCollection.currentRegistryObjectBean.fields.stateOrProvince}" disabled="false" required="false"/>
                <h:message id="PostalCityIn_zh_message" for="PostalCityIn_zh" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCityInLabel_zh" for="PostalCityIn_zh" value="#{bundle.cityLabel}" />
                <h:inputText id="PostalCityIn_zh" value="#{roCollection.currentRegistryObjectBean.fields.city}" disabled="false" required="false"/>
                <h:outputLabel id="PostalStreetInLabel_zh" for="PostalStreetIn_zh" value="#{bundle.streetLabel}" />
                <h:inputText id="PostalStreetIn_zh" value="#{roCollection.currentRegistryObjectBean.fields.street}" disabled="false"/>
                <h:outputLabel id="PostalNumInLabel_zh" for="PostalNumIn_zh" value="#{bundle.streetNumberLabel}" />
                <h:inputText id="PostalNumIn_zh" value="#{roCollection.currentRegistryObjectBean.fields.streetNumber}" disabled="false"/>
                <h:outputLabel id="PostalCodeInLabel_zh" for="PostalCodeIn_zh" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeIn_zh" value="#{roCollection.currentRegistryObjectBean.fields.postalCode}" disabled="false"/>
            </h:panelGrid>
            <f:verbatim><br></f:verbatim>
        </h:panelGrid>
    </f:subview>

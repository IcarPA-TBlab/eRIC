<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalViewRow_zh">        
        <h:panelGrid id="PostalPanelWrapperRow_zh" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:message id="PostalCountryInRow_zh_message" for="PostalCountryInRow_zh" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCountryInLabelRow_zh" for="PostalCountryInRow_zh" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryInRow_zh" value="#{result.country}" maxlength="2" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:message id="PostalStateInRow_zh_message" for="PostalStateInRow_zh" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalStateInLabelRow_zh" for="PostalStateInRow_zh" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.stateOrProvinceLabel}" />
                <h:inputText id="PostalStateInRow_zh" value="#{result.stateOrProvince}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:message id="PostalCityInRow_zh_message" for="PostalCityInRow_zh" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCityInLabelRow_zh" for="PostalCityInRow_zh" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.cityLabel}" />
                <h:inputText id="PostalCityInRow_zh" value="#{result.city}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:outputLabel id="PostalStreetInLabelRow_zh" for="PostalStreetInRow_zh" value="#{bundle.streetLabel}" />
                <h:inputText id="PostalStreetInRow_zh" value="#{result.street}" disabled="false"/>
                <h:outputLabel id="PostalNumInLabelRow_zh" for="PostalNumInRow_zh" value="#{bundle.streetNumberLabel}" />         
                <h:inputText id="PostalNumInRow_zh" value="#{result.streetNumber}" disabled="false"/>
                <h:outputLabel id="PostalCodeInLabelRow_zh" for="PostalCodeInRow_zh" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeInRow_zh" value="#{result.postalCode}" disabled="false"/>

        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
    </f:subview>


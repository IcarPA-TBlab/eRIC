<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalViewRow_ko">        
        <h:panelGrid id="PostalPanelWrapperRow_ko" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:message id="PostalCountryInRow_ko_message" for="PostalCountryInLabelRow_ko" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCountryInLabelRow_ko" for="PostalCountryInRow_ko" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryInRow_ko" value="#{result.country}" maxlength="2" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:outputLabel id="PostalCodeInLabelRow_ko" for="PostalCodeInRow_ko" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeInRow_ko" value="#{result.postalCode}" disabled="false"/>
                <h:message id="PostalStateInRow_ko_message" for="PostalStateInRow_ko" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalStateInLabelRow_ko" for="PostalStateInRow_ko" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.stateOrProvinceLabel}" />
                <h:inputText id="PostalStateInRow_ko" value="#{result.stateOrProvince}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:message id="PostalCityInRow_ko_message" for="PostalCityInRow_ko" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>                
                <h:outputLabel id="PostalCityInLabelRow_ko" for="PostalCityInRow_ko" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.cityLabel}" />
                <h:inputText id="PostalCityInRow_ko" value="#{result.city}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:outputLabel id="PostalStreetInLabelRow_ko" for="PostalStreetInRow_ko" value="#{bundle.streetLabel}" />
                <h:inputText id="PostalStreetInRow_ko" value="#{result.street}" disabled="false"/>
                <h:outputLabel id="PostalNumInLabelRow_ko" for="PostalNumInRow_ko" value="#{bundle.streetNumberLabel}" />         
                <h:inputText id="PostalNumInRow_ko" value="#{result.streetNumber}" disabled="false"/>
            
        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
    </f:subview>

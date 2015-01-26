<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>


    <f:subview id="PostalViewRow_de">        
        <h:panelGrid id="PostalPanelWrapperRow_de" columns="2" columnClasses="rightAlign160, list-row-default">

            <h:outputLabel id="PostalStreetInLabelRow_de" for="PostalStreetInRow_de" value="#{bundle.streetLabel}" />
            <h:inputText id="PostalStreetInRow_de" value="#{result.street}" disabled="false"/>
            <h:outputLabel id="PostalNumInLabelRow_de" for="PostalNumInRow_de" value="#{bundle.streetNumberLabel}" />         
             <h:inputText id="PostalNumInRow_de" value="#{result.streetNumber}" disabled="false"/>
             <h:outputLabel id="PostalCodeInLabelRow_de" for="PostalCodeInRow_de" value="#{bundle.postalCodeLabel}" />
             <h:inputText id="PostalCodeInRow_de" value="#{result.postalCode}" disabled="false"/>
             <h:message id="PostalCityInRow_de_message" for="PostalCityInRow_de" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
             <h:outputLabel id="PostalCityInLabelRow_de" for="PostalCityInRow_de" 
                            value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.cityLabel}" 
                            required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
             <h:inputText id="PostalCityInRow_de" value="#{result.city}" disabled="false"/>
             <h:message id="PostalCountryInRow_de_message" for="PostalCityInRow_de" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
             <h:outputLabel id="PostalCountryInLabelRow_de" for="PostalCountryInRow_de" 
                            value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.countryCodeLabel}" />
             <h:inputText id="PostalCountryInRow_de" value="#{result.country}" maxlength="2" disabled="false" 
                          required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
    </f:subview>

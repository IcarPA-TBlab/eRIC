<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>


            <f:subview id="TelephoneNumberView"> 
                <h:panelGrid id="phoneNumberPanel" columns="1" headerClass="header" 
                    rowClasses="h3,list-row-default,"
                    styleClass="list-background">
                    <h:outputText id="telephoneNumberOut" value="#{bundle.telephoneNumber}#{bundle.colon}" />
                    <h:panelGrid id="phoneTypePanelWrapper" columns="2" columnClasses="rightAlign160, list-row-default">
                        <h:outputLabel id="phoneTypeMenuLabel" for="phoneTypeMenu" 
                            value="#{bundle.phoneTypeLabel}"/>
                        <h:selectOneMenu id="phoneTypeMenu" 
                                      value="#{roCollection.currentRegistryObjectBean.fields.type}">
                            <f:selectItems id="phoneTypeMenuId" value="#{roCollection.phoneTypes}"/>
                        </h:selectOneMenu>
                        <h:outputLabel id="phoneCountryInLabel" for="phoneCountryIn" value="#{bundle.countryCodeLabel}"/>
                        <h:inputText id="phoneCountryIn" value="#{roCollection.currentRegistryObjectBean.fields.countryCode}" maxlength="8" disabled="false"/>
                        <h:outputLabel id="phoneAreaInLabel" for="phoneAreaIn" value="#{bundle.areaCodeLabel}"/>
                        <h:inputText id="phoneAreaIn" value="#{roCollection.currentRegistryObjectBean.fields.areaCode}" maxlength="8" disabled="false"/>
                        <h:outputLabel id="phoneNumInLabel" for="phoneNumIn" value="#{bundle.phoneNumberLabel}"/>
                        <h:inputText id="phoneNumIn" value="#{roCollection.currentRegistryObjectBean.fields.number}" maxlength="16" disabled="false"/>
                        <h:outputLabel id="phoneExtInLabel" for="phoneExtIn" value="#{bundle.phoneExtensionLabel}"/>
                        <h:inputText id="phoneExtIn" value="#{roCollection.currentRegistryObjectBean.fields.extension}" maxlength="8" disabled="false"/>
                    </h:panelGrid>
                </h:panelGrid>
            </f:subview>


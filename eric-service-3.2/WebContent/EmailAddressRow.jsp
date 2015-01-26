<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<f:subview id="emailRowSubview">
          <h:panelGrid id="emailRowPanel" columns="2" columnClasses="rightAlign160, list-row-default">
              <h:outputLabel id="emailRowTypeMenuLabel" for="emailRowTypeMenu" 
                      value="#{bundle.emailAddressTypeLabel}"/>
              <h:selectOneMenu id="emailRowTypeMenu"
                              value="#{result.type}">
                  <f:selectItems value="#{roCollection.emailTypes}" id="emailTypesItems"/>
              </h:selectOneMenu>
              <h:outputLabel id="emailAddressRowInLabel" for="emailAddressRowIn" 
                value="#{bundle.emailAddressLabel}" />
              <h:inputText id="emailAddressRowIn" value="#{result.address}" size="60" 
                disabled="false"/>
          </h:panelGrid>
          <f:verbatim><br></f:verbatim>
</f:subview>
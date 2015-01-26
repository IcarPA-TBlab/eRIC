<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<jsp:useBean id="registrationInfo" class="it.cnr.icar.eric.client.ui.thin.RegistrationInfoBean" scope="session" />

    <f:subview id="registrationAuthView">

      <%-- Control for required form details: upload certificate or generate key --%>
      <h:panelGrid>
        <h:outputLabel id="certOptionHelpMsg" for="selectKey" value="#{bundle.certOptionHelpText}"/>
        <h:selectOneRadio id="selectKey"
        title="radio button to select upload or download of key"
         value="#{registrationInfo.generatePrivateKey}"
         valueChangeListener="#{registrationInfo.changeGeneratePrivateKey}"
         onclick="submit()">
           <f:selectItem id="selectToGenerateKey" itemValue="true" itemLabel="#{bundle.generateX509ItemLabel}"/>
           <f:selectItem id="selectToUploadKey" itemValue="false" itemLabel="#{bundle.uploadX509ItemLabel}"/>
        </h:selectOneRadio>
      </h:panelGrid>
      
      <h:panelGrid>

        <%-- GeneratePrivate Key --%>
        <c:if test="${registrationInfo.generatePrivateKey == 'true'}">
            <h:panelGrid id="generateKey"
                       columns="1" 
                       headerClass="header" 
                       rowClasses="h3,list-row-default"
                       styleClass="list-background">
            <h:outputText  value="#{bundle.generatePrivateKeyDetails}" />
            <h:outputText id="genCertHelpMsg" value="#{bundle.generatePrivateKeyHelpText}"/>
            </h:panelGrid>
            <h:panelGrid id="genCertAliasTable" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:outputLabel id="aliasInLabel" for="aliasIn" 
                               value="*#{bundle.aliasLabel}#{bundle.colon}" />
                <h:inputText id="aliasIn" 
                 value="#{registrationInfo.alias}" disabled="false"/>
                <h:outputLabel id="passwordInLabel" for="passwordIn" 
                   value="*#{bundle.passwordLabel}#{bundle.colon}" />
                <h:inputSecret id="passwordIn"  redisplay="false"
                 value="#{registrationInfo.password}"/>
                <h:outputLabel id="passwordRepeatInLabel" for="passwordRepeatIn"
                   value="*#{bundle.passwordRepeatLabel}#{bundle.colon}" />
                <h:inputSecret id="passwordRepeatIn" redisplay="false"
                 value="#{registrationInfo.passwordRepeat}"/>

            <f:verbatim><br></f:verbatim>
            </h:panelGrid>
            <h:panelGrid id="nameHelpTable" columns="1">
                <h:outputText id="genX500NameHelpMsg" value="#{bundle.generateX500NameHelpText}"/>
            </h:panelGrid>
            <h:panelGrid id="genCertOrgTable" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:outputLabel id="x500_nameInLabel" for="x500_nameIn" 
                    value="*#{bundle.x500_nameLabel}#{bundle.colon}" />
                <h:inputText id="x500_nameIn" 
                    value="#{registrationInfo.x500Bean.name}" disabled="true"/>
                <h:outputLabel id="x500_unitInLabel" for="x500_unitIn" 
                    value="*#{bundle.x500_unitLabel}#{bundle.colon}" />
                <h:inputText id="x500_unitIn" 
                    value="#{registrationInfo.x500Bean.unit}" disabled="false"/>
                <h:outputLabel id="x500_organizationInLabel" for="x500_organizationIn" 
                    value="*#{bundle.x500_organizationLabel}#{bundle.colon}" />
                <h:inputText id="x500_organizationIn" 
                    value="#{registrationInfo.x500Bean.organization}" disabled="false"/>
                <h:outputLabel id="x500_cityInLabel" for="x500_cityIn" 
                   value="*#{bundle.x500_cityLabel}#{bundle.colon}" />
                <h:inputText id="x500_cityIn" 
                   value="#{registrationInfo.x500Bean.city}" disabled="true"/>
                <h:outputLabel id="x500_stateOrProvinceInLabel" for="x500_stateOrProvinceIn" 
                   value="*#{bundle.x500_stateOrProvinceLabel}#{bundle.colon}" />
                <h:inputText id="x500_stateOrProvinceIn" 
                   value="#{registrationInfo.x500Bean.stateOrProvince}" disabled="true"/>
                <h:outputLabel id="x500_countryInLabel" for="x500_countryIn" 
                   value="*#{bundle.x500_countryLabel}#{bundle.colon}" />
                <h:inputText id="x500_countryIn" 
                   value="#{registrationInfo.x500Bean.country}" disabled="true"/>
          </h:panelGrid>

        </c:if>

        <c:if test="${registrationInfo.generatePrivateKey != 'true'}">
          <%-- X509 certificate provided by user --%>
          <h:panelGrid id="uploadCert"
                       columns="1"
                       headerClass="header" 
                       rowClasses="h3,list-row-default,list-row-default,list-row-default,list-row-default,list-row-default,"
                       styleClass="list-background">
            <h:outputText value="#{bundle.certAuthDetails}" />
            <h:panelGrid columns="1">
              <h:panelGrid columns="2">
                <h:outputLabel id="certFileNameOutLabel" for="certFileNameOut"
                   value="#{bundle.certFileNameLabel}" />
                <f:verbatim>*</f:verbatim>
              </h:panelGrid>
              <h:panelGrid columns="2">
                <h:commandButton id="certFileSelect" 
                                 value="#{bundle.chooseCertFileButtonText}"
                                 immediate="true" 
                                 onclick="window.open('FileUpload2.jsp','my_new_window','toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width=710, height=180, left=300,top=400')"
                                 styleClass="Btn2Mni" 
                                />
                <h:outputText id="certFileNameOut" value="#{registrationInfo.fileUploadBean.fileName}"/>
              </h:panelGrid>
              <h:outputText id="certHelpMsg" value="#{bundle.chooseCertFileHelpText}"/>
              <h:outputText id="certHelpJKSMsg" value="#{bundle.chooseCertFileJKSHelpText}"/>
              <h:outputText id="certHelpP12Msg" value="#{bundle.chooseCertFileP12HelpText}"/>
            </h:panelGrid>
          </h:panelGrid>
        </c:if>

      </h:panelGrid>
      
    </f:subview>


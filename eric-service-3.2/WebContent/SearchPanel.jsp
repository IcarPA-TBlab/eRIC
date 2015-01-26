<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="d" uri="/components" %>
<jsp:useBean id="searchPanel" class="it.cnr.icar.eric.client.ui.thin.SearchPanelBean" scope="session" />

<f:subview id="searchPanelSubview">
      <h:form id="discpanelForm" >
    <h:panelGrid id="searchPanelWrapper" styleClass="DicoveryPanel"  columnClasses="topColumn" style="height: 400px;">
        <h:panelGrid id="searchEnclosure" styleClass="tabPage">
                <h:panelGrid id="searchQueryPanel">
                    <h:outputLabel id="adhocQueryMenuLabel" for="adhocQueryMenu" styleClass="h3" 
                    value="#{bundle.SelectAdhocQuery}#{bundle.colon}"/>
                    <h:selectOneMenu id="adhocQueryMenu" 
                        title="#{bundle.SelectAdhocQuery}" 
                        value="#{searchPanel.currentQuery.name}" 
                        valueChangeListener="#{searchPanel.querySelectionChanged}"
                        onchange="submit(); return true;">
                        <f:selectItems value="#{searchPanel.querySelectItems}" id="adhocQuerySelectItems"/>
                        <h:outputText id="searchQueryDescOut" value="#{searchPanel.currentQuery.description}" />
                    </h:selectOneMenu>
                    <h:panelGrid 
                        id="searchQueryOptionsPanel"
                        columns="2"
                        styleClass="tabPage"
                        >
                        <h:outputLabel id="federatedQueryLabel" for="federatedQueryCheckbox" 
                                    value="#{bundle.federatedQuery}:" styleClass="h3"/>
                        <h:selectBooleanCheckbox id="federatedQueryCheckbox" 
                                              value="#{searchPanel.federatedQuery}"
                                             title="#{bundle.federatedQuery}"
                                          onchange="onchange=submit(); return true;"
                        />
                        <h:outputLabel 
                            id="compressContentOut" 
                            value="#{bundle.exportZipFile}:" 
                            styleClass="h3" 
                            for="compressContentCheckbox"/>
                        <h:selectBooleanCheckbox 
                            id="compressContentCheckbox"
                            value="#{searchPanel.compressContent}"
                            title="#{bundle.exportZipFileToolTip}"/>
                        <h:outputLabel 
                            id="compressContentLevelOut" 
                            value="#{bundle.searchDepth}:" 
                            styleClass="h3" 
                            for="compressContentLevelMenu"/>
                        <h:selectOneMenu 
                            id="compressContentLevelMenu" 
                            value="#{searchPanel.searchDepth}"
                            disabled="false"
                            >
                            <f:selectItems 
                                value="#{searchPanel.searchDepthItems}" 
                                id="searchDepthSelectItems"/>
                        </h:selectOneMenu>
                    </h:panelGrid>
                </h:panelGrid>
                <h:panelGrid id="searchQuery2Panel">
                    <f:subview id="userDefinedQueryPanels">
                        <d:queryPanel binding="#{searchPanel.queryComponent}" />
                    </f:subview>
                    <h:panelGrid id="searchQueryInnerPanel">
                        <c:if test="${searchPanel.menuDisplayTree}">
                            <h:outputLink id="classSchemeSelectorButton" 
                                     onclick="window.open('SearchClassSchemeSelector.jsp', 'CSwin','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"
                                  styleClass="BtnLook">
                                 <h:outputText value="#{bundle.selectClassificationNode}"
                                                  id="selectClassificationNodeOut"/>
                            </h:outputLink>
                            <h:dataTable id="csDisplayTable" headerClass="alignleft"
                                rowClasses="alignleft, alignright"
                                value="#{searchPanel.currentQuery.parameters.$classificationPath.listValue}"
                                var="result">
                                <h:column>
                                    <h:outputLabel id="classVal2InLabel" for="classValIn" styleClass="h3"
                                    value="#{bundle.classificationValueLabel}#{bundle.colon}" />
                                    <h:inputText id="classVal2In" value="#{result}" title="#{result}" size="30"/>
                                </h:column>
                            </h:dataTable>
                        </c:if>
                    </h:panelGrid>
                    <h:panelGrid id="searchButtonPanel" columns="3">     
          <h:commandButton id="searchButton" 
                           value="#{bundle.searchButtonText}" 
                           action="#{searchPanel.doSearch}" 
                           onclick="Wait()"
                           styleClass="Btn1Def"
                   />
          <h:commandButton id="searchQueryClearButton" 
                           value="#{bundle.clearButtonText}" 
                           action="#{searchPanel.doClear}" 
                           styleClass="Btn1Def"
                   />
         <h:commandButton id="searchQueryPanelHelpLink" 
                           value="#{bundle.help}"
                           styleClass="Btn1Def"
                           immediate="true" 
                           onclick="window.open('#{registryBrowser.searchHelp}', 'SearchHelpwin','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"/>
                    </h:panelGrid>
                    <h:outputText id="searchQuery2Out" value="#{bundle.wildcardToolTip}" />
                </h:panelGrid>
        </h:panelGrid>
    </h:panelGrid>
      </h:form>
</f:subview>

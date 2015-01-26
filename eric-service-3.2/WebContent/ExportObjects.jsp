<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>
<%@ taglib prefix="d" uri="/components" %>
<jsp:useBean id="exportBean" class="it.cnr.icar.eric.client.ui.thin.ExportBean" scope="session" />

<f:subview id="exportPanelSubview">
    <f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
    <body bgcolor='#ffffff' onload="">
        <h:form id="exportObjectsForm" >
            <h:panelGrid id="exportObjectsPanelWrapper" styleClass="tabPage">
                <f:verbatim><br></f:verbatim>
                <h:outputText id="exportObjectsOut" value="Export Objects" 
                              styleClass="h2"/>
                <f:verbatim><br></f:verbatim>
                <h:messages id="errorMessages_EO" globalOnly="true" styleClass="h3"/>
                <h:panelGrid 
                    id="compressedQueryResultsPanel" 
                    rendered="#{searchPanel.compressContent}" 
                    columns="2">
                    <h:panelGrid 
                        id="compressContentNoEOsPanel"
                        rendered="#{exportBean.zipFileName == null}">
                        <h:outputText
                            id="compressContentNoEOsOutput"
                            value="#{bundle.noExtrinsicObjectsFound}"
                            styleClass="h3"
                        />
                    </h:panelGrid>
                    <h:panelGrid 
                        id="compressContentDownloadPanel"
                        rendered="#{exportBean.zipFileName != null}">
                        <h:outputLabel
                            id="compressContentDownloadLabel" 
                            value="#{bundle.downloadCompressedContent}:" 
                            styleClass="h3" 
                            for="compressedQueryLink"/>
                        <h:outputLink id="compressedQueryLink"
                                      value="#{facesContext.externalContext.request.contextPath}/registry/thin/ExportFileDownload.jsp">
                            <h:outputText 
                                id="downloadButtonLabel" 
                                value="#{bundle.downloadButtonText}"/>
                        </h:outputLink>
                    </h:panelGrid>
                </h:panelGrid>
                <h:panelGrid id="exportObjectsPanel" rendered="#{! searchPanel.compressContent}">
                    <h:panelGrid id="exportObjectsNoSelectedTable" rendered="#{! exportBean.registryObjectBeanSelected}">
                        <f:verbatim><br></f:verbatim>
                        <h:outputText id="noSelectedExportObjectsOut" value="#{bundle.noObjectsSelectedForExport}" />
                        <f:verbatim><br></f:verbatim>
                    </h:panelGrid>
                    <h:panelGrid id="exportObjectsExportObjectsTable" rendered="#{exportBean.registryObjectBeanSelected}">
                        <h:outputText id="selectedExportObjectsOut" value="#{bundle.objectsToExport}" />
                        <f:verbatim><br></f:verbatim>
                        <f:verbatim><a href="#gotobuttons"><img src="images/a.gif" width="1" height="2" 
                                                                    border="0" alt="Skip to Buttons"></a>
                        </f:verbatim>
                        <h:panelGrid id="exportObjectsTable" styleClass="tabPage">
                            <h:dataTable columnClasses=""
                                         headerClass="TblColHdrSel"
                                         rowClasses="#{exportBean.rowClasses}"
                                         styleClass="list-background"
                                         id="exportedTable"
                                         rows="#{exportBean.numberAllSelectedRegistryObjectBeans}"
                                         binding="#{exportBean.scrollerBean.data}"
                                         value="#{exportBean.allSelectedRegistryObjectBeans}"
                                         var="result">
                                <h:column id="exportedResultsCol1">
                                    <f:facet name="header">
                                        <h:outputText id="exportedHeaderOut1" value="Id"/>
                                    </f:facet>
                                    <h:outputText id="exportedResultsOut1" escape="false" value="#{result.registryObject.id}"/>
                                </h:column>
                                <h:column id="exportedResultsCol2">
                                    <f:facet name="header">
                                        <h:outputText id="exportedHeaderOut2" value="Object Type"/>
                                    </f:facet>
                                    <h:outputText id="exportedResultsOut2" escape="false" value="#{result.registryObject.objectType}"/>
                                </h:column>
                                <h:column id="exportedResultsCol3">
                                    <f:facet name="header">
                                        <h:outputText id="exportedHeaderOut3" value="Name"/>
                                    </f:facet>
                                    <h:outputText id="exportedResultsOut3" escape="false" value="#{result.registryObject.name}"/>
                                </h:column>
                                <h:column id="exportedResultsCol4">
                                    <f:facet name="header">
                                        <h:outputText id="exportedHeaderOut4" value="Description"/>
                                    </f:facet>
                                    <h:outputText id="exportedResultsOut4" escape="false" value="#{result.registryObject.description}"/>
                                </h:column>
                            </h:dataTable>
                            <h:outputLabel 
                                id="exportContentLevelOut" 
                                value="#{bundle.searchDepth}:" 
                                styleClass="h3" 
                                for="exportLevelMenu"/>
                            <h:selectOneMenu 
                                id="exportLevelMenu" 
                                value="#{searchPanel.searchDepth}" 
                                disabled="false"
                                >
                                <f:selectItems 
                                    value="#{searchPanel.searchDepthItems}" 
                                    id="exportSearchDepthSelectItems"/>
                            </h:selectOneMenu>
                        </h:panelGrid>
                    </h:panelGrid>
                </h:panelGrid>
                <!--/h:panelGrid-->
                <f:verbatim><a name="gotobuttons"></a></f:verbatim>
                <h:panelGrid id="exportObjectsButtonTable" columns="3" rendered="#{! searchPanel.compressContent}">
                    <h:commandButton id="exportObjectsSaveFile" 
                                     value="#{bundle.SaveFile}"
                                     styleClass="Btn2Mni"  
                                     action="#{exportBean.doExport}" 
                                     rendered="#{exportBean.registryObjectBeanSelected}"
                                     />
                    <f:verbatim>&nbsp;&nbsp;</f:verbatim>
                    <h:commandButton id="exportObjectsDone" 
                                     value="#{bundle.backToRegistryObjects}" 
                                     styleClass="Btn2Mni"  
                                     action="showSearchResultsPage"
                                     onclick=""/>
                </h:panelGrid>
            </h:panelGrid>
        </h:form>
</f:subview>

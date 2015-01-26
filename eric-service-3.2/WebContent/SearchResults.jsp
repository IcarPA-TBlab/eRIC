<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="d" uri="/components" %>

<f:subview id="searchResultsView">
    <f:verbatim><br></f:verbatim>
    <h:panelGrid id="searchHelpPanel" columns="2" width="100%" columnClasses="leftAlign, rightAlign">
        <h:outputText id="registryObjectsOut" value="#{bundle.registryObjects}" styleClass="h2"/>
        <h:outputLink id="searchResultsHelp"
            value="#{registryBrowser.registryObjectHelp}" target="_new">
            <h:outputText id="searchResultHelpOut" value="#{bundle.registryObjectsHelp}"/>
        </h:outputLink>
    </h:panelGrid>
    <c:choose>
        <c:when test="${registryBrowser.sessionExpired}">
            <h:panelGrid id="sessionTimeoutPanel">
                <h:form id="sessionTimeoutForm">
                    <h:outputText id="sessionTimoutClearResults" value="#{bundle.clearResults}"/>
                    <f:verbatim><br><br></f:verbatim>
                    <h:outputText id="sessionTimeout" value="#{bundle.sessionTimeout}"/>
                </h:form>
            </h:panelGrid>
        </c:when>
        <c:when test='${roCollection.numberOfRegistryObjectBeans > 0  || roCollection.numberOfPinnedRegistryObjectBeans > 0}'>
            <h:form id="SearchResultsForm" onsubmit="return checkboxSelected(this)">
                <h:panelGrid id="includePagesPanel" columns="1">
                    <h:column id="summaryToolbarIncludePage">
                        <c:import url="/SummaryToolbar.jsp"/>
                    </h:column>                        
                    <h:column id="bookmarkPageIncludePage">                        
                        <c:import url="/BookmarkPage.jsp"/>
                    </h:column>
                </h:panelGrid>
               <h:panelGrid id="statusPanel" columns="1" width="100%" columnClasses="leftAlign">
                    <h:messages id="searchResultsMessages" globalOnly="true" />
                    <h:outputText id="dirtyObjectsOut" escape="false" value="#{roCollection.dirtyObjectsMessage}" />
                <!--this section will display Association Error Message in multiple lines-->
                <c:if test="${roCollection.associationErrorMessage != null }">
                  <c:forEach items="${roCollection.associationErrorMessage}" var="assoMessage">
                    <f:verbatim>
                      <li><c:out value="${assoMessage}"/></li>
                     </f:verbatim>
                   </c:forEach>
                 </c:if>                                 
               </h:panelGrid>
                <c:if test="${roCollection.numberOfRegistryObjectBeans > 0}">
                    <h:panelGrid id="searchResultsPanel" width="100%" columnClasses="rightAlign">
                        <c:if test="${roCollection.scrollerBean.totalResultCount > 0}">
                            <h:panelGrid id="resultCountPanelWrapper" width="100%" columns="2" columnClasses="leftAlign, rightAlign" styleClass="TblTtlTxt">
                                <h:outputText id="searchResultsPanelOut" value="#{bundle.results} #{roCollection.scrollerBean.currentRow + 1} - #{roCollection.scrollerBean.nextRow} #{bundle.of} #{roCollection.scrollerBean.totalResultCount}"/>      
                                <h:outputLink id="pinObjectsLink"
                                    value="#{registryBrowser.pinObjectsHelp}" target="_new">
                                    <h:outputText id="pinObjectOut" value="#{bundle.bookmarkAndRelateFeature}"/>
                                </h:outputLink>
                            </h:panelGrid>
                        </c:if>
			<h:panelGrid id="searchResultsPanelRows" width="100%" columnClasses="rightAlign" styleClass="Tbl">
                        <h:dataTable columnClasses=""
                            headerClass="TblColHdrSel"
                            rowClasses="#{roCollection.rowClasses}"                  
                            styleClass="list-background"
                            id="table"
                            rows="#{roCollection.scrollerBean.numberOfSearchResults}"
                            binding="#{roCollection.scrollerBean.data}"
                            value="#{roCollection.registryObjectBeans}"
                            var="result">
                            <h:column id="searchResultCheckboxColumn">
                                <f:facet name="header">
                                    <h:outputLabel id="searchResultsSelectedCheckboxLabel" for="searchResultsSelectedCheckbox" 
                                    value="#{bundle.pick}"/>
                                </f:facet>
                                <h:selectBooleanCheckbox id="searchResultsSelectedCheckbox" 
                                value="#{result.selected}"
                                title="#{bundle.pick}"/>
                            </h:column>
                            <c:forEach begin="1" end="${roCollection.numberOfSearchResultValueBeans}" 
                                varStatus="columnStatus">
                                
                                <c:if test="${columnStatus.count==1}">
                                    <h:column id="searchResultsCol1">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut1" value="#{roCollection.header}"/>
                                        </f:facet>
                                        <h:panelGrid id="searchResultPanel1" columns = "2" >
                                            <h:commandLink id="searchResultLink1" action="showDetails"
                                                actionListener="#{roCollection.setCurrentRegistryObjectBean}">
                                                <h:outputText value="#{bundle.details}" id="searchResultOut1"/>
                                                <f:param id="SRIdParam" name="idValue" value="#{result.value}"/>
                                                <f:param id="SRTypeParam" name="objectType" value="#{result.objectType}"/>
                                            </h:commandLink>
                                        </h:panelGrid>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==2}">
                                    <h:column id="searchResultsCol2">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut2" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut2" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==3}">
                                    <h:column id="searchResultsCol3">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut3" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut3" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                              
                                <c:if test="${columnStatus.count==4}">
                                    <h:column id="searchResultsCol4">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut4" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut4" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==5}">
                                    <h:column id="searchResultsCol5">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut5" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut5" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                              
                                <c:if test="${columnStatus.count==6}">
                                    <h:column id="searchResultsCol6">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut6" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut6" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==7}">
                                    <h:column id="searchResultsCol7">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut7" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut7" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                              
                                <c:if test="${columnStatus.count==8}">
                                    <h:column id="searchResultsCol8">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut8" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut8" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==9}">
                                    <h:column id="searchResultsCol9">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut9" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut9" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                              
                                <c:if test="${columnStatus.count==10}">
                                    <h:column id="searchResultsCol10">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut10" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut10" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==11}">
                                    <h:column id="searchResultsCo11">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut11" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut11" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                              
                                <c:if test="${columnStatus.count==12}">
                                    <h:column id="searchResultsCol12">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut12" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut12" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==13}">
                                    <h:column id="searchResultsCo13">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut13" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut13" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==14}">
                                    <h:column id="searchResultsCol14">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut14" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut14" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==15}">
                                    <h:column id="searchResultsCo15">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut15" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut15" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==16}">
                                    <h:column id="searchResultsCol16">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut16" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut16" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==17}">
                                    <h:column id="searchResultsCo17">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut17" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut17" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==18}">
                                    <h:column id="searchResultsCol18">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut18" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut18" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==19}">
                                    <h:column id="searchResultsCo19">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut19" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut19" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==20}">
                                    <h:column id="searchResultsCol20">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut20" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut20" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==21}">
                                    <h:column id="searchResultsCo21">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut21" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut21" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==22}">
                                    <h:column id="searchResultsCol22">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut22" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut22" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==23}">
                                    <h:column id="searchResultsCo23">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut23" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut23" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==24}">
                                    <h:column id="searchResultsCol24">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut24" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut24" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==25}">
                                    <h:column id="searchResultsCo25">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut25" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut25" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==26}">
                                    <h:column id="searchResultsCol26">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut26" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut26" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==27}">
                                    <h:column id="searchResultsCo27">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut27" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut27" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>                          
                                <c:if test="${columnStatus.count==28}">
                                    <h:column id="searchResultsCol28">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut28" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut28" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==29}">
                                    <h:column id="searchResultsCo29">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut29" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut29" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count==30}">
                                    <h:column id="searchResultsCo30">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut30" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut30" escape="false" value="#{result.value}"/>
                                    </h:column>
                                </c:if>
                                <c:if test="${columnStatus.count>30}">
                                    <h:column id="searchResultsCo31">
                                        <f:facet name="header">
                                            <h:outputText id="searchResultsHeaderOut31" value="#{roCollection.header}"/>
                                        </f:facet>                       
                                        <h:outputText id="searchResultsOut31" escape="false" value="#{bundle.message.maxColumns}"/>
                                    </h:column>
                                </c:if>
                            </c:forEach>   
                        </h:dataTable>
			</h:panelGrid>
                    </h:panelGrid>
                </c:if>
                <h:panelGrid id="scrollerPanel" width="100%">
                    <d:scroller navFacetOrientation="SOUTH" for="table" 
                        actionListener="#{roCollection.processScrollEvent}"
                        action="showSearchResultsPage"
                        id="scroller">

                        <f:facet name="next">
                            <h:panelGroup id="scrollNextPanel">
                                <h:outputText id="scrollNextOut" value="#{bundle.next}"/>
                                <h:graphicImage id="scrollNextImg" height="9" width="13" style="border: 0px solid;" 
                                url="/images/arrow-right.gif" alt=""/>
                            </h:panelGroup>
                        </f:facet>

                        <f:facet name="previous">
                            <h:panelGroup id="scrollPrevPanel">
                                <h:outputText id="scrollPrevOut" value="#{bundle.previous}"/>
                                <h:graphicImage id="scrollPrevImg" height="9" width="13" style="border: 0px solid;" 
                                url="/images/arrow-left.gif" alt=""/>
                            </h:panelGroup>
                        </f:facet>

                        <f:facet name="number">
                            <!-- You can put a panel here if you like -->
                        </f:facet>

                        <f:facet name="current">
                            <!-- You can put a panel here if you like -->
                        </f:facet>

                    </d:scroller>
  
                </h:panelGrid>
            </h:form>
        </c:when>
        <c:when test="${roCollection.registryObjectBeans == null}">
            <h:panelGrid id="clearResultsPanel">
                <h:form id="clearResultsForm">
                    <h:outputText id="clearResultsOut" value="#{bundle.clearResults}"/>
                </h:form>
            </h:panelGrid>
  
            <h:panelGrid id="errorMessagePanel" columns="1" width="100%" columnClasses="leftAlign">
                <h:messages id="errorSearchResultMessages" layout="table" globalOnly="true" />
            </h:panelGrid>
        </c:when>
        <c:otherwise>
            <h:panelGrid id="noResultsPanel">
                <h:form id="noResultsForm">
                    <h:outputText id="noResults" value="#{bundle.noRegistryObjectsFound}"/>
                </h:form>
            </h:panelGrid>
        </c:otherwise>
    </c:choose>
    <f:verbatim><br></f:verbatim>
    <h:panelGrid id="relationPane" width="100%">
        <c:if test="${roCollection.relationshipBean != null}">  
            <c:import url="/Relationship.jsp"/>
        </c:if>
    </h:panelGrid>
    <h:panelGrid id="detailsPane" width="100%">
        <c:if test="${roCollection.currentDrilldownRegistryObjectBean != null && roCollection.relationshipBean == null}">
            <c:import url="/Details.jsp"/>
        </c:if>
    </h:panelGrid>
</f:subview>

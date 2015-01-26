<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
    <jsp:useBean id="registryBrowser" 
            class="it.cnr.icar.eric.client.ui.thin.RegistryBrowser" 
            scope="session" /> 
    <jsp:useBean id="roCollection" 
        class="it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean" 
        scope="session" />

<f:subview id="searchPinnedView">
    <c:if test="${roCollection.numberOfSearchPinnedValueBeans > 0 && roCollection.numberOfPinnedRegistryObjectBeans > 0}">  
        <h:panelGrid id="bookmarkPane" width="100%" styleClass="tabPage">        
            <c:import url="/RelateToolbar.jsp"/>
            <h:panelGrid id="searchPinnedPanel" width="100%" columns="1" columnClasses="rightAlign" styleClass="Tbl">
	    <h:dataTable columnClasses=""
                    headerClass="TblColHdrSel"
                    rowClasses="#{roCollection.rowClasses}"                  
                    styleClass="list-background"
                    id="pinnedTable"
                    rows="#{roCollection.pinnedScrollerBean.numberOfSearchResults}"
                    binding="#{roCollection.pinnedScrollerBean.data}"
                    value="#{roCollection.pinnedRegistryObjectBeans}"
                    var="result">
                    <h:column id="pickPinnedColumn">
                        <f:facet name="header">
                            <h:outputLabel id="searchPinnedSelectedCheckboxLabel" for="searchPinnedSelectedCheckbox" 
                            value="#{bundle.pick}"/>
                        </f:facet>
                        <h:selectBooleanCheckbox id="searchPinnedSelectedCheckbox" 
                        value="#{result.pinned}"
                        title="#{bundle.pick}"/>
                    </h:column>
                    <c:forEach begin="1" end="${roCollection.numberOfSearchPinnedValueBeans}" 
                        varStatus="columnStatus">
			<c:if test="${columnStatus.count==1}">
                        <h:column id="pinnedResultsCol1">
                            <f:facet name="header">
                                <h:outputText id="pinnedHeaderOut1" value="#{roCollection.pinnedHeader}"/>
                            </f:facet>
                                    <h:panelGrid id="innerPinnedDisplayTable1" columns = "2" >
                                        <h:commandLink id="pinnedResultLink1" action="showDetails"
                                            actionListener="#{roCollection.setCurrentRegistryObjectBean}">
                                            <h:outputText id="detailsValDisplay1" value="#{bundle.details}"/>
                                            <f:param id="PIdParam1" name="idValue" value="#{result.value}"/>
                                            <f:param id="PTypeParam1" name="objectType" value="#{result.objectType}"/>
                                        </h:commandLink>
                                    </h:panelGrid>
                        </h:column>
			</c:if>
                        <c:if test="${columnStatus.count==2}">
	                        <h:column id="pinnedResultsCol2">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut2" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut2" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==3}">
	                        <h:column id="pinnedResultsCol3">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut3" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut3" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==4}">
	                        <h:column id="pinnedResultsCol4">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut4" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut4" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==5}">
	                        <h:column id="pinnedResultsCol5">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut5" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut5" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==6}">
	                        <h:column id="pinnedResultsCol6">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut6" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut6" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==7}">
	                        <h:column id="pinnedResultsCol7">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut7" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut7" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==8}">
	                        <h:column id="pinnedResultsCol8">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut8" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut8" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==9}">
	                        <h:column id="pinnedResultsCol9">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut9" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut9" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==10}">
	                        <h:column id="pinnedResultsCol10">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut10" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut10" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==11}">
	                        <h:column id="pinnedResultsCol11">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut11" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut11" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==12}">
	                        <h:column id="pinnedResultsCol12">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut12" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut12" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>
                        <c:if test="${columnStatus.count==13}">
	                        <h:column id="pinnedResultsCol13">
					<f:facet name="header">
						<h:outputText id="pinnedHeaderOut13" value="#{roCollection.pinnedHeader}"/>
					</f:facet>                       
					<h:outputText id="pinnedResultsOut13" escape="false" value="#{result.value}"/>
				</h:column>
                        </c:if>			
			
                    </c:forEach>   
                </h:dataTable>
	    </h:panelGrid>
        </h:panelGrid>                            
    </c:if>
</f:subview>

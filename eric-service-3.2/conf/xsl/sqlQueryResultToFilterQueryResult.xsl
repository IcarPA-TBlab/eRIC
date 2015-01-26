<?xml version="1.0"?> 

<!-- 

$Header: /cvsroot/ebxmlrr/omar/conf/xsl/sqlQueryResultToFilterQueryResult.xsl,v 1.1.1.1 2003/09/24 03:56:01 farrukh_najmi Exp $

This stylesheet converts a RegistryResponse that has an SQLQueryResult
into a RegistryResponse that has a FilterQueryResponse. The param1
specifies which sub-element of FilterQueryResult should be inserted.

This style sheet is used by the FilterQueryProcessor which uses the 
SQLQueryProcessor to perform its modified query and then uses this stylesheet
to convert it to a FilterQueryResult.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:rs="urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.0"
	xmlns:query="urn:oasis:names:tc:ebxml-regrep:query:xsd:2.0"
	version="1.0">

	<xsl:param name="param1" select="'OrganizationQueryResult'"/>

	<xsl:template match="@*|node()">
    	<xsl:copy>
        	<xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
	</xsl:template>
			 
	<xsl:template match="//query:SQLQueryResult">
		<FilterQueryResult>
			<xsl:element name="{$param1}">
				<xsl:apply-templates/>
			</xsl:element>
		</FilterQueryResult>
		
	</xsl:template>
</xsl:stylesheet>

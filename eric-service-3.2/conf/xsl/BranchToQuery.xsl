<?xml version = "1.0" encoding = "UTF-8"?>
<!--$Header: /cvsroot/ebxmlrr/omar/conf/xsl/BranchToQuery.xsl,v 1.1.1.1 2003/09/24 03:56:01 farrukh_najmi Exp $-->
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" xmlns:rs = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.0" xmlns:query = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.0" version = "1.0">

	<xsl:param name = "outputQuery"/>

	<xsl:template match = "//query:OrganizationParentBranch">
		<xsl:element name = "{$outputQuery}">
			<xsl:copy-of select = "child::*"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match = "//query:OrganizationChildrenBranch">
		<xsl:element name = "{$outputQuery}">
			<xsl:copy-of select = "child::*"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match = "//query:ClassificationNodeParentBranch">
		<xsl:element name = "{$outputQuery}">
			<xsl:copy-of select = "child::*"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match = "//query:ClassificationNodeChildrenBranch">
		<xsl:element name = "{$outputQuery}">
			<xsl:copy-of select = "child::*"/>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>

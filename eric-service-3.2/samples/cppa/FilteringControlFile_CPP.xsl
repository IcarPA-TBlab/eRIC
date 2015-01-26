<?xml version = "1.0" encoding = "UTF-8"?>

<!--$Header: /cvsroot/ebxmlrr/omar/misc/samples/cppa/FilteringControlFile_CPP.xsl,v 1.2 2007/01/09 15:26:28 farrukh_najmi Exp $-->
<!--$Revision: 1.2 $-->

<!--

This is a style sheet to test and demonstrate the Role Based Content Filtering feature.
It demonstrates the filtering of both CPP ExtrinsicObject as well as CPP
RepositoryItem based upon the following rules:

ExtrinsicObject Filtering Rules:

    -Remove the name unless Role is ProjectLead
    -Mask but not remove the description unless Role is ProjectLead

RepositoryItem Filtering Rules:

    -Remove tp:Comment unless Role is ProjectLead
    
Note that because xalan does not support XSLT2.0 yet we cannot produce
multiple output files yet using <xsl:result-document>. 
As a workaround, this stylesheet is invoked twice, once for ExtrinsicObject
and again for RepositoryItem filetring.

-->
<xsl:stylesheet 
    xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" 
    xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:rim = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
    xmlns:rs = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0" 
    xmlns:tp = "http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd" 
    xmlns:xlink = "http://www.w3.org/1999/xlink" version = "1.0"
    xmlns:az="xalan://it.cnr.eric.common.server.security.authorization.AuthorizationServiceImpl"
    extension-element-prefixes="az">
    
    <xsl:output method = "xml" indent = "yes"/>
    <xsl:strip-space elements = "*"/>
    
    <!--The subjectId-->
    <xsl:param name="subjectId" select="''"></xsl:param>
    
    <!--The id of the repositoryItem. Resolved by a URIResolver in code.-->
    <xsl:param name="repositoryItem" select="''"></xsl:param>
    
    <!--Only copy repositoryItem tree if repositoryItem is not null-->
    <xsl:if test="$repositoryItem != 'null'">
        <xsl:message>
            repositoryItem not null = &quot;<xsl:value-of select="$repositoryItem"/>&quot;
        </xsl:message>        
    </xsl:if>
    
    <!--
    Default template for identity transform to simply duplicate entire source tree to target tree.
    Specialized templates may transform certain attributes and nodes based upon 
    Subject credentials.
    -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>    
    
    <!--
    Multiplex between primary input ExtrinsicObject and
    secondary input RepositoryItem depending upon whether
    repositoryItem is specified.
    -->
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$repositoryItem = ''">
                <xsl:apply-templates select="./child::node()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="document($repositoryItem)/child::node()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--
    Only copy rim:Name if Role is ProjectLead
    -->
    <xsl:template match="//rim:Name">        
        <xsl:message>
            subjectId: <xsl:value-of select="$subjectId"/>
        </xsl:message>        
        <!--xsl:if test="az:isSubjectInRole($subjectId, &quot;urn:freebxml:registry:demoDB:SubjectRole:ProjectLead&quot;)"-->
        <xsl:if test="$subjectId = 'urn:freebxml:registry:predefinedusers:farrukh'">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:if>        
    </xsl:template>    
    
    <!--
    Mask but not remove the description unless Role is ProjectLead
    -->
    <xsl:template match="//rim:Description/rim:LocalizedString/@value">        
        <!--xsl:if test="az:isSubjectInRole($subjectId, &quot;urn:freebxml:registry:demoDB:SubjectRole:ProjectLead&quot;)"-->
        <xsl:choose>
            <xsl:when test="$subjectId = 'urn:freebxml:registry:predefinedusers:farrukh'">
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="value">***</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>        
    
    <!--
    Only copy tp:Comment if Role is ProjectLead
    -->
    <xsl:template match="tp:Comment">        
        <!--xsl:if test="az:isSubjectInRole($subjectId, &quot;urn:freebxml:registry:demoDB:SubjectRole:ProjectLead&quot;)"-->
        <xsl:if test="$subjectId = 'urn:freebxml:registry:predefinedusers:farrukh'">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:if>        
    </xsl:template>    
    
    
    
</xsl:stylesheet>

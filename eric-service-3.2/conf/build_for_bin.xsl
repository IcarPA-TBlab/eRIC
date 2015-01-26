<?xml version="1.0"?> 

<!-- 
$Header: /cvsroot/ebxmlrr/eric/conf/build_for_bin.xsl,v 1.1.1.1 2003/09/24 03:55:56 farrukh_najmi Exp $

This XSLT file transform the build.xml into the build.xml for binary distribution

Difference from build.xml of source distribution:
- no all, conf, compile, doc, javacc, config, dist, distBin, distSrc targets
- property build.home = .
- property ebxmlrr.lib = ${basedir}/lib

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method = "xml" /> 
	
    <xsl:template match="/*">
        <!-- Copy the root element (project) -->
        <xsl:copy>
            <!-- Copy the attributes of the project root element-->
            <xsl:apply-templates select="@*"/>
            <!-- Copy the children of project root element and make some
            transformation if necessary-->
            <xsl:for-each select = "./*" > 
                <xsl:choose > 
                    <xsl:when test = "name()='property' and @name='build.home'"> 
                        <property name="build.home" value="."/>      
                    </xsl:when>
                    <xsl:when test = "name()='property' and @name='ebxmlrr.lib'">
                        <property name="ebxmlrr.lib">
                            <xsl:attribute name="value">${basedir}/lib</xsl:attribute>
                        </property>
                    </xsl:when>
                    <xsl:when test = "name()='target' and @name='all'"/>
                    <xsl:when test = "name()='target' and @name='conf'">
                        <target name="conf"/>
                    </xsl:when>
                    <xsl:when test = "name()='target' and @name='compile'">
                        <target name="compile"/>
                    </xsl:when>
                    <xsl:when test = "name()='target' and @name='doc'"/>
                    <xsl:when test = "name()='target' and @name='javacc'"/>
                    <xsl:when test = "name()='target' and @name='config'"/>
                    <xsl:when test = "name()='target' and @name='dist'"/>
                    <xsl:when test = "name()='target' and @name='distLib'"/>
                    <xsl:when test = "name()='target' and @name='distCommon'"/>
                    <xsl:when test = "name()='target' and @name='distBin'"/>
                    <xsl:when test = "name()='target' and @name='distSrc'"/>
                    <xsl:otherwise> 
                        <xsl:copy-of select="."/>
                    </xsl:otherwise> 
               </xsl:choose> 
            </xsl:for-each> 
        </xsl:copy>
    </xsl:template>

    <xsl:template match = "@*" > 
        <xsl:copy/> 
    </xsl:template>

</xsl:stylesheet>

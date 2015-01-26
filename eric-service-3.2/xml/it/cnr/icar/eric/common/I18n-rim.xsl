<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : I18n-rim.xsl
    Created on : 01 May 2005, 19:21
    Author     : Diego Ballve / Digital Artefacts
    Description:
        Copy an xml file creating rim:LocalizedStrings using values from a
        ResourceBundle. Desired locales must be added manually (see
        <xsl:template match="//rim:LocalizedString">). Original value will
        be used as key.
        
        Indenting algorithm from:
        http://www.dpawson.co.uk/xsl/sect2/pretty.html
-->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
  xmlns:i18n-xslt-task="xalan://it.cnr.icar.eric.common.I18nXSLTTask"
  version="1.0">
  
  <xsl:output method="xml" indent="yes"/>
  <xsl:param name="indent-increment" select="'  '"/>
  <xsl:param name="bundleBaseName" select="'ResourceBundle'"/>
  <xsl:param name="localeList" select="'en_US:en_US_mm'"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <!--
    Handles the "to be localized" rim:LocalizedString
    Add one call-template for each desired language.
  -->
  <xsl:template match="//rim:LocalizedString">
    <xsl:param name="indent" select="'&#xA;'"/>
    <xsl:call-template name="create-localized-strings">
      <xsl:with-param name="indent" select="$indent"/>
      <xsl:with-param name="locales" select="$localeList"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Deep copy all nodes -->
  <xsl:template match="*">
    <xsl:param name="indent" select="'&#xA;'"/>
    <xsl:value-of select="$indent"/>
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates>
        <xsl:with-param name="indent"
          select="concat($indent, $indent-increment)"/>
      </xsl:apply-templates>
      <xsl:if test="*">
        <xsl:value-of select="$indent"/>
      </xsl:if>
    </xsl:copy>
  </xsl:template>        

  <!-- These are not indented -->
  <xsl:template match="comment()|processing-instruction()">
    <xsl:copy />
  </xsl:template>

  <!-- WARNING: this is dangerous. Handle with care -->
  <xsl:template match="text()[normalize-space(.)='']"/>

  <xsl:template name="create-localized-strings">
    <xsl:param name="locales" select="'en_US'" />
    <xsl:param name="indent" select="'&#xA;'" />
    <xsl:choose>
      <xsl:when test="contains($locales,':')">
        <xsl:call-template name="create-localized-string" >
          <xsl:with-param name="locale"
                          select="substring-before($locales,':')" />
          <xsl:with-param name="indent" select="$indent" />
        </xsl:call-template>
        <xsl:call-template name="create-localized-strings" >
          <xsl:with-param name="locales"
                          select="substring-after($locales,':')" />
          <xsl:with-param name="indent" select="$indent" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="create-localized-string" >
          <xsl:with-param name="locale" select="$locales" />
          <xsl:with-param name="indent" select="$indent" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <!-- Creates the new rim:LocalizedString, fetching value from I18nXSLTTask class -->
  <xsl:template name="create-localized-string">
    <xsl:param name="indent" select="'&#xA;'"/>
    <xsl:param name="locale" select="'en_US'"/>
    <xsl:param name="charset" select="@charset"/>
    <xsl:param name="key" select="@value"/>
    <xsl:value-of select="$indent"/>
    <xsl:element name="rim:LocalizedString">
      <xsl:attribute name="xml:lang">
        <xsl:value-of select="i18n-xslt-task:toXMLLangString(string($locale))"/>
      </xsl:attribute>
      <xsl:attribute name="charset">
        <xsl:value-of select="$charset"/>
      </xsl:attribute>
      <xsl:attribute name="value">
        <xsl:value-of  select="i18n-xslt-task:getLocalizedString(string($bundleBaseName), string($locale), string($key))"/>
      </xsl:attribute>
    </xsl:element>
  </xsl:template>
    
</xsl:stylesheet>

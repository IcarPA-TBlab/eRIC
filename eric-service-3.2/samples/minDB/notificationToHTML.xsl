<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lcm="urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0"
    xmlns:query="urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0"
    xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
    xmlns:rs="urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0"
    exclude-result-prefixes="lcm query rim rs">

    <!--
    | Document   : Notification.xsl
    | Created on : 16 June 2006
    | Author     : Andrzej Taramina
    | Description: This stylesheet converts an ebXML RR Notification to displayable HTML
    | $Header: /cvsroot/ebxmlrr/omar/misc/samples/minDB/notificationToHTML.xsl,v 1.1 2006/06/23 13:58:39 farrukh_najmi Exp $
    -->

    <xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>

    <!--+
    |
    | Version info
    |
    +-->

    <xsl:variable name="stylesheet_version" select="'1.0'"/>


    <!--+
    |
    | Transform Parameters
    |
    +-->

    <xsl:param name="registryBaseURL" select="'http://localhost:8080/omar/registry'"/>
    <xsl:param name="action" select="'unknown'"/>
    <xsl:param name="user" select="'unknown'"/>
    

    <!--+
    |
    | Global Constants
    |
    +-->

    <xsl:variable name="pageTitle">FreebXML Registry - Event Notification</xsl:variable>


    <!--+
    |
    | Templates
    |
    +-->

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>


    <!-- Process the Registry Object List -->
    <xsl:template match="rim:RegistryObjectList">
        <html>
            <title>
                <xsl:value-of select="$pageTitle"/>
            </title>
            <body>
                <h2><xsl:value-of select="$pageTitle"/></h2>
                
                <table>    
                    <tr>
                        <td><b>Action:</b></td>
                        <td><xsl:value-of select="$action"/></td>
                    </tr>
                    <tr>
                        <td><b>User:</b></td>
                        <td><xsl:value-of select="$user"/></td>
                    </tr>
                </table>  
                <br/>
                
                <xsl:apply-templates/>
            </body>

        </html>
    </xsl:template>


    <!-- Process common stuff for each Registry Object -->
    <xsl:template match="rim:RegistryObjectList/*">

        <table border="1">
            <tr>
                <td colspan="2">
                    <xsl:value-of select="local-name()"/>
                </td>

            </tr>

            <tr>
                <td>ID:</td>
                <td>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="$registryBaseURL"/>/http?interface=QueryManager&amp;method=getRegistryObject&amp;param-id=<xsl:value-of select="@id"/>
                        </xsl:attribute>
                        <xsl:value-of select="@id"/>
                    </a>
                </td>
            </tr>

            <tr>
                <td>LID:</td>
                <td>
                    <xsl:value-of select="@lid"/>
                </td>
            </tr>

            <tr>
                <td>Object Type:</td>
                <td>
                    <xsl:value-of select="@objectType"/>
                </td>
            </tr>

            <tr>
                <td>Name:</td>
                <td>
                    <xsl:value-of select="rim:Name/rim:LocalizedString/@value"/>
                </td>
            </tr>

            <tr>
                <td>Description:</td>
                <td>
                    <xsl:value-of select="rim:Description/rim:LocalizedString/@value"/>
                </td>
            </tr>

            <tr>
                <td>Version:</td>
                <td>
                    <xsl:value-of select="rim:VersionInfo/@versionName"/>
                </td>
            </tr>

            <tr>
                <td>Version Comment:</td>
                <td>
                    <xsl:value-of select="rim:VersionInfo/@comment"/>
                </td>
            </tr>

            <!-- Process specific object type details -->
            <xsl:apply-templates select="." mode="objectDetails"/>
        </table>
        <br/>

    </xsl:template>


    <!-- Process Extrinsic Object -->
    <xsl:template match="rim:ExtrinsicObject" mode="objectDetails">
        <tr>
            <td>Registry Object:</td>
            <td>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$registryBaseURL"/>/http?interface=QueryManager&amp;method=getRegistryObject&amp;param-id=<xsl:value-of select="@id"/>
                    </xsl:attribute>
                    <xsl:value-of select="@id"/>
                </a>
            </td>
        </tr>
    </xsl:template>
    
    
    <!-- Process Organization -->
    <xsl:template match="rim:Organization" mode="objectDetails">
        <tr>
            <td>Primary Contact:</td>
            <td>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$registryBaseURL"/>/http?interface=QueryManager&amp;method=getRegistryObject&amp;param-id=<xsl:value-of select="@primaryContact"/>
                    </xsl:attribute>
                    <xsl:value-of select="@primaryContact"/>
                </a>
            </td>
        </tr>
        
        <tr>
            <td>Address:</td>
            <td>
                <xsl:value-of select="rim:Address/@streetNumber"/><xsl:value-of select="' '"/><xsl:value-of select="rim:Address/@street"/>
                <br/>               
                <xsl:value-of select="rim:Address/@city"/>, <xsl:value-of select="rim:Address/@stateOrProvince"/>          
                <br/>
                <xsl:value-of select="rim:Address/@country"/><xsl:text> </xsl:text><xsl:value-of select="rim:Address/@postalCode"/>
            </td>
        </tr>
        
        <tr>
            <td>Telephone Number:</td>
            <td>
                <xsl:value-of select="rim:TelephoneNumber/@phoneType"/>: <xsl:value-of select="rim:TelephoneNumber/@countryCode"/>-<xsl:value-of select="rim:TelephoneNumber/@areaCode"/>-<xsl:value-of select="rim:TelephoneNumber/@number"/>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="node() | @*">
        <xsl:apply-templates select=" @* | node()"/>
    </xsl:template>


</xsl:stylesheet>

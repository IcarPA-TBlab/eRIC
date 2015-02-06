<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- RegistryObject.xsl -->
<xsl:import href="http://localhost:8080/eric/registry/http?interface=QueryManager&amp;method=getRepositoryItem&amp;param-id=urn:uuid:0f4e8307-b208-4e91-97b6-aadd0134e3cd"/>

<xsl:template match="*" name="ServiceBinding">
    <xsl:call-template name="RegistryObject"/>
        <tr valign="top">
            <td colspan="2" width="49%">
            <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>Access URI:</b></font></p>
            </td>
        </tr>
        <tr valign="top">
            <td colspan="2" width="49%">
            <xsl:variable name="accessURI" select="@accessURI"/>
            <p><input type="text" name="ServiceBinding1" size="50" value="{$accessURI}"></input></p>
            </td>
        </tr>
</xsl:template>
</xsl:stylesheet>

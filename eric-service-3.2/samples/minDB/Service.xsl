<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- RegistryEntry.xsl -->
<xsl:import href="http://localhost:8080/eric/registry/http?interface=QueryManager&amp;method=getRepositoryItem&amp;param-id=urn:uuid:326a388d-d467-4f91-a31a-9da8c1a39cef"/>

<xsl:template match="*" name="Service">
<xsl:call-template name="RegistryEntry"/>
</xsl:template>
</xsl:stylesheet>

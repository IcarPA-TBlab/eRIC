<?xml version = "1.0" encoding = "UTF-8"?>
<!-- $Revision: 1.4 $ -->
<xsl:stylesheet version="1.0" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1" xmlns:rs="urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output indent="yes" method="xml"/>
  <xsl:strip-space elements="*"/>
  <xsl:output indent="yes" method="xml"/>
  <xsl:strip-space elements="*"/>
  <xsl:param name="repositoryItem"/>
  <xsl:template match="/">
    <!-- create the list -->
    <rim:RegistryObjectList>
      <!-- process all the rim:ExtrinsicObjects -->
      <xsl:apply-templates select="//rim:ExtrinsicObject"/>
    </rim:RegistryObjectList>
  </xsl:template>
  <xsl:template match="rim:ExtrinsicObject">
    <!-- store CPP in variable -->
    <xsl:variable name="profile" select="document($repositoryItem, .)"/>
    <!-- store Specification for convenience -->
    <xsl:variable name="Specification" select="$profile/Specification"/>
    <!-- another convenience... -->
    <xsl:variable name="hl7msg" select="$profile/Specification/Message"/>
    <!-- Make an ExtrinsicObject in the output -->
    <rim:ExtrinsicObject id="{@id}" objectType="{@objectType}" userVersion="{$Specification/@SpecVersion}">
      <!-- OrgName Slot -->
      <rim:Slot name="OrgName">
        <rim:ValueList>
          <rim:Value>
            <xsl:value-of select="$Specification/@OrgName"/>
          </rim:Value>
        </rim:ValueList>
      </rim:Slot>
      <rim:Slot name="Status">
        <rim:ValueList>
          <rim:Value>
            <xsl:value-of select="$Specification/@Status"/>
          </rim:Value>
        </rim:ValueList>
      </rim:Slot>
      <!-- The name is derived from the SpecName attriubute on the Specification -->
      <rim:Name>
        <rim:LocalizedString value="{$Specification/@SpecName}"/>
      </rim:Name>
      <!-- The description is derived from the Comments attriubute on the Specification? -->
      <rim:Description>
        <rim:LocalizedString value="{$Specification/@Comments}"/>
      </rim:Description>
      <!-- from HL7Version. Hardwire to some schemeId (HL7 Version - Table 0104) -->
      <rim:Classification classificationScheme="urn:freebxml:registry:demo:schemes:HL7" classifiedObject="{@id}" nodeRepresentation="{$Specification/@HL7Version}"/>
      <!-- from ConformanceType. Hardwire to some schemeId (HL7 ConformanceType - HL7Standard/Constrainable/Implementable ) -->
      <rim:Classification classificationScheme="urn:freebxml:registry:demo:schemes:HL7" classifiedObject="{@id}" nodeRepresentation="{$Specification/@ConformanceType}"/>
      <!-- from Role. Hardwire to some schemeId (HL7 Sender/Receiver roles) -->
      <rim:Classification classificationScheme="urn:freebxml:registry:demo:schemes:HL7" classifiedObject="{@id}" nodeRepresentation="{$Specification/@Role}"/>
      <!-- from MsgType. Hardwire to some schemeId (HL7 Messgage Type - Table 0076) -->
      <rim:Classification classificationScheme="urn:freebxml:registry:demo:schemes:HL7" classifiedObject="{@id}" nodeRepresentation="{$hl7msg/@MsgType}"/>
      <!-- from EventType. Hardwire to some schemeId (HL7 Event Type - Table 0003 -->
      <rim:Classification classificationScheme="urn:freebxml:registry:demo:schemes:HL7" classifiedObject="{@id}" nodeRepresentation="{$hl7msg/@EventType}"/>
      <!-- from MsgStructID. Hardwire to some schemeId (HL7 Messgage Structure - Table 0354) -->
      <rim:Classification classificationScheme="urn:freebxml:registry:demo:schemes:HL7" classifiedObject="{@id}" nodeRepresentation="{$hl7msg/@MsgStructID}"/>
    </rim:ExtrinsicObject>
    <!-- existing idForHL7Version. ObjctRef needed to satisfy validation -->
    <rim:ObjectRef id="urn:uuid:87286b7f-4f1f-4de7-8165-893d6ba35290"/>
    <!-- existing idForHL7ConformanceType. ObjctRef needed to satisfy validation -->
    <rim:ObjectRef id="urn:uuid:9a2b2bc7-9954-47b4-9587-86c03df5b522"/>
    <!-- existing idForHL7Role. ObjctRef needed to satisfy validation -->
    <rim:ObjectRef id="urn:uuid:0607a42b-c66e-4aaf-987c-e25f3edcfe6f"/>
    <!-- existing idForHL7MsgType. ObjctRef needed to satisfy validation -->
    <rim:ObjectRef id="urn:uuid:51b4633d-dcdf-471a-be16-ae93d5cda05d"/>
    <!-- existing idForHL7EventType. ObjctRef needed to satisfy validation -->
    <rim:ObjectRef id="urn:uuid:5844182d-1eed-4df7-9b2a-a9ec9bedbe19"/>
    <!-- existing idForHL7MsgStructID. ObjctRef needed to satisfy validation -->
    <rim:ObjectRef id="urn:uuid:39f6de53-4e17-41da-ad92-fe45700c93fe"/>
    <!-- existing id For HL7 Organization. ObjctRef needed to satisfy validation -->
    <!-- Check if ConformanceType = HL7; if so, link it to the HL7 Organization object -->
    <xsl:if test="$Specification/@ConformanceType = &quot;HL7&quot; ">
      <rim:Association associationType="ResponsibleFor" sourceObject="{@id}" targetObject="urn:uuid:3757f541-7263-40e4-9872-c5b29837b1f2"/>
      <rim:ObjectRef id="urn:uuid:3757f541-7263-40e4-9872-c5b29837b1f2"/>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>

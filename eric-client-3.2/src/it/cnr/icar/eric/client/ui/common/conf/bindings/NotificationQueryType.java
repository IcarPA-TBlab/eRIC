//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.12.20 at 09:38:53 AM CET 
//


package it.cnr.icar.eric.client.ui.common.conf.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotificationQueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NotificationQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0}RegistryObjectQueryType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0}RegistryObjectQuery" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotificationQueryType", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", propOrder = {
    "registryObjectQuery"
})
public class NotificationQueryType
    extends RegistryObjectQueryType
{

    @XmlElement(name = "RegistryObjectQuery")
    protected RegistryObjectQueryType registryObjectQuery;

    /**
     * Gets the value of the registryObjectQuery property.
     * 
     * @return
     *     possible object is
     *     {@link RegistryObjectQueryType }
     *     
     */
    public RegistryObjectQueryType getRegistryObjectQuery() {
        return registryObjectQuery;
    }

    /**
     * Sets the value of the registryObjectQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistryObjectQueryType }
     *     
     */
    public void setRegistryObjectQuery(RegistryObjectQueryType value) {
        this.registryObjectQuery = value;
    }

}

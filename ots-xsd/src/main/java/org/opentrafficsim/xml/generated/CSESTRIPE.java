//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.10 at 05:15:51 AM CET 
//


package org.opentrafficsim.xml.generated;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StripeTypeAdapter;
import org.opentrafficsim.xml.bindings.types.StripeType;


/**
 * <p>Java class for CSESTRIPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CSESTRIPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opentrafficsim.org/ots}CROSSSECTIONELEMENT"&gt;
 *       &lt;attribute name="TYPE" use="required" type="{http://www.opentrafficsim.org/ots}STRIPETYPE" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CSESTRIPE")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T05:15:51+01:00", comments = "JAXB RI v2.3.0")
public class CSESTRIPE
    extends CROSSSECTIONELEMENT
{

    @XmlAttribute(name = "TYPE", required = true)
    @XmlJavaTypeAdapter(StripeTypeAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T05:15:51+01:00", comments = "JAXB RI v2.3.0")
    protected StripeType type;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T05:15:51+01:00", comments = "JAXB RI v2.3.0")
    public StripeType getTYPE() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T05:15:51+01:00", comments = "JAXB RI v2.3.0")
    public void setTYPE(StripeType value) {
        this.type = value;
    }

}

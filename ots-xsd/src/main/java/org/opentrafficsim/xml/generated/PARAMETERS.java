//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.19 at 02:14:27 PM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element name="STRING" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPESTRING"/&gt;
 *           &lt;element name="ACCELERATION" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPEACCELERATION"/&gt;
 *           &lt;element name="BOOLEAN" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPEBOOLEAN"/&gt;
 *           &lt;element name="CLASS" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPECLASS"/&gt;
 *           &lt;element name="DOUBLE" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPEDOUBLE"/&gt;
 *           &lt;element name="DURATION" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPEDURATION"/&gt;
 *           &lt;element name="FRACTION" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPEFRACTION"/&gt;
 *           &lt;element name="FREQUENCY" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPEFREQUENCY"/&gt;
 *           &lt;element name="INTEGER" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPEINTEGER"/&gt;
 *           &lt;element name="LENGTH" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPELENGTH"/&gt;
 *           &lt;element name="LINEARDENSITY" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPELINEARDENSITY"/&gt;
 *           &lt;element name="SPEED" type="{http://www.opentrafficsim.org/ots}PARAMETERTYPESPEED"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "stringOrACCELERATIONOrBOOLEAN"
})
@XmlRootElement(name = "PARAMETERS")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-12-19T02:14:27+01:00", comments = "JAXB RI v2.3.0")
public class PARAMETERS
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-12-19T02:14:27+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "STRING", type = PARAMETERTYPESTRING.class),
        @XmlElement(name = "ACCELERATION", type = PARAMETERTYPEACCELERATION.class),
        @XmlElement(name = "BOOLEAN", type = PARAMETERTYPEBOOLEAN.class),
        @XmlElement(name = "CLASS", type = PARAMETERTYPECLASS.class),
        @XmlElement(name = "DOUBLE", type = PARAMETERTYPEDOUBLE.class),
        @XmlElement(name = "DURATION", type = PARAMETERTYPEDURATION.class),
        @XmlElement(name = "FRACTION", type = PARAMETERTYPEFRACTION.class),
        @XmlElement(name = "FREQUENCY", type = PARAMETERTYPEFREQUENCY.class),
        @XmlElement(name = "INTEGER", type = PARAMETERTYPEINTEGER.class),
        @XmlElement(name = "LENGTH", type = PARAMETERTYPELENGTH.class),
        @XmlElement(name = "LINEARDENSITY", type = PARAMETERTYPELINEARDENSITY.class),
        @XmlElement(name = "SPEED", type = PARAMETERTYPESPEED.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-12-19T02:14:27+01:00", comments = "JAXB RI v2.3.0")
    protected List<PARAMETERTYPE> stringOrACCELERATIONOrBOOLEAN;

    /**
     * Gets the value of the stringOrACCELERATIONOrBOOLEAN property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stringOrACCELERATIONOrBOOLEAN property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSTRINGOrACCELERATIONOrBOOLEAN().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PARAMETERTYPESTRING }
     * {@link PARAMETERTYPEACCELERATION }
     * {@link PARAMETERTYPEBOOLEAN }
     * {@link PARAMETERTYPECLASS }
     * {@link PARAMETERTYPEDOUBLE }
     * {@link PARAMETERTYPEDURATION }
     * {@link PARAMETERTYPEFRACTION }
     * {@link PARAMETERTYPEFREQUENCY }
     * {@link PARAMETERTYPEINTEGER }
     * {@link PARAMETERTYPELENGTH }
     * {@link PARAMETERTYPELINEARDENSITY }
     * {@link PARAMETERTYPESPEED }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-12-19T02:14:27+01:00", comments = "JAXB RI v2.3.0")
    public List<PARAMETERTYPE> getSTRINGOrACCELERATIONOrBOOLEAN() {
        if (stringOrACCELERATIONOrBOOLEAN == null) {
            stringOrACCELERATIONOrBOOLEAN = new ArrayList<PARAMETERTYPE>();
        }
        return this.stringOrACCELERATIONOrBOOLEAN;
    }

}

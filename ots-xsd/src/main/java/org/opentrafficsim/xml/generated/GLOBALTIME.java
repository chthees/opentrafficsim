//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.24 at 12:36:34 PM CET 
//


package org.opentrafficsim.xml.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.xml.bindings.TimeAdapter;


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
 *         &lt;element name="TIME" maxOccurs="unbounded" minOccurs="2"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="VALUE" use="required" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
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
    "time"
})
@XmlRootElement(name = "GLOBALTIME")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-24T12:36:34+01:00", comments = "JAXB RI v2.3.0")
public class GLOBALTIME {

    @XmlElement(name = "TIME", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-24T12:36:34+01:00", comments = "JAXB RI v2.3.0")
    protected List<GLOBALTIME.TIME> time;

    /**
     * Gets the value of the time property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the time property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTIME().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GLOBALTIME.TIME }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-24T12:36:34+01:00", comments = "JAXB RI v2.3.0")
    public List<GLOBALTIME.TIME> getTIME() {
        if (time == null) {
            time = new ArrayList<GLOBALTIME.TIME>();
        }
        return this.time;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="VALUE" use="required" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-24T12:36:34+01:00", comments = "JAXB RI v2.3.0")
    public static class TIME {

        @XmlAttribute(name = "VALUE", required = true)
        @XmlJavaTypeAdapter(TimeAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-24T12:36:34+01:00", comments = "JAXB RI v2.3.0")
        protected Time value;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-24T12:36:34+01:00", comments = "JAXB RI v2.3.0")
        public Time getVALUE() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-24T12:36:34+01:00", comments = "JAXB RI v2.3.0")
        public void setVALUE(Time value) {
            this.value = value;
        }

    }

}

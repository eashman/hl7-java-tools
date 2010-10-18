/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL72XMLTransform.java : The HL72XML Transform processor class.
 *
 *  Copyright (c) 2010  Scott Herman
 *
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package us.conxio.hl7.hl72xml;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import us.conxio.XMLUtilities.XMLUtils;

import us.conxio.hl7.hl7message.HL7Message;
import us.conxio.hl7.hl7service.HL7Transform;
import us.conxio.hl7.hl7system.HL7Logger;

/**
 * A translation class for specifying transformation of HL7 transaction message
 * content to XML. The specification itself is given in XML, and adheres to the
 * following form:
 * <p>
 * The entire specification is contained within a single XML elemental node,
 * with any legal (XML) name. The sub-elements which specify the form and content
 * of the resulting XML may have one of two names <b>element</b> or <b>attribute</b>.
 * <p>
 * Both items are structured similarly. They are defined by 3 attributes
 * <ul><li>name (required) The name of the resulting element or attribute.
 * <li>designator (optional) The HL7 message content item designation that is to
 * be expressed by the resulting element or attribute.
 * <br>(see <a href="../hl7message/HL7Designator.html">HL7Designator</a>)
 * 
 * <li>value (optional) A &quot;hardcoded&quot; value that is to be expressed by
 * the resulting element or attribute. Note that is cases where both a designator
 * and a value are given the value is used only when the designated HL7 message
 * content item is empty.
 * </ul>
 * <p>
 * An example transform specification is given below:
 * <p><pre>
 * {@code
 *  <specification-element-name>
 *     <element name="root-element">
 *        <attribute name="id" value="unknown" designator="MSH.9" />
 *        <element name="sub-element-name" value="default-value-string" designator="PID.3:2.1" />
 *     </element>
 *  </specification-element-name>
 * }
 * </pre><p>
 * This will yield the following XML...
 * <p><pre>
 * {@code
 * <root-element id="43718854"><sub-element-name>94556</sub-element-name></root-element>
 * }
 * </pre><p>
 * When the HL7 transaction message below is applied to it.
 * <p><pre>
 * {@code
 * MSH|^~\&|XDX|SX|DXD|XS|200902100941||ADT^Z08|43718854|P|2.3||
 * EVN|Z08|||||
 * ZPD|||3813315^^someh.org^MRN~94556^^someh.org^MPI|3812240|||||||||
 * PD1||||||||||||
 * NK1||^^|||||EC|||||||||||||||||||||||^^|||||||
 * PV1||||||||||||||||||||81||||||||||||||||||||||||||||||||
 * }
 * </pre><p>
 *
 * @author scott
 */
public class HL72XMLTransform {
   private String                   name = null;
   private ArrayList<HL7Transform>  prep = null;
   private ElementSpec              specRoot = null;
   private static Logger            logger = HL7Logger.getHL7Logger();

   
   private static final String NAME_HL7TRANSFORM = HL7Transform.NAME_HL7TRANSFORM;


   /**
    * Reads a HL7 to XML specification from a URI and creates a transform object,
    * which can subsequently be invoked to transform a HL7 transaction message
    * to a XML String.
    * @param uri The URI form which to read the transform specification.
    */
   public HL72XMLTransform(URI uri) {
      this(XMLUtils.readXML(uri));
   } // HL72XMLTransform constructor


   /**
    * Reads a HL7 to XML specification from a InputStream and creates a transform
    * object, which can subsequently be invoked to transform a HL7 transaction
    * message to a XML String.
    * @param inStream The stream form which to read the transform specification.
    */
   public HL72XMLTransform(InputStream inStream) {
      this(XMLUtils.readXML(inStream));
   } // HL72XMLTransform constructor


   /**
    * Reads a HL7 to XML specification from a XML String and creates a transform
    * object, which can subsequently be invoked to transform a HL7 transaction
    * message to a XML String.
    * @param xmlStr The String object containing the XML transform specification.
    */
   public HL72XMLTransform(String xmlStr) {
      this(XMLUtils.readXML(xmlStr));
   } // HL72XMLTransform constructor


   /**
    * Reads a HL7 to XML specification from a org.w3c.dom document node and creates
    * a transform object, which can subsequently be invoked to transform a HL7
    * transaction message to a XML String.
    * @param node The document node containing the transform specification.
    */
   HL72XMLTransform(Node node) {
      if (node == null) throw new IllegalArgumentException("null argument.");

      name = node.getNodeName();
      if (node.hasChildNodes()) {
         NodeList kids = node.getChildNodes();
         int numKids = kids.getLength();
         for (int index = 0; index < numKids; ++index) parseChildNode(kids.item(index));
      } // if
   } // HL72XMLTransform constructor


   private void parseChildNode(Node node) {
      if (node == null) return;

      String nodeName = node.getNodeName();

      if (nodeName.equalsIgnoreCase(HL72XMLSpecificationItem.NAME_ELEMENT)) {
         specRoot = new ElementSpec(node);
      } else if (nodeName.equalsIgnoreCase(NAME_HL7TRANSFORM)) {
         addPreparatoryHL7Transform(node);
      } else {
         logger.error("Unexpected element:" + nodeName);
      } // if - else if - else
   } // parseChildNode

   private void addPreparatoryHL7Transform(Node node) {
      if (node == null) return;
      if (prep == null) prep = new ArrayList<HL7Transform>();

      HL7Transform xForm = new HL7Transform(node);
      if (xForm != null) prep.add(xForm);
   } // addPreparatoryHL7Transform


   /**
    * Transforms a HL7 transaction message in accordance with the context transform
    * specification.
    * @param hl7Msg The HL7Message to be transformed.
    * @return A String object containing the specified XML translation of the
    * argument HL7Message. If the message does not qualify for transformation,
    * according to the transform specification, the returned String object will
    * be empty.
    */
   public String transform(HL7Message hl7Msg) {
      if (!isQualified(hl7Msg)) return "";

      HL7Message preparedMsg = prepare(hl7Msg);
      return specRoot.toXMLString(preparedMsg);
   } // transform


   /**
    * Transforms a HL7 transaction message String in accordance with the context
    * transform specification.
    * @param hl7MsgStr The HL7 transaction message to be transformed, as a String.
    * @return A String object containing the specified XML translation of the
    * argument HL7 transaction message String. If the message does not qualify
    * for transformation, according to the transform specification, the returned
    * String object will be empty.
    */
   public String transform(String hl7MsgStr) {
      return transform(new HL7Message(hl7MsgStr));
   } // transform

   
   private HL7Message prepare(HL7Message hl7Msg) {
      if (hl7Msg == null) return null;
      if (!hasPrep()) return hl7Msg;

      for (HL7Transform xForm : prep) {
         if (xForm.isQualified(hl7Msg)) hl7Msg = xForm.render(hl7Msg);
      } // for

      return hl7Msg;
   } // prepare


   private boolean hasPrep() {
      return prep != null &&!prep.isEmpty();
   } // hasPrep

   /**
    * Determines whether the argument HL7Message is qualified for transformation
    * by context transform specification.
    * @param hl7Msg The subject HL7Message to be evaluated for qualification.
    * @return true if the argument HL7Message object qualifies. Otherwise false.
    */
   public boolean isQualified(HL7Message hl7Msg) {
      if (hl7Msg == null) return false;
      if (!hasPrep()) return true;

      for (HL7Transform xForm : prep) {
         if (xForm.isQualified(hl7Msg)) return true;
      } // for

      return false;
   } // isQualified
} // HL72XMLTransform

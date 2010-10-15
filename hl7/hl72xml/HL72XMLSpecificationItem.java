/*
 *  $Id: $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL72XMLSpecificationItem.java : The abstract base HL72XML specification component class.
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

import org.apache.commons.lang.StringUtils;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import us.conxio.hl7.hl7message.HL7Message;

/**
 *
 * @author scott
 */
abstract class HL72XMLSpecificationItem {
   private String sourceDesignator;
   private String name;
   private String value;

   public static final String NAME_ELEMENT         = "element";
   public static final String NAME_ATTRIBUTE       = "attribute";
   public static final String ATTRIBUTE_NAME       = "name";
   public static final String ATTRIBUTE_DESIGNATOR = "designator";
   public static final String ATTRIBUTE_VALUE      = "value";

   HL72XMLSpecificationItem(String desginatorStr, String nameStr, String valueStr) {
      sourceDesignator = desginatorStr;
      name = nameStr;
      value = valueStr;
   } // HL72XMLSpecificationItem


   HL72XMLSpecificationItem(Node node) {
      if (node == null) throw new IllegalArgumentException("null argument.");

      String nodeName = node.getNodeName();
      if (!(nodeName.equalsIgnoreCase(NAME_ELEMENT)
      ||    nodeName.equalsIgnoreCase(NAME_ATTRIBUTE))) {
         throw new IllegalArgumentException("unexpected element name:" + nodeName);
      } // if

      NamedNodeMap attributes = node.getAttributes();
      int attribCount = attributes.getLength();
      for (int index = 0; index < attribCount; ++index) {
         Node attribNode = attributes.item(index);
         String attribName = attribNode.getNodeName();
         if (attribName.equalsIgnoreCase(ATTRIBUTE_NAME)) {
            name = attribNode.getNodeValue();
         } else if (attribName.equalsIgnoreCase(ATTRIBUTE_DESIGNATOR)) {
            sourceDesignator = attribNode.getNodeValue();
         } else if (attribName.equalsIgnoreCase(ATTRIBUTE_VALUE)) {
            value = attribNode.getNodeValue();
         } else {
            throw new IllegalArgumentException("unexpected attribute:"
                                             + attribName
                                             + ":"
                                             + attribNode.getNodeValue());
         } // if - else if
      } // for
   } // HL72XMLSpecificationItem


   String getContent(HL7Message hl7Msg) {
      if (hasDesignator()) {
         String hl7Content = hl7Msg.get(designator());
         if (StringUtils.isNotEmpty(hl7Content)) return hl7Content;
      } // if

      if (hasValue()) return value();
      return "";
   } // getContent


   boolean hasDesignator() {
      return StringUtils.isNotEmpty(sourceDesignator);
   } // hasDesignator

   boolean hasValue() {
      return StringUtils.isNotEmpty(value);
   } // hasValue

   boolean hasName() {
      return StringUtils.isNotEmpty(name);
   } // hasName

   /**
    * @return the sourceDesignator
    */
   String designator() {
      return sourceDesignator;
   } // designator

   /**
    * @return the name
    */
   String name() {
      return name;
   } // name

   /**
    * @return the value
    */
   String value() {
      return value;
   } // value

} // HL72XMLSpecificationItem

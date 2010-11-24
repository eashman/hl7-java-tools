/*
 * $Id$
 * $HeadURL$
 *
 * This code is derived from public domain sources. Commercial use is allowed.
 * However, all rights remain permanently assigned to the public domain.
 *
 * AttributeSpecification.java : A HL72XML specification component class.
 *
 * Copyright (c) 2010  Scott Herman
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package us.conxio.hl7.hl72xml;

import java.util.ArrayList;

import org.w3c.dom.Node;

import us.conxio.XMLUtilities.AttributeMap;
import us.conxio.hl7.hl7message.HL7Message;

/**
 *
 * @author scott
 */
class AttributeSpecification extends HL72XMLSpecificationItem {

   AttributeSpecification(String desginatorStr, String nameStr, String valueStr) {
      super(desginatorStr, nameStr, valueStr);
   } // AttributeSpecification constructor

   AttributeSpecification(Node node) {
      super(node);
   } // AttributeSpecification constructor

   protected String toXMLString() {
      String argV = "";
      if (!hasName()) return "";
      if (hasValue()) argV = value();
      return toXMLString(argV);
   } // toXMLString


   private String toXMLString(String str) {
      if (!hasName()) return "";

      return new StringBuffer(name())
              .append("=\"")
              .append(str)
              .append("\"").toString();
   } // toXMLString

   protected String toXMLString(HL7Message msg) {
      return toXMLString(getContent(msg));
   } // toXMLString


   static AttributeMap attributeMap(ArrayList<AttributeSpecification> attributes, HL7Message msg) {
      if (attributes == null || attributes.isEmpty()) return null;
      AttributeMap map = new AttributeMap();
      for (AttributeSpecification spec : attributes) map.add(spec.name(), spec.getContent(msg));
      return map;
   } // attributeMap


} // AttributeSpecification

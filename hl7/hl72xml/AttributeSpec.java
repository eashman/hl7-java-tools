/*
 *  $Id: $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  AttributeSpec.java : A HL72XML specification component class.
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

import java.util.ArrayList;

import org.w3c.dom.Node;

import us.conxio.XMLUtilities.AttributeMap;
import us.conxio.hl7.hl7message.HL7Message;

/**
 *
 * @author scott
 */
class AttributeSpec extends HL72XMLSpecificationItem {

   AttributeSpec(String desginatorStr, String nameStr, String valueStr) {
      super(desginatorStr, nameStr, valueStr);
   } // AttributeSpec constructor

   AttributeSpec(Node node) {
      super(node);
   } // AttributeSpec constructor

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
      String xtrV = "";
      if (hasDesignator()) {
         xtrV = msg.get(designator());
      } else if (hasValue()) {
         xtrV = value();
      } // if - else

      return this.toXMLString(xtrV);
   } // toXMLString


   static AttributeMap attributeMap(ArrayList<AttributeSpec> attributes, HL7Message msg) {
      if (attributes == null || attributes.isEmpty()) return null;

      AttributeMap map = new AttributeMap();
      for (AttributeSpec spec : attributes) {
         if (spec.hasDesignator()) {
            map.add(spec.name(), msg.get(spec.designator()));
         } else if (spec.hasValue()) {
            map.add(spec.name(), spec.value());
         } // if - else if
      } // for

      return map;
   } // attributeMap


} // AttributeSpec

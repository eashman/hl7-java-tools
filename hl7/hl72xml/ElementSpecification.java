/*
 *  $Id$
 *  $HeadURL$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  ElementSpecification.java : A HL72XML specification component class.
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
import org.w3c.dom.NodeList;

import us.conxio.XMLUtilities.AttributeMap;
import us.conxio.XMLUtilities.XMLUtils;

import us.conxio.hl7.hl7message.HL7Message;

/**
 *
 * @author scott
 */
class ElementSpecification extends HL72XMLSpecificationItem {
   ArrayList<AttributeSpecification>   attributes;
   ArrayList<ElementSpecification>     subElements;

   ElementSpecification(String desginatorStr, String nameStr, String valueStr) {
      super(desginatorStr, nameStr, valueStr);
   } // ElementSpecification constructor

   ElementSpecification(Node node) {
      super(node);

      if (node.hasChildNodes()) {
         NodeList kids = node.getChildNodes();
         int numKids = kids.getLength();
         for (int index = 0; index < numKids; ++index) parseChildNode(kids.item(index));
      } // if
   } // ElementSpecification constructor


   private void parseChildNode(Node node) {
      if (node == null) return;

      String nodeName = node.getNodeName();

      if (nodeName.equalsIgnoreCase(HL72XMLSpecificationItem.NAME_ELEMENT)) {
         addSubElement(new ElementSpecification(node));
      } else if (nodeName.equalsIgnoreCase(HL72XMLSpecificationItem.NAME_ATTRIBUTE)) {
         addAttribute(new AttributeSpecification(node));
      } // if - else if
   } // parseChildNode


   private void addSubElement(ElementSpecification elementSpec) {
      if (elementSpec == null) return;
      if (subElements == null) subElements = new ArrayList<ElementSpecification>();
      subElements.add(elementSpec);
   } // addSubElement


   private void addAttribute(AttributeSpecification attributeSpec) {
      if (attributeSpec == null) return;
      if (attributes == null) attributes = new ArrayList<AttributeSpecification>();
      attributes.add(attributeSpec);
   } // addAttribute


   private String toXMLString(String content, AttributeMap attribs) {
      return XMLUtils.elementString(name(), attribs, content);
   } // toXMLString


   String toXMLString(HL7Message msg) {
      String contentStr = hasSubElements()
                           ? toSubElementalContentString(msg)
                           : getContent(msg);

      AttributeMap attr = hasAttributes()
                           ? AttributeSpecification.attributeMap(attributes, msg)
                           : null;
      return toXMLString(contentStr, attr);
   } // toXMLString


   private boolean hasAttributes() {
      return attributes != null && !attributes.isEmpty();
   } // hasAttributes

   private boolean hasSubElements() {
      return subElements != null && !subElements.isEmpty();
   } // hasSubElements

   private String toSubElementalContentString(HL7Message msg) {
      if (!hasSubElements()) return "";

      StringBuilder contentBuilder = new StringBuilder();
      for (ElementSpecification element : subElements) {
         contentBuilder.append(element.toXMLString(msg));
      } // for

      return contentBuilder.toString();
   } // toSubElementalContentString

} // ElementSpecification

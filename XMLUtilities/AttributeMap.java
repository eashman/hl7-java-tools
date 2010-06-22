/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  AttributeMap.java : A constrained HashMap class for handling of XML
 *                      attribute key value pairs.
 *
 *  Copyright (C) 2009, 2010  Scott Herman
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

package us.conxio.XMLUtilities;


import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A map class for XML attributes.
 * @author scott
 */
public class AttributeMap {
   private HashMap attributeMap = null;

   /**
    * Simple constructor.
    */
   public AttributeMap() {
      this.attributeMap = new HashMap();
   } // AttributeMap constructor


   /**
    * Add a name value attribute pair to the map.
    * @param name   The name of the attribute to be added.
    * @param value  The value, as a String, of the added attribute.
    * @return       The attribute map to whcih the argument name value pair was added.
    */
   public AttributeMap add(String name, String value) {
      this.attributeMap.put(name, value);
      return this;
   } // add


   /**
    * Retrieves the value of the argument named attribute.
    * @param name   The name of an attribute, in the attribute map.
    * @return       The value of the argument attribute, as a String.
    */
   public String get(String name) {
      return (String)this.attributeMap.get(name);
   } // get


   /**
    * Return the entire attribute map as a string.
    * @return A String representation of the entire attribute map, suitable for
    * inclusion in an XML element.
    */
   @Override
   public String toString() {
      if (this.attributeMap.isEmpty()) {
         return "";
      } // if

      StringBuffer buildBuffer = new StringBuffer();
      Set attributeSet = this.attributeMap.entrySet();
      Iterator attributeIterator = attributeSet.iterator();
      while(attributeIterator.hasNext()) {
         Map.Entry attributeEntry = (Map.Entry)attributeIterator.next();
         String attributeKey = (String)attributeEntry.getKey();
         String attributeValue = (String)attributeEntry.getValue();
         if (attributeKey != null && !attributeKey.isEmpty()) {
            buildBuffer.append(" ").append(attributeKey).append("=\"");
            if (attributeValue != null) {
               buildBuffer.append(attributeValue);
            } // if
            buildBuffer.append("\"");
         } // if
      } // while

      return buildBuffer.toString();
   } // toString


   public static AttributeMap getAttributes(Node node, String[] allowed) {
      if (node == null) return null;
      if (!node.hasAttributes()) return null;

      AttributeMap newMap = new AttributeMap();

      NamedNodeMap attribs = node.getAttributes();
      int attribCount = attribs.getLength();
      for (int index = 0;index < attribCount; ++index) {
         Node attribNode = attribs.item(index);
         String attribName = attribNode.getNodeName().toLowerCase();
         if (XMLUtils.equalsAny(attribName, allowed)) {
            newMap.add(attribName, attribNode.getNodeValue());
         } // if
      } // for

      if (newMap.entryCount() > 0) {
         return newMap;
      } // if

      return null;
   } // getAttributes


   public boolean hasKey(String key) {
      return this.attributeMap.containsKey(key.toLowerCase());
   } // hasKey


   public void remove(String key) {
      this.attributeMap.remove(key.toLowerCase());
   } // remove
   

   /**
    * Returns the number of attribute entries in the map.
    * @return the number of attribute entries in the map.
    */
   public int entryCount() {
      return this.attributeMap.size();
   } // entryCount
   
} // AttributeMap

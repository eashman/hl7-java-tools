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
   private HashMap<String, String> attributeMap = null;

   /**
    * Simple constructor.
    */
   public AttributeMap() {
      attributeMap = new HashMap();
   } // AttributeMap constructor


   /**
    * Construct an AttributeMap from an argument org.w3c.dom.Node
    * @param node The node from which to construct the AttrtibuteMap.
    */
   public AttributeMap(Node node) {
      this();
      _getAttributes(node);
   } // AttributeMap constructor

   private AttributeMap _add(String name, String value) {
      if (name == null) return this;
      attributeMap.put(name.toLowerCase(), value);
      return this;
   } // _add


   /**
    * Add a name value attribute pair to the map.
    * @param name   The name of the attribute to be added.
    * @param value  The value, as a String, of the added attribute.
    * @return       The attribute map to which the argument name value pair was added.
    */
   public AttributeMap add(String name, String value) { return _add(name, value); }

   /**
    * Retrieves the value of the argument named attribute.
    * @param name   The name of an attribute, in the attribute map.
    * @return       The value of the argument attribute, as a String.
    */
   public String get(String name) {
      if (name == null) return null;
      return (String) attributeMap.get(name.toLowerCase());
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

      StringBuilder buildBuffer = new StringBuilder();
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


   private void _getAttributes(Node node) {
      if (node == null) return;
      if (!node.hasAttributes()) return;

      NamedNodeMap attribs = node.getAttributes();
      int attribCount = attribs.getLength();
      for (int index = 0;index < attribCount; ++index) {
         Node attribNode = attribs.item(index);
         String attribName = attribNode.getNodeName().toLowerCase();
         _add(attribName, attribNode.getNodeValue());
      } // for
   } // _getAttributes


   /**
    * A static org.w3c.dom.Node extraction constructor.
    * @param node The node from which to construct the returned AttributeMap.
    * @param allowed A String array of allowed attribute names.
    * @return The resulting AttributeMap, or null, if the operation fails.
    */
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
            newMap._add(attribName, attribNode.getNodeValue());
         } // if
      } // for

      if (newMap.entryCount() > 0) return newMap;

      return null;
   } // getAttributes


   /**
    * A static org.w3c.dom.Node extraction constructor.
    * @param node The node from which to construct the returned AttributeMap
    * @return The resulting AttributeMap, or null, if the operation fails.
    */
   public static AttributeMap getAttributes(Node node) {
      if (node == null) return null;
      if (!node.hasAttributes()) return null;

      AttributeMap newMap = new AttributeMap(node);
      if (newMap.entryCount() > 0) return newMap;

      return null;
   } // getAttributes


   /**
    * Determines whether the context AttributeMap contains the argument key.
    * @param key A String representation of the argument key.
    * @return true if the AttributyeMap contains the argument key, otherwise false.
    */
   public boolean hasKey(String key) {
      if (key == null) return false;
      return attributeMap.containsKey(key.toLowerCase());
   } // hasKey


   /**
    * Removes the entry associated with the argument key.
    * @param key The key associated with the entry to be removed.
    */
   public AttributeMap remove(String key) {
      if (key == null) return this;
      if (!hasKey(key)) return this;
      attributeMap.remove(key.toLowerCase());
      return this;
   } // remove
   

   /**
    * Returns the number of attribute entries in the map.
    * @return the number of attribute entries in the map.
    */
   public int entryCount() {
      return attributeMap.size();
   } // entryCount

   boolean isNotEmpty() {
      return entryCount() > 0;
   } // isNotEmpty

   boolean isEmpty() {
      return entryCount() < 1;
   } // isEmpty


   public static AttributeMap newInstance(AttributeMap oldInstance) {
      AttributeMap retn = new AttributeMap();
      Set<String> keySet = oldInstance.attributeMap.keySet();

      for (String key : keySet) retn.add(key, oldInstance.get(key));
      return retn;
   } // newInstance
} // AttributeMap

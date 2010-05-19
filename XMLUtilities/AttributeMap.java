/*
 *  $Id: AttributeMap.java 71 2010-05-05 12:36:40Z scott $
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

/**
 *
 * @author scott
 */
public class AttributeMap {
   private HashMap attributeMap = null;

   public AttributeMap() {
      this.attributeMap = new HashMap();
   } // AttributeMap constructor


   public AttributeMap add(String name, String value) {
      this.attributeMap.put(name, value);
      return this;
   } // add


   public String get(String name) {
      return (String)this.attributeMap.get(name);
   } // get


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
   
} // AttributeMap

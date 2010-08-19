/*
 *  $Id$
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7SubComponent.java : Provides access to parsed HL7 message sub component data.
 *
 *  Copyright (c) 2009, 2010  Scott Herman
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

package us.conxio.hl7.hl7message;

/**
 *
 * @author scott
 */
public class HL7SubComponent implements HL7Element {
   private   HL7ElementLevel        level;
   protected String                 content;
   private boolean                  touched;

   public HL7SubComponent() {
      this.level = new HL7ElementLevel(HL7ElementLevel.SUBCOMPONENT);
   } // HL7SubComponent


   public HL7SubComponent(String subCompStr, HL7Encoding encoders) {
      this();
      this.content = subCompStr;
   } // HL7SubComponent


   public boolean hasContent() {
      if (this.content != null && !this.content.isEmpty()) {
         return true;
      } // if

      return false;
   } // hasContent


   public boolean hasConstituents() {
      return false;
   } // hasConstituents

   
   public String getContent() {
      return this.content;
   } // getContent


   public int getLevel() {
      return this.level.get();
   } // getLevel


   public HL7Element getElement(int index) {
      return null;
   } // getElement


   public void set(String msgText, HL7Encoding encoders) {
      this.content = msgText;
      touched = true;
   } // set


   public String toHL7String(HL7Encoding encoders) {
      if (this.content == null) {
         return "";
      } // if

      return this.content;
   } // if

   
   public String toXMLString(int subComponentIndex) {
      String tag = "subComponent";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(subComponentIndex))
              .append("\">");

      if (this.content == null) {
         returnBuffer.append("");
      } else {
         returnBuffer.append(this.content);
      } // if - else

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


} // HL7SubComponent

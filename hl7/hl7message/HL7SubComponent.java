/*
 *  $Id$
 *  $HeadURL$
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
 * A HL7 element class for sub-component level items.
 * @author scott
 */
public class HL7SubComponent implements HL7Element {
   private   HL7ElementLevel        level;
   protected String                 content;
   private boolean                  touched;


   /**
    * Creates a new empty HL7SubComponent object.
    */
   public HL7SubComponent() {
      level = new HL7ElementLevel(HL7ElementLevel.SUBCOMPONENT);
   } // HL7SubComponent


   /**
    * Creates a new HL7SubComponent object and populates it with the argument content.
    * @param subCompStr The content with which to populate the HL7SubComponent.
    */
   public HL7SubComponent(String subCompStr) {
      this();
      content = subCompStr;
   } // HL7SubComponent


   /**
    * Determines whether the context HL7SubComponent contains any content.
    * @return true if the HL7SubComponent has content. Otherwise false.
    */
   public boolean hasContent() {
      return content != null && !content.isEmpty();
  } // hasContent


   /**
    * Always returns false.
    * @return false
    */
   public boolean hasConstituents() {
      return false;
   } // hasConstituents


   /**
    * Retrieves the content of the context HL7SubComponent.
    * @return A String representation of the content of the context HL7SubComponent.
    */
   public String getContent() {
      return content;
   } // getContent


   /**
    * @return The level of the context HL7SubComponent.
    */
   public int getLevel() {
      return level.get();
   } // getLevel


   /**
    * Always returns null
    * @param index
    * @return null
    */
   public HL7Element getElement(int index) {
      return null;
   } // getElement


   /**
    * Sets the content of the context HL7SubComponent to the argument text.
    * @param msgText The text to set the content to.
    * @param encoders ignored.
    */
   public void set(String msgText, HL7Encoding encoders) {
      content = msgText;
      touched = true;
   } // set


   /**
    * @param encoders ignored.
    * @return The content of the context HL7SubComponent.
    */
   public String toHL7String(HL7Encoding encoders) {
      if (content == null) return "";
      return encoders.escape(content);
   } // if


   /**
    * @param subComponentDesignator An integral sub-component designator.
    * @return A XML String representation of the content of the context HL7SubComponent.
    */
   public String toXMLString(int subComponentDesignator) {
      String tag = "subComponent";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(subComponentDesignator))
              .append("\">");

      returnBuffer.append(content == null ? "" : content);
      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


} // HL7SubComponent

/*
 *  $Id$
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Segment.java : Provides access to parsed HL7 message segment data.
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


import java.util.ArrayList;


/**
 * A HL7 element class for segment level items.
 * @author scott
 */
public class HL7Segment implements HL7Element {
   private String                idStr;
   private HL7ElementLevel       level;
   protected ArrayList<HL7Field> fields = null;
   private boolean               touched;


   /**
    * Creates a new empty HL7Segment object at the segment level.
    */
   public HL7Segment() {
      level = new HL7ElementLevel(HL7ElementLevel.SEGMENT);
   } // HL7Segment


   /**
    * Creates a HL7Segment object form the argument string, using the argument
    * set of HL7 encoding characters.
    * @param segmentStr The segment to be parsed, as a String object.
    * @param encoders The HL7 encoding character set to use in parsing this segment.
    */
   public HL7Segment(String segmentStr, HL7Encoding encoders) {
      this();
      idStr = segmentStr.substring(0, 3);
      _set(segmentStr, encoders);
   } // HL7Segment


   /**
    * @return A String representation of the 3 character segment ID
    */
   private String getIDString() {
      return this .getField(0)
                  .getRepetition(0)
                  .getComponent(0)
                  .getSubComponent(0)
                  .getContent();
   } // getIDString


   /**
    * Retrieves the segment ID, that is the content of field(0).
    * @return The 3 character segment ID string.
    */
   public String getID() {
      if (idStr == null || idStr.isEmpty()) idStr = getIDString();  
      return idStr;
   } // getID


   /**
    * Retrieves the field corresponding to the argument index.
    * @param index The index of the field to retrieve.
    * @return The field at the argument index, or null if the field does not exist.
    */
   public HL7Field getField(int index) {
      if (!hasFields() || !hasField(index)) return null;
      return fields.get(index);
   } // getField


   /**
    * Returns the state of the touched flag.
    * @return the state of the touched flag, true or false.
    */
   public boolean wasTouched() {
      return touched;
   } // wasTouched


   private void _set(String msgText, HL7Encoding encoders) {
      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.FIELD);
      ArrayList<String>  elements = encoders.hl7Split(msgText, nextLevel);
      fields = new ArrayList<HL7Field>();
      for (String elementStr : elements) fields.add(new HL7Field(elementStr, encoders));
      touched = true;
   } // set


   /**
    * Parses the argument segment text into the segment using the argument HL7 encoding
    * character set.
    * @param segText The segment text to be parsed.
    * @param encoders The HL7 encoding character set to be used to parse the segment text.
    * Note that this method will dispose of any prior data residing in the segment.
    */
   public void set(String segText, HL7Encoding encoders) { _set(segText, encoders); }


   /**
    * Formulates and returns the context segment as a String of HL7 text, using
    * the argument HL7 encoding character set.
    * @param encoders The HL7 encoding character set to use to delimit the segment text.
    * @return The segment text as a HL7 String.
    */
   public String toHL7String(HL7Encoding encoders) {
      if (!hasFields()) return "";

      ArrayList<String> fieldStrings = new ArrayList<String>();
      for (HL7Field field : fields) fieldStrings.add(field.toHL7String(encoders));
      
      if (idStr.equals("MSH") || idStr.equals("BHS") ) {
         // special handling for encoding charcters.
         // remove 2nd field and replace with encoding characters
         // as a field.
         fieldStrings.remove(1);
         fieldStrings.add(1, encoders.toString().substring(1));
      } // if

      return encoders.hl7Join(fieldStrings, level.next());
   } // toHL7String


   /**
    * Formulates and returns a XML String representation of the context HL7 segment.
    * @return A XML String representation of the context HL7 segment.
    */
   public String toXMLString() {
      return toXMLString(null);
   } // toXMLString


   /**
    * Formulates and returns a XML String representation of the context HL7 
    * segment, using the argument HL7 encoding character set, if the segment is 
    * a MSHG segment.
    * @param encoders The HL7 encoding character set to be represented, if the 
    * segment is a MSH segment.
    * @return A XML String representation of the context HL7 segment.
    */
   public String toXMLString(HL7Encoding encoders) {
      String tag = "Segment";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(this.idStr)
              .append("\">");


      int fieldOffset = 0;
      if (idStr.equals("MSH")) {
         fieldOffset = 1;
         if (encoders != null) returnBuffer.append(encoders.toXMLString());
      } // if

      int fieldIndex = 0;
      for (HL7Field field : fields) {
         if (fieldIndex > 0 && field.hasContent()) {
            returnBuffer.append(field.toXMLString(fieldIndex + fieldOffset));
         } // if
         ++fieldIndex;
      } // for

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


   /**
    * An alias for getField()
    * @param index The index of the field to retrieve.
    * @return The HL7Filed at the argument index, or null, if the operation fails.
    */
   public HL7Element getElement(int index) {
      return getField(index);
   } // getElement


   /**
    * @return Always returns false
    */
   public boolean hasContent() {
      return false;
   } // hasContent


   /**
    * @return true if the context HL7Segment contains one or more fields.
    * Otherwise false.
    */
   public boolean hasFields() {
      return fields != null && !fields.isEmpty();
   } // hasFields


   /**
    * @param index The index at which to check for the existence of a field.
    * @return true if the context HL7Segment contains a filed at the argument index.
    * Otherwise false.
    */
   public boolean hasField(int index) {
      return hasFields() && index >= 0 && index < fields.size();
   } // hasField


   /**
    * @return A count of fields contained in the context HL7Segment.
    */
   int fieldCount() {
      return hasFields() ? fields.size() : 0;
   } // fieldCount


   /**
    * Adds an empty field to the context HL7Segment.
    */
   private void addField() {
      HL7Field field = new HL7Field();
      field.addRepetition();

      if (!hasFields()) fields = new ArrayList<HL7Field>();
      fields.add(field);
   } // addField


   /**
    * Returns the field from the context HL7Segment, which is located at the
    * argument sequence index.
    * @param sequence The sequence index specifying the field to retrieve.
    * @param create A boolean indicating whether the field should be created
    * if it does not exist.
    * @return the specified field
    */
   HL7Field pickField(int sequence, boolean create) {
      if (!hasField(sequence)) {
         if (!create) return null;
         for (int newIndex = fieldCount(); newIndex <= sequence; ++newIndex) addField();
      } // if

      return getField(sequence);
   } // pickField


   /**
    * Adds an empty field at the argument index, which implies, correctly that any
    * missing fields at lesser indices are also added.
    * @param index The index of the field to add.
    */
   public void addField(int index) {
      if (hasField(index)) return;

      if (fields == null) fields = new ArrayList<HL7Field>();
      if (index >= fields.size()) {
         while (index >= fields.size()) fields.add(new HL7Field());
      } // if
   } // addField

} // HL7Segment

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;


import java.util.ArrayList;
/**
 *
 * @author scott
 */
public class HL7Segment implements HL7Element {
   private String                idStr;
   private HL7ElementLevel       level;
   protected ArrayList<HL7Field> fields = null;
   private boolean               touched;


   
   public HL7Segment() {
      this.level = new HL7ElementLevel(HL7ElementLevel.SEGMENT);
   } // HL7Segment


   public HL7Segment(String segmentStr, HL7Encoding encoders) {
      this();
      this.idStr = segmentStr.substring(0, 3);
      this.set(segmentStr, encoders);
   } // HL7Segment


   private String getIDString(HL7Element element) {
      return this .getField(0)
                  .getRepetition(0)
                  .getComponent(0)
                  .getSubComponent(0)
                  .getContent();
   } // getIDString


   public String getID() {
      if (this.idStr == null || this.idStr.isEmpty()) {
         this.idStr = getIDString(this);
      } // if
      
      return this.idStr;
   } // getID


   public HL7Field getField(int index) {
      if (!this.hasFields() || !this.hasField(index)) {
         return null;
      } // if

      return this.fields.get(index);
   } // getField


   public void setLevel(int level) {
      this.level.set(level);
   } // setLevel


   public int getLevel() {
      return this.level.get();
   } // getLevel


   public boolean wasTouched() {
      return this.touched;
   } // wasTouched


   public void set(String msgText, HL7Encoding encoders) {
      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.FIELD);
      ArrayList<String>  elements = encoders.hl7Split(msgText, nextLevel);
      this.fields = new ArrayList<HL7Field>();
      for (String elementStr : elements) {
         this.fields.add(new HL7Field(elementStr, encoders));
      } // for

      this.touched = true;
   } // set


   public String toHL7String(HL7Encoding encoders) {
      if (!this.hasFields()) {
         return "";
      } // if

      ArrayList<String> fieldStrings = new ArrayList<String>();
      for (HL7Element element : this.fields) {
         fieldStrings.add(element.toHL7String(encoders));
      } // for
      
      if (this.idStr.equals("MSH") || this.idStr.equals("BHS") ) {
         // special handling for encoding charcters.
         // remove 2nd field and replace with encoding characters
         // as a field.
         fieldStrings.remove(1);
         fieldStrings.add(1, encoders.toString().substring(1));
      } // if

      return encoders.hl7Join(fieldStrings, this.level.next());
   } // toHL7String


   public String toXMLString() {
      return this.toXMLString(null);
   } // toXMLString

   public String toXMLString(HL7Encoding encoders) {
      String tag = "Segment";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(this.idStr)
              .append("\">");


      int fieldOffset = 0;
      if (this.idStr.equals("MSH")) {
         fieldOffset = 1;
         if (encoders != null) {
            returnBuffer.append(encoders.toXMLString());
         } // if
      } // if

      int fieldIndex = 0;
      for (HL7Field field : this.fields) {
         if (fieldIndex > 0) {
            if (field.hasContent() ) {
               returnBuffer.append(field.toXMLString(fieldIndex + fieldOffset));
            } // if
         } // if
         ++fieldIndex;
      } // for

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString

   public HL7Element getElement(int index) {
      return this.getField(index);
   } // getElement


   public boolean hasContent() {
      return false;
   } // hasContent


   public boolean hasFields() {
      if (this.fields == null || this.fields.isEmpty()) {
         return false;
      } // if

      return true;
   } // hasFields


   public boolean hasField(int index) {
      if (  this.fields == null
      ||    index < 0
      ||    index >= this.fields.size()) {
         return false;
      } // if

      return true;
   } // hasField

   int fieldCount() {
      if (!this.hasFields()) {
         return 0;
      } // if

      return this.fields.size();
   } // fieldCount

   void addField() {
      HL7Field field = new HL7Field();
      field.addRepetition();

      if (!this.hasFields()) {
         this.fields = new ArrayList<HL7Field>();
      } // if

      this.fields.add(field);
   } // addField


   HL7Field pickSequence(int sequence, boolean create) {
      if (!this.hasField(sequence)) {
         if (!create) {
            return null;
         } // if

         for (int newIndex = this.fieldCount(); newIndex <= sequence; ++newIndex) {
            this.addField();
         } // for
      } // if

      return this.getField(sequence);
   } // pickSequence

} // HL7Segment

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
public class HL7Field implements HL7Element {
   private   HL7ElementLevel                 level;
   protected ArrayList<HL7FieldRepetition>   repetitions = null;
   private boolean                           touched;


   public HL7Field() {
      this.level = new HL7ElementLevel(HL7ElementLevel.FIELD);
   } // HL7Field


   public HL7Field(String fieldStr, HL7Encoding encoders) {
      this();
      this._set(fieldStr, encoders);
   } // HL7Field

   public void setLevel(int level) {
      this.level.set(level);
   } // setLevel


   public int getLevel() {
      return this.level.get();
   } // getLevel


   public boolean wasTouched() {
      return this.touched;
   } // wasTouched


   private void _set(String msgText, HL7Encoding encoders) {
      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.REPETITION);
      ArrayList<String>  elements = encoders.hl7Split(msgText, nextLevel);
      this.repetitions = new ArrayList<HL7FieldRepetition>();
      for (String elementStr : elements) {
         HL7FieldRepetition element = new HL7FieldRepetition(elementStr, encoders);
         this.repetitions.add(element);
      } // for

      this.touched = true;
   } // set

   public void set(String msgText, HL7Encoding encoders) { this._set(msgText, encoders); }
   
   public String toHL7String(HL7Encoding encoders) {
      if (!this.hasConstituents()) {
         return "";
      } // if

      ArrayList<String> elementStrings = new ArrayList<String>();
      for (HL7Element element : this.repetitions) {
         elementStrings.add(element.toHL7String(encoders));
      } // for

      return encoders.hl7Join(elementStrings, this.level.next());
   } // toHL7String

   public String toXMLString(int fieldIndex) {
      if (!this.hasContent()) return "";
      
      String tag = "Field";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(fieldIndex))
              .append("\">");

      if (this.hasSimpleContent()) {
         returnBuffer.append(this.getSimpleContent());
      } else {
         int repetitionIndex = 0;
         for (HL7FieldRepetition fieldRep : this.repetitions) {
            if (fieldRep.hasContent() ) {
               returnBuffer.append(fieldRep.toXMLString(repetitionIndex));
            } // if
            ++repetitionIndex;
         } // for
      } // if - else

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


   public HL7Element getElement(int index) {
      if (this.repetitions == null || this.repetitions.isEmpty()) {
         return null;
      } // if

      return this.repetitions.get(index);
   } // getElement


   public boolean hasContent() {
      if (this.hasConstituents()) {
         for (HL7FieldRepetition rep : this.repetitions) {
            if (rep.hasContent()) {
               return true;
            } // if
         } // if
      } // if
      
      return false;
   } // hasContent


   public boolean hasSimpleContent() {
      if (this.hasConstituents() ) {
         if (this.repetitions.size() < 2 && this.repetitions.get(0).hasSimpleContent()) {
            return true;
         } // if
      } // if

      return false;
   } // hasSimpleContent


   public String getSimpleContent() {
      if (this.hasSimpleContent()) return this.repetitions.get(0).getSimpleContent();

      return "";
   } // getSimpleContent


   public boolean hasConstituents() {
      if (this.repetitions == null || this.repetitions.isEmpty()) {
         return false;
      } // if

      return true;
   } // hasConstituents

   public String getContent() {
      if (this.hasSimpleContent()) {
         return this.toHL7String(null);
      } // if

      return null;
   } // getContent

   
   public boolean hasRepetition(int index) {
      if (this.repetitions == null || this.repetitions.isEmpty()) {
         return false;
      } // if

      if (index >= this.repetitions.size() ) return false;
      return true;
   } // hasRepetition

   public HL7FieldRepetition getRepetition(int index) {
      if (this.hasRepetition(index)) {
         return this.repetitions.get(index);
      } // if

      return null;
   } // getRepetition

   public void addRepetition() {
      if (this.repetitions == null) {
         this.repetitions = new ArrayList<HL7FieldRepetition>();
      } // if

      this.repetitions.add(new HL7FieldRepetition());
   } // addRepetition


   HL7FieldRepetition pickRepetition(int repetition, boolean create) {
      if (repetition == HL7Designator.UNSPECIFIED) {
         repetition = 0;
      } // if

      if (!this.hasRepetition(repetition)) {
         if (!create) {
            return null;
         } // if

         for (int newIndex = this.repCount(); newIndex <= repetition; ++newIndex) {
            this.addRepetition();
         } // for
      } // if - else

      return this.getRepetition(repetition);
   } // pickRepetition
   

   private int repCount() {
      if (this.repetitions == null) {
         return 0;
      } // if

      return this.repetitions.size();
   } // repCount;

} // HL7Field

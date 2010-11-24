/*
 *  $Id$
 *  $HeadURL$
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Field.java : Provides access to HL7 message field level items.
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
 *
 * @author scott
 */
public class HL7Field implements HL7Element {
   private   HL7ElementLevel                 level;
   protected ArrayList<HL7FieldRepetition>   repetitions = null;
   private boolean                           touched;


   public HL7Field() {
      level = new HL7ElementLevel(HL7ElementLevel.FIELD);
   } // HL7Field


   public HL7Field(String fieldStr, HL7Encoding encoders) {
      this();
      _set(fieldStr, encoders);
   } // HL7Field

   public int getLevel() {
      return level.get();
   } // getLevel


   public boolean wasTouched() {
      return touched;
   } // wasTouched


   private void _set(String msgText, HL7Encoding encoders) {
      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.REPETITION);
      ArrayList<String>  elements = encoders.hl7Split(msgText, nextLevel);
      repetitions = new ArrayList<HL7FieldRepetition>();
      for (String elementStr : elements) {
         HL7FieldRepetition element = new HL7FieldRepetition(elementStr, encoders);
         repetitions.add(element);
      } // for

      touched = true;
   } // set

   public void set(String msgText, HL7Encoding encoders) { this._set(msgText, encoders); }
   
   public String toHL7String(HL7Encoding encoders) {
      if (!hasRepetitions()) return "";

      ArrayList<String> repStrings = new ArrayList<String>();
      for (HL7FieldRepetition rep : repetitions) repStrings.add(rep.toHL7String(encoders));
      return encoders.hl7Join(repStrings, level.next());
   } // toHL7String

   
   public String toXMLString(int fieldIndex) {
      if (!hasContent()) return "";
      
      String tag = "Field";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(fieldIndex))
              .append("\">");

      if (hasSimpleContent()) {
         returnBuffer.append(getSimpleContent());
      } else {
         int repetitionIndex = 0;
         for (HL7FieldRepetition fieldRep : repetitions) {
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
      return getRepetition(index);
   } // getElement


   public boolean hasContent() {
      if (hasRepetitions()) {
         for (HL7FieldRepetition rep : repetitions) if (rep.hasContent()) return true;
      } // if
      
      return false;
   } // hasContent


   public boolean hasSimpleContent() {
      if (hasRepetitions() ) {
         if (  repetitions.size() < 2
         &&    repetitions.get(0).hasSimpleContent()) {
            return true;
         } // if
      } // if

      return false;
   } // hasSimpleContent


   public String getSimpleContent() {
      if (hasSimpleContent()) return repetitions.get(0).getSimpleContent();

      return "";
   } // getSimpleContent


   public boolean hasRepetitions() {
      return repetitions != null && !repetitions.isEmpty();
   } // hasRepetitions


   public boolean hasRepetition(int index) {
      return hasRepetitions()
          && index >= 0
          && index < repetitions.size();
   } // hasRepetition

   public HL7FieldRepetition getRepetition(int index) {
      if (hasRepetition(index)) return repetitions.get(index);
      return null;
   } // getRepetition

   public void addRepetition() {
      if (repetitions == null) repetitions = new ArrayList<HL7FieldRepetition>();
      repetitions.add(new HL7FieldRepetition());
   } // addRepetition


   HL7FieldRepetition pickRepetition(int repetition, boolean create) {
      if (repetition == HL7Designator.UNSPECIFIED) repetition = 0;

      if (!hasRepetition(repetition)) {
         if (!create) return null;

         for (int newIndex = repCount(); newIndex <= repetition; ++newIndex) {
            addRepetition();
         } // for
      } // if - else

      return getRepetition(repetition);
   } // pickRepetition
   

   private int repCount() {
      return repetitions == null ? 0 : repetitions.size();
   } // repCount;

} // HL7Field

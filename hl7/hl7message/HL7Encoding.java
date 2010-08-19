/*
 *  $Id$
 * 
 *  This code is derived from public domain sources. 
 *  Commercial use is allowed. 
 *  However, all rights remain permanently assigned to the public domain.
 * 
 *  HL7Encoding.java : Provides access to HL7 message encoding characters.
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
import us.conxio.XMLUtilities.XMLUtils;

/**
 * Provides access to HL7 message encoding characters,
 * as well as encoding and decoding methodology.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7Encoding {
   private int          fieldSeparator,
                        componentSeparator,
                        repetitionSeparator,
                        subComponentSeparator,
                        escapeChar;


   // * constructors *
   /**
    * Creates a HL7Encoding object based on the argument encoding characters array.
    * @param chars The array of encoding character values.
    * <ul>Must be populated as follows:
    * <li>field separator
    * <li>component separator
    * <li>repetition separator
    * <li>escape indicator
    * <li>subcomponent separator
    * </ul>Encoding characters must be printable ascii character values, and not
    * alpha-numeric or whitespace.
    */
   public HL7Encoding(int[] chars) {
      this.initialize(chars);
   } // HL7Encoding

   /**
    * Creates a HL7Encoding object based on the argument encoding characters.
    * Encoding characters must be printable ascii character values, and not
    * alpha-numeric or whitespace.
    * @param fs field separator
    * @param cs component separator
    * @param rs repetition separator
    * @param ec escape indicator
    * @param ss subcomponent separator
    */
   public HL7Encoding(int fs, int cs, int rs, int ec, int ss) {
      int[] ints = { fs, cs, rs, ec, ss };
      this.initialize(ints);
   } // HL7Encoding


   /**
    * Creates a HL7Encoding object based on the argument string of encoding characters.
    * @param encodingChars The argument string of encoding characters.
    * <ul>Must be populated as follows:
    * <li>field separator
    * <li>component separator
    * <li>repetition separator
    * <li>escape indicator
    * <li>subcomponent separator
    * </ul>Encoding characters must be printable ascii character values, and not
    * alpha-numeric or whitespace.
    */
   public HL7Encoding(String encodingChars) {
      char[] chars = encodingChars.toCharArray();
      int len = chars.length;
      int[] ints = new int[len];
      for (int index = 0; index < len; ++index) {
         ints[index] = (int)chars[index];
      } // for

      this.initialize(ints);
   } // HL7Encoding


   // * construction utilities *
   private void initialize(int[] chars) {
      if (!this.isUnambiguous(chars)) {
         throw new IllegalArgumentException(
                 new StringBuffer("Ambiguous encoding characters:")
                     .append(this.infoString(chars))
                     .append(".").toString());
      } // if

      int len = chars.length;
      for (int index = 0; index < len; ++index) {
         if (!this.isValidEncoder(chars[index])) {
            throw new IllegalArgumentException( "Not a valid encoding character:"
                                             +  Character.toString((char)chars[index])
                                             +  "("
                                             +  Integer.toString(chars[index])
                                             + ").");
         } // if
      } // for

      this.fieldSeparator = chars[0];
      this.componentSeparator = chars[1];
      this.repetitionSeparator = chars[2];
      this.escapeChar = chars[3];
      this.subComponentSeparator = chars[4];
      this.toString();
   } // initialize


   /**
    * Checks the argument array of encoding characters to verify that each
    * character occurs once and only once.
    * @param ints the array of encoding characters to check
    * @return true if all characters are unique within the argument array,
    * otherwise false.
    */
   private boolean isUnambiguous(int[] ints) {
      int len = ints.length;

      for (int leftIndex = 0; leftIndex < len; ++leftIndex) {
         for (int rightIndex = len -1; rightIndex > leftIndex; --rightIndex) {
            if (  ints[rightIndex] == ints[leftIndex]
            &&    leftIndex != rightIndex) {
               return false;
            } // if
         } // for
      } // for

      return true;
   } // isUnambiguous


   /**
    * Checks for valid encoding character
    * @param charV the encoding character value to check
    * @return true if valid, otherwise false.
    */
   private boolean isValidEncoder(int charV) {
      if (  charV == 0
      ||    Character.isLetterOrDigit(charV)
      ||    Character.isWhitespace(charV)) {
         return false;
      } // if

      return true;
   } // isValidEncoder


   /**
    * Creates an arguemtn information string for error logging.
    * @param chars
    * @return
    */
   private String infoString(int[] chars) {
      StringBuilder sb = new StringBuilder();
      int len = chars.length;
      for (int index = 0; index < len; ++index) {
         sb.append(chars[index])
           .append("(")
           .append(Integer.toString(chars[index]))
           .append("), ");
      } // for

      return sb.toString();
   } // infoString


   // Utilities
   String NextSeparator(String separator) {
      if (separator.equals("\r")) {
         return(this.getFieldSeparator());
      } // if 

      if (separator.equals(getFieldSeparator())) {
            return(this.getRepetitionSeparator());
      } // if

      if (separator.equals(getRepetitionSeparator())) {
            return(this.getComponentSeparator());
      } // if

      if (separator.equals(getComponentSeparator()) ) {
            return(this.getSubComponentSeparator());
      } // if

      // fall through 
      return(null);

   } // NextSeparator
   

   /**
    * @return The set of encoding characers as a String.
    */
   @Override
   public String toString() {
      return(new StringBuffer()
              .append((char)this.fieldSeparator)
              .append((char)this.componentSeparator)
              .append((char)this.repetitionSeparator)
              .append((char)this.escapeChar)
              .append((char)this.subComponentSeparator)
              .toString());
   } // toString


   public String toXMLString() {
      StringBuilder contentBuffer = new StringBuilder();
      contentBuffer.append(XMLUtils.elementString( "FieldSeparator",
                                                   "&#"
                                                 + Integer.toString(this.fieldSeparator)));
      contentBuffer.append(XMLUtils.elementString( "ComponentSeparator",
                                                   "&#"
                                                 + Integer.toString(this.componentSeparator)));
      contentBuffer.append(XMLUtils.elementString( "RepetitionSeparator",
                                                   "&#"
                                                 + Integer.toString(this.repetitionSeparator)));
      contentBuffer.append(XMLUtils.elementString( "EscapeCharacter",
                                                   "&#"
                                                 + Integer.toString(this.escapeChar)));
      contentBuffer.append(XMLUtils.elementString( "SubComponentSeparator",
                                                   "&#"
                                                 + Integer.toString(this.subComponentSeparator)));
      return XMLUtils.elementString("HL7Encoding", contentBuffer.toString());
   } // toXMLString


   /**
    * @return the fieldSeparator
    */
   public String getFieldSeparator() {
      return Character.toString((char)fieldSeparator);
   } // getFieldSeparator

   /**
    * @param fieldSeparator the fieldSeparator to set
    */
   public void setFieldSeparator(String fieldSep) {
      this.fieldSeparator = fieldSep.charAt(0);
   } // setFieldSeparator

    /**
    * @param fieldSeparator the fieldSeparator to set
    */
   public void setFieldSeparator(int fieldSep) {
      this.fieldSeparator = fieldSep;
   } // setFieldSeparator

  /**
    * @return the componentSeparator
    */
   public String getComponentSeparator() {
      return Character.toString((char)componentSeparator);
   } // getComponentSeparator

   /**
    * @param componentSeparator the componentSeparator to set
    */
   public void setComponentSeparator(String componentSep) {
      this.componentSeparator = componentSep.charAt(0);
   } // setComponentSeparator

   /**
    * @param componentSeparator the componentSeparator to set
    */
   public void setComponentSeparator(int componentSep) {
      this.componentSeparator = componentSep;
   } // setComponentSeparator

   /**
    * @return the repetitionSeparator
    */
   public String getRepetitionSeparator() {
      return Character.toString((char)repetitionSeparator);
   } // getRepetitionSeparator

   /**
    * @param repetitionSeparator the repetitionSeparator to set
    */
   public void setRepetitionSeparator(String repetitionSep) {
      this.repetitionSeparator = repetitionSep.charAt(0);
   } // setRepetitionSeparator

    /**
    * @param repetitionSeparator the repetitionSeparator to set
    */
   public void setRepetitionSeparator(int repetitionSep) {
      this.repetitionSeparator = repetitionSep;
   } // setRepetitionSeparator

  /**
    * @return the subComponentSeparator
    */
   public String getSubComponentSeparator() {
      return Character.toString((char)subComponentSeparator);
   } // getSubComponentSeparator

   /**
    * @param subComponentSeparator the subComponentSeparator to set
    */
   public void setSubComponentSeparator(String subComponentSep) {
      this.subComponentSeparator = subComponentSep.charAt(0);
   } // setSubComponentSeparator

   /**
    * @param subComponentSeparator the subComponentSeparator to set
    */
   public void setSubComponentSeparator(int subComponentSep) {
      this.subComponentSeparator = subComponentSep;
   } // setSubComponentSeparator

   /**
    * @return the escapeChar
    */
   public String getEscapeChar() {
      return Character.toString((char)escapeChar);
   } // getEscapeChar

   /**
    * @param escapeChar the escapeChar to set
    */
   public void setEscapeChar(String escapeChar) {
      this.escapeChar = escapeChar.charAt(0);
   } // setEscapeChar


   /**
    * @param escapeChar the escapeChar to set
    */
   public void setEscapeChar(int  escapeChar) {
      this.escapeChar = escapeChar;
   } // setEscapeChar


   /**
    * Escapes any encoding characters in the argument string.
    * @param str the argiument string to operate upon.
    * @return the argument string with encoding characters escaped
    */
   public String escape(String str) {
      str = this.escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.FIELD));
      str = this.escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.COMPONENT));
      str = this.escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.REPETITION));
      return  this.escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.SUBCOMPONENT));
   } // escape


   public int separatorAt(HL7ElementLevel level) {
      if (level == null) {
         throw new IllegalArgumentException("Null level.");
      } // if

      switch (level.get()) {
         case HL7ElementLevel.SEGMENT :      return 0x0d;
         case HL7ElementLevel.FIELD :        return this.fieldSeparator;
         case HL7ElementLevel.REPETITION :   return this.repetitionSeparator;
         case HL7ElementLevel.COMPONENT :    return this.componentSeparator;
         case HL7ElementLevel.SUBCOMPONENT : return this.subComponentSeparator;
         default : throw new IllegalArgumentException(   "Illegal level:"
                                                      +  Integer.toString(level.get()));
      } // switch
   }


   private int nextBreak(String str, int separator) {
      int brk = str.indexOf(separator);
      if (brk < 0) {
         return brk;
      } // if

      if (brk > 0 && str.charAt(brk - 1) == this.escapeChar) {
         return brk + this.nextBreak(str.substring(brk + 1), separator);
      } // if

      return brk;
   } // nextToken


   public ArrayList<String> hl7Split(String str, HL7ElementLevel level) {
      ArrayList<String> retn = new ArrayList<String>();
      char separator = (char)this.separatorAt(level);
      int brk = 0;
      while (true) {
         brk = this.nextBreak(str, separator);
         if (brk < 0) {
            retn.add(str);
            return retn;
         }
         
         retn.add(str.substring(0, brk));
         str = str.substring(++brk);
      } // while
   } // hl7Split


   private String hl7Join(ArrayList<String> elements, HL7ElementLevel level, boolean escaped) {
      StringBuilder retnBuffer = new StringBuilder();
      char separator = (char)this.separatorAt(level);

      boolean firstTime = true;
      for (String element : elements) {
         if (firstTime) {
            firstTime = false;
         } else {
            if (escaped) {
               retnBuffer.append((char)this.escapeChar);
            } // if
            retnBuffer.append(separator);
         } // if

         retnBuffer.append(element);
      } // for

      return retnBuffer.toString();
   } // hl7Join


   public String hl7Join(ArrayList<String> elements, HL7ElementLevel level) {
      return this.hl7Join(elements, level, false);
   } // hl7Join


   private String escapeSeparator(String str, HL7ElementLevel level) {
      ArrayList<String> elements = this.hl7Split(str, level);
      return this.hl7Join(elements, level, true);
   } // escapeSeparator
} // class HL7Encoding

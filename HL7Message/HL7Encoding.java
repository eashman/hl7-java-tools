/*
 *  $Id$
 * 
 *  This code is derived from public domain sources. 
 *  Commercial use is allowed. 
 *  However, all rights remain permanently assigned to the public domain.
 * 
 *  HL7Encoding.java : Provides access to HL7 message encoding characters.
 * 
 *  Copyright (C) 2009  Scott Herman
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


package us.conxio.HL7.HL7Message;

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
      StringBuffer sb = new StringBuffer();
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
   public String toString() {
      return(new StringBuffer()
              .append((char)this.fieldSeparator)
              .append((char)this.componentSeparator)
              .append((char)this.repetitionSeparator)
              .append((char)this.escapeChar)
              .append((char)this.subComponentSeparator)
              .toString());
   } // toString

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


} // class HL7Encoding

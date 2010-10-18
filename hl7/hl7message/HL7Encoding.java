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
import org.apache.commons.lang.StringUtils;
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

   public static final String SEGMENT_TERMINATOR = HL7Message.SEGMENT_TERMINATOR;

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
      initialize(chars);
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
      initialize(ints);
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
      if (!isUnambiguous(chars)) {
         throw new IllegalArgumentException(
                 new StringBuffer("Ambiguous encoding characters:")
                     .append(infoString(chars))
                     .append(".").toString());
      } // if

      int len = chars.length;
      for (int index = 0; index < len; ++index) {
         if (!isValidEncoder(chars[index])) {
            throw new IllegalArgumentException( "Not a valid encoding character:"
                                             +  Character.toString((char)chars[index])
                                             +  "("
                                             +  Integer.toString(chars[index])
                                             + ").");
         } // if
      } // for

      fieldSeparator = chars[0];
      componentSeparator = chars[1];
      repetitionSeparator = chars[2];
      escapeChar = chars[3];
      subComponentSeparator = chars[4];
      toString();
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
    * Creates an argument information string for error logging.
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
         return(getFieldSeparator());
      } // if 

      if (separator.equals(getFieldSeparator())) {
            return(getRepetitionSeparator());
      } // if

      if (separator.equals(getRepetitionSeparator())) {
            return(getComponentSeparator());
      } // if

      if (separator.equals(getComponentSeparator()) ) {
            return(getSubComponentSeparator());
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
              .append((char)fieldSeparator)
              .append((char)componentSeparator)
              .append((char)repetitionSeparator)
              .append((char)escapeChar)
              .append((char)subComponentSeparator)
              .toString());
   } // toString


   public String toXMLString() {
      StringBuilder contentBuffer = new StringBuilder();
      contentBuffer.append(XMLUtils.elementString( "FieldSeparator",
                                                   "&#"
                                                 + Integer.toString(fieldSeparator)));
      contentBuffer.append(XMLUtils.elementString( "ComponentSeparator",
                                                   "&#"
                                                 + Integer.toString(componentSeparator)));
      contentBuffer.append(XMLUtils.elementString( "RepetitionSeparator",
                                                   "&#"
                                                 + Integer.toString(repetitionSeparator)));
      contentBuffer.append(XMLUtils.elementString( "EscapeCharacter",
                                                   "&#"
                                                 + Integer.toString(escapeChar)));
      contentBuffer.append(XMLUtils.elementString( "SubComponentSeparator",
                                                   "&#"
                                                 + Integer.toString(subComponentSeparator)));
      return XMLUtils.elementString("HL7Encoding", contentBuffer.toString());
   } // toXMLString


   /**
    * @return the fieldSeparator
    */
   public String getFieldSeparator() {
      return Character.toString((char)fieldSeparator);
   } // getFieldSeparator

   /**
    * Set the field separator.
    * @param fieldSep the field separator value to set
    */
   public void setFieldSeparator(String fieldSep) {
      if (StringUtils.isEmpty(fieldSep)) return;
      fieldSeparator = fieldSep.charAt(0);
   } // setFieldSeparator

  /**
   * Set the field separator.
   * @param fieldSep the field separator value to set, as an int value.
   */
   public void setFieldSeparator(int fieldSep) {
      fieldSeparator = fieldSep;
   } // setFieldSeparator

  /**
   * Retrieve the component separator value.
   * @return the componentSeparator
   */
   public String getComponentSeparator() {
      return Character.toString((char)componentSeparator);
   } // getComponentSeparator

   /**
    * Set the component separator value.
    * @param componentSep the component separator value to set
    */
   public void setComponentSeparator(String componentSep) {
      if (StringUtils.isEmpty(componentSep)) return;
      componentSeparator = componentSep.charAt(0);
   } // setComponentSeparator

   /**
    * @param componentSeparator the componentSeparator to set
    */
   public void setComponentSeparator(int componentSep) {
      componentSeparator = componentSep;
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
      if (StringUtils.isEmpty(repetitionSep)) return;
      repetitionSeparator = repetitionSep.charAt(0);
   } // setRepetitionSeparator

    /**
    * @param repetitionSeparator the repetitionSeparator to set
    */
   public void setRepetitionSeparator(int repetitionSep) {
      repetitionSeparator = repetitionSep;
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
      if (StringUtils.isEmpty(subComponentSep)) return;
      subComponentSeparator = subComponentSep.charAt(0);
   } // setSubComponentSeparator

   /**
    * @param subComponentSeparator the subComponentSeparator to set
    */
   public void setSubComponentSeparator(int subComponentSep) {
      subComponentSeparator = subComponentSep;
   } // setSubComponentSeparator

   /**
    * @return the escapeChar
    */
   public String getEscapeChar() {
      return Character.toString((char)escapeChar);
   } // getEscapeChar

   /**
    * @param escapeChr the escapeChr to set
    */
   public void setEscapeChar(String escapeChr) {
      if (StringUtils.isEmpty(escapeChr)) return;
      escapeChar = escapeChr.charAt(0);
   } // setEscapeChar


   /**
    * @param escapeChr the escapeChr to set
    */
   public void setEscapeChar(int  escapeChr) {
      escapeChar = escapeChr;
   } // setEscapeChar


   /**
    * Escapes any encoding characters in the argument string.
    * @param str the argument string to operate upon.
    * @return the argument string with encoding characters escaped
    */
   public String escape(String str) {
      str = escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.FIELD));
      str = escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.COMPONENT));
      str = escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.REPETITION));
      return  escapeSeparator(str, new HL7ElementLevel(HL7ElementLevel.SUBCOMPONENT));
   } // escape


   private int separatorAt(HL7ElementLevel level) {
      if (level == null) throw new IllegalArgumentException("Null level.");

      switch (level.get()) {
         case HL7ElementLevel.SEGMENT :      return HL7Message.CR;
         case HL7ElementLevel.FIELD :        return fieldSeparator;
         case HL7ElementLevel.REPETITION :   return repetitionSeparator;
         case HL7ElementLevel.COMPONENT :    return componentSeparator;
         case HL7ElementLevel.SUBCOMPONENT : return subComponentSeparator;
         default : throw new IllegalArgumentException(   "Illegal level:"
                                                      +  Integer.toString(level.get()));
      } // switch
   }


   private int nextBreak(String str, int separator) {
      int brk = str.indexOf(separator);
      if (brk < 0) return brk;

      if (brk > 0 && str.charAt(brk - 1) == escapeChar) {
         return brk + nextBreak(str.substring(brk + 1), separator);
      } // if

      return brk;
   } // nextToken


   ArrayList<String> hl7Split(String str, HL7ElementLevel level) {
      ArrayList<String> retn = new ArrayList<String>();
      char separator = (char)separatorAt(level);
      int brk = 0;
      while (true) {
         brk = nextBreak(str, separator);
         if (brk < 0) {
            retn.add(str);
            return retn;
         } // if
         
         retn.add(str.substring(0, brk));
         str = str.substring(++brk);
      } // while
   } // hl7Split


   private String hl7Join(ArrayList<String> elements, HL7ElementLevel level, boolean escaped) {
      StringBuilder retnBuffer = new StringBuilder();
      char separator = (char)separatorAt(level);
      String encoders = toString();

      boolean firstTime = true;
      for (String element : elements) {
         if (firstTime) {
            firstTime = false;
         } else {
            if (escaped) retnBuffer.append((char)escapeChar);
            retnBuffer.append(separator);
         } // if

         retnBuffer.append(element);
      } // for

      return retnBuffer.toString();
   } // hl7Join


   String hl7Join(ArrayList<String> elements, HL7ElementLevel level) {
      return hl7Join(elements, level, false);
   } // hl7Join


   private String escapeSeparator(String str, HL7ElementLevel level) {
      ArrayList<String> elements = hl7Split(str, level);
      return hl7Join(elements, level, true);
   } // escapeSeparator


   public static HL7Encoding getDefaultEncoding() {
      return new HL7Encoding('|', '^', '~', '\\', '&');
   } // getDefaultEncoding

} // class HL7Encoding

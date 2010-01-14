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
 * Provides access to HL7 message encoding characters.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7Encoding {
   private String       fieldSeparator,
                        componentSeparator,
                        repetitionSeparator,
                        subComponentSeparator,
                        escapeChar,
                        string;
   /* TBD: make the encoding characters private (and protected?).
    */

   // constructors
   HL7Encoding(String encodingChars) {
      this.string = new String(encodingChars);
      // check for string overrun / underrun.
      if (encodingChars.length() > 0) {
         int lastFieldSep = encodingChars.lastIndexOf(encodingChars.charAt(0));
         if (lastFieldSep != 0) {
            this.string = encodingChars.substring(0, lastFieldSep);
            encodingChars = this.string;
         } // if
         this.fieldSeparator = encodingChars.substring(0, 1);
      } else {
          this.fieldSeparator = null;
      } // if - else

      if (encodingChars.length() > 1) {
         int lastCompSep = encodingChars.lastIndexOf(encodingChars.charAt(1));
         this.componentSeparator = (lastCompSep == 1) ? encodingChars.substring(1, 2) : null;
         if (this.componentSeparator != null) {
            int compSepChar = this.componentSeparator.charAt(0);
            if (Character.isLetterOrDigit(compSepChar)
            ||  Character.isSpaceChar(compSepChar)) {
               this.componentSeparator = null;
            } // if
         } // if
      } else {
         this.componentSeparator = null;
      } // if - else


      if (encodingChars.length() > 2) {
         int lastRepSep = encodingChars.lastIndexOf(encodingChars.charAt(2));
         this.repetitionSeparator = (lastRepSep == 2) ? encodingChars.substring(2, 3) : null;
         if (this.repetitionSeparator != null) {
            int repSepChar = this.repetitionSeparator.charAt(0);
            if (Character.isLetterOrDigit(repSepChar)
            ||  Character.isSpaceChar(repSepChar)) {
               this.repetitionSeparator = null;
            } // if
         } // if
      } else {
         this.repetitionSeparator = null;
      } // if - else

      if (encodingChars.length() > 3) {
         int lastEscChar = encodingChars.lastIndexOf(encodingChars.charAt(3));
         this.escapeChar = (lastEscChar == 3) ? encodingChars.substring(3, 4) : null;
         if (this.escapeChar != null) {
            int escChar = this.escapeChar.charAt(0);
            if (Character.isLetterOrDigit(escChar)
            ||  Character.isSpaceChar(escChar)) {
               this.escapeChar = null;
            } // if
         } // if
      } else {
         this.escapeChar = null;
      } // if - else

      if (encodingChars.length() > 4) {
         int lastSubCompSep = encodingChars.lastIndexOf(encodingChars.charAt(4));
         this.subComponentSeparator = (lastSubCompSep == 4) ? encodingChars.substring(4, 5) : null;
         if (this.subComponentSeparator != null) {
            int subCompSepChar = this.subComponentSeparator.charAt(0);
            if (Character.isLetterOrDigit(subCompSepChar)
            ||  Character.isSpaceChar(subCompSepChar)) {
               this.subComponentSeparator = null;
            } // if
         } // if
      } else {
         this.subComponentSeparator = null;
      } // if - else
   } // HL7Encoding


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
   
   
   public String toString() {
      return(this.string);
   } // toString

   /**
    * @return the fieldSeparator
    */
   public String getFieldSeparator() {
      return fieldSeparator;
   }

   /**
    * @param fieldSeparator the fieldSeparator to set
    */
   public void setFieldSeparator(String fieldSeparator) {
      this.fieldSeparator = fieldSeparator;
   }

   /**
    * @return the componentSeparator
    */
   public String getComponentSeparator() {
      return componentSeparator;
   }

   /**
    * @param componentSeparator the componentSeparator to set
    */
   public void setComponentSeparator(String componentSeparator) {
      this.componentSeparator = componentSeparator;
   }

   /**
    * @return the repetitionSeparator
    */
   public String getRepetitionSeparator() {
      return repetitionSeparator;
   }

   /**
    * @param repetitionSeparator the repetitionSeparator to set
    */
   public void setRepetitionSeparator(String repetitionSeparator) {
      this.repetitionSeparator = repetitionSeparator;
   }

   /**
    * @return the subComponentSeparator
    */
   public String getSubComponentSeparator() {
      return subComponentSeparator;
   }

   /**
    * @param subComponentSeparator the subComponentSeparator to set
    */
   public void setSubComponentSeparator(String subComponentSeparator) {
      this.subComponentSeparator = subComponentSeparator;
   }

   /**
    * @return the escapeChar
    */
   public String getEscapeChar() {
      return escapeChar;
   }

   /**
    * @param escapeChar the escapeChar to set
    */
   public void setEscapeChar(String escapeChar) {
      this.escapeChar = escapeChar;
   }
      
} // class HL7Encoding

/*
 *  $Id: HL7Encoding.java 14 2009-11-20 22:03:44Z scott $
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
   public String        fieldSeparator,
                        componentSeparator,
                        repetitionSeparator,
                        subComponentSeparator,
                        escapeChar,
                        string;

   // constructors
   HL7Encoding(String encodingChars) {
       this.string = new String(encodingChars);
       this.fieldSeparator = encodingChars.substring(0, 1);
       this.componentSeparator = encodingChars.substring(1, 2);
       this.repetitionSeparator = encodingChars.substring(2, 3);
       this.escapeChar = encodingChars.substring(3, 4);
       this.subComponentSeparator = encodingChars.substring(4, 5);
   } // HL7Encoding

   
   // Utilities
   String NextSeparator(String separator) {
      if (separator.equals("\r")) {
         return(this.fieldSeparator);
      } // if 

      if (separator.equals(fieldSeparator)) {
            return(this.repetitionSeparator);
      } // if

      if (separator.equals(repetitionSeparator)) {
            return(this.componentSeparator);
      } // if

      if (separator.equals(componentSeparator) ) {
            return(this.subComponentSeparator);
      } // if

      // fall through 
      return(null);

   } // NextSeparator
   
   
   public String toString() {
      return(this.string);
   } // toString
      
} // class HL7Encoding

/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Time.java : A utility class of HL7 Time and Date related methods.
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

import java.util.Date;
import java.text.SimpleDateFormat;


/**
 *
 * @author scott
 */
public class HL7Time {
   private static final String hl7DTFormatStr = "yyyyMMddkkmmssZ";
   private static final SimpleDateFormat hl7DTFormat = new SimpleDateFormat(hl7DTFormatStr);
   private static final SimpleDateFormat hl7DateFormat = new SimpleDateFormat(hl7DTFormatStr.substring(0, 8));

   public HL7Time() { }

   private static String trimTime(String ts) {
      if (ts.substring(8, 10).equals("24")) return ts.substring(0, 8);
      return ts;
   } // trimTime


   public static String get(Date dateTime) {
      return(trimTime(hl7DTFormat.format(dateTime)));
   } // get


   public static String get() {
      Date dateTime = new Date();
      return(trimTime(hl7DTFormat.format(dateTime)));
   } // get

   public static String getDate(Date dateTime) {
      return(hl7DateFormat.format(dateTime));
   } // getDate


   public static String getDate() {
      Date dateTime = new Date();
      return(hl7DateFormat.format(dateTime));
   } // getDate

} // HL7Time



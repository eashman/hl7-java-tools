/*
 *  $Id: HL7Message.java 24 2010-04-08 19:29:05Z scott.herman@unconxio.us $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Message.java : A structured base class for parsed HL7 Messages, providing structured access
 *                    to message data content, and constituent items.
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


package us.conxio.hl7.hl7message;

import java.util.Date;
import java.text.SimpleDateFormat;


/**
 *
 * @author scott
 */
public class HL7Time {
   final String hl7DTFormatStr = "yyyyMMddkkmmssZ";
   SimpleDateFormat hl7DTFormat = new SimpleDateFormat(hl7DTFormatStr);

   public HL7Time() { }

   public String get(Date dateTime) {
      return(hl7DTFormat.format(dateTime));
   } // get


   public String get() {
      Date dateTime = new Date();
      return(hl7DTFormat.format(dateTime));
   } // get

} // HL7Time



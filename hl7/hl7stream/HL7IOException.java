/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7IOException.java : An exception class for HL7 message streams.
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

package us.conxio.hl7.hl7stream;

import java.io.IOException;

/**
 * An exception class for HL7 message streams.
 * 
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7IOException extends IOException {
   /**
    * No error.
    */
   public static final int    NO_ERROR = 0;
   /**
    * Unspecified (or unknown) error.
    */
   public static final int    UNSPECIFIED_ERROR = 1;
   /**
    * HL7 transaction was rejected with a NAck message.
    */
   public static final int    HL7_NACK = 501;
   /**
    * The stream was closed at the time of the call.
    */
   public static final int    STREAM_CLOSED = 502;
   /**
    * The stream was in an inconsistent state at the time of the call.
    */
   public static final int    INCONSISTENT_STATE = 503;
   /**
    * The call causing this exception requested an operation which is
    * inconsistent with the characterisitics of the context stream.
    */
   public static final int    INAPPROPRIATE_OPERATION = 504;
   /**
    * An attempt was made to operate on a HL7 message which had no type defined.
    */
   public static final int    NO_MSG_TYPE = 505;
   /**
    * An attempt was made to operate on a null message reference.
    */
   public static final int    NULL_MSG = 506;
   /**
    * An attempt was made to operate on an incorrectly identified HL7 message.
    */
   public static final int    WRONG_CTLID = 507;
   /**
    * An operation was attmepted on a message that was not a valid HL7 message.
    */
   public static final int    NOT_VALID_MSG = 508;
   /**
    * An attempt was made to operate on a null stream reference.
    */
   public static final int    NULL_STREAM = 511;
   /**
    * An attempt was made to perform an operation inconsistent with the access
    * characteristics of the context stream. ie; read from a writer or write to
    * a reader.
    */
   public static final int    INAPPROPRIATE_DIRECTIVE = 514;
   /**
    * A URI parameter was not interperable within the context of the stream.
    */
   public static final int    UNINTERPERABLE_URI = 521;
   /**
    * The file towards whcih the offending operation was directed, was not found.
    */
   public static final int    FILE_NOT_FOUND = 701;
   /**
    * A file i/o error occurred.
    */
   public static final int    FILE_IO_ERROR = 702;
   /**
    * A specified host was not found.
    */
   public static final int    UNKNOWN_HOST = 901;
   /**
    * A general IO exception was thrown.
    */
   public static final int    IO_EXCEPTION = 1001;
   int                        errorType = NO_ERROR;


   /**
    * Creates a new exception having the argument message.
    * @param msg
    */
   public HL7IOException(String msg) {
      super(msg);
      this.errorType = HL7IOException.UNSPECIFIED_ERROR;
   } // HL7IOException


   /**
    * Creates a new exception having the argument message, which is associated
    * with the argument throwable cause.
    * @param msg
    * @param thrown
    */
   public HL7IOException(String msg, Throwable thrown) {
      super(msg, thrown);
      this.errorType = HL7IOException.UNSPECIFIED_ERROR;
   } // HL7IOException


   /**
    * Creates a new exception having the argument message and error type code.
    * @param msg
    * @param errorType
    */
   public HL7IOException(String msg, int errorType) {
      super(msg);
      this.errorType = errorType;
   } // HL7IOException


   /**
    * Access to the exception's error type code.
    * @return the exception's error type code.
    */
   public int errorType() {
      return this.errorType;
   } // errorType
   
} // HL7IOException

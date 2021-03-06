/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7StreamBase.java : A default base implementation of the HL7Stream interface.
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

package us.conxio.hl7.hl7stream;

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */

import us.conxio.hl7.hl7message.HL7Message;


/**
 * An abstract base class of basic and default behaviors for HL7Stream objects.
 * @author scott herman <scott.herman@unconxio.us>
 */
public abstract class HL7StreamBase implements HL7Stream, HL7MessageHandler {
   /**
    * The current status of the stream
    */
   int            statusValue;
   /**
    * The current i/o directive of the stream; READER, WRITER, or APPENDER
    */
   int            directive;
   /**
    * The current media type of the stream; FILE_TYPE, SOCKET_TYPE, ...
    */
   int            mediaType;

   /**
    * Returns the current status value of the stream.
    * @return the current status value of the stream.
    */
   public int status() {
      return statusValue;
   } // status


   /**
    * Returns the current directive value of the stream.
    * @return the current directive value of the stream.
    */
   public int directive() {
      return directive;
   } // directive


   /**
    * Returns the current media type value of the stream.
    * @return the current media type value of the stream.
    */
   public int media() {
      return mediaType;
   } // media


   /**
    * Determines whether the stream is closed.
    * @return true if the stream is closed, otherwise false.
    */
   public boolean isClosed() {
      return statusValue == HL7Stream.CLOSED;
   } // isClosed


   /**
    * Determines whether the stream is open.
    * @return true if the stream is open, otherwise false.
    */
   public boolean isOpen() {
      return statusValue == HL7Stream.OPEN;
   } // isClosed


   /**
    * Returns the current status of the stream as a string.
    * @return the current status of the stream as a string.
    */
   public String statusString() {
      switch (statusValue) {
         case UNINITIALIZED : return "UNINITIALIZED";
         case CLOSED :        return "CLOSED";
         case OPEN :          return "OPEN";
         default :            return "UNEXPECTED_STATUS";
      } // switch
   } // statusString


   /**
    * Creates a description of the current state of the stream.
    * @return the resulting description as a string.
    */
   public String description() {
      StringBuilder retn = new StringBuilder();
      retn.append(statusString());
      retn.append(":");
      if (mediaType == HL7Stream.FILE_TYPE) {
         retn.append("File:");
      } else if (mediaType == HL7Stream.SOCKET_TYPE) {
         retn.append("Socket:");
      } else if (mediaType == HL7Stream.SECURE_SOCKET_TYPE) {
         retn.append("SSL-Socket:");
      } else if (mediaType == HL7Stream.NO_TYPE) {
         retn.append("No-Type:");
      } else {
         retn.append("UnknownType:");
      } // if - else if - else

      if (directive == HL7Stream.READER) {
         retn.append("Reader:");
      } else if (directive == HL7Stream.WRITER) {
         retn.append("Writer:");
      } else if (directive == HL7Stream.APPENDER) {
         retn.append("Appender:");
      } // if - else if

      return retn.toString();
   } // description


   /**
    * Determines whether the stream is a server.
    * @return true if the stream is a server, otherwise false.
    */
   public boolean isServer() { return false; }

   /**
    * Creates a default dispatch handler which consists solely of a write to
    * the destination.
    * @param msg
    * @return 1 if successful, otherwise 0.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public int dispatch(HL7Message msg) throws HL7IOException {
      return write(msg) ? 1 : 0;
   } // dispatch


   /**
    * Returns the stream as a HL7MessageHandler.
    * @return the stream as a HL7MessageHandler.
    */
   public HL7MessageHandler dispatchHandler() {
      return (directive == HL7Stream.WRITER || directive == HL7Stream.APPENDER)
           ?  this
           : null;
   } // dispatchHandler
   
} // HL7Stream

/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Stream.java : A HL7 message stream interface.
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

import us.conxio.hl7.hl7message.HL7Message;


/**
 * An abstract class to serve as a base for HL7 message stream classes.
 *
 * @author scott herman <scott.herman@unconxio.us>
 */
public interface HL7Stream {
   /**
    * No type specified.
    */
   final int      NO_TYPE              = 0;
   /**
    * Unitialized - type is not set.
    */
   final int      UNINITIALIZED        = 0;
   /**
    * The linefeed character value (10).
    */
   final int      LF                   = 0x0a;
   /**
    * The carriage return character value (13).
    */
   final int      CR                   = 0x0d;
   /**
    * Reader type
    */
   final int      READER               = 1;
   /**
    * Writer type
    */
   final int      WRITER               = 2;
   /**
    * Appender type
    */
   final int      APPENDER             = 3;
   /**
    * Closed status
    */
   final int      CLOSED               = 0xffff;
   /**
    * Open status
    */
   final int      OPEN                 = 201;
   /**
    * File media type
    */
   final int      FILE_TYPE            = 101;
   /**
    * Socket media type
    */
   final int      SOCKET_TYPE          = 103;
   /**
    * Secured socket media type.
    */
   final int      SECURE_SOCKET_TYPE   = 105;


   /**
    * Opens the context stream.
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean open() throws HL7IOException;
   /**
    * Closes the context stream.
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean close() throws HL7IOException;
   /**
    * Reads a HL7 message from the context stream.
    * @return the message read as a HL7Message object.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Message read() throws HL7IOException;
   /**
    * Write a HL7 message to the context stream.
    * @param msg the message to send as a HL7Message object.
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean write(HL7Message msg) throws HL7IOException;
   /**
    * Access to the current status of the context stream.
    * @return the current status value of the context stream.
    */
   public int status();
   /**
    * Determines whether the context stream is closed.
    * @return true if the context stream is closed, otherwise false.
    */
   public boolean isClosed();
   /**
    * Determines whether the context stream is open.
    * @return true if the context stream is open, otherwise false.
    */
   public boolean isOpen();
   /**
    * Determines whether the context stream is a server.
    * @return true if the context stream is a server, otherwise false.
    */
   public boolean isServer();
   /**
    * Creates a description of the current state of the context stream.
    * @return the description as a string.
    */
   public String description();
   /**
    * Extracts the HL7MessageHandler from the context stream, if it is valid.
    * @return the HL7MessageHandler, or null if no HL7MessageHandler is available.
    */
   public HL7MessageHandler dispatchHandler();

} // HL7Stream

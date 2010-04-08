/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7FileReader.java : A file reader class for HL7 message streams.
 *                       Reads HL7 (v.2.x) transaction messages from a file.
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

package us.conxio.HL7.HL7Stream;

/**
 *
 * @author Scott Herman <scott.herman@unconxio.us>
 *
 *
 */

import java.io.*;
import java.net.URI;

import us.conxio.HL7.HL7Message.*;

/**
 * Reads HL7 (v.2.x) transaction messages from a file.
 * @author Scott Herman <scott.herman@unconxio.us>
 */
public class HL7FileReader extends HL7StreamBase implements HL7Stream {
   File           file;
   BufferedReader reader;
   private String waiting;


   /**
    * Constructs a new reader object from the argument File object.
    * @param hl7File
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7FileReader(File hl7File) throws HL7IOException  {
      this.initialize(hl7File);
   } // HL7FileReader


   /**
    * Constructs a new reader object from the argument file name string.
    * @param hl7FileName
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7FileReader(String hl7FileName) throws HL7IOException {
      if (hl7FileName == null || hl7FileName.isEmpty()) {
         throw new IllegalArgumentException("HL7FileReader(): file name not specified.");
      } // if

      this.initialize(new File(hl7FileName));
   } // HL7FileReader


   /**
    * Constructs a new reader object form the argument file URI.
    * @param fileURI
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7FileReader(URI fileURI) throws HL7IOException {
      HL7StreamURI streamURI = new HL7StreamURI(fileURI);
      
      if (  streamURI.isFileWriterURI()
      ||    (!streamURI.isFileReaderURI() && !streamURI.isFileURI())) {
         throw new IllegalArgumentException("HL7FileReader(" + fileURI.toString() + "):Not a file reader URI.");
      } // if

      this.initialize(new File(fileURI) );
   } // HL7FileReader


   // * Construction support.
   private void initialize(File hl7File) throws HL7IOException {
      this.directive = HL7FileReader.READER;

      if (hl7File == null) {
         throw new NullPointerException("HL7FileReader():File is null.");
      } // if

      if (!hl7File.exists()) {
         throw new HL7IOException("HL7FileReader(" + hl7File.toString() + "): File Not found:",
                                  HL7IOException.FILE_NOT_FOUND);
      } // if

      this.file = hl7File;
   } // initialize


   /**
    * Opens the context HL7FileReader stream.
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean open() throws HL7IOException {
      if (this.isOpen()) {
         return true;
      } // if

      FileInputStream  inputStream;

      try {
         inputStream = new FileInputStream(this.file);
      } catch (FileNotFoundException fEx) {
         throw new HL7IOException("HL7FileReader.open:Not found:" + this.file.toString());
      }
      DataInputStream in = new DataInputStream(inputStream);
      this.reader = new BufferedReader(new InputStreamReader(in));
      this.statusValue = HL7Stream.OPEN;
      return true;
   } // openReader


   /**
    * Closes the context HL7FileReader stream.
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean close()throws HL7IOException {
      if (!this.isOpen()) {
         return true;
      } // if

      try {
         this.reader.close();
      } catch (IOException ioEx) {
         throw new HL7IOException("HL7FileReader.close:IOException", ioEx);
      } // try - catch

      this.statusValue = HL7FileReader.CLOSED;
      return true;
   } // close


   /**
    * Reads from the context HL7FileReader stream.
    * @return a HL7Message parswed HL7 message object.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Message read() throws HL7IOException {
      String msgStr = this.readMsg();

      if (msgStr == null) {
         return null;
      } // if

      return new HL7Message(msgStr);
   } // read

   private String _readMsg() throws HL7IOException {
      StringBuffer   hl7Msg = new StringBuffer();
      StringBuffer   segStart = new StringBuffer();

      if (this.waiting != null) {
         hl7Msg.append(waiting);
      } // if

      int     inData;
      int     segLength = 0;

      // Read File char by char
      try {
         while ( (inData = reader.read()) != -1) {
            if (inData == HL7FileReader.CR || inData == HL7FileReader.LF) {
               if (segLength > 0) {
                  segStart.setLength(0);
                  segLength = 0;
                  inData = 13;
               } else {
                  continue;
               } // if - else
            } // if

            else if (segLength < 4) {
               segStart.append( (char)inData );
               ++segLength;
            } // if - else if

            if (segLength == 3) {
               String segStr = segStart.toString();

               // found beginning of a new message
               if (segStr.startsWith("MSH")
               ||  segStr.startsWith("BHS")
               || segStr.startsWith("BTS")) {
                  // Save it off for the next call.
                  this.waiting = new String( segStr );
                  // return the message we already have.
                  if (hl7Msg.length() > 12) {
                     hl7Msg.append("\r");
                     String retnStr = hl7Msg.toString();
                     return(retnStr.substring(0, retnStr.length() - segStr.length()));
                  } else {
                     // Start a new message.
                     hl7Msg.setLength(0);
                     hl7Msg.append(segStr.substring(0, 2));
                  } // if - else
               } // if
            } // if - else

            hl7Msg.append( (char)inData);
         } // while

         // Fall thru at EOF
         // close the stream
         this.close();
      } catch (IOException ioEx) {
         if (this.isClosed()) {
            return null;
         } // if
         
         throw new HL7IOException("HL7FileReader.readMsg:IOException:"
                                  + ioEx.getMessage(), ioEx);
      } // try - catch - finally

      // return the final message string
      if (hl7Msg.length() > 9) {
         hl7Msg.append("\r");
         return hl7Msg.toString();
      } // if

      return null;
   } // _readMsg


   /**
    * Reads a HL7 Message String from the context stream.
    * @return the HL7 message read as a String.
    * @throws java.io.IOException
    */
   public String readMsg() throws HL7IOException {
      if (this.reader == null) {
         if (this.statusValue == HL7FileReader.CLOSED) {
            return(null);
         } // if

         this.open();

         if (this.reader == null) {
            throw new HL7IOException(  "HL7MessageFileStream.ReadMsg: unspecified reader.",
                                       HL7IOException.INCONSISTENT_STATE);
         } // if
      } // if

      return this._readMsg();
   } // readMsg


   /**
    * Reads a HL7 Message String from the context stream. If the stream is not
    * already open, at the time of the call, this method opens the stream
    * using the argument file name.
    * @param argFileName
    * @return the HL7 message read as a String.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public String readMsg(String argFileName) throws HL7IOException {
      if (this.reader == null) {
         if (this.file == null) {
            if (argFileName.length() > 0) {
               this.initialize(new File(argFileName));
            } // if
         } // if
         this.open();
      } // if

      return(this.readMsg());
   } // readMsg


   /**
    * This method wil always throw a INAPPROPRIATE_OPERATION HL7IOException.
    * @param msg
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean write(HL7Message msg) throws HL7IOException {
      throw new HL7IOException( "HL7FileReader.write:Innapropriate operation.",
                                 HL7IOException.INAPPROPRIATE_OPERATION);
   } // write

} // HL7FileReader

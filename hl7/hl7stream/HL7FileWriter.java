/*
 *  $Id: HL7FileWriter.java 25 2010-04-08 19:33:27Z scott.herman@unconxio.us $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7FileWriter.java : A file writer class for HL7 message streams.
 *                       Writes or appends HL7 (v.2.x) transaction messages to a file.
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

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */

import java.io.*;
import java.net.URI;

import us.conxio.hl7.hl7message.HL7Message;


/**
 * Writes HL7Message objects to a file.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7FileWriter extends HL7StreamBase implements HL7Stream {
   private boolean   isAppender = false;
   File              file;
   BufferedWriter    writer;

   /**
    * Creates a new file writer object using the argument File object.
    * @param hl7File
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7FileWriter(File hl7File) throws HL7IOException {
         this.initialize(hl7File);
   } // HL7FileWriter


   /**
    * Creates a new file writer object using the argument file name string.
    * @param hl7FileName
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7FileWriter(String hl7FileName) throws HL7IOException {
      if (hl7FileName == null || hl7FileName.isEmpty()) {
         throw new IllegalArgumentException("HL7FileWriter(): file name not specified.");
      } // if

      this.initialize(new File(hl7FileName));
   } // HL7FileReader


   /**
    * Creates a new file writer object using the argument file URI.
    * @param fileURI
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7FileWriter(URI fileURI) throws HL7IOException {
      HL7StreamURI streamURI = new HL7StreamURI(fileURI);

      if (!streamURI.isFileURI()
      ||   streamURI.isFileReaderURI()
      ||  !(streamURI.isFileWriterURI() || streamURI.isFileAppenderURI() ) ) {
         throw new IllegalArgumentException("HL7FileWriter(" 
                                          + fileURI.toString()
                                          + "):Not a file writer URI.");
      } // if

      if (streamURI.isFileAppenderURI()) {
         this.isAppender = true;
      } // if

      this.initialize(new File(streamURI.fileURIOf()) );
   } // HL7FileWriter


   // * Construction support.
   private void initialize(File hl7File) throws HL7IOException {
      this.mediaType = HL7Stream.FILE_TYPE;
      this.directive = HL7FileWriter.WRITER;
      if (this.isAppender == true) {
         this.directive = HL7Stream.APPENDER;
      } // if

      if (hl7File == null) {
         throw new HL7IOException("HL7FileWriter():File is null.");
      } // if

      this.file = hl7File;
   } // initialize


   /**
    * Opens the context file writer, as a writer.
    * @return a boolean success indicator. true if the operation is successful,
    * otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean open() throws HL7IOException {
      return this.open(this.isAppender);
   } // open


   /**
    * Opens the context file writer, as a writer, or as an appender.
    * @param append a boolean flag indicating whether the writer stream is to be
    * opened as a writer (false) or as an appender (true);
    * @return a boolean success indicator. true if the operation is successful,
    * otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean open(boolean append) throws HL7IOException {
      if (this.file == null) {
         throw new HL7IOException( "HL7FileWriter.open: file name not specified." );
      } // if

      try {
         this.writer = new BufferedWriter(new FileWriter(this.file, append) );
      } catch (IOException ioEx) {
         throw new HL7IOException(  "HL7FileWriter.open:IOException:"
                                 +  ioEx.getMessage(), ioEx);
      } // try - catch

      this.statusValue = HL7Stream.OPEN;
      return true;
   } // openWriter


   /**
    * Writes the argument HL7 message string to the context stream.
    * @param hl7Msg
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public void writeMsg(String hl7Msg) throws HL7IOException {
      if (this.writer == null || !this.isOpen()) {
         this.open();
      } // if
      
      if (this.writer == null) {
         throw new HL7IOException(  "HL7FileWriter.writeMsg: Null writer.",
                                    HL7IOException.INCONSISTENT_STATE);
      } // if

      try {
         // FileWriter always assumes default encoding is OK!
         this.writer.write( hl7Msg + "\n" );
         this.writer.flush();
      } catch (IOException ioEx) {
         throw new HL7IOException(  "HL7FileWriter.writeMsg: IOException", ioEx);

      } // try - catch
   } // writeMsg


   /**
    * Writes the argument HL7 message string to the context stream,
    * opening it using the argument file name string if the stream is not open
    * at the time of the call.
    * @param fileName
    * @param hl7Msg
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
  public void writeMsg(String fileName, String hl7Msg) throws HL7IOException {
      if (this.writer == null && this.statusValue == HL7FileWriter.UNINITIALIZED) {
         if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("HL7FileWriter.writeMsg: file not specified.");
         } // if

         this.initialize(new File(fileName));
         this.open();
      } // if

      this.writeMsg(hl7Msg);
   } // writeMsg


   /**
    * Write the argument HL7Message to the stream
    * @param msg
    * @return a boolean success indicator. true if the operation is successful,
    * otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean write(HL7Message msg) throws HL7IOException {
      this.writeMsg(msg.toString());
      return true;
   } // write


   /**
    * this method always throws a INAPPROPRIATE_OPERATION HL7IOException.
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Message read() throws HL7IOException {
      throw new HL7IOException( "HL7FileWriter.read:Innapropriate operation.",
                                 HL7IOException.INAPPROPRIATE_OPERATION);
   } // read


   /**
    * Closes the context stream writer.
    * @return true if the operation succeeded, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean close()throws HL7IOException {
      if (!this.isOpen()) {
         return true;
      } // if

      try {
         this.writer.close();
      } catch (IOException ioEx) {
         throw new HL7IOException("HL7FileWriter.close:IOException", ioEx);
      } // try - catch

      this.statusValue = HL7FileWriter.CLOSED;
      return true;
   } // close

} // HL7FileWriter

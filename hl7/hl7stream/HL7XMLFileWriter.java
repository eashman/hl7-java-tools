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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

import us.conxio.hl7.hl7message.HL7Message;

/**
 * A HL7Stream class which delivers HL7 XML as either a single file
 * or directory of one or more transactions, 1 per file.
 * @author scott
 */
public class HL7XMLFileWriter extends HL7StreamBase implements HL7Stream {
   private boolean   singleFile = false; // single file is one file for all messages                                        // Otherwise, each message creates it's own file.
   private File      filePath = null;

   private static final String STRING_XML    = "xml";

   /**
    * Fundamental parameterless constructor.
    */
   private HL7XMLFileWriter() { }

   /**
    * Fundamental parameterized constructor.
    * @param path The file path for the delivery destination.
    * If the path exists and is not a directory then single file mode is assumed
    * and the XML transactions are appended to the end of the file.
    * Otherwise the given path is assumed to be a directory and it, and
    * non-existent parent directories, are created
    */
   public HL7XMLFileWriter(File path) {
      filePath = path;
      if (filePath.exists() && filePath.isFile()) singleFile = true;
   } // HL7XMLFileWriter constructor


   /**
    * Universal parameterized constructor.
    * @param uri
    */
   public HL7XMLFileWriter(URI uri) {
      if (uri == null) throw new IllegalArgumentException();
      
      String uriScheme = uri.getScheme();

      if (uriScheme != null && uriScheme.toLowerCase().contains(STRING_XML)) {
         String query = uri.getQuery();
         if (query != null && query.toLowerCase().contains("single") ) {
            singleFile = true;
         } // if

         filePath = new File(uri.getPath());
      } // if
   } // HL7XMLFileWriter constructor


   /**
    * HL7Stream initialization.
    * @return true, always.
    * @throws HL7IOException
    */
   public boolean open() throws HL7IOException {
      directive = HL7Stream.WRITER;
      mediaType = HL7Stream.FILE_TYPE;
      statusValue = HL7Stream.OPEN;

      // For file per message, write actually opens the file.
      // ...so always return true;
      return true;
   } // open


   /**
    * HL7Stream finalization.
    * @return true.
    * @throws HL7IOException
    */
   public boolean close() throws HL7IOException {
      // For file per message, write closes the file.
      // ...so always return true;
      return true;
   } // close


   /**
    * Always throws an HL7IOException exception
    * @return nothing.
    * @throws HL7IOException
    */
   public HL7Message read() throws HL7IOException {
      throw new HL7IOException("read:", 
                               new UnsupportedOperationException("writer does not read.") );
   } // read


   /**
    * Writes the argument HL7Message as an XML file.
    * @param msg A structured HL7Message object.
    * @return true, always.
    * @throws HL7IOException
    */
   public boolean write(HL7Message msg) throws HL7IOException {
      return writeHL7XMLMessageFile(msg);
   } // write


   /**
    * Creates a message based file with the extension(s) ".hl7.xml", as follows:
    *   - If the message control id is non-empty then the it precedes the extension(s).
    *   - Otherwise the name is created from the message type code, message event
    *     code, and the message date time stamp concatenated in that order, and
    *     separated by a dot ('.'), and precedes the extension(s).
    * @param msg the message from which, and for which to create the file name.
    * @return a String representation of the created file name.
    */
   private String hl7XMLMessageFileName(HL7Message msg) {
      String msgID = msg.controlID();

      if (StringUtils.isEmpty(msgID)) {
         String msgTypeCode = msg.get("MSH.9.1");
         String msgEventCode = msg.get("MSH.9.2");
         String msgDateTime = msg.get("MSH.7");
         StringBuilder msgIDBuffer = new StringBuilder();

         if (StringUtils.isNotEmpty(msgTypeCode)) msgIDBuffer.append(msgTypeCode);

         if (StringUtils.isNotEmpty(msgEventCode)) {
            if (msgIDBuffer.length() > 0) msgIDBuffer.append(".");
            msgIDBuffer.append(msgEventCode);
         } // if

         if (StringUtils.isNotEmpty(msgDateTime)) {
            if (msgIDBuffer.length() > 0) msgIDBuffer.append(".");
            msgIDBuffer.append(msgDateTime);
         } // if

         msgID = msgIDBuffer.toString();
      } // if

      return new StringBuffer(filePath.toString())
                  .append(File.separator)
                  .append(msgID)
                  .append(".hl7.xml").toString();

   } // hl7XMLMessageFileName


   /**
    * The write supervisor for creation, writing, and finalization of individual
    * message files.
    * @param msg the message to be written.
    * @return true, always.
    */
   private boolean writeHL7XMLMessageFile(HL7Message msg) throws HL7IOException {
      if (!filePath.exists()) createPath();
      
      
      File xmlFile   = singleFile
                     ? filePath
                     : new File(hl7XMLMessageFileName(msg));

      BufferedWriter writer = null;
      try {
         writer = new BufferedWriter(new FileWriter(xmlFile, singleFile));
         writer.write(msg.toXMLString());
      } catch (IOException ioEx) {
         throw new HL7IOException("write caught IOException:", ioEx);
      } finally {
         try {
            writer.close();
         } catch (IOException ioEx) {
            throw new HL7IOException( "close caught IOException:", ioEx);
         } // try - catch
      } // try - catch - finally

      return true;
   } // writeHL7XMLMessageFile


   private void createPath() throws HL7IOException {
      if (singleFile) {
         File parentPath = filePath.getParentFile();
         if (!parentPath.exists()) parentPath.mkdirs();
         if (!filePath.exists()) {
            try {
               filePath.createNewFile();
            } catch (IOException ioEx) {
               throw new HL7IOException("IOException:", ioEx);
            } // try - catch
         } // if

         return;
      } // if

      if (filePath.exists() ) {
         if (filePath.isFile()) singleFile = true;
         return;
      } // if

      filePath.mkdirs();
   } // createPath

} // HL7XMLFileWriter

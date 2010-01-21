/*
 *  $Id: $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7StreamBase.java : A default base implementation of the HL7Stream interface.
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

import java.io.*;
import java.net.URI;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import us.conxio.HL7.HL7Message.HL7Message;

/**
 *
 * @author scott
 */
public class HL7XMLFileWriter extends HL7StreamBase implements HL7Stream {
   private boolean   isDir = true;
   private boolean   singleFile = false;
   private File      filePath = null;

   private HL7XMLFileWriter() { }

   public HL7XMLFileWriter(File path) {
      this.filePath = path;
   } // HL7XMLFileWriter constructor

   public HL7XMLFileWriter(URI uri) {
      if (uri != null) {
         String uriScheme = uri.getScheme();

         if (uriScheme != null && uriScheme.toLowerCase().contains("xml")) {
            String query = uri.getQuery();
            if (query != null && query.toLowerCase().contains("single") ) {
               this.singleFile = true;
            } // if
            this.filePath = new File(uri.getPath());
         } // if
      } // if
   } // HL7XMLFileWriter constructor


   public boolean open() throws HL7IOException {
      throw new HL7IOException("open:", new UnsupportedOperationException("Not supported yet.") );
   } // open

   public boolean close() throws HL7IOException {
      throw new HL7IOException("close:", new UnsupportedOperationException("Not supported yet.") );
   } // close

   public HL7Message read() throws HL7IOException {
      throw new HL7IOException("read:", new UnsupportedOperationException("Not supported yet.") );
   } // read

   public boolean write(HL7Message msg) throws HL7IOException {
      if (!this.singleFile) {
         return writeHL7XMLMessageFile(msg);
      } else {
         throw new HL7IOException("write/singleFile:", new UnsupportedOperationException("Not supported yet.") );
      } // if - else
   } // write

   private boolean writeHL7XMLMessageFile(HL7Message msg) {
      String msgID = msg.controlID();
      String fqFilePath = new StringBuffer(this.filePath.toString())
                                       .append(this.filePath.separator)
                                       .append(msgID)
                                       .append(".hl7.xml").toString();
      if (!this.filePath.exists()) {
         this.filePath.mkdir();
      } // if
      File xmlFile = new File(fqFilePath);

      BufferedWriter writer = null;
      try {
         writer = new BufferedWriter(new FileWriter(xmlFile));
         writer.write(msg.toXMLString());
      } catch (IOException ex) {
         Logger.getLogger(HL7XMLFileWriter.class.getName()).log(Level.ERROR, null, ex);
      } finally {
         try {
            writer.close();
         } catch (IOException ex) {
            Logger.getLogger(HL7XMLFileWriter.class.getName()).log(Level.ERROR, null, ex);
         } // try - catch
      } // try - catch - finally

      return true;
   } // writeHL7XMLMessageFile

} // HL7XMLFileWriter

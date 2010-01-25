/*
 *  $Id: HL7StreamURI.java 49 2009-12-15 03:53:03Z scott $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7StreamURI.java : An interpreter class for HL7Stream URI specifiers.
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
 * @author scott herman <scott.herman@unconxio.us>
 */

import java.net.URI;
import java.net.URISyntaxException;


/**
 * A URI class for parsing HL7 i/o stream URIs.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7StreamURI {
   URI   uri;


   /**
    * Constructs a new stream URI from the argument URI.
    * @param uri
    */
   public HL7StreamURI(URI uri) {
      this.uri = uri;
   } // HL7StreamURI


   /**
    * Constructs a new stream URI from the argument string.
    * @param uriStr
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7StreamURI(String uriStr) throws HL7IOException {
      try {
         this.uri = new URI(uriStr);
      } catch (URISyntaxException uEx) {
         throw new HL7IOException("HL7StreamURI:URISyntaxException", uEx);
      } // try - catch
   } // HL7StreamURI


   /**
    * Determines whether the context URI refers to a server.
    * @return true if the context URI could refer to a server, otherwise false.
    */
   public boolean isServerURI() {
      String uriScheme = this.uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (uriScheme.contains("server")) {
         return true;
      } // if

      return false;
   } // isServerURI


   /**
    * Determines whether the context HL7URI is a XML stream.
    * @return true if the context HL7URI is a XML stream, otherwise false.
    */
   public boolean isXMLURI() {
      String uriScheme = this.uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (uriScheme.contains("xml")) {
         return true;
      } // if

      return false;
   } // isXMLURI


   /**
    * Determines whether the context URI refers to a socket.
    * @return true if the context URI could refer to a socket, otherwise false.
    */
   public boolean isSocketURI() {
      String uriScheme = this.uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (  uriScheme.contains("tcp")
         || uriScheme.contains("hl7")
         || uriScheme.contains("llp") ) {
         return true;
      } // if

      return false;
   } // isSocketURI


   /**
    * Determines whether the context URI refers to a file.
    * @return true if the context URI could refer to a file, otherwise false.
    */
   public boolean isFileURI() {
      String uriScheme = this.uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (uriScheme.contains("file")) {
         return true;
      } // if

      return false;
   } // isFileURI


   /**
    * Reformulates the context URI to have a shceme of "file".
    * @return the URI resulting from the scheme replacement.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public URI fileURIOf() throws HL7IOException {
      String uriScheme = this.uri.getScheme();
      String uriString = this.uri.toString();
      String replaced = uriString.replaceFirst(uriScheme, "file");
      try {
         return new URI(replaced);
      } catch (URISyntaxException uEx) {
         throw new HL7IOException("HL7StreamURI:fileURIOf():URISyntaxException:", uEx);
      } // try - catch
   } // fileURIOf


   /**
    * Access to the context URI itself.
    * @param uri The context URI itself.
    */
   public void setURI(URI uri) {
      this.uri = uri;
   } // setURI


   /**
    * Determines whether the context URI is a file reader.
    * @return true if the context URI refers to a file reader, otherwise false.
    */
   public boolean isFileReaderURI() {
      String uriScheme = this.uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (uriScheme.equals("file-reader") ) {
         return true;
      } // if

      return false;
   } // isFileReaderURI


   /**
    * Determines whether the context URI is a file writer.
    * @return true if the context URI refers to a file writer, otherwise false.
    */
   public boolean isFileWriterURI() {
      String uriScheme = this.uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (uriScheme.equals("file-writer")) {
         return true;
      } // if

      return false;
   } // isFileWriterURI


   /**
    * Determines the server thread pool size specified in the context URI
    * @return 0 or the specified thread pool size for the context server URI.
    */
   public int uriServerPoolSize() {
      int poolSize = 0;

      String queryStr = this.uri.getQuery();
      if ( queryStr != null && queryStr.length() > 0
      &&   queryStr.toLowerCase().startsWith("pool")) {
         int eqIndex = queryStr.indexOf("=");
         if (eqIndex >= 0) {
            poolSize = Integer.parseInt(queryStr.substring(eqIndex + 1));
         } // if
      } // if

      return poolSize;
   } // uriServerPoolSize


   /**
    * Determines the port number specified in the context URI
    * @return 0 or the specified port number for the context server URI.
    * @return the port number specified in the context URI.
    */
   public int uriPortNo() {
      int portNo = 0;
      if ( (portNo = this.uri.getPort()) < 0) {
         String schemeStr = uri.getScheme();
         if (schemeStr.toLowerCase().contains("hl7")) {
            portNo = 2575;
         } // if
      } // if

      return portNo;
   } // uriPortNo


   /**
    * Creates and returns an apppropriate stream reader based on the context URI.
    * @return the reader as a HL7Stream implementation.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Stream getHL7StreamReader() throws HL7IOException {
      if (this.isFileURI() && !this.isFileWriterURI()) {
         return new HL7FileReader(this.fileURIOf());
      } else if (this.isServerURI()) {
         return new HL7Server(this.uriPortNo(), this.uriServerPoolSize());
      } // if - else if

      throw new HL7IOException(  "HL7StreamURI.getHL7StreamReader():Uninterpreable URI:"
                                 + this.uri.toString(),
                                 HL7IOException.UNINTERPERABLE_URI);
   } // getHL7StreamReader


   /**
    * Creates and returns an apppropriate stream writer based on the context URI.
    * @return the writer as a HL7Stream implementation.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Stream getHL7StreamWriter() throws HL7IOException {
      if (this.isFileURI() && !this.isFileReaderURI()) {
         return new HL7FileWriter(this.fileURIOf());
      } else if (this.isSocketURI()) {
         return new HL7SocketStream(this.uri.getHost(), this.uriPortNo());
      } else if (this.isXMLURI()) {
         return new HL7XMLFileWriter(this.uri);
      } // if - else if

      throw new HL7IOException(  "HL7StreamURI.getHL7StreamWriter():Uninterpreable URI:"
                                 + this.uri.toString(),
                                 HL7IOException.UNINTERPERABLE_URI);
   } // getHL7StreamWriter


} // HL7StreamURI

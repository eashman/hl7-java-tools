/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7StreamURI.java : An interpreter class for HL7Stream URI specifiers.
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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;


/**
 * A URI class for parsing HL7 i/o stream URIs.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7StreamURI {
   private URI   uri;

   public static final String HL7STREAM_URI_SCHEME_FILE_READER          = "file-reader";
   public static final String HL7STREAM_URI_SCHEME_FILE_WRITER          = "file-writer";
   public static final String HL7STREAM_URI_SCHEME_FILE_APPENDER        = "file-appender";
   public static final String HL7STREAM_URI_SCHEME_FILE                 = "file";

   public static final String HL7STREAM_URI_SCHEME_MLLP_WRITER          = "mllp-writer";
   public static final String HL7STREAM_URI_SCHEME_MLLP_READER          = "mllp-reader";
   public static final String HL7STREAM_URI_SCHEME_MLLP_CLIENT          = "mllp-client";
   public static final String HL7STREAM_URI_SCHEME_MLLP_SERVER          = "mllp-server";
   public static final String HL7STREAM_URI_SCHEME_MLLP                 = "mllp";

   public static final String HL7STREAM_URI_SCHEME_LLP_WRITER           = "llp-writer";
   public static final String HL7STREAM_URI_SCHEME_LLP_READER           = "llp-reader";
   public static final String HL7STREAM_URI_SCHEME_LLP_CLIENT           = "llp-client";
   public static final String HL7STREAM_URI_SCHEME_LLP_SERVER           = "llp-server";
   public static final String HL7STREAM_URI_SCHEME_LLP                  = "llp";

   public static final String HL7STREAM_URI_SCHEME_HL7_CLIENT           = "hl7-client";
   public static final String HL7STREAM_URI_SCHEME_HL7_SERVER           = "hl7-server";
   public static final String HL7STREAM_URI_SCHEME_HL7                  = "hl7";

   public static final String HL7STREAM_URI_SCHEME_SECURE_HL7           = "hl7-ssl";
   public static final String HL7STREAM_URI_SCHEME_SECURE_HL7_CLIENT    = "hl7-ssl-client";
   public static final String HL7STREAM_URI_SCHEME_SECURE_HL7_SERVER    = "hl7-ssl-server";

   public static final String HL7STREAM_URI_SCHEME_TCP_CLIENT           = "tcp-client";
   public static final String HL7STREAM_URI_SCHEME_TCP_SERVER           = "tcp-server";
   public static final String HL7STREAM_URI_SCHEME_TCP                  = "tcp";

   public static final String HL7STREAM_URI_SCHEME_SECURE               = "ssl";
   public static final String HL7STREAM_URI_SCHEME_SECURE_CLIENT        = "ssl-client";
   public static final String HL7STREAM_URI_SCHEME_SECURE_SERVER        = "ssl-server";

   private static final String STRING_SERVER = "server";
   private static final String STRING_XML    = "xml";
   private static final String STRING_SSL    = "ssl";

   private static final String URI_SCHEME_TERMINATOR = ":";
   private static final String URI_AUTH_PREFIX = "//";
   private static final String URI_PORT_PREFIX = URI_SCHEME_TERMINATOR;


   private HL7StreamURI() { }
   /**
    * Constructs a new stream URI from the argument URI.
    * @param uri
    */
   public HL7StreamURI(URI uriArg) {
      uri = uriArg;
   } // HL7StreamURI


   /**
    * Constructs a new stream URI from the argument string.
    * @param uriStr
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7StreamURI(String uriStr) throws HL7IOException {
      try {
         uri = new URI(uriStr);
      } catch (URISyntaxException uEx) {
         throw new HL7IOException("HL7StreamURI:URISyntaxException", uEx);
      } // try - catch
   } // HL7StreamURI

   public HL7StreamURI(String host, int port) throws HL7IOException {
      this();

      StringBuilder uriBuilder = new StringBuilder(HL7STREAM_URI_SCHEME_TCP)
                                       .append(URI_SCHEME_TERMINATOR)
                                       .append(URI_AUTH_PREFIX);

      if (StringUtils.isNotEmpty(host)) uriBuilder.append(host);

      if (port > 0) uriBuilder.append(URI_PORT_PREFIX)
                              .append(Integer.toString(port));

      try {
         uri = new URI(uriBuilder.toString());
      } catch (URISyntaxException uEx) {
         throw new HL7IOException("URISyntaxException", uEx);
      } // try - catch
   } // HL7StreamURI


   /**
    * Determines whether the context URI refers to a server.
    * @return true if the context URI could refer to a server, otherwise false.
    */
   public boolean isServerURI() {
      return schemeHas(STRING_SERVER);
   } // isServerURI


   /**
    * Determines whether the context HL7URI is a XML stream.
    * @return true if the context HL7URI is a XML stream, otherwise false.
    */
   public boolean isXMLURI() {
      return schemeHas(STRING_XML);
   } // isXMLURI


   /**
    * Determines whether the context URI refers to a socket.
    * @return true if the context URI could refer to a socket, otherwise false.
    */
   public boolean isSocketURI() {
      return isSimpleSocketURI() || isSecureSocketURI();
   } // isSocketURI


   /**
    * Determines whether the context URI refers to an unsecured socket.
    * @return true if the context URI could refer to an unsecured socket,
    * otherwise false.
    */
    public boolean isSimpleSocketURI() {
      return schemeHas(HL7STREAM_URI_SCHEME_TCP)
          || schemeHas(HL7STREAM_URI_SCHEME_HL7)
          || schemeHas(HL7STREAM_URI_SCHEME_LLP);
    } // isSocketURI


   /**
    * Determines whether the context URI refers to a secured socket.
    * @return true if the context URI could refer to a secured socket,
    * otherwise false.
    */
   public boolean isSecureSocketURI() {
      return schemeHas(STRING_SSL);
   } // isSocketURI

  /**
    * Determines whether the context URI refers to a file.
    * @return true if the context URI could refer to a file, otherwise false.
    */
   public boolean isFileURI() {
      return schemeHas(HL7STREAM_URI_SCHEME_FILE);
   } // isFileURI


   /**
    * Reformulates the context URI to have a scheme of "file".
    * @return the URI resulting from the scheme replacement.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public URI fileURIOf() throws HL7IOException {
      String uriScheme = getScheme();
      String uriString = uri.toString();
      String replaced = uriString.replaceFirst(uriScheme, "file");
      try {
         return new URI(replaced);
      } catch (URISyntaxException uEx) {
         throw new HL7IOException("HL7StreamURI:fileURIOf():URISyntaxException:", uEx);
      } // try - catch
   } // fileURIOf


   /**
    * Access to the context URI itself.
    * @param uriArg The context URI itself.
    */
   public void setURI(URI uriArg) {
      uri = uriArg;
   } // setURI


   /**
    * Determines whether the context URI is a file reader.
    * @return true if the context URI refers to a file reader, otherwise false.
    */
   public boolean isFileReaderURI() {
      return schemeHas(HL7STREAM_URI_SCHEME_FILE_READER);
   } // isFileReaderURI


   /**
    * Determines whether the context URI is a file writer.
    * @return true if the context URI refers to a file writer, otherwise false.
    */
   public boolean isFileWriterURI() {
      return schemeHas(HL7STREAM_URI_SCHEME_FILE_WRITER);
   } // isFileWriterURI


    public boolean isFileAppenderURI() {
      return schemeHas(HL7STREAM_URI_SCHEME_FILE_APPENDER);
   } // isFileAppenderURI


  /**
    * Determines the server thread pool size specified in the context URI
    * @return 0 or the specified thread pool size for the context server URI.
    */
   public int uriServerPoolSize() {
      int poolSize = 0;

      String queryStr = uri.getQuery();
      if ( StringUtils.isNotEmpty(queryStr)
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
   public int getPortNo() {
      int portNo = 0;
      if ( (portNo = uri.getPort()) < 0) {
         if (schemeHas(HL7STREAM_URI_SCHEME_HL7)) portNo = 2575;
      } // if

      return portNo;
   } // getPortNo


   /**
    * Creates and returns an appropriate stream reader based on the context URI.
    * @return the reader as a HL7Stream implementation.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Stream getHL7StreamReader() throws HL7IOException {
      if (isFileURI() && !isFileWriterURI()) {
         return new HL7FileReader(fileURIOf());
      } else if (isServerURI()) {
         return new HL7Server(getPortNo(), uriServerPoolSize());
      } // if - else if

      throw new HL7IOException(  "HL7StreamURI.getHL7StreamReader():Uninterpreable URI:"
                                 + uri.toString(),
                                 HL7IOException.UNINTERPERABLE_URI);
   } // getHL7StreamReader


   /**
    * Creates and returns an appropriate stream writer based on the context URI.
    * @return the writer as a HL7Stream implementation.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Stream getHL7StreamWriter() throws HL7IOException {
      if (canWriteFiles()) {
         return new HL7FileWriter(uri);
      } else if (isSimpleSocketURI()) {
         return new HL7MLLPStream(uri.getHost(), getPortNo());
      } else if (isXMLURI()) {
         return new HL7XMLFileWriter(uri);
      } // if - else if

      throw new HL7IOException(  "HL7StreamURI.getHL7StreamWriter():Uninterpreable URI:"
                                 + this.uri.toString(),
                                 HL7IOException.UNINTERPERABLE_URI);
   } // getHL7StreamWriter

   public String getHostName() {
      return uri.getHost();
   } // getHostName

   public boolean isValid() {
      // TODO: More comprehehnsive validation.
      return   isFileAppenderURI()
         ||    isFileReaderURI()
         ||    isFileWriterURI()
         ||    isServerURI()
         ||    isSocketURI();
   } // isValid


   public URI uri() {
      return uri;
   } // uri

   private boolean hasScheme() {
      return hasURI() && uri.getScheme() != null;
   } // hasScheme

   private boolean hasURI() {
      return uri != null;
   } // hasURI

   private String getScheme() {
      return hasScheme() ? uri.getScheme().toLowerCase() : null;
   } // getScheme

   private boolean schemeHas(String str) {
      return hasScheme() && getScheme().contains(str);
   } // schemeHas

   boolean canWriteFiles() {
      return isFileWriterURI()
      ||     isFileAppenderURI()
      ||    (isFileURI() && !isFileReaderURI());
   } // canWriteFiles


   @Override
   public String toString() {
      return uri.toString();
   } // toString
} // HL7StreamURI

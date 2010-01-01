/*
 *  $Id: HL7SocketStream.java 68 2009-12-30 20:17:31Z scott $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7SocketStream.java : A HL7Stream clas for tcp/ip HL7Message i/o.
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
 * @author scott
 */

import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;


import us.conxio.HL7.HL7Message.*;



/**
 * A tcp socket stream object for reading and writing HL7Message objects.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7SocketStream extends HL7StreamBase implements HL7Stream {
   private static Logger   logger;
   static final int        STX           = 0x0b,
                           FS            = 0x1c,
                           EOB           = 0x0d;
   /**
    * the host name of the destination of the socket, if a writer, or "locahost"
    * if the stream is a reader.
    */
   String                  host;
   /**
    * The port number of the destination if a writer, or the port whcih the
    * server is listening on, if a server.
    */
   int                     port,
   /**
    * A desired timeout value for socket i/o. defaulted to 30 seconds.
    */
                           timeOutSeconds = 30;
   /**
    * The i/o socket reference by which the object performs i/o.
    */
   Socket                  socket;
   /**
    * The BufferedReader used for reading the socket.
    */
   BufferedReader          in;
   /**
    * The BufferedWriter used for writing to the socket.
    */
   BufferedWriter          out;


   /**
    * Constructs a new stream object using the argument socket.
    * @param sock A connected socket resulting from a connection request to a
    * listening port specified in an invocation of accept().
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7SocketStream(Socket sock) throws HL7IOException {
      if (HL7SocketStream.logger == null) {
         logger = Logger.getLogger(this.getClass());
         logger.setLevel(Level.TRACE);
      } // if

      this.socket = sock;
      this.directive = HL7SocketStream.READER;
      this.mediaType = HL7SocketStream.SOCKET_TYPE;
   } // HL7SocketStream


   /**
    * Constructs a new stream object using the argument socket.
    * @param sock A connected socket resulting from a connection request to a
    * listening port specified in an invocation of accept().
    * @param openReq A boolean flag indicating that the reader should be opened
    * at the time of construction.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7SocketStream(Socket sock, boolean openReq) throws HL7IOException {
      this(sock);
      if (openReq) {
         this.openReader();
      } // if
   } // HL7SocketStream


   /**
    * Creates an outgoing tcp socket stream connection to the argument host
    * and port.
    * @param host The host DNS name to which the connection is requested.
    * @param port The tcp port number upon which the connection is requested.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7SocketStream(String host, int port) throws HL7IOException {
      if (HL7SocketStream.logger == null) {
         logger = Logger.getLogger(this.getClass());
         logger.setLevel(Level.TRACE);
      } // if

      this.port = port;
      this.host = host;
      this.directive = HL7SocketStream.WRITER;
      this.mediaType = HL7SocketStream.SOCKET_TYPE;
   } // HL7SocketStream


   /**
    * Creates an outgoing tcp socket stream connection to the argument host
    * and port.
    * @param host The host DNS name to which the connection is requested.
    * @param port The tcp port number upon which the connection is requested.
    * @param openReq A boolean flag indicating that the writer should be opened
    * at the time of construction.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7SocketStream(String host, int port, boolean openReq) throws HL7IOException {
      this(host, port);
      if (openReq) {
         this.openWriter();
      } // if
   } // HL7SocketStream


   /**
    * Closes the context tcp socket stream.
    * @return true if the operation is successful, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean close() throws HL7IOException {
      try {
         this.socket.close();
      } catch (IOException ioEx) {
         throw new HL7IOException(  "HL7SocketStream("
                                 +  this.host 
                                 +  ", " 
                                 +  this.port 
                                 +  ").close:IOException", ioEx);
      } // try - catch

      this.statusValue = HL7SocketStream.CLOSED;
      return true;
   }  // close


   private boolean openReader() throws HL7IOException {
      try {
         this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
         this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      } catch (IOException ioEx) {
         throw new HL7IOException("HL7SocketStream(sock):IOException", ioEx);
      } // try - catch

      this.statusValue = HL7SocketStream.OPEN;
      return true;
   } // openReader


   private boolean openWriter() throws HL7IOException {
      Socket      sock;

      try {
         sock = new Socket(this.host, this.port);
         sock.setSoTimeout(this.timeOutSeconds * 1000);
         this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
         this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      } catch (UnknownHostException uhEx) {
         throw new HL7IOException(  "HL7SocketStream(" + host + ", " + port + "):UnknownHost.",
                                    HL7IOException.UNKNOWN_HOST);
      } catch (SocketException sEx) {
         throw new HL7IOException("HL7SocketStream(" + host + ", " + port + "):SocketException", sEx);
      } catch (IOException ioEx) {
         throw new HL7IOException("HL7SocketStream(" + host + ", " + port + "):IOException", ioEx);
      } // try - catch

      this.socket = sock;
      this.statusValue = HL7SocketStream.OPEN;
      return true;
   } // openWriter


   /**
    * Opens the specified tcp socket stream.
    * @return true if the operation is successful, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean open() throws HL7IOException {
      if (this.statusValue == HL7SocketStream.OPEN) {
         return true;
      } // if

      if (this.directive == HL7SocketStream.WRITER) {
         return this.openWriter();
      } else if (this.directive == HL7SocketStream.READER) {
         return this.openReader();
      } // if - else if

      throw new HL7IOException(  "HL7SocketStream.open():Not a reader. Not a writer.",
                                 HL7IOException.INCONSISTENT_STATE);
   } // open


   private String _readMsg() throws HL7IOException {
      StringBuffer   hl7Msg = new StringBuffer();
      int            inData;
      boolean        startFound = false,
                     fsFound = false;

      try {
         while ( (inData = in.read()) != -1) {
            if (inData == STX) {
               startFound = true;
               continue;
            } // if

            if (inData == FS) {
               fsFound = true;
               continue;
            } // if

            if (inData == EOB && fsFound == true) {
               return(hl7Msg.toString());
            } // if

            if (startFound == true) {
               hl7Msg.append( (char)inData);
            } // if
         } // while
      } catch (IOException ioEx) {
         throw new HL7IOException(  "HL7SocketStream("
                                 +  this.host
                                 +  ", "
                                 +  this.port
                                 +  "):IOException", ioEx);
      } // try - catch

      if (hl7Msg.length() > 0) {
         return hl7Msg.toString();
      } // if

      return null;
   } // _readMsg


   /**
    * Reads a HL7 message string from the context socket stream.
    * @return a String containing the read message.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public String readMsg() throws HL7IOException {
      if (this.isClosed()) {
         throw new HL7IOException(  "HL7SocketStream("
                                 +  this.host
                                 +  ", "
                                 +  this.port
                                 +  ").readMsg:Socket previously closed.", HL7IOException.STREAM_CLOSED);
      } // if

      if (this.socket.isClosed() || !this.socket.isConnected()) {
         this.close();
         throw new HL7IOException(  "HL7SocketStream("
                                 +  this.host
                                 +  ", "
                                 +  this.port
                                 +  ").readMsg:Socket closed. Closing Stream", HL7IOException.STREAM_CLOSED);
      } // if - else if

      String retnStr = this._readMsg();
      if (retnStr != null && !retnStr.isEmpty()) {
         return retnStr;
      } // if

      if (this.socket.isClosed() || !this.socket.isConnected()) {
         this.close();
         throw new HL7IOException(  "HL7SocketStream("
                                 +  this.host
                                 +  ", "
                                 +  this.port
                                 +  ").readMsg:Socket closed after read. Closing Stream", HL7IOException.STREAM_CLOSED);
      } // if - else if

      return(null);
    } // readMsg


   /**
    * Reads a HL7Message object from the context socket stream.
    * @return the read HL7Message object.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Message read() throws HL7IOException {
      String msgStr = this.readMsg();
      if (msgStr == null || msgStr.isEmpty()) {
         return null;
      } // if

      HL7Message msg = new HL7Message(msgStr);
      String msgCtlID = msg.get("MSH.10")[0];
      String traceHeader = new StringBuffer( "read(): got [")
                                    .append( msg.IDString())
                                    .append( "].[")
                                    .append( msgCtlID)
                                    .append( "]:").toString();
      HL7SocketStream.logger.trace(traceHeader);
      HL7SocketStream.logger.trace(msg.toString());

      HL7Message ack = msg.Acknowledgment(true, "ok", null, null);
      traceHeader = new StringBuffer( "read(): Sending acknowledgment [")
                              .append(ack.IDString())
                              .append("].[")
                              .append(ack.get("MSH.10")[0])
                              .append( "]:").toString();
      this.writeMsg(ack.toString());
      HL7SocketStream.logger.trace(traceHeader);
      HL7SocketStream.logger.trace(ack.toString());

      return(msg);
   } // read


   /**
    * Writes a HL7 message string on the context outound stream.
    * @param msg a HL7 message string.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public void writeMsg(String msg) throws HL7IOException {
      if (msg == null) {
         throw new HL7IOException(  "HL7SocketStream.writeMsg:Null HL7 Msg.",
                                    HL7IOException.NULL_MSG);
      } // if

      if (!this.isOpen() || this.out == null) {
         this.open();
      } // if

      try {
         this.out.write( (char)HL7SocketStream.STX
                        + msg
                        + (char)HL7SocketStream.FS
                        + (char)HL7SocketStream.EOB);
         this.out.flush();
      } catch (IOException ioEx) {
         throw new HL7IOException(  "HL7SocketStream("
                                 +  this.host
                                 +  ", "
                                 +  this.port
                                 +  ").writeMsg:IOException", ioEx);
      } // try - catch
   } // writeMsg


   /**
    * Writes the argument HL7Message object on the context outbound stream.
    * @param hl7Msg the HL7Message object to be written.
    * @return true if the operation is successful, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   @SuppressWarnings("empty-statement")
   public boolean write(HL7Message hl7Msg) throws HL7IOException {
      String msgCtlID = hl7Msg.controlID();
      String traceHeader = "write(" + hl7Msg.IDString() + "):";
      HL7SocketStream.logger.trace(traceHeader + " writing.");
      this.writeMsg(hl7Msg.toString());
      HL7SocketStream.logger.trace(traceHeader + " wrote.");

      String hl7MsgStr;
      while ( (hl7MsgStr = this.readMsg() ) == null)
         ;

      // * If sent msg has no encoding characters then the ack will have no encoders either.
      HL7Message reply = new HL7Message(hl7MsgStr);
      String ackCode = reply.get("MSA.1")[0];
      String replyCtlID = reply.get("MSA.2")[0];
      HL7SocketStream.logger.trace( traceHeader
                                 +  " got "
                                 +  reply.IDString()
                                 +  ":"
                                 +  ackCode
                                 +  "."
                                 +  replyCtlID
                                 +  ".");

      if (!ackCode.equals("AA")) {
         HL7IOException hiEx = new HL7IOException( "HL7SocketStream."
                                                +  traceHeader
                                                +  "NAck ("
                                                +  ackCode
                                                +  ") received with "
                                                +  replyCtlID
                                                +  " in "
                                                +  reply.IDString(),
                                            HL7IOException.HL7_NACK);
         HL7SocketStream.logger.error("throwing ", hiEx);
         throw hiEx;
      } else if (!replyCtlID.equals(msgCtlID)) {
         HL7IOException hiEx =  new HL7IOException(traceHeader
                                             +     "Ack received for "
                                             +     msgCtlID
                                             +     "with MSA.2: "
                                             +     replyCtlID
                                             +     " in "
                                             +     reply.IDString(),
                                          HL7IOException.WRONG_CTLID);
         HL7SocketStream.logger.error("throwing ", hiEx);
         throw hiEx;
      } // if

      return true;
   } // write


} // HL7SocketStream

/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7MLLPStream.java : A HL7Stream class for unsecured tcp/ip HL7Message i/o.
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


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

import us.conxio.hl7.hl7message.HL7Message;

/**
 *
 * @author scott
 */
public class HL7MLLPStream extends HL7StreamBase implements HL7Stream {
   private static final int   STX           = 0x0b,
                              FS            = 0x1c,
                              EOB           = 0x0d;
   private static Logger      logger = Logger.getLogger(HL7MLLPStream.class);

   private static final String ACK_CODE_OK = HL7Message.ACK_CODE_OK;


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
   public HL7MLLPStream(Socket sock) throws HL7IOException {
      socket = sock;
      directive = READER;
      mediaType = SOCKET_TYPE;
   } // HL7MLLPStream


   /**
    * Constructs a new stream object using the argument socket.
    * @param sock A connected socket resulting from a connection request to a
    * listening port specified in an invocation of accept().
    * @param openReq A boolean flag indicating that the reader should be opened
    * at the time of construction.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7MLLPStream(Socket sock, boolean openReq) throws HL7IOException {
      this(sock);
      if (openReq) openReader();
   } // HL7MLLPStream


   /**
    * Creates an outgoing tcp socket stream connection to the argument host
    * and port.
    * @param host The host DNS name to which the connection is requested.
    * @param port The tcp port number upon which the connection is requested.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7MLLPStream(String hostStr, int portV) throws HL7IOException {
      port = portV;
      host = hostStr;
      directive = WRITER;
      mediaType = SOCKET_TYPE;
   } // HL7MLLPStream


   /**
    * Creates an outgoing tcp socket stream connection to the argument host
    * and port.
    * @param host The host DNS name to which the connection is requested.
    * @param port The tcp port number upon which the connection is requested.
    * @param openReq A boolean flag indicating that the writer should be opened
    * at the time of construction.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7MLLPStream(String host, int port, boolean openReq) throws HL7IOException {
      this(host, port);
      if (openReq) openWriter();
   } // HL7MLLPStream




   public boolean open() throws HL7IOException {
      if (isOpen()) return true;

      if (isWriter()) {
         return openWriter();
      } else if (isReader()) {
         return openReader();
      } // if - else if

      throw new HL7IOException(  "Not a reader. Not a writer.",
                                 HL7IOException.INCONSISTENT_STATE);
   } // open


   /**
    * Closes the context tcp socket stream.
    * @return true if the operation is successful, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public boolean close() throws HL7IOException {
      if (isClosed()) return true;

      try {
         if (!socket.isClosed()) socket.close();
      } catch (IOException ioEx) {
         throw new HL7IOException(streamID() + "IOException", ioEx);
      } // try - catch

      statusValue = CLOSED;
      return true;
   }  // close



   private String _readMsg() throws HL7IOException {
      StringBuilder  hl7Msg = new StringBuilder();
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

            if (inData == EOB && fsFound) return(hl7Msg.toString());

            if (startFound) hl7Msg.append( (char)inData);
         } // while
      } catch (IOException ioEx) {
         throw new HL7IOException(streamID() + "IOException", ioEx);
      } // try - catch

      if (hl7Msg.length() > 0) return hl7Msg.toString();

      return null;
   } // _readMsg


   /**
    * Reads a HL7 message string from the context socket stream.
    * @return a String containing the read message.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public String readMsg() throws HL7IOException {
      if (isClosed()) {
         throw new HL7IOException(streamID() + "Socket previously closed.", HL7IOException.STREAM_CLOSED);
      } // if

      if (socket.isClosed() || !socket.isConnected()) {
         close();
         throw new HL7IOException(streamID() + "Socket closed. Closing Stream", HL7IOException.STREAM_CLOSED);
      } // if - else if

      String retnStr = _readMsg();
      if (retnStr != null && !retnStr.isEmpty()) return retnStr;

      if (socket.isClosed() || !socket.isConnected()) {
         close();
         throw new HL7IOException(streamID() + "Socket closed after read. Closing Stream", HL7IOException.STREAM_CLOSED);
      } // if - else if

      return(null);
    } // readMsg


   /**
    * Reads a HL7Message object from the context socket stream.
    * @return the read HL7Message object.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public HL7Message read() throws HL7IOException {
      String msgStr = readMsg();
      if (msgStr == null || msgStr.isEmpty()) return null;

      HL7Message msg = new HL7Message(msgStr);
      String msgCtlID = msg.get("MSH.10");
      String traceHeader = new StringBuffer( "read(): got [")
                                    .append( msg.idString())
                                    .append( "].[")
                                    .append( msgCtlID)
                                    .append( "]:").toString();
      logger.trace(traceHeader);
      logger.trace(msg.toString());

      HL7Message ack = msg.acknowledgment(true, "ok", null, null);
      traceHeader = new StringBuffer( "read(): Sending acknowledgment [")
                              .append(ack.idString())
                              .append("].[")
                              .append(ack.get("MSH.10"))
                              .append( "]:").toString();
      writeMsgString(ack.toString());
      logger.trace(traceHeader);
      logger.trace(ack.toString());

      return(msg);
   } // read


   /**
    * Writes a HL7 message string on the context outound stream.
    * @param msg a HL7 message string.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public void writeMsgString(String msg) throws HL7IOException {
      if (msg == null) {
         throw new HL7IOException(  "Null HL7 Msg.",
                                    HL7IOException.NULL_MSG);
      } // if

      if (!isOpen() || out == null) open();

      try {
         out.write( (char)STX
                  + msg
                  + (char)FS
                  + (char)EOB);
         out.flush();
      } catch (IOException ioEx) {
         throw new HL7IOException(streamID() +  "IOException", ioEx);
      } // try - catch
   } // writeMsgString



   /**
    * Writes the argument HL7Message object on the context outbound stream.
    * @param hl7Msg the HL7Message object to be written.
    * @return true if the operation is successful, otherwise false.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   @SuppressWarnings("empty-statement")
   public boolean write(HL7Message hl7Msg) throws HL7IOException {
      String msgCtlID = hl7Msg.controlID();
      String traceHeader = "write(" + hl7Msg.idString() + "):";
      logger.trace(traceHeader + " writing.");
      writeMsgString(hl7Msg.toHL7String());
      logger.trace(traceHeader + " wrote.");

      String hl7MsgStr;
      while ( (hl7MsgStr = readMsg() ) == null)
         ;

      // * If sent msg has no encoding characters then the ack will have no encoders either.
      HL7Message reply = new HL7Message(hl7MsgStr);
      String ackCode = reply.get("MSA.1");
      String replyCtlID = reply.get("MSA.2");
      logger.trace( traceHeader
                     +  " got "
                     +  reply.idString()
                     +  ":"
                     +  ackCode
                     +  "."
                     +  replyCtlID
                     +  ".");

      if (!ackCode.equals(ACK_CODE_OK)) {
         HL7IOException hiEx = new HL7IOException( streamID()
                                                +  traceHeader
                                                +  "NAck ("
                                                +  ackCode
                                                +  ") received with "
                                                +  replyCtlID
                                                +  " in "
                                                +  reply.idString(),
                                            HL7IOException.HL7_NACK);
         logger.error("throwing ", hiEx);
         throw hiEx;
      } else if (!replyCtlID.equals(msgCtlID)) {
         HL7IOException hiEx =  new HL7IOException(traceHeader
                                             +     "Ack received for "
                                             +     msgCtlID
                                             +     "with MSA.2: "
                                             +     replyCtlID
                                             +     " in "
                                             +     reply.idString(),
                                          HL7IOException.WRONG_CTLID);
         logger.error("throwing ", hiEx);
         throw hiEx;
      } // if

      return true;
   } // write



   private boolean isWriter() {
      return this.directive == WRITER;
   } // isWriter

   private boolean isReader() {
      return this.directive == READER;
   } // isReader


   private boolean openReader() throws HL7IOException {
      try {
         this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
         this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      } catch (IOException ioEx) {
         throw new HL7IOException(streamID() + "IOException", ioEx);
      } // try - catch

      this.statusValue = OPEN;
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
         throw new HL7IOException(streamID() + "UnknownHost.",  HL7IOException.UNKNOWN_HOST);
      } catch (SocketException sEx) {
         throw new HL7IOException(streamID() + "SocketException", sEx);
      } catch (IOException ioEx) {
         throw new HL7IOException(streamID() + "IOException", ioEx);
      } // try - catch

      socket = sock;
      statusValue = OPEN;
      return true;
   } // openWriter

   private String streamID() {
      return new StringBuilder("Stream(")
                  .append(host)
                  .append(Integer.toString(port))
                  .append("):")
                  .toString();
   } // streamID



} // HL7MLLPStream

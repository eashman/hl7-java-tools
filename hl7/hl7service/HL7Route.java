/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Route.java : A container class providing delivery i/o information,
 *  as well as translation/transformation specifications for handling of
 *  HL7 transaction messages.
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


package us.conxio.hl7.hl7service;

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */


import java.io.InputStream;
import java.util.ArrayList;
import java.net.URI;

import org.w3c.dom.Node;

import org.apache.log4j.Logger;

import us.conxio.hl7.hl7message.HL7Message;
import us.conxio.hl7.hl7stream.HL7IOException;
import us.conxio.hl7.hl7stream.HL7Stream;
import us.conxio.hl7.hl7stream.HL7StreamURI;


/**
 * An object encapsulating the inbound source, outbound destination, qualification
 * and exclusion filtering, and transformations for a given HL7 transaction vector.
 * @author scott herman
 */
public class HL7Route extends HL7ServiceElement {
   private URI                      hl7SourceURI;
   private ArrayList<URI>           hl7DeliveryURIs;
   private ArrayList<HL7Transform>  transforms;
   private HL7Stream                hl7StreamIn;
   private ArrayList<HL7Stream>     hl7StreamsOut;
   private static Logger            logger = HL7ServiceElement.getLogger();


    public HL7Route(String xmlStr) throws Exception {
      this.initialize("HL7Route", xmlStr);
      initializeHL7Route(this.root);
   } // HL7Route (Constructor)


   public HL7Route(InputStream inStream) throws Exception {
      this.initialize("HL7Route", inStream);
      initializeHL7Route(this.root);
   } // HL7Route (Constructor)


   /**
    * Creates a HL7Route object from the XML content of the object referenced by the argument URI.
    * @param uri a URI which contains an XML string containing the HL7Route specification
    * as a series of XML HL7Transform, and i/o specifications. 
    * Note that if the URI contains more that one HL7Route specification, only the first is returned.
    */
   public HL7Route(URI uri) throws Exception {
      this.initialize("HL7Route", uri);
      initializeHL7Route(this.root);
   } // HL7Route (Constructor)

   /**
    * Creates a HL7Route object from the XML content of the argument document node.
    * @param node A HL7Route node of a DOM document.
    */
   public HL7Route(Node node) throws Exception {
      this.initialize("HL7Route", node);
      initializeHL7Route(this.root);
   } // HL7Route
   
   
   private void initializeHL7Route(Node node) throws Exception {
      this.transforms = this.readHL7Transforms();
      this.hl7SourceURI = this.extractURI("HL7Source");
      this.hl7DeliveryURIs = this.extractURIs("HL7Delivery");
      this.verbosity = 11;
   } // initializeHL7Route
   
   
   public URI getHL7SourceURI() {
      return this.hl7SourceURI;
   } // getHL7SourceURI


   public void setHL7SourceURI(URI uri) {
      this.hl7SourceURI = uri;
   } // setHL7SourceURI


   private ArrayList<HL7Transform> readHL7Transforms() throws Exception {
      ArrayList<Node> xForms = this.getElements("HL7Transform");
      if (xForms == null) return(null);
      ArrayList<HL7Transform> retnXForms = new ArrayList<HL7Transform>();

      for (Node node : xForms) retnXForms.add(new HL7Transform(node));
      return retnXForms;            
   } // readHL7Transforms
   
   private ArrayList<URI> extractURIs(String uriTagName) throws Exception {
      ArrayList<Node> uris = this.getElements(uriTagName);

      if (uris == null || uris.size() < 1) {
         logger.debug( "extractURIs("
                      +  uriTagName
                      +  "): None found.");
         return null;
      } // if

      ArrayList<URI> uriList = new ArrayList<URI>();

      for (Node node : uris) {
         String uriStr = this.getAttribute(node, "uri");

         if (uriStr == null) {
            uriStr = this.getAttribute(node, "URI");
         } // if

         if (uriStr != null) {
            uriList.add(new URI(uriStr) );
         } // if
      } // for
      
      return uriList;
   } // extractURIs
   

   private URI extractURI(String uriTagName) throws Exception {
      Node node = this.getElement(uriTagName);
      String uriStr = this.getAttribute(node, "uri");
      if (uriStr == null) {
         uriStr = this.getAttribute(node, "URI");
      } // if

      if (uriStr == null) return null;
      return new URI(uriStr);
   } // extractURI

   
   /**
    * Determines whether or not argument HL7Message qualifies for passage according to the context HL7Specification.
    * @param msg a HL7Message which is tested against the qualifications of the context HL7Specification.
    * @return true if the argument HL7Message qualifies for passage, by any HL7Transform, or false if it does not. 
    * Note that the message will not qualify if the route contains no HL7Transforms
    */
   public boolean isQualified(HL7Message msg) {
      for (HL7Transform xForm : this.transforms) if (xForm.isQualified(msg)) return true;
      return false;
   } // isQualified
   
   
   /**
    * Modifies the argument HL7Message in accordance with the context specification.
    * @param msg the HL7Message to be transformed.
    * @return the transformed HL7Message.
    * @throws HL7IOException in the event of a HL7Message handling issue.
    */
   public HL7Message render(HL7Message msg) throws HL7IOException {
      HL7Message workMsg = new HL7Message(msg.toHL7String());

      for (HL7Transform xForm : transforms) if (xForm.isQualified(msg)) xForm.render(workMsg);

      return workMsg;
   } // render
   

   private HL7Stream openDelivery(URI uri) throws HL7IOException {
      if (uri == null) return null;

      logger.trace("constructing outbound HL7Stream("
                    +  uri.toString()
                    +  ").");
      HL7Stream stream =  new HL7StreamURI(uri).getHL7StreamWriter();
      stream.open();
      return stream;
   } // openDelivery


   /**
    * Opens the specified HL7MessageStreams of the context HL7Route.
    * @throws HL7IOException
    */ 
   public void open() throws HL7IOException {
      if (!this.hasDeliveryURIs()) {
         throw new HL7IOException(  "No outbound stream specified.",
                                    HL7IOException.NULL_STREAM);
      } // if

      if (this.hl7StreamsOut == null) this.hl7StreamsOut = new ArrayList<HL7Stream>();

      for (URI uri : this.hl7DeliveryURIs) {
         HL7Stream hl7Stream = this.openDelivery(uri);
         if (!isOpen(hl7Stream)) throw new HL7IOException(  "Cannot open Outbound HL7Stream:"
                                                         +  uri.toString()
                                                         +  ".",
                                                         HL7IOException.NULL_STREAM);
         hl7StreamsOut.add(hl7Stream);
         logger.trace("hl7Stream["
                     + Integer.toString(this.hl7StreamsOut.size() - 1)
                     + "]:"
                     + hl7Stream.description() );
      } // for

      if (hl7StreamsOut.isEmpty()) throw new HL7IOException(   "Cannot open any Outbound HL7Stream",
                                                               HL7IOException.NULL_STREAM);
   } // open
   
   
    /**
    * Closes the specified HL7MessageStreams of the context HL7Route.
    * @throws HL7IOException
    */ 
   public void close() throws HL7IOException  {
      if (!isClosedInput()) hl7StreamIn.close();
      if (!this.hasOutputStreams()) return;
      for (HL7Stream hl7Stream : hl7StreamsOut) if (isNotClosed(hl7Stream)) hl7Stream.close();
   } // close


   public boolean isOpen() {
      if (!hasOpenInputStream()) return false;
      if (!hasOutputStreams()) return false;
      for (HL7Stream hl7Stream : this.hl7StreamsOut) if (!isOpen(hl7Stream)) return false;
      // fall through
      return true;
   } // isOpen


   /**
    * Applies all of the operations specified by the context HL7Route to the argument
    * HL7Message object.
    * @param msg The argument HL7Message object representation of a parsed HL7 transaction message.
    * @return true if the message was written to output, false if not.
    */
   public boolean route(HL7Message msg) throws HL7IOException {
      if (msg == null) return false;
      boolean wrote = false;

      if (this.isQualified(msg) ) {
         HL7Message msgOut = this.render(msg);
         if (!isOpen()) open();
         if (!hasOutputStreams()) return false;


         for (HL7Stream hl7Stream : this.hl7StreamsOut) {
            if (isOpen(hl7Stream)) {
               hl7Stream.write(msgOut);
               wrote = true;
            } // if
         } // for
      } // if

      return wrote;
   } // route


   /**
    * A generic "run" method for a thread manifestation of the context HL7Route.
    */
   public void run() {
      if (this.hl7SourceURI == null) {
         logger.error("No HL7 source specified.");
         return;
      } // if

      try {
         if (!hasOpenInputStream()) open();

         HL7Message inboundMsg = null;
         while ( (inboundMsg = this.hl7StreamIn.read()) != null) route(inboundMsg);
      } catch (HL7IOException ex) {
         logger.error(null, ex);
      } // try - catch
   } // run

   
  /**
   * Creates a formatted dump of the context HL7Route.
   */
   public void dump() {
      logger.debug("HL7Route.idString:" + this.idString);
      if (this.documentURI != null) {
         logger.debug("HL7Route.documentURI:" + this.documentURI.toString());
      } // if

      if (this.hl7DeliveryURIs != null && !this.hl7DeliveryURIs.isEmpty()) {
         for (URI uri : this.hl7DeliveryURIs) {
            logger.debug("HL7Route.hl7DeliveryURI:" + uri.toString());
         } // for
      } // if

      for (HL7Transform xForm : transforms) xForm.dump();
   } // dump

   private boolean hasOpenInputStream() {
      return hl7StreamIn != null && hl7StreamIn.isOpen();
   } // hasOpenInputStream

   private boolean hasDeliveryURIs() {
      return this.hl7DeliveryURIs != null && !this.hl7DeliveryURIs.isEmpty();
   } // hasDeliveryURIs

   private boolean hasOutputStreams() {
      return this.hl7StreamsOut != null && !this.hl7StreamsOut.isEmpty();
   } // hasOutputStreams

   private boolean isOpen(HL7Stream stream) {
      return stream != null && stream.isOpen();
   } // isOpen

   private boolean isClosedInput() {
      return this.hl7StreamIn != null && this.hl7StreamIn.isClosed();
   } // isClosedInput

   private boolean isNotClosed(HL7Stream hl7Stream) {
      return hl7Stream != null && !hl7Stream.isClosed();
   } // isNotClosed

} // class HL7Route
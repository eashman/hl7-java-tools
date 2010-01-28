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


package us.conxio.HL7.HL7MessageService;

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */

import java.net.*;
import java.io.InputStream;
import java.util.ArrayList;
import org.w3c.dom.*;

import us.conxio.HL7.HL7Message.*;
import us.conxio.HL7.HL7Stream.*;


/**
 * An object encapsulating the inbound source, outbound destination, qualification
 * and exclusion filtering, and transformations for a given HL7 transaction vector.
 * @author scott herman
 */
public class HL7Route extends HL7SpecificationElement {
   private URI                   hl7SourceURI;
   private ArrayList<URI>        hl7DeliveryURIs;
   HL7Transform[]                transforms;
   private HL7Stream             hl7StreamIn;
   private ArrayList<HL7Stream>  hl7StreamsOut;


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
    * Note that if the URI contains more that one HL7Route specificaton, only the first is returned.
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


   private HL7Transform[] readHL7Transforms() throws Exception {
      ArrayList<Node> xForms = this.getElements("HL7Transform");
      if (xForms == null) return(null);
      int xFormCount = xForms.size(); 
      if (xFormCount < 1) return(null);

      HL7Transform[] retnXForms = new HL7Transform[xFormCount];
      
      for (int index = 0; index < xFormCount; ++index) {
         retnXForms[index] = new HL7Transform(xForms.get(index) );
      } // for   
      
      return retnXForms;            
   } // readHL7Transforms
   
   private ArrayList<URI> extractURIs(String uriTagName) throws Exception {
      ArrayList<Node> uris = this.getElements(uriTagName);

      if (uris == null || uris.size() < 1) {
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
   } // extractURI
   

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
   public boolean IsQualified(HL7Message msg) {
      int xFormCount = 0;
      if (this.transforms != null) {
         xFormCount = this.transforms.length;
      } // if
      
      for (int index = 0; index < xFormCount; ++index) {
         if (this.transforms[index].IsQualified(msg) != false) {
            if (this.verbosity > 10) {
               this.logDebug( "Message "
                           +  msg.IDString()
                           +  " qualified for "
                           +  this.transforms[index].idStr
                           +  ".");
            } // if
            return true;
         } else if (this.verbosity > 0) {
            this.logDebug( "Message "
                        +  msg.IDString()
                        +  " not qualified for "
                        +  this.transforms[index].idStr
                        +  ".");
         } // if - else
      } // for
      
      return false;
   } // IsQualified
   
   
   /**
    * Modifies the argument HL7Message in accordance with the context specification.
    * @param msg the HL7Message to be transformed.
    * @return the transformed HL7Message.
    * @throws java.lang.Exception in the event of a ML7Message handling issue.
    */
   public HL7Message Render(HL7Message msg) throws HL7IOException {
      HL7Message workMsg = null;
     
      int xFormCount = 0;
      if (this.transforms != null) {
         xFormCount = this.transforms.length;
      } // if
      
      for (int index = 0; index < xFormCount; ++index) {
         if (this.transforms[index].IsQualified(msg) != false) {         
            workMsg = this.transforms[index].Render(msg);
            if (workMsg != null) {
               msg = workMsg;
               this.logDebug( "Message "
                           +  msg.IDString()
                           +  " rendered in "
                           +  this.transforms[index].idStr
                           +  ".");
            } else {
               this.logDebug( "Message "
                           +  msg.IDString()
                           +  " not rendered in "
                           +  this.transforms[index].idStr
                           + ".");
            } // if - else if
         } // if
      } // for
            
      return msg;
   } // Render   
   

   private HL7Stream openDelivery(URI uri) throws HL7IOException {
      if (uri == null) {
         return null;
      } // if

      this.logTrace("HL7Route.openDelivery():constructing outbound HL7Stream("
                    +  uri.toString()
                    +  ").");
      return new HL7StreamURI(uri).getHL7StreamWriter();
   } // openDelivery


   /**
    * Opens the specified HL7MessageStreams of the context HL7Route.
    * @throws java.lang.Exception
    */ 
   public void open() throws HL7IOException {
      if (this.hl7DeliveryURIs == null || this.hl7DeliveryURIs.isEmpty()) {
         throw new HL7IOException(  "HL7Route.open():No outbound stream specified.",
                                    HL7IOException.NULL_STREAM);
      } // if

      if (this.hl7StreamsOut == null) {
         this.hl7StreamsOut = new ArrayList<HL7Stream>();

         for (URI uri : this.hl7DeliveryURIs) {
            HL7Stream hl7Stream = this.openDelivery(uri);
            if (hl7Stream != null) {
               this.hl7StreamsOut.add(hl7Stream);
            } // if

            if (!hl7Stream.isOpen()) {
               throw new HL7IOException(  "HL7Route.open():Cannot open Outbound HL7Stream",
                                    HL7IOException.NULL_STREAM);
            } // if
            this.logTrace("HL7Route.open():hl7Stream["
                        + Integer.toString(this.hl7StreamsOut.size() - 1)
                        + "]:"
                        + hl7Stream.description() );
         } // for

         if (this.hl7StreamsOut.isEmpty()) {
            throw new HL7IOException(  "HL7Route.open():Cannot open Outbound HL7Stream",
                                    HL7IOException.NULL_STREAM);
         } // if 
      } // if
   } // open
   
   
    /**
    * Closes the specified HL7MessageStreams of the context HL7Route.
    * @throws java.lang.Exception
    */ 
   public void close() throws Exception {
      if (this.hl7StreamIn != null && !this.hl7StreamIn.isClosed()) {
         this.hl7StreamIn.close();
      } // if

      if (this.hl7StreamsOut != null && !this.hl7StreamsOut.isEmpty()) {
         for (HL7Stream hl7Stream : this.hl7StreamsOut) {
            if (hl7Stream != null && !hl7Stream.isClosed()) {
               hl7Stream.close();
            } // if
         } // for
      } // if
   } // close


   public boolean isOpen() {
      if (this.hl7StreamIn.isOpen()) {
         return true;
      } // if

      if (this.hl7StreamsOut != null && !this.hl7StreamsOut.isEmpty()) {
         for (HL7Stream hl7Stream : this.hl7StreamsOut) {
            if (hl7Stream != null && hl7Stream.isOpen()) {
               return true;
            } // if
         } // for
      } // if

      return false;
   } // isOpen


   /**
    * Applies all of the operations specified by the context HL7Route to the argument
    * HL7Message object.
    * @param msg The argument HL7Message object representation of a parsed HL7 transaction message.
    * @return true if the messae was written to output, false if not.
    */
   public boolean route(HL7Message msg) {
      if (msg == null) return false;

      try {
         if (this.IsQualified(msg) ) {
            HL7Message msgOut = this.Render(msg);
            if (this.hl7StreamsOut == null || this.hl7StreamsOut.isEmpty()) {
               this.open();
            } // if

            if (this.hl7StreamsOut != null && !this.hl7StreamsOut.isEmpty()) {
               for (HL7Stream hl7Stream : this.hl7StreamsOut) {
                  if (hl7Stream != null && !hl7Stream.isClosed()) {
                     hl7Stream.write(msgOut);
                  } // if
               } // for
            } else {
               this.logError("HL7MessageRoute: No output stream.");
            } // if
         } // if
      } catch (HL7IOException ex) {
         this.logError("HL7Route.route(" + msg.IDString() + ") caught HL7IOException:", ex);
      }  // try - catch

      return false;
   } // route


   /**
    * A generic "run" methjod for a thread manifestation of the context HL7Route.
    */
   public void run() {
      if (this.hl7SourceURI == null) {
         this.logError("HL7Route.run():No HL7 source specified.");
         return;
      } // if

      try {
         if (this.hl7StreamIn == null || !(this.hl7StreamIn.status() == HL7Stream.OPEN)) {
            this.open();
         } // if

         HL7Message inboundMsg = null;
         while ( (inboundMsg = this.hl7StreamIn.read()) != null) {
           this.route(inboundMsg);
         } // while
      } catch (HL7IOException ex) {
         this.logError("HL7Route.run() caught ", ex);
      } // try - catch
   } // run

   
  /**
   * Creates a formatted dump of the context HL7Route.
   */
   public void dump() {
      this.logDebug("HL7Route.idString:" + this.idString);
      this.logDebug("HL7Route.documentURI:" + this.documentURI.toString());

      if (this.hl7DeliveryURIs != null && !this.hl7DeliveryURIs.isEmpty()) {
         for (URI uri : this.hl7DeliveryURIs) {
            this.logDebug("HL7Route.hl7DeliveryURI:" + uri.toString());
         } // for
      } // if

      int xFormCount = 0;
      if (this.transforms != null) {
         xFormCount = this.transforms.length;
      } // if
      
      for (int index = 0; index < xFormCount; ++index) {
         this.transforms[index].dump();
      } // for
   } // dump
} // class HL7Route

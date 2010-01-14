/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7MessageService.java : A superordinate container class for
 *  configuration/specification information, i/o structures, filtering,
 *  translation/transformation, routing, and delivery of HL7 transaction messages.
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

import java.io.*;
import java.util.ArrayList;
import java.net.URI;

import org.w3c.dom.*;

import us.conxio.HL7.HL7Message.*;
import us.conxio.HL7.HL7Stream.*;


/**
 * A superordinate container class for configuration/specification information,
 * i/o, filtering, translation/transformation, routing, and delivery of HL7
 * transaction messages. Specifications are read from a XML DOM document, using
 * the following <ul>high level tags:
 * <li>HL7Source
 * <li>HL7Route
 * </ul>
 * 
 * On a more abstract level the message service offers specified subscriptions by multiple routes to 
 * the HL7Source specified by a URI.
 * The subscribing route may (and should) contain transforms which specify qualification parameters.
 * <br>
 * An simple XML example appears below:<br>
 <code><pre>
 &lt;HL7MessageService id="HL7LoggingService"&gt;
   &lt;HL7Source uri="hl7-server://localhost:3334?pool=25" /&gt;
   &lt;HL7Route id="HL7.ADT.LogRoute"&gt;
      &lt;HL7Transform  ID="HL7.ADT.LogRoute.Transform"&gt;
         &lt;qualify designator="MSH.9.1"&gt;ADT&lt;/qualify&gt;
      &lt;/HL7Transform&gt;
      &lt;HL7Delivery uri="file://HL7.ADT.log" /&gt;
   &lt;/HL7Route&gt;
   &lt;HL7Route id="HL7.ORM.LogRoute"&gt;
      &lt;HL7Transform  ID="HL7.ORM.LogRoute.Transform"&gt;
         &lt;qualify designator="MSH.9.1"&gt;ORM&lt;/qualify&gt;
      &lt;/HL7Transform&gt;
      &lt;HL7Delivery uri="file://HL7.ORM.log" /&gt;
   &lt;/HL7Route&gt;
   &lt;LogConfig file="/etc/HL7MessageService/HL7LoggingService/LogConfig.xml" /&gt;
 &lt;/HL7MessageService&gt;
 </pre></code>
 * 
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7MessageService extends HL7SpecificationElement implements HL7MessageHandler {
   /**
    * A URI referencing/specifying the source of inbound HL7 transaction messages for this service.
    */
   URI                  hl7SourceURI = null;
   /**
    * A reference to an open instance of the open thread pooled server identified by the 
    * source URI above, when the source URI specifies a thread pooled server.
    */
   HL7Server            pooledServer = null;
   /**
    * A HL7Stream which is associated to an open instance of the resource identified by the 
    * source URI above, when the source URI does not specify a thread pooled server.
    */
   HL7Stream            inputStream = null;
   /**
    * A list of route specification objects, used to route the incoming HL7 transaction messages.
    */
   ArrayList<HL7Route>  routes = new ArrayList<HL7Route>();
   
   
   /**
    * Creates an instance of the HL7MessageService class, which reads, qualifies, transforms and routes
    * HL7 transction messages in accordance with the argument DOM object or subordinate objects.  
    * @param uri An identifier which references a XML document which specifies the message service
    * @throws java.lang.Exception URI and DOM parsing exceptions, IOExceptions, Instantiation exceptions...
    */
   public HL7MessageService(URI uri) throws Exception {
      this.initialize("HL7MessageService", uri);
      initializeHL7MessageService();
   } // HL7MessageService
   

   /**
     * Creates an instance of the HL7MessageService class, which reads, qualifies, transforms and routes
    * HL7 transction messages in accordance with the argument DOM object or subordinate objects.  
    * @param svcNode A DOM Node for a HL7MessageService element.
    * @throws java.lang.Exception URI and DOM parsing exceptions, IOExceptions, Instantiation exceptions...
    */
   public HL7MessageService(Node svcNode) throws Exception {
      this.initialize("HL7MessageService", svcNode);
      initializeHL7MessageService();
   }  // HL7MessageService
   
   
   private void initializeHL7MessageService() throws Exception {
      this.hl7SourceURI = this.extractURI("HL7Source");

      ArrayList<Node> routeNodes = this.getElements("HL7Route");
      if (routeNodes == null) {
         throw new InstantiationException("HL7MessageService: No HL7Route found.");
      } // if

      int routeCount = routeNodes.size();
      for (int index = 0; index < routeCount; ++index) {
         this.subscribe(new HL7Route(routeNodes.get(index) ) );
      } // for
   } // initializeHL7MessageService


   private URI extractURI(String uriTagName) throws Exception {
      Node node = this.getElement(uriTagName);
      if (node == null) {
         throw new Exception("HL7MessageService.extractURI(" + uriTagName + "):Tag not found.");
      } // if

      String uriStr = this.getAttribute(node, "uri");
      if (uriStr == null) {
         uriStr = this.getAttribute(node, "URI");
      } // if
      
      if (uriStr == null) {
         throw new Exception("HL7MessageService.extractURI(" + uriTagName + "):URI attribute not found in \"" + node.getNodeValue() +"\".");
      } // if
      
      return new URI(uriStr);
   } // extractURI
   
   
   public String id() {
      return this.idString;
   } // id
   
   
   /**
    * Subscribes, or attaches, the argument HL7Route object to the inbound HL7MessageStream of the context 
    * HL7MessageService.
    * @param route The route to subscribe to the inbound HL7MessageStream.
    */
   public void subscribe(HL7Route route) {
      if (route == null) return;
      if (this.hl7SourceURI == null) {
         this.hl7SourceURI = route.hl7SourceURI;
      } // if
      
      route.hl7SourceURI = null;
      this.routes.add(route);
   } // subscribe


   private boolean isServerURI(URI uri) {
      String uriScheme = uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (  uriScheme.equals("tcp")
         || uriScheme.equals("tcp-server")
         || uriScheme.equals("hl7")
         || uriScheme.equals("hl7-llp")
         || uriScheme.equals("hl7-server")
         || uriScheme.equals("hl7-llp-server")) {
         return true;
      } // if

      return false;
   } // isServerURI


   private boolean isFileURI(URI uri) {
      String uriScheme = uri.getScheme();

      if (uriScheme == null) {
         return false;
      } // if

      uriScheme = uriScheme.toLowerCase();
      if (uriScheme.equals("file")
      ||  uriScheme.equals("file-reader")) {
         return true;
      } // if

      return false;
   } // isFileURI


   private int uriPoolSize(URI uri) {
      int poolSize = 0;

      String queryStr = uri.getQuery();
      if ( queryStr != null && queryStr.length() > 0
      &&   queryStr.toLowerCase().startsWith("pool")) {
         int eqIndex = queryStr.indexOf("=");
         if (eqIndex >= 0) {
            poolSize = Integer.parseInt(queryStr.substring(eqIndex + 1));
         } // if
      } // if

      return poolSize;
   } // uriPoolSize


   private int uriServerPort(URI uri) {
      int portNo = 0;
      if ( (portNo = uri.getPort()) < 0) {
         String schemeStr = this.hl7SourceURI.getScheme();
         if (schemeStr.toLowerCase().matches(".*hl7.*")) {
            portNo = 2575;
         } // if
      } // if

      return portNo;
   } // uriServerPort


   /**
    * Opens the inbound HL7MessageStream specified by the context hl7SourceURI.
    * @throws java.lang.Exception
    */
   public void open() throws Exception {
      if (this.hl7SourceURI == null) {
         throw new IOException("HL7MessageService.open(): Source URI not specified.");
      } // if

      if (this.isServerURI(this.hl7SourceURI)) {
         this.openServer();
      } else if (this.isFileURI(this.hl7SourceURI) ) {
         this.openFileReader();
      } // if - else if
   } // open


   private void openServer() throws HL7IOException {
      // * Check URI for pooled server specification.
      int poolSize = this.uriPoolSize(this.hl7SourceURI);
      if (poolSize < 2) {
         poolSize = 2;
      } // if

      // * Get the port no.
      int portNo = this.uriServerPort(this.hl7SourceURI);
      if (portNo < 0) {
         String uriStr = this.hl7SourceURI.toString();
         throw new HL7IOException("HL7MessageService.open(): Incorrect URI port specification:[" + uriStr + "].");
      } // if

      this.pooledServer = new HL7Server(portNo, poolSize, this);
      new Thread(this.pooledServer).start();
      while (true) {
         try {
            Thread.sleep(60 * 1000);
            // this.logTrace("awakened.");
         } catch (InterruptedException ex) {
            this.logDebug("InterruptedException caught.", ex);
         } // try - catch
      } // while
   } // openServer


   @SuppressWarnings("empty-statement")
   private void openFileReader() throws HL7IOException {
      HL7FileReader reader = new HL7FileReader(this.hl7SourceURI);
      HL7Message inboundMsg = null;
      while (true) {
         if ( (inboundMsg = reader.read()) != null) {
            this.dispatch(inboundMsg);
         } // if
      } // while
   } // openFileReader


   /**
    * Closes the service and all subordinate streams, threads, etc.
    */
   public void close() {
      try {
         if (this.inputStream != null) {
               this.inputStream.close();
         } // if
         
         if (this.pooledServer != null) {
            this.pooledServer.stop();
         } // if
      } catch (Exception ex) {
         this.logError("Exception on close()", ex);
      } // try - catch
   } // close
   
   
   /**
    * Routes HL7 transaction messages arriving at the inbound HL7MessageStream via the context HL7Route objects.
    * @param msg The inbound message.
    * @return The number of HL7Route object through which the argument HL7Message object was routed. 
    * @throws java.io.IOException
    */
   public int route(HL7Message msg) throws IOException {
      int routeCount = this.routes.size();
      int passCount = 0;
      for (int index = 0; index < routeCount; ++index) {
         if (this.routes.get(index).route(msg) ) {
            ++passCount;
         } // if
      } // for
      
      return passCount;
   } // route

   public int dispatch(HL7Message msg) throws HL7IOException {
      if (msg == null) {
         throw new HL7IOException(  "HL7MessageService.expedite():null messgae argument.",
                                    HL7IOException.NULL_MSG);
      } // if

      try {
         return this.route(msg);
      } catch (IOException ioEx) {
         throw new HL7IOException(  "HL7MessageService.expedite("
                                 +  msg.get("MSH.10")[0]
                                 +  "):IOException:", ioEx);
      } // try - catch
   } // expedite
   
   /**
    * A simple configuration/specification dump to aid in test and debugging. 
    */
   public void dump() {
      this.logDebug("HL7MessageService:" + this.idString);
      this.logDebug("HL7MessageService.hl7SourceURI:" + this.hl7SourceURI);
      this.logDebug("HL7MessageService.documentURI:" + this.documentURI);
      int routeCount = this.routes.size();
      for (int index = 0; index < routeCount; ++index) {
         this.routes.get(index).dump();
      } // for
   } // dump

   // * TBD: 
   // *  - Monitoring/Status reporting 
} // Hl7MessageService

/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7MessageService.java : A HL7 message service specification class.
 *
 *  Copyright (c) 2009, 2010  Scott Herman
 *
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package us.conxio.HL7MessageServiceRunner;

import java.net.URI;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.w3c.dom.Node;
import us.conxio.hl7.hl7message.HL7Message;

import us.conxio.hl7.hl7service.HL7Route;
import us.conxio.hl7.hl7service.HL7ServiceElement;
import us.conxio.hl7.hl7stream.HL7FileReader;
import us.conxio.hl7.hl7stream.HL7IOException;
import us.conxio.hl7.hl7stream.HL7MessageHandler;
import us.conxio.hl7.hl7stream.HL7Server;
import us.conxio.hl7.hl7stream.HL7Stream;
import us.conxio.hl7.hl7stream.HL7StreamURI;


/**
 *
 * @author scott
 */
public class HL7MessageService extends HL7ServiceElement implements HL7MessageHandler {
   /**
    * A URI referencing/specifying the source of inbound HL7 transaction messages for this service.
    */
   HL7StreamURI                  hl7SourceURI = null;
   /**
    * A reference to an runService instance of the runService thread pooled server identified by the
    * source URI above, when the source URI specifies a thread pooled server.
    */
   HL7Server            pooledServer = null;
   /**
    * A HL7Stream which is associated to an runService instance of the resource identified by the
    * source URI above, when the source URI does not specify a thread pooled server.
    */
   HL7Stream            inputStream = null;
   /**
    * A list of route specification objects, used to route the incoming HL7 transaction messages.
    */
   ArrayList<HL7Route>  routes = new ArrayList<HL7Route>();

   private static Logger logger = Logger.getLogger("us.conxio.hl7");


   /**
    * Creates an instance of the HL7MessageService class, which reads, qualifies, transforms and routes
    * HL7 transaction messages in accordance with the argument DOM object or subordinate objects.
    * @param uri An identifier which references a XML document which specifies the message service
    * @throws java.lang.Exception URI and DOM parsing exceptions, IOExceptions, Instantiation exceptions...
    */
   public HL7MessageService(URI uri) throws HL7IOException  {
      initialize("HL7MessageService", uri);
      initializeHL7MessageService();
   } // HL7MessageService


   /**
     * Creates an instance of the HL7MessageService class, which reads, qualifies, transforms and routes
    * HL7 transaction messages in accordance with the argument DOM object or subordinate objects.
    * @param svcNode A DOM Node for a HL7MessageService element.
    * @throws java.lang.Exception URI and DOM parsing exceptions, IOExceptions, Instantiation exceptions...
    */
   public HL7MessageService(Node svcNode) throws HL7IOException  {
      initialize("HL7MessageService", svcNode);
      initializeHL7MessageService();
   }  // HL7MessageService


   private void initializeHL7MessageService() throws HL7IOException {
      hl7SourceURI = extractURI("HL7Source");

      ArrayList<Node> routeNodes = getElements("HL7Route");
      if (routeNodes == null || routeNodes.isEmpty()) {
         throw new HL7IOException("No HL7Route found.");
      } // if

      int routeCount = routeNodes.size();
      for (int index = 0; index < routeCount; ++index) subscribe(new HL7Route(routeNodes.get(index) ) );
   } // initializeHL7MessageService


   private HL7StreamURI extractURI(String uriTagName) throws HL7IOException {
      if (StringUtils.isEmpty(uriTagName)) throw new HL7IOException("Empty Tag name.");

      Node node = getElement(uriTagName);
      if (node == null) throw new HL7IOException("Tag not found:" +  uriTagName);

      String uriStr = getAttribute(node, "uri");
      if (uriStr == null) uriStr = getAttribute(node, "URI");
      if (StringUtils.isEmpty(uriStr)) throw new HL7IOException(  "URI attribute not found in:"
                                                               +  uriTagName);
      return new HL7StreamURI(uriStr);
   } // extractURI


   public String id() {
      return getID();
   } // id


   /**
    * Subscribes, or attaches, the argument HL7Route object to the inbound HL7MessageStream of the context
    * HL7MessageService.
    * @param route The route to subscribe to the inbound HL7MessageStream.
    */
   public void subscribe(HL7Route route) {
      if (route == null || route.isOpen())  return;
      if (hl7SourceURI == null) hl7SourceURI = route.getHL7SourceURI();

      route.setHL7SourceURI(null);
      routes.add(route);
   } // subscribe


   private boolean isServerURI(HL7StreamURI uri) {
      return uri.isServerURI();
   } // isServerURI


   private boolean isFileURI(HL7StreamURI uri) {
      return uri.isFileURI();
   } // isFileURI


   /**
    * Opens the inbound HL7MessageStream specified by the context hl7SourceURI.
    * @throws java.lang.Exception
    */
   public void runService() throws Exception {
      if (hl7SourceURI == null) {
         throw new HL7IOException("HL7MessageService.open(): Source URI not specified.");
      } // if

      if (isServerURI(hl7SourceURI)) {
         runServer();
      } else if (isFileURI(hl7SourceURI) ) {
         runFileReader();
      } // if - else if
   } // runService


   private void runServer() throws HL7IOException {
      // * Check URI for pooled server specification.
      int poolSize = hl7SourceURI.uriServerPoolSize();
      if (poolSize < 2) poolSize = 2;

      // * Get the port no.
      int portNo = hl7SourceURI.getPortNo();
      if (portNo <= 0) {
         String uriStr = hl7SourceURI.toString();
         throw new HL7IOException(  "Incorrect URI port specification:[" + uriStr + "].",
                                    HL7IOException.UNINTERPERABLE_URI);
      } // if

      pooledServer = new HL7Server(portNo, poolSize, this);
      new Thread(pooledServer).start();
      while (true) {
         try {
            Thread.sleep(60 * 1000);
            // this.logTrace("awakened.");
         } catch (InterruptedException ex) {
            logger.debug("InterruptedException caught.", ex);
         } // try - catch
      } // while
   } // runServer


   @SuppressWarnings("empty-statement")
   private void runFileReader() throws HL7IOException {
      HL7FileReader reader = new HL7FileReader(hl7SourceURI);
      HL7Message inboundMsg = null;
      while (true) {
         if ( (inboundMsg = reader.read()) != null) dispatch(inboundMsg);
      } // while
   } // runFileReader


   /**
    * Closes the service and all subordinate streams, threads, etc.
    */
   public void close() {
      try {
         if (inputStream != null) inputStream.close();
         if (pooledServer != null) pooledServer.stop();
      } catch (HL7IOException hl7Ex) {
         logger.error(this, hl7Ex);
      } // try - catch
   } // close


   /**
    * Routes HL7 transaction messages arriving at the inbound HL7MessageStream via the context HL7Route objects.
    * @param msg The inbound message.
    * @return The number of HL7Route object through which the argument HL7Message object was routed.
    * @throws java.io.IOException
    */
   public int route(HL7Message msg) throws HL7IOException {
      int passCount = 0;
      for (HL7Route route : routes) if (route.route(msg) ) ++passCount;
      return passCount;
   } // route

   public int dispatch(HL7Message msg) throws HL7IOException {
      if (msg == null) throw new HL7IOException(  "null message argument.",
                                    HL7IOException.NULL_MSG);

      return route(msg);
   } // dispatch

   /**
    * A simple configuration/specification dump to aid in test and debugging.
    */
   public void dump() {
      logger.debug("HL7MessageService:" + id());
      logger.debug("HL7MessageService.hl7SourceURI:" + hl7SourceURI);
      logger.debug("HL7MessageService.documentURI:" + getDocumentURI());
      for (HL7Route route : routes) route.dump();
   } // dump

} // Hl7MessageService

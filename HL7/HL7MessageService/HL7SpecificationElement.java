/*
 *  $Id: HL7SpecificationElement.java 69 2010-01-06 17:09:51Z scott $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7SpecificationElement.java : An extensible generic base class for HL7
 *                                 specification XML items.
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Level;

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */

/**
 * An extensible generic base class for HL7 specification XML items.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7SpecificationElement {
   private static Logger   logger = null;
   String                  elementName = null,
                           idString = null;
   Node                    root = null;
   Document                document = null;
   URI                     documentURI = null;
   int                     verbosity = 0;
   
   
   
   /**
    * Generic initialization for a document level HL7 specification XML item.
    * @param name The name of the document level HL7 specification XML item.
    * @param uri The URI of the source document.
    * @throws java.lang.Exception
    */
   void initialize(String name, URI uri) throws IllegalArgumentException {
      this.documentURI = uri;
      try {
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = builderFactory.newDocumentBuilder();

         Document doc = builder.parse(uri.toString());
         NodeList nodes = doc.getElementsByTagName(name);
         if (nodes.getLength() > 0) {
            this.initialize(name, nodes.item(0));
         } // if

      } catch (SAXException saxEx) {
         throw new IllegalArgumentException("HL7SpecificationElement: Caught SAXException: ",  saxEx);
      } catch (IOException ioEx) {
         throw new IllegalArgumentException("HL7SpecificationElement: Caught IOException: "
                                          + ioEx.getMessage(),  ioEx);
      } catch (ParserConfigurationException parsEx) {
         throw new IllegalArgumentException("HL7SpecificationElement: Caught ParserConfigurationException: ",  parsEx);
      } // try - catch        
   } // initialize

  
   /**
    * Generic initialization for a Node level HL7 specification XML item.
    * @param name The name of the Node level HL7 specification XML item.
    * @param node The root element node of the Node level HL7 specification XML item.
    * @throws java.lang.Exception
    */
   void initialize(String name, Node node) throws IllegalArgumentException  {
      String nodeNameStr = node.getNodeName();
      if (nodeNameStr.equals(name)) {
         this.root = node;
         this.elementName = name;
         this.getID();        
      } else {
         throw new IllegalArgumentException("HL7MessageService: Incorrect node [" + nodeNameStr + "].");
      } // if - else

      if (HL7SpecificationElement.logger == null) {
         HL7SpecificationElement.logger = Logger.getLogger(this.loggerName(name));
         this.configureLogger();
         HL7SpecificationElement.logger.setLevel(Level.TRACE);
         this.logInfo("loggerName:"  + HL7SpecificationElement.logger.getName());
      } // if
   } // initialize


   public String loggerName(String name) {
      String nameStr = this.getClass().getPackage().toString();
      if (nameStr != null && nameStr.startsWith("package ")) {
         nameStr = nameStr.substring(8);
      } // if

      return new StringBuffer(nameStr).append(".").append(name).toString();
   } // loggerName


   /**
    * Acquires the context HL7 specification XML item's specified unique identification attribute, or generates
    * unique ID in the case where no unique identification attribute is specified for the context HL7 specification
    * XML item.
    * @return The previously set or newly assigned identification String.
    */
   public String getID() {
      if (this.idString == null) {
         // * Retrieve the ID
         NamedNodeMap attributes = this.root.getAttributes();
         Node idNode = attributes.getNamedItem("ID");
         if (idNode == null) {
            idNode = attributes.getNamedItem("id");
         } // if
         if (idNode != null) {
            this.idString = idNode.getNodeValue();
         } else {
            // * Generate a unique ID string.
            UUID uuid = UUID.randomUUID();
            this.idString = uuid.toString();
         } // if         
      } // if
      
      return(this.idString);
   } // getID
   

   /**
    * Finds the first subordinate element of the argument element type name.
    * @param elementName The element type name of the element to retrieve.
    * @return The first subordinate element of the argument element type name, that was found. 
    */
   public Node getElement(String elementName) {
      if (this.root.hasChildNodes()) {
         NodeList kids = this.root.getChildNodes();
         int numKids = kids.getLength();
         for (int index = 0; index < numKids; ++index) {
            Node kid = kids.item(index);   
            if (kid.getNodeType() == Node.TEXT_NODE) continue;
            String kidName = kid.getNodeName();
            if (kidName.startsWith("#") ) continue;
            if (kidName.toLowerCase().equals(elementName.toLowerCase())) return kid;
         } // for
      } // if
     
      return null;
   } // getElement


   /**
    * Finds and returns the attribute value corresponding to the argument attribute name, for the argument
    * element node.
    * @param elementNode The element node in which to seek the argument attribute.
    * @param attributeName The name of the attribute sought.
    * @return The sought attribute value, if found.
    */
   public String getAttribute(Node elementNode, String attributeName) {
      if (elementNode == null) return null;
      if (!elementNode.hasAttributes() ) return null;
      
      NamedNodeMap attrMap;
      if ( (attrMap = elementNode.getAttributes() )== null) return null;

      Node attrNode;
      if ( (attrNode = attrMap.getNamedItem(attributeName)) == null) return null;

      return attrNode.getNodeValue();
   } // getAttribute


   /**
    * Finds and returns the attribute value corresponding to the argument attribute name, for the 
    * first found subordinate element node corresponding to the argument.
    * @param elementName The name of the element node in whioch to seek the attribute.
    * @param attributeName The name of the attribute sought.
    * @return the found value of the arguemtn attribute, as a String.
    */
   public String getElementAttribute(String elementName, String attributeName) {
      Node elementNode;
      if ( (elementNode = this.getElement(elementName) ) == null) { return null; }
      return getAttribute(elementNode, attributeName);
   } // getElementAttribute

   
   /**
    * Finds and returns all of the subordinate elements of the argument element name.
    * @param elementName The name of the subordinate element to seek.
    * @return All of the subordinate element nodes of the argument element name, as an ArrayList of nodes 
    * of type Node
    * @throws java.lang.Exception
    */
   public ArrayList<Node> getElements(String elementName) throws Exception {
      ArrayList<Node> nodes = new ArrayList<Node>();
      
      if (this.root.hasChildNodes()) {
         NodeList kids = this.root.getChildNodes();
         int numKids = kids.getLength();
         for (int index = 0; index < numKids; ++index) {
            Node kid = kids.item(index);   
            if (kid.getNodeType() == Node.TEXT_NODE) continue;
            String kidName = kid.getNodeName();
            if (kidName.startsWith("#") ) continue;
            if (kidName.toLowerCase().equals(elementName.toLowerCase())) nodes.add(kid);
         } // for
      } // if
     
      if (nodes.isEmpty()) return null;
      return nodes;
   } // getElements


   // * Logging
   private void configureLogger() {
      String successStr = "*** Logging initialized for " + this.elementName + ":" + this.idString  + " ***";
      String nodeErrorStr = null;

      BasicConfigurator.configure();

      Node node = this.getElement("LogConfig");
      if (node != null) {
         String fileName = this.getAttribute(node, "file");
         if (fileName != null) {
            DOMConfigurator.configure(fileName);
            HL7SpecificationElement.logger.info(successStr);
            return;
         } // if

         String urlStr = this.getAttribute(node, "url");
         if (urlStr != null) {
            DOMConfigurator.configure(urlStr);
            HL7SpecificationElement.logger.info(successStr);
            return;
         } // if

         nodeErrorStr = "Log Configuration Error: file or url attribute not found in[" + node.getTextContent() + "].";
      } // if

      String fileName = "/etc/" + this.elementName + "/logger.xml";
      File file = new File(fileName);
      if (file.exists()) {
         DOMConfigurator.configure(fileName);
         HL7SpecificationElement.logger.info(successStr);
         if (nodeErrorStr != null) {
            HL7SpecificationElement.logger.info(nodeErrorStr);
         } // if
         return;
      }

      HL7SpecificationElement.logger.info( "No logger configuration found. "
                                         + "Using basic configuration for "
                                         + this.elementName
                                         + ":"
                                         + this.idString);
   } // configureLogger


   public void logDebug(String msg) {
      if (this.logger != null) {
         this.logger.debug(msg);
      } else {
         System.out.println(msg);
      } // if - else
   } // logDebug


   public void logDebug(String msg, Throwable thrown) {
      if (this.logger != null) {
         this.logger.debug(msg, thrown);
      } else {
         System.out.println(msg + ":" + thrown.toString() + ":" + thrown.getMessage());
      } // if - else
   } // logDebug



   public void logTrace(String msg) {
      if (this.logger != null) {
         this.logger.trace(msg);
      } else {
         System.out.println(msg);
      } // if - else
   } // logTrace


   public void logTrace(String msg, Throwable thrown) {
      if (this.logger != null) {
         this.logger.trace(msg, thrown);
      } else {
         System.out.println(msg + ":" + thrown.toString() + ":" + thrown.getMessage());
      } // if - else
   } // logTrace


   public void logInfo(String msg) {
      if (this.logger != null) {
         this.logger.info(msg);
      } else {
         System.out.println(msg);
      } // if - else
   } // logInfo


   public void logInfo(String msg, Throwable thrown) {
      if (this.logger != null) {
         this.logger.info(msg, thrown);
      } else {
         System.out.println(msg + ":" + thrown.toString() + ":" + thrown.getMessage());
      } // if - else
   } // logInfo


   public void logError(String msg) {
      if (this.logger != null) {
         this.logger.error(msg);
      } else {
         System.out.println(msg);
      } // if - else
   } // logError

   public void logError(String msg, Throwable thrown) {
      if (this.logger != null) {
         this.logger.error(msg, thrown);
      } else {
         System.out.println(msg + ":" + thrown.toString() + ":" + thrown.getMessage());
      } // if - else
   } // logError

} // HL7SpecificationElement

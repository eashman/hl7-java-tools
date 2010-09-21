/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7ServiceElement.java : An extensible generic base class for HL7 service items
 *                           specified as XML.
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

import java.io.*;

import java.util.ArrayList;
import java.util.UUID;

import java.net.URI;

import org.w3c.dom.*;



import org.apache.log4j.Logger;
import us.conxio.XMLUtilities.AttributeMap;

import us.conxio.XMLUtilities.XMLUtils;

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */

/**
 * An extensible generic base class for HL7 specification XML items.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7ServiceElement {
   private static Logger   logger = Logger.getLogger("us.conxio.hl7");
   String                  elementName = null,
                           idString = null;
   Node                    root = null;
   Document                document = null;
   URI                     documentURI = null;
   int                     verbosity = 0;


   void initialize(String name, String xmlString) {
      Node node = XMLUtils.readXML(xmlString);
      if (node.getNodeName().equalsIgnoreCase(name)) {
         this.initialize(name, node);
      } else {
         Node child = XMLUtils.findChild(name, node);
         if (child != null) this.initialize(name, child);
      } // if - else
   } // initialize


   void initialize(String name, InputStream inStream) {
      Node node = XMLUtils.readXML(inStream);
      if (node.getNodeName().equalsIgnoreCase(name)) {
         this.initialize(name, node);
      } else {
         Node child = XMLUtils.findChild(name, node);
         if (child != null) this.initialize(name, child);
      } // if - else
   } // initialize


   /**
    * Generic initialization for a document level HL7 specification XML item.
    * @param name The name of the document level HL7 specification XML item.
    * @param uri The URI of the source document.
    * @throws java.lang.Exception
    */
   void initialize(String name, URI uri) {
      this.documentURI = uri;
      Node node = XMLUtils.readXML(uri);
      if (node.getNodeName().equalsIgnoreCase(name)) {
         this.initialize(name, node);
      } else {
         Node child = XMLUtils.findChild(name, node);
         if (child != null) this.initialize(name, child);
      } // if - else
   } // initialize

  
   /**
    * Generic initialization for a Node level HL7 specification XML item.
    * @param name The name of the Node level HL7 specification XML item.
    * @param node The root element node of the Node level HL7 specification XML item.
    * @throws java.lang.Exception
    */
   void initialize(String name, Node node) throws IllegalArgumentException  {
      String nodeNameStr = node.getNodeName();
      if (nodeNameStr.equalsIgnoreCase(name)) {
         this.root = node;
         this.elementName = name;
         this.setID();
      } else {
         throw new IllegalArgumentException("Incorrect node: expected:[" + name + "] got:[" + nodeNameStr + "].");
      } // if - else
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
   public String setID() {
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
   } // setID
   

   public String getID() {
      return idString;
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

      AttributeMap map = AttributeMap.getAttributes(elementNode);
      if (map != null) return map.get(attributeName.toLowerCase());

      return null;
   } // getAttribute


   /**
    * Finds and returns the attribute value corresponding to the argument attribute name, for the 
    * first found subordinate element node corresponding to the argument.
    * @param elementName The name of the element node in which to seek the attribute.
    * @param attributeName The name of the attribute sought.
    * @return the found value of the argument attribute, as a String.
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
   public ArrayList<Node> getElements(String elementName) {
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



   /**
    * @return the logger
    */
   public static Logger getLogger() {
      return logger;
   } // getLogger

} // HL7SpecificationElement

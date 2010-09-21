/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7service;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.net.URI;

import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import us.conxio.XMLUtilities.AttributeMap;

import us.conxio.XMLUtilities.XMLUtils;

/**
 *
 * @author scott
 */
class TranslationTable {
   private  HashMap<String, String>    map = null;
   private long                        lastModified = 0;
   private URI                         uri;

   private static Logger               logger = Logger.getLogger("us.conxio.hl7");

   private static final String         NAME_KEY = "key";
   private static final String         NAME_VALUE = "value";
   private static final String         NAME_ENTRY = "entry";
   private static final String         NAME_TRANSLATION_TABLE = "translationtable";

   private TranslationTable(URI resourceURI) throws IOException {
      uri = resourceURI;
      if (!uri.getScheme().equalsIgnoreCase("file")) {
         throw new IOException(  "URI schemes other than file/// not handled. ("
                              +  uri.toString()
                              +  ")");
      } // if

      loadTranslationTable(uri);
   } // TranslationTable constructor


   private void loadTranslationTable(URI resourceURI) throws IOException {
      File tableFile = new File(resourceURI);
      if (!tableFile.exists()) {
         throw new FileNotFoundException("File not found:" + tableFile.getAbsolutePath());
      } // if

      long newModificationTime = tableFile.lastModified();
      if (newModificationTime > lastModified) {
         if (tableFile.getName().toLowerCase().endsWith("xml")) {
            readXMLMap(tableFile);
         } else {
            readTableFile(tableFile);
         } // if - else

         lastModified = newModificationTime;
      } // if
   } // loadTranslationTable


   private void readXMLMap(File tableFile) throws IOException {
      HashMap<String, String> retn = new HashMap();
      Node node = XMLUtils.readXML(new FileInputStream(tableFile));
      String nodeName = node.getNodeName().toLowerCase();
      if (nodeName.equalsIgnoreCase(NAME_TRANSLATION_TABLE)) parseTableElement(node);
      if (node.hasChildNodes()) {
         NodeList kids = node.getChildNodes();
         for (int index = 0; index < kids.getLength(); ++index) {
            Node kid = kids.item(index);
            if (kid.getNodeType() == Node.TEXT_NODE) continue;
            
            String kidName = kid.getNodeName().toLowerCase();
            if (kidName.equalsIgnoreCase(NAME_ENTRY))  {
               parseEntryElement(kid, retn);
            } else {
               logger.debug("Unexpected inner element:" + kidName);
            } // if - else
         } // for 
      } else {
         logger.debug("Unexpected outer element:" + nodeName);
      } // if - else if - else

      if (!retn.isEmpty()) map = retn;
   } // readXMLMap


   private void parseTableElement(Node node) {
      logger.trace("moot table element:" + node.getTextContent());
   } // parseTableElement

   private void parseEntryElement(Node node, HashMap<String, String> sMap) {
      String key = null;
      String value = null;

      if (node.hasChildNodes()) {
         NodeList kids = node.getChildNodes();
         for (int index = 0; index < kids.getLength(); ++index) {
            Node kid = kids.item(index);
            if (kid.getNodeType() == Node.TEXT_NODE) continue;

            String kidName = kid.getNodeName().toLowerCase();
            if (kidName.equalsIgnoreCase(NAME_KEY)) {
               key = kid.getTextContent();
            } else if (kidName.equalsIgnoreCase(NAME_VALUE)) {
               value = kid.getTextContent();
            } else {
               logger.debug("Unexpected entry element:" + kidName);
            } // if - else if - else
         } // for
      } // if

      if (node.hasAttributes()) {
         AttributeMap attributes = new AttributeMap(node);
         if (attributes.hasKey(NAME_KEY))    key = attributes.get(NAME_KEY);
         if (attributes.hasKey(NAME_VALUE))  value = attributes.get(NAME_VALUE);
      } // if

      if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
         sMap.put(key, value);
      } // if
   } // parseEntryElement


   private void readTableFile(File tableFile) throws IOException {
      BufferedReader reader = new BufferedReader(new FileReader(tableFile));
      HashMap retn = new HashMap();
      String line = null;
      while ((line = reader.readLine()) != null) {
         String key = parseKey(line);
         String value = parseValue(line);
         retn.put(key, value);
      } // while

      reader.close();

      if (!retn.isEmpty()) map = retn;
   } // readTableFile

   private String parseKey(String line) {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   private String parseValue(String line) {
      throw new UnsupportedOperationException("Not yet implemented");
   }


   static TranslationTable make(URI resourceURI) throws IOException {
      if (TranslationTableDirectory.has(resourceURI)) return TranslationTableDirectory.get(resourceURI);
      return TranslationTableDirectory.add(new TranslationTable(resourceURI));
   } // make


   String get(String subject) {
      if (!has(subject)) return null;
      return map.get(subject);
   } // get

   private boolean has(String subject) {
      return hasMap() && map.containsKey(subject);
   } // has

   private boolean hasMap() {
      return map != null && !map.isEmpty();
   } // hasMap

   boolean isEmpty() {
      return !hasMap();
   } // isEmpty

   URI getURI() {
      return uri;
   } // getURI

} // TranslationTable

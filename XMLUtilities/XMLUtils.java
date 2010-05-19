/*
 *  $Id: XMLUtils.java 71 2010-05-05 12:36:40Z scott $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  XMLUtils.java : A class of static XML utility methods.
 *
 *  Copyright (C) 2009, 2010  Scott Herman
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

package us.conxio.XMLUtilities;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;

import org.xml.sax.SAXException;


/**
 *
 * @author scott
 */
public class XMLUtils {

   /**
    * Reads the argument XML input stream.
    * @param inStream an input stream carrying the only subject XML
    * @return the root node of the read DOM document.
    */
   public static Node readXML(InputStream inStream) {
      try {
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = builderFactory.newDocumentBuilder();

         Document doc = builder.parse(inStream);
         inStream.close();
         return doc.getDocumentElement();
      } catch (SAXException saxEx) {
         throw new IllegalArgumentException("readXML() Caught SAXException: ",  saxEx);
      } catch (IOException ioEx) {
         throw new IllegalArgumentException("readXML() Caught IOException: "
                                          + ioEx.getMessage(),  ioEx);
      } catch (ParserConfigurationException parsEx) {
         throw new IllegalArgumentException("readXML() Caught ParserConfigurationException: ",  parsEx);
      } // try - catch
   } // readXML


   public static Node readXML(String xmlString) {
      try {
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = builderFactory.newDocumentBuilder();

         Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
         return doc.getDocumentElement();
      } catch (SAXException saxEx) {
         throw new IllegalArgumentException("readXML() Caught SAXException: ",  saxEx);
      } catch (IOException ioEx) {
         throw new IllegalArgumentException("readXML() Caught IOException: "
                                          + ioEx.getMessage(),  ioEx);
      } catch (ParserConfigurationException parsEx) {
         throw new IllegalArgumentException("readXML() Caught ParserConfigurationException: ",  parsEx);
      } // try - catch
   } // readXML


    public static String toXMLString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    } // toXMLString

   public static String elementString(String tagName, String content) {
      if (tagName == null) {
         return null;
      } // if

      StringBuffer buildBuffer = new StringBuffer("<").append(tagName);
      if (content == null) {
         return buildBuffer.append(" />").toString();
      } // if

      buildBuffer.append(">").append(content);
      return buildBuffer.append("</").append(tagName).append(">").toString();
   } // elementString


   public static String elementString(String tagName, AttributeMap attributes, String content) {
      if (tagName == null) {
         return null;
      } // if

      StringBuffer buildBuffer = new StringBuffer("<").append(tagName);
      buildBuffer.append(attributes.toString());
      if (content == null) {
         return buildBuffer.append(" />").toString();
      } // if

      buildBuffer.append(">").append(content);
      return buildBuffer.append("</").append(tagName).append(">").toString();
   } // elementString
} // XMLUtils

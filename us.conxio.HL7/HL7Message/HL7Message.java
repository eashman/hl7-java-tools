/*
 *  $Id: HL7Message.java 67 2009-12-30 20:16:52Z scott $
 * 
 *  This code is derived from public domain sources. Commercial use is allowed. 
 *  However, all rights remain permanently assigned to the public domain.
 * 
 *  HL7Message.java : A structured base class for parsed HL7 Messages, providing structured access  
 *                    to message data content, and constituent items. 
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

package us.conxio.HL7.HL7Message;


/**
 *
 * $Revision: 67 $
 * $Date: 2009-12-30 15:16:52 -0500 (Wed, 30 Dec 2009) $
 * $Author: scott $
 * *
 * 
 * @author scott herman <scott.herman@unconxio.us>
 */

import java.util.ArrayList;
import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import us.conxio.HL7.HL7Stream.*;

/**
 * Generic HL7 message subordinate item.
 */
class HL7MsgItem {
   public  String                hl7Content,          // Content text
                                 hl7Separator;        // Separator between subordinate items
   public  HL7MsgItem[]          hl7Constituents;     // subordinate items
   private HL7Encoding           encodingCharacters;
   public  HL7Designator         location;
   private boolean               touched;


   // Constructors
   HL7MsgItem() {}
   
   HL7MsgItem(String argStr, String separator, HL7Encoding encodingChars) {
      this.hl7Content = argStr;
      this.hl7Separator = separator;
      this.encodingCharacters = encodingChars;
      this.parse();
   } // HL7MsgItem constructor


   HL7MsgItem(String argStr, String separator, HL7Encoding encodingChars, HL7Designator argLocation) {
      this.hl7Content = argStr;
      this.hl7Separator = separator;
      this.encodingCharacters = encodingChars;
      
      if (argLocation != null) {
         this.location = new HL7Designator(argLocation);
      } // if
      
      this.parse();
   } // HL7MsgItem constructor


   private String NextSeparator() {
      return this.encodingCharacters.NextSeparator(this.hl7Separator);
   } // NextSeparator
   
   
   private boolean IsComposite() {
      if (this.hl7Content == null || this.hl7Content.length() < 1) {   
         return false; 
      } // if
   
      String argStr = this.hl7Content;
      boolean firstTime = true;
      String separator = this.hl7Separator;
      
      
      do {
         if (separator == null) {
            return(false);
         } // if
         
         int separatorIndex = argStr.indexOf(separator);
      
         if (separatorIndex < 0) {
            separator = this.encodingCharacters.NextSeparator(separator);
            argStr = this.hl7Content;
            continue;
         } // if
      
         if (firstTime == true && separatorIndex == 0) {
            return true;
         } // if
         
         firstTime = false;
      
         String prefix = this.hl7Content.substring(separatorIndex - 1, separatorIndex);
         if (!prefix.equals(this.encodingCharacters.escapeChar) ) {
            return true;
         } // if
      
         if (++separatorIndex >= argStr.length() ) {       // check for further separators...
            separator = this.encodingCharacters.NextSeparator(separator);
            argStr = this.hl7Content;
            continue;
         } // if
         
         argStr = argStr.substring(separatorIndex);
         
      } while (true);
      
   } // IsComposite
   
   
   private String[] HL7Split() {
      if (this.hl7Separator == null) {
         String[] array = new String[1];
         array[0] = this.hl7Content;
         return array;
      } else {
         String   regExp = null;
         if (this.encodingCharacters.escapeChar != null
         &&  this.encodingCharacters.escapeChar.charAt(0) == 0x5c) {
            regExp  = "(?<!\\"                             // negative look back for the lack of
                           + this.encodingCharacters.escapeChar   // an escape character
                           + ")\\"                                // before the
                           + this.hl7Separator;                   // item separator
         } else {
             regExp  = "\\" + this.hl7Separator;                   // item separator
         } // if - else
         return(this.hl7Content.split(regExp, -2) );
      } // if
   } // HL7Split
   
   
   private void parse() {
      this.hl7Constituents = null;

      if (this.hl7Content == null || this.hl7Separator == null) {
         return;
      } // if

      if (this.IsComposite() ) {
         String[] subItemStrings = this.HL7Split();
         HL7MsgItem[] subItems = new HL7MsgItem[subItemStrings.length];
         
         for (int index = 0; index < subItemStrings.length; ++index) {
            if (this.location != null) {
               HL7Designator newLocation = new HL7Designator(this.location);
               
               subItems[index] = new HL7MsgItem(subItemStrings[index], 
                                                this.NextSeparator(), 
                                                this.encodingCharacters, 
                                                newLocation.spawn(index));
            } else {
               subItems[index] = new HL7MsgItem(subItemStrings[index], 
                                                this.NextSeparator(), 
                                                this.encodingCharacters);
            } // if - else
         } // for

         this.hl7Constituents = subItems;
      } // if
      
      this.touched = true;
   } // parse


   boolean isTouched() {
      if (this.touched == true) { return(true); } 

      if (  this.hl7Separator == null 
      ||    this.hl7Constituents == null) {
         return(false);
      } // if

      for (int index = 0; index < this.hl7Constituents.length; ++index) {
         if (this.hl7Constituents[index] != null) {
            if (this.hl7Constituents[index].isTouched() == true) {
               return(true);
            } // if
         } // if
      } // for

      return(false);

   } // isTouched


   String toHL7String() {
      if (  this.hl7Separator == null 
      ||    this.hl7Constituents == null
      ||    this.isTouched() == false) {
         return(this.hl7Content);
      } // if

      int length = this.hl7Constituents.length;
      StringBuffer hl7Str = new StringBuffer();

      for (int index = 0; index < length; ) {
         if (this.hl7Constituents[index] == null) {
            hl7Str.append("");
         } else {
            hl7Str.append(this.hl7Constituents[index].toHL7String());
         } // if - else 

         if (++index < length) {
            hl7Str.append(this.hl7Separator);
         } // if
      } // for

      this.hl7Content = hl7Str.toString();
      if (this.touched == true) {
         this.touched = false;
      } // if

      return(this.hl7Content);

   } // toHL7String


   private boolean isAtSegmentLevel() {
      if (this.hl7Separator == null)                                                return false;
      if (this.encodingCharacters == null)                                          return false;
      if (this.encodingCharacters.repetitionSeparator == null)                      return false;
      if (this.hl7Separator.equals(this.encodingCharacters.fieldSeparator) )        return true;
      return false;
   } // isAtSegmentLevel
   
   
   private boolean isAtSequenceLevel() {
      if (this.hl7Separator == null)                                                return false;
      if (this.encodingCharacters == null)                                          return false;
      if (this.encodingCharacters.repetitionSeparator == null)                      return false;
      if (this.hl7Separator.equals(this.encodingCharacters.repetitionSeparator) )   return true;
      return false;
   } // isAtSequenceLevel
   
   
   private boolean isAtRepetitionLevel() {
      if (this.hl7Separator == null)                                                return false;
      if (this.encodingCharacters == null)                                          return false;
      if (this.encodingCharacters.repetitionSeparator == null)                      return false;
      if (this.hl7Separator.equals(this.encodingCharacters.componentSeparator) )    return true;
      return false;
   } // isAtRepetitionLevel
   
   
   private boolean isAtComponentLevel() {
      if (this.hl7Separator == null)                                                return false;
      if (this.encodingCharacters == null)                                          return false;
      if (this.encodingCharacters.repetitionSeparator == null)                      return false;
      if (this.hl7Separator.equals(this.encodingCharacters.subComponentSeparator) ) return true;
      return false;
   } // isAtComponentLevel
   
   
   private boolean isAtSubComponentLevel() {
      if (this.hl7Separator == null && this.hl7Constituents == null) return true;
      return false;
   } // isAtSubComponentLevel
   
   
   String toXMLString() {
      StringBuffer contentBuffer = new StringBuffer();
      
      if (this.hl7Constituents != null) {
         for (int index = 0; index < this.hl7Constituents.length; ++index) {
            if (this.hl7Constituents[index] != null) {
               contentBuffer.append(this.hl7Constituents[index].toXMLString());
            } // if
         } // for
      } else {
         String nestedXMLStr = this.toHL7String();
         contentBuffer.append(nestedXMLStr); 
      } // if - else   

      String contentStr = contentBuffer.toString();
      if (contentStr.trim().length() < 1) {
         return("");
      } // if
      
      StringBuffer xmlStrBuffer = new StringBuffer();
      String tagString = "";
      // * Sequence (field) level content will appear as 1 or more repetitions.
      // * Therefore sequence (field) level encapsulation will be redundant, and is not desired,
      // * except where there are no constituent data below the sequence (field) level.
      if (this.location == null) {
         tagString = "null.location";
      } else if (this.isAtSegmentLevel()) {
         tagString = this.location.segID;
      } else if (this.isAtRepetitionLevel() 
             ||  this.isAtComponentLevel() 
             ||  this.isAtSubComponentLevel()
             ||  (this.isAtSequenceLevel() && this.hl7Constituents == null) ) {
         tagString = this.location.toXMLString();    
      } // if - else if,,, 
      
      if (tagString.length() > 0) {
         xmlStrBuffer.append( "<" + tagString + ">" );
         
         // Handle special characters in MSH.1 
         if (tagString.equals("MSH.1")) {
            String xmlSpecial = this.xmlClean();
            xmlStrBuffer.append(xmlSpecial);
         } else {
            xmlStrBuffer.append(contentStr);
         } // if - else 
         
         xmlStrBuffer.append( "</" + tagString + ">" );
      } else if (contentStr.trim().length() > 0) {
            xmlStrBuffer.append(contentStr);
      } // if - else
      
      return(xmlStrBuffer.toString());
      
   } // toXMLString
   
   
   private String xmlClean() {
      // Special chars : <, >, &, ", '
      //&lt; &amp; &gt; &quot; &apos;
      String argStr = this.toHL7String();
      // Do the ampersand first!
      String escAmpersand = argStr.replaceAll("&", "&amp;");
      String escLT = escAmpersand.replaceAll("<", "&lt;");
      String escGT = escLT.replaceAll(">", "&gt;");
      String escQuote = escGT.replaceAll("\"", "&quot;");
      String escApostrophe = escQuote.replaceAll("\'", "&apos;");
      return new String(escApostrophe);    
   } // xmlClean
   
   
   int[] subArray(int[] argArray) {
      if (argArray.length < 2) { return null; }

      int[] retnArray = new int[argArray.length - 1];
      for (int index = 1; index < argArray.length; ++index) { 
         retnArray[index - 1] = argArray[index]; 
      } // for
      
      return retnArray;
   } // subArray


   void expandConstituents(int toSize) {
      HL7MsgItem[]   newConstituents = new HL7MsgItem[toSize];
      int            nextIndex = 0;
      String         nextSeparator = this.encodingCharacters.NextSeparator(this.hl7Separator);

      if (this.hl7Constituents != null) {
         for (int index = 0; index < this.hl7Constituents.length; ++index) {
            newConstituents[index] = this.hl7Constituents[index];
         } // for

         nextIndex = this.hl7Constituents.length;
      } // if

      for (int index = nextIndex; index < newConstituents.length; ++index) {
         newConstituents[index] = new HL7MsgItem(  index == 0 ? this.hl7Content : "", 
                                                   nextSeparator, 
                                                   this.encodingCharacters);
         if (this.location != null) {
            newConstituents[index].location = this.location.spawn(index);
         } // if
      } // for

      this.hl7Constituents = newConstituents;
      this.touched = true;
   } // expandConstituents


   // Given a location specifier, returns the index of the constituent component
   // referenced by the argument location specifier.
   private int locate(HL7Designator location) {
      if (this.hl7Separator.equals(this.encodingCharacters.fieldSeparator)) {
         return(location.sequence);
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.repetitionSeparator)) {
         return(location.repetition);
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.componentSeparator)) {
         return(location.component);
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.subComponentSeparator)) {
         return(location.subComponent);
      } // if
      
      return(-11);
   } // locate
   
   
   private boolean isTerminal(HL7Designator location) {
      if (this.hl7Separator.equals(this.encodingCharacters.fieldSeparator)) {
         return(location.sequence < 0  ? true : false);
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.repetitionSeparator)) {
         if (location.repetition < 0 && location.component < 0) {
            return true;
         } else {
            return false;
         } // if - else
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.componentSeparator)) {
         return(location.component < 0 ? true : false);
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.subComponentSeparator)) {
         return(location.subComponent < 0 ? true : false);
      } // if
      
      return(true);
      
   } // isTerminal
   
   
   HL7MsgItem pick(HL7Designator location, boolean create) {
      if (this.hl7Separator == null) {                      // at the bottom-most (sub-component) level.
         return this;
      } // if      
      
      if ( (this.isTerminal(location) || this.hl7Constituents == null) && create == false) {   // at the bottom-most existent level
        return this;
      } // if
      
      int locator = this.locate(location);                  // index for the next item down the tree.
      
      if (locator >= 0) {
         if (this.hl7Constituents == null || locator >= this.hl7Constituents.length) {
            if (create == true) {
               this.expandConstituents(locator + 1);
            } else {
               return null; 
            } // if - else  
         } // if
         
         return this.hl7Constituents[locator].pick(location, create);
      } else if ( this.hl7Separator.equals(this.encodingCharacters.repetitionSeparator) ) {
        //* create a new constituent if needed, and authorized...   
         if (this.hl7Constituents == null && create == true) {
            this.expandConstituents(1);
         } // if
      
         if (this.hl7Constituents != null) {
            return(this.hl7Constituents[0].pick(location, create));
         } // if
      } // if - else if
      
      return this;
      
   } // pick
   
   
   // Returns the number of HL7 repetitions at the specified location, 
   // if, and only if, the current item context is at or above the repetition level.
   int numberOfRepetitionsAt(HL7Designator here) {
      if (this.hl7Constituents == null) {
         return (1);
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.repetitionSeparator)) {
         return(this.hl7Constituents.length);
      } // if
      
      if (this.hl7Separator.equals(this.encodingCharacters.fieldSeparator) 
      &&  this.hl7Constituents.length > here.sequence) {
         return(this.hl7Constituents[here.sequence].numberOfRepetitionsAt(here));
      } // if
      
      return(0);
      
   } // numberOfRepetitions
   
   
   void set(String argStr) {
      this.hl7Content = argStr;
      this.parse();
   } // set
   
} // HL7MsgItem




/** 
 * HL7SegmentHash is a keyed hash of arrays of segments as HL7MsgItem types. eg;
 *    get("PID") returns an array of PID segments
 *    put(HL7MsgItem segment) adds the segment to the appropriate mapped location.
 */
class HL7SegmentHash {
   HashMap  segmentHash;

   HL7SegmentHash(int size) {
      this.segmentHash = new HashMap(size);
   } // SegmentHash


   HL7MsgItem[] get(String argStr) {
      ArrayList<HL7MsgItem> retnList = (ArrayList<HL7MsgItem>)this.segmentHash.get(argStr);
      if (retnList == null) return null;
      HL7MsgItem[]   retnArray = new HL7MsgItem[retnList.size()];
      return(retnList.toArray(retnArray));
   } // get


   void put(HL7MsgItem segment) {
      if (segment == null) { return; }

       String segID = segment.hl7Content.substring(0, 3);
       if (this.segmentHash.containsKey(segID)) {
          ArrayList<HL7MsgItem> thisList = (ArrayList<HL7MsgItem>)this.segmentHash.get(segID);
          if (segment.location != null) {
            segment.location.segIndex = thisList.size();
          } // if
          thisList.add(segment);
       } else {
          if (segment.location != null) {
            segment.location.segIndex = 0;
          } // if
          ArrayList<HL7MsgItem> segList = new ArrayList<HL7MsgItem>();
          segList.add(segment);
          this.segmentHash.put(segID, segList);
       } // if - else
   } // put
} // SegmentHash

/**
 * Internal private class for handling time in HL7.
 */
class HL7Time {
   final String hl7DTFormatStr = "yyyyMMddkkmmssZ";
   SimpleDateFormat hl7DTFormat = new SimpleDateFormat(hl7DTFormatStr);
   
   HL7Time() { }
   
   public String get(Date dateTime) {
      return(hl7DTFormat.format(dateTime));
   } // get
   
   
   String get() {
      Date dateTime = new Date();
      return(hl7DTFormat.format(dateTime));
   } // get
   
} // HL7Time


/**
 * Structured base class for parsed HL7 Messages. Provides structured access to message content, and constituents. <ul>
 * <li>Provides "keyed" access to the individual constituents of the HL7 message, using familiar and accessible HL7 v.2.x terminology, 
 * via the HL7Designator class. 
 * <li>Constituent items can be added, modified, copied, or deleted. 
 * <li>The entire message can be "re-marshalled" into a HL7 message string, 
 * <li>...or a XML string.
 * </ul>
 */
public class HL7Message {
   /**
    * The subject HL7 message represneted as a String.
    */
   private String                hl7MessageString;
   /**
    * The set of characters used to encode the subject HL7 message
    */
   private HL7Encoding encodingCharacters;
   /**
    * An array of the the constituent segments of the subject HL7 message.
    */
   private HL7MsgItem[]          segments;
   /**
    * A HashMap of arrays of the constituent segments of the subject HL7 message, keyed by segment ID.
    */
   private HL7SegmentHash        segmentHash;

   
   // HL7Message constructors
   /**
    * Constructs an empty HL7Message.
    * 
    */
   public HL7Message() { }

   /**
    * Parses the argument HL7 message string into a HL7Message object.
    * 
    * @param   hl7MsgStr   A String representation of the HL7 message to be parsed.
    * @throws              IOException.
    */ 
   public HL7Message(String hl7MsgStr) throws HL7IOException {
      this.hl7MessageString = hl7MsgStr;
       
      if (hl7MsgStr == null || hl7MsgStr.length() < 9) {
         throw new HL7IOException("HL7Message: Not a valid message:[" + hl7MsgStr + "].");
      } // if
       
      // Set the encoding characters
      if (hl7MsgStr.startsWith("MSH") || hl7MsgStr.startsWith("BHS")) {
         this.encodingCharacters = new HL7Encoding(hl7MsgStr.substring(3, 8));
      } else if (hl7MsgStr.startsWith("BTS")) {
         this.encodingCharacters = new HL7Encoding("|^~\\&");
      } else {
         throw new HL7IOException("HL7Message: Not a valid message:[" + hl7MsgStr + "].");
      } // if - else if - else
          
      // split to segments
      String[] segmentStrings = hl7MsgStr.split("\r");
       
      // build the segment items array
      this.segments = new HL7MsgItem[segmentStrings.length];
       
      // build the segment hash
      this.segmentHash = new HL7SegmentHash(segmentStrings.length);
       
      for (int index = 0; index < segmentStrings.length; ++index) {
         if (segmentStrings[index].length() < 4) {
            continue;
         } // if
         
         int segIndex = 0;
         HL7MsgItem[] segHashEntry = this.segmentHash.get(segmentStrings[index].substring(0, 3));
         if (segHashEntry != null) {
            segIndex = segHashEntry.length;
         } // if
         
         HL7Designator location = new HL7Designator(segmentStrings[index].substring(0, 3) );
         if (segIndex > 0) {
            location.segIndex = segIndex;
         } // if
         
         this.segments[index] = new HL7MsgItem( segmentStrings[index], 
                                                this.encodingCharacters.fieldSeparator, 
                                                this.encodingCharacters,
                                                location);
         this.segmentHash.put(this.segments[index]);
      } // for
   } // HL7Message constructor
 
   
   /**
    * Access to the HL7 encoding characters of the context HL7 message.
    * @return The HL7 encoding characters of the context HL7 message, as a string.
    */  
   public String HL7Encoding() { return(this.encodingCharacters.string); } 

   /**
    * Access to the HL7 field separator of the context HL7 message.
    * @return The HL7 field separator of the context HL7 message, as a string.
    */
   public String HL7FieldSeparator() { return(this.encodingCharacters.fieldSeparator); } 
    
   /**
    * Access to the HL7 component separator of the context HL7 message.
    * @return The HL7 component separator of the context HL7 message, as a string.
    */
   public String HL7ComponentSeparator() { return(this.encodingCharacters.componentSeparator); }
   
   /**
    * Access to the HL7 escape character of the context HL7 message.
    * @return The HL7 escape character of the context HL7 message, as a string.
    */
   public String HL7Escape() { return(this.encodingCharacters.escapeChar); }
   
   /**
    * Access to the HL7 repetition separator of the context HL7 message.
    * @return The HL7 repetition separator of the context HL7 message, as a string.
    */
   public String HL7RepetitionSeparator() { return(this.encodingCharacters.repetitionSeparator); }
   
   /**
    * Access to the HL7 sub-component separator of the context HL7 message.
    * @return The HL7 sub-component separator of the context HL7 message, as a string.
    */
   public String HL7SubComponentSeparator() { return(this.encodingCharacters.subComponentSeparator); }
   
   /**
    * Access to the HL7 component separator of the context HL7 message.
    * @return The HL7 component separator of the context HL7 message, as a char.
    */
   public char HL7ComponentSeparatorChar() { return(this.encodingCharacters.componentSeparator.charAt(0)); }
   
   /**
    * Access to the HL7 escape character of the context HL7 message.
    * @return The HL7 escape character of the context HL7 message, as a char.
    */
   public char HL7EscapeChar() { return(this.encodingCharacters.escapeChar.charAt(0)); }
   
   /**
    * Access to the HL7 repetition separator of the context HL7 message.
    * @return The HL7 repetition separator of the context HL7 message, as a char.
    */
   public char HL7RepetitionSeparatorChar() { return(this.encodingCharacters.repetitionSeparator.charAt(0)); }
   
   /**
    * Access to the HL7 sub-component separator of the context HL7 message.
    * @return The HL7 sub-component separator of the context HL7 message, as a char.
    */
   public char HL7SubComponentSeparatorChar() { return(this.encodingCharacters.subComponentSeparator.charAt(0)); }
   
   
   private void addSegment(HL7MsgItem newSeg) {
      HL7MsgItem[]   currSegArray = this.segments;
      this.segments = new HL7MsgItem[currSegArray.length + 1];
      
      for (int index = 0; index < this.segments.length; ++index) {
         if (index < currSegArray.length) {
            this.segments[index] = currSegArray[index];
         } else {
            this.segments[index] = newSeg;
         } // if - else         
      } // for
    } // addSegment
    

    /**
     * Retrieves the complete item from the specified location of the parsed HL7Message as a string.
     * 
     * @param   location   A String respresenting the location, within the message of the item to be 
     *                     retrieved. Location is specified left to right starting with the segment ID.
     *                     eg; PID.3.2 specifies the 2nd component of the third element of the PID segment.
     *                         PID.3[2].2 specifies the 2nd component of the second repetition of the 
     *                                    third element of the PID segment.    
     * @return             A String representing the entire HL7 message item specified, 
     *                     or null if the item is empty, or does not exist.     
     */
    public String[] get(String location) {
      HL7Designator     hl7Designator = new HL7Designator(location);
      String            segKey  = hl7Designator.segID;
      HL7MsgItem[]      segs = this.segmentHash.get(segKey);
      
      if (segs == null) {
         return(null);
      } // if
      
      ArrayList<String> retnItems = new ArrayList<String>();
      
      // Handle MSH / BHS idiosyncracy
      if (segKey.equals("MSH") || segKey.equals("BHS") ) {
         int seqIndex = hl7Designator.sequence;
         if (seqIndex == -1) {
            retnItems.add(segKey);
         } else if (seqIndex == 0) {
            retnItems.add(this.encodingCharacters.fieldSeparator);
         } else if (seqIndex == 1) {
            retnItems.add(this.encodingCharacters.string);
         } // if - else if
         
         if (!retnItems.isEmpty()) {
            String[] retnArray = new String[retnItems.size()];
            return(retnArray = retnItems.toArray(retnArray));            
         } // if
      } // if
      
      boolean dontCreate = false;
      boolean expandReps = false;
      if (hl7Designator.depth() > 2 && hl7Designator.repetition < 0) {
         expandReps = true;
      } // if
     
      int segLocation = hl7Designator.segIndex;
      for (int segIndex = 0; segIndex < segs.length; ++segIndex) {   
         if (segLocation < 0 || segLocation == segIndex) {
            // expand repetitions
            if (expandReps == true) {
               int numberOfReps = segs[segIndex].numberOfRepetitionsAt(hl7Designator);
               HL7Designator tempLocn = new HL7Designator(location);

               for (int repIndex = 0; repIndex < numberOfReps; ++repIndex) {
                  tempLocn.repetition = repIndex;
                  HL7MsgItem item = segs[segIndex].pick(tempLocn, dontCreate);
                  // check in case pick returns null
                  if (item != null) {
                     retnItems.add(item.toHL7String() );
                  } // if
               } // for
            } else {
               HL7MsgItem item = segs[segIndex].pick(hl7Designator, dontCreate);
               if (item != null) {
                  retnItems.add(item.toHL7String() );
               } // if
            } // if - else 
         } // if
      } // for
      
      String[] retnArray = new String[retnItems.size()];
      return(retnArray = retnItems.toArray(retnArray));
    } // get
        
   
    /**
     * Retrieves a census of valid message items at or subordinate to the specified location.
     * 
     * @param   location      A string representing the subject message location. eg; PID.3, OBX.15.4, PID[1], ...
     * 
     * @return                An array of Strings, each representing a message item location that is <ul> 
     *                           <li> subordinate to the argument location.
     *                           <li> present in the message of curent context.
     *                           <li> not empty.
     * </ul>
     */    
   public String[] census(String location) {
      HL7Designator     hl7Designator = new HL7Designator(location);
      String            segKey = hl7Designator.segID;
      HL7MsgItem[]      segs = this.segmentHash.get(segKey);
      
      if (segs == null) {
         return(null);
      } // if
      
      ArrayList<String> retnItems = new ArrayList<String>();
         
      int segLocation = hl7Designator.segIndex;
      for (int segIndex = 0; segIndex < segs.length; ++segIndex) {   
         if (segLocation < 0 || segLocation == segIndex) {
            // expand repetitions
            int numberOfReps = segs[segIndex].numberOfRepetitionsAt(hl7Designator);
            HL7Designator tempLocn = new HL7Designator(location);

            for (int repIndex = 0; repIndex < numberOfReps; ++repIndex) {
               tempLocn.repetition = repIndex;
               HL7MsgItem item = segs[segIndex].pick(tempLocn, false);
               // check in case pick returns null
               if (item != null && item.location != null) {
                  retnItems.add(item.location.toString() );
               } // if
            } // for
         } // if
      } // for
      
      String[] retnArray = new String[retnItems.size()];
      return(retnArray = retnItems.toArray(retnArray));
   } // census
   
   
   /**
    * Sets the specified message location(s) to the argument string value, and marks the message as touched 
    * for re-marshalling.
    * 
    * @param location   The location to be set
    * @param hl7Str     The string value to se the argument location to.
    */
   public void set(String location, String hl7Str) {
      HL7Designator  hl7Designator = new HL7Designator(location);
      String         segKey  = hl7Designator.segID;
      
      // Handle MSH idiosyncracy
      if (segKey.equals("MSH") || segKey.equals("BHS")) {
         int seqIndex = hl7Designator.sequence;
         if (seqIndex == -1) {
            return;
         } else if (seqIndex == 0) {
            if (hl7Str.length() > 1) {
               return;
            } // if
            
            this.encodingCharacters.fieldSeparator = hl7Str;
            return;
         } else if (seqIndex == 1) {
            this.encodingCharacters.string = hl7Str;
            return;
         } // if - else if
      } // if
      
      // create new segment key if one does not exist....
      HL7MsgItem[] segmentList = this.segmentHash.get(segKey);
      if (segmentList == null || (segmentList.length - 1) < hl7Designator.segIndex) {
         String newSegmentStr = segKey + this.encodingCharacters.fieldSeparator;
         HL7MsgItem newSegment = new HL7MsgItem(newSegmentStr,
                                                this.encodingCharacters.fieldSeparator, 
                                                this.encodingCharacters);
         this.segmentHash.put(newSegment);
         segmentList = this.segmentHash.get(segKey);
         this.addSegment(newSegment);
      } // if
      
      boolean  create = true;
      boolean  expandReps = false;
      
      if (hl7Designator.depth() > 2 && hl7Designator.repetition < 0) {
         expandReps = true;
      } // if
       
      int segLocation = hl7Designator.segIndex; // parse out segment repetition(s)
      
      for (int segIndex = 0; segIndex < segmentList.length; ++segIndex) {
         if (segLocation < 0 || segIndex == segLocation) {
            if (expandReps == true) {
               int numberOfReps = segmentList[segIndex].numberOfRepetitionsAt(hl7Designator);
               HL7Designator tempLocn = new HL7Designator(location);
               if (numberOfReps == 0) { ++numberOfReps; }
               
               for (int repIndex = 0; repIndex < numberOfReps; ++repIndex) {
                  tempLocn.repetition = repIndex;
                  segmentList[segIndex].pick(tempLocn, create).set(hl7Str);
               } // for
            } else {
               segmentList[segIndex].pick(hl7Designator, create).set(hl7Str);
            } // if - else 
         } // if
      } // while
      this.toString();
   } // set

   
   /**
    * regenrates and replaces the segment hash.
    */
   private void reKey() {
      int hashSize = this.segmentHash.segmentHash.size();
      HL7SegmentHash newHash = new HL7SegmentHash(hashSize + 1);
      
      int segCount = this.segments.length;
      for (int index =0; index < segCount; ++index) {
         newHash.put(this.segments[index]);
      } // for
      
      this.segmentHash = newHash;
   } // reKey
   
   
   /**
    * Sets the argument message location to empty.
    * 
    * @param location
    */
   public void remove(String location) {
      HL7Designator hl7Designator = new HL7Designator(location);
      
      if (hl7Designator.depth() < 2) {
         // * Remove one or more entire segment(s) *
         // Set the segment to empty
         this.set(location, "");
         
         // remove empty segments from segments array.
         int arrayLen = this.segments.length;
         HL7MsgItem[] newSegArray = new HL7MsgItem[arrayLen - 1];
         int outIndex = 0;
         for (int inIndex = 0; inIndex < arrayLen; ++inIndex) {
            if (this.segments[inIndex].hl7Content.isEmpty()) {
               continue;
            } // if
            
            newSegArray[outIndex] = this.segments[inIndex];
            ++outIndex;
         } // for
         
         this.segments = newSegArray;
         this.reKey();
      } else {
         this.set(location, "");
      } // if - else   
   } // remove
   
   
   /**
    * Cleans the argument string by escaping any encoding characters used by the current HL7Message context.
    * 
    * @param argStr  A String which may contain untreated HL7 encoding characters.
    * @return        A modification of the argument String, which escapes HL7 encoding characters.
    */
   public String clean(String argStr) {    
      char[]   encoderArray = this.encodingCharacters.toString().toCharArray();
      char     escapeChar = this.encodingCharacters.escapeChar.charAt(0);
      char[]   argArray = argStr.toCharArray();
      char[]   newArray = new char[(argStr.length() * 2) + 16];
      
      int fromIndex = 0;
      int toIndex = 0;
      while (fromIndex < argArray.length) {
         boolean foundEncoder = false;
         
         for ( int encoderIndex = 0; 
               encoderIndex < encoderArray.length && !foundEncoder; 
               ++encoderIndex) {
            if (argArray[fromIndex] == encoderArray[encoderIndex]) {
               foundEncoder = true;
            } // if
         } // for
         
         if (foundEncoder) {
            newArray[toIndex] = escapeChar;
            ++toIndex;
         } // if
         
         newArray[toIndex] = argArray[fromIndex];
         ++toIndex;
         ++fromIndex;
      } // while
      
      String retnStr = new String(newArray);
      return(retnStr.substring(0, --toIndex) );
   } // clean
     
   
   /**
    * Swaps the value of the two specified locations
    * @param location1
    * @param location2
    */
   public void swap(String location1, String location2) {
      String value1[] = this.get(location1);
      String value2[] = this.get(location2);
      this.set(location1, value2[0]);
      this.set(location2, value1[0]);
   } // swap
   
   
   /**
    * Updates the message time to current.
    */
   public void fresh() {
      HL7Time tHL7 = new HL7Time();
      String tStr = new String(tHL7.get().toString());
      if (this.hasSegment("MSH")) {
         this.set("MSH.7", tStr);
      } else if (this.hasSegment("BHS")) {
         this.set("BHS.7", tStr);
      } // if - else if
   } // fresh
   
   /**
    * Re-marshalls the entire HL7 message into a returnable string.
    * @return  The entire message as a String.
    */
   public String toString() {
      StringBuffer msgStr = new StringBuffer();
      
      for (int index = 0; index < this.segments.length; ++index) {
         msgStr.append(this.segments[index].toHL7String() );
         msgStr.append("\r");
      } // for
      
      return(this.hl7MessageString = msgStr.toString() );
   } // toString
   
   
   /**
    * Re-marshalls the entire message into a XML String.
    * @return a XML String representation of the message. 
    * Note that the XML does not include items whcih are either empty or not present.
    */
   public String toXMLString() {
      StringBuffer xmlStrBuffer = new StringBuffer();
      
      // extract message attributes
      xmlStrBuffer.append("<Message");
      
      String[] sendingApp;
      if ( (sendingApp = this.get("MSH.3") ) != null && sendingApp[0].length() > 0) {
         xmlStrBuffer.append(" SendingApp=\"" + sendingApp[0] + "\"");
      } // if
      
      String[] sendingFacility;
      if ( (sendingFacility = this.get("MSH.4") ) != null && sendingFacility[0].length() > 0) {
         xmlStrBuffer.append(" SendingFacility=\"" + sendingFacility[0] + "\"");
      } // if
      
      String[] receivingApp;
      if ( (receivingApp = this.get("MSH.5") ) != null && receivingApp[0].length() > 0) {
         xmlStrBuffer.append(" ReceivingApp=\"" + receivingApp[0] + "\"");
      } // if
      
      String[] receivingFacility;
      if ( (receivingFacility = this.get("MSH.6") ) != null && receivingFacility[0].length() > 0) {
         xmlStrBuffer.append(" ReceivingFacility=\"" + receivingFacility[0] + "\"");
      } // if
      
      String[] dateTime;
      if ( (dateTime = this.get("MSH.7") ) != null && dateTime[0].length() > 0) {
         xmlStrBuffer.append(" DateTime=\"" + dateTime[0] + "\"");
      } // if
      
      String[] security;
      if ( (security = this.get("MSH.8") ) != null && security[0].length() > 0) {
         xmlStrBuffer.append(" Security=\"" + security[0] + "\"");
      } // if
      
      String[] typeCode;
      if ( (typeCode = this.get("MSH.9.1") ) != null && typeCode[0].length() > 0) {
         xmlStrBuffer.append(" TypeCode=\"" + typeCode[0] + "\"");
      } // if
      
      String[] eventCode;
      if ( (eventCode = this.get("MSH.9.2") ) != null && eventCode[0].length() > 0) {
         xmlStrBuffer.append(" EventCode=\"" + eventCode[0] + "\"");
      } // if     
      
      String[] ControlID;
      if ( (ControlID = this.get("MSH.10") ) != null && ControlID[0].length() > 0) {
         xmlStrBuffer.append(" ControlID=\"" + ControlID[0] + "\"");         
      } // if
      
      String[] processingID;
      if ( (processingID = this.get("MSH.11") ) != null && processingID[0].length() > 0) {
         xmlStrBuffer.append(" ProcessingID=\"" + processingID[0] + "\"");         
      } // if
      
      String[] versionID;
      if ( (versionID = this.get("MSH.12") ) != null && versionID[0].length() > 0) {
         xmlStrBuffer.append(" VersionID=\"" + versionID[0] + "\"");         
      } // if
                
      xmlStrBuffer.append(">");
      
      for (int index = 0; index < this.segments.length; ++index) {
         xmlStrBuffer.append(this.segments[index].toXMLString() );
      } // for
      
      xmlStrBuffer.append("</Message>\n");
      return(xmlStrBuffer.toString());
      
   } // toXMLString


   public boolean hasSegment(String segID) {
      return (this.segmentHash.get(segID) == null) ? false : true;
   } // hasSegment
   

   public String controlID() {
      if (this.hasSegment("MSH")) {
         return this.get("MSH.10")[0];
      } else if (this.hasSegment("BHS")) {
         return this.get("BHS.11")[0];
      } // if - else if

      return "";
   } // controlID


   /**
    * Generates a message identification string using items from the MSH.
    * @return the resultant string:
    */
   public String IDString() {
      StringBuffer idStrBuffer = new StringBuffer();

      if (this.hasSegment("MSH")) {
         idStrBuffer.append(this.get("MSH.3")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("MSH.4")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("MSH.5")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("MSH.6")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("MSH.7")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("MSH.9.1")[0]);

         String eventType[] = this.get("MSH.9.2");
         if (eventType != null && eventType.length > 0 && eventType[0] != null) {
            idStrBuffer.append(".");
            idStrBuffer.append(eventType[0]);
         } // if

         String ctlID[] = this.get("MSH.10");
         if (ctlID != null && ctlID.length > 0 && ctlID[0] != null) {
            idStrBuffer.append(".");
            idStrBuffer.append(ctlID[0]);
         } // if
      } else if (this.hasSegment("BHS")) {
         idStrBuffer.append(this.get("BHS.3")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("BHS.4")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("BHS.5")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("BHS.6")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("BHS.7")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("BHS.9")[0]);
         idStrBuffer.append(".");
         idStrBuffer.append(this.get("BHS.11")[0]);
      } else if (this.hasSegment("BHS")) {
         idStrBuffer.append("BTS");
      } else {
         idStrBuffer.append("???");
      }
      return(idStrBuffer.toString() );
   } // IDString
   
   
   
   /**
    * Returns a HL7 acknowledgement message for the context HL7Message, corresponding to the argument conditions.
    * @param ack              Indicates the acknowledgment type; true:Ack, false:NAck
    * @param errorCondition   In the event of a negative acknowledgement (NAck), 
    *                         describes the primary error condition.
    * @param errorLocation    The message location of the error, if known.
    * @param errorCode        A proprietary code, if any, associated with the error.
    * @return                 A HL7Message Acknowledgment, consisting of <ul>
    *                         <li> MSH segment
    *                         <li> MSA segment
    *                         <li> ERR segment (optional, if required.)
    * @throws java.io.IOException in the unlikely event of an error composing the acknowledgment message.
    */
   public HL7Message Acknowledgment(boolean        ack, 
                                    String         errorCondition, 
                                    HL7Designator  errorLocation, 
                                    String         errorCode) 
      throws HL7IOException
   {
      String   sendingApplication = null,
               sendingFacility = null,
               receivingApplication = null,
               receivingFacility = null,
               msgCtlID = null;
      
      if (this.hasSegment("MSH")) {
         sendingApplication = this.get("MSH.3")[0];
         sendingFacility = this.get("MSH.4")[0];
         receivingApplication = this.get("MSH.5")[0];
         receivingFacility = this.get("MSH.6")[0];
         msgCtlID = this.get("MSH.10")[0];
      } else if (this.hasSegment("BHS")) {
         sendingApplication = this.get("BHS.3")[0];
         sendingFacility = this.get("BHS.4")[0];
         receivingApplication = this.get("BHS.5")[0];
         receivingFacility = this.get("BHS.6")[0];
         msgCtlID = this.get("BHS.11")[0];
      } else if (this.hasSegment("BTS")) {
         sendingApplication = "";
         sendingFacility = "";
         receivingApplication = "";
         receivingFacility = "";
         msgCtlID = "";
      } else {
         throw new HL7IOException("Not a valid message.", HL7IOException.NOT_VALID_MSG);
      } // if - else if - else
      
      // create new message for acknowledgement
      HL7Message ackMsg = new HL7Message("MSH" 
                                       + this.HL7Encoding()
                                       + this.encodingCharacters.fieldSeparator
                                       + receivingApplication
                                       + this.encodingCharacters.fieldSeparator
                                       + receivingFacility
                                       + this.encodingCharacters.fieldSeparator
                                       + sendingApplication
                                       + this.encodingCharacters.fieldSeparator
                                       + sendingFacility
                                       + this.encodingCharacters.fieldSeparator);
      // Message DateTime
      String now = new HL7Time().get();
      ackMsg.set("MSH.7", now);
      
      // Message type
      ackMsg.set("MSH.9.1", "ACK");
      ackMsg.set("MSH.9.2", "");
      
      // Control ID
      StringBuffer   msgCtlIDStr = new StringBuffer();
      if (msgCtlID == null) {
         msgCtlID = "";
      } // if
      msgCtlIDStr.append(msgCtlID);
      msgCtlIDStr.append(".AK");
      ackMsg.set("MSH.10", msgCtlIDStr.toString());
      
      // build MSA
      String ackCode;
      if (ack == true) {
         ackCode = "AA";
      } else {
         ackCode = "AE";
      } // if - else 
      
      StringBuffer msaSegmentBuffer = new StringBuffer();
      msaSegmentBuffer.append("MSA");
      msaSegmentBuffer.append(ackMsg.encodingCharacters.fieldSeparator);
      msaSegmentBuffer.append(ackCode);
      msaSegmentBuffer.append(ackMsg.encodingCharacters.fieldSeparator);
      msaSegmentBuffer.append(msgCtlID);
      msaSegmentBuffer.append(ackMsg.encodingCharacters.fieldSeparator);
      msaSegmentBuffer.append(ackMsg.encodingCharacters.fieldSeparator);
      // msaSegmentBuffer.append("\r");
      ackMsg.set("MSA", msaSegmentBuffer.toString());
      
      String msgTypeArray[] = this.get("MSH.9");
      if (msgTypeArray == null) {
         throw new HL7IOException(  "HL7Message.Acknowledgment:Message Type not defined.",
                                    HL7IOException.NO_MSG_TYPE);
      } // if 
      
      if (!msgTypeArray[0].startsWith("NMQ") ) {
         return (ackMsg);
      } // if
      
      // If it's a NMQ add the NCK segment.
      StringBuffer nckSegmentBuffer = new StringBuffer();
      nckSegmentBuffer.append("NCK"); 
      nckSegmentBuffer.append(ackMsg.encodingCharacters.fieldSeparator);
      nckSegmentBuffer.append(now);
      nckSegmentBuffer.append(ackMsg.encodingCharacters.fieldSeparator);
      // nckSegmentBuffer.append("\r");
      ackMsg.set("NCK", nckSegmentBuffer.toString());
      
      ackMsg.set("MSH.9.1", "NMR");
      ackMsg.set("MSH.9.2", "");
    
      return(ackMsg);
      
   } // Acknowledgment
  
} // HL7Message


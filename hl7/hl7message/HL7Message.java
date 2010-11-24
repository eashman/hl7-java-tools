/*
 *  $Id$
 *  $HeadURL$
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Message.java : Provides access to parsed HL7 message content.
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

package us.conxio.hl7.hl7message;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;


/**
 * A parsed HL7 transaction message object class.
 * @author scott
 */
public class HL7Message {
   private HL7Encoding           encoders = null;
   private ArrayList<HL7Segment> segments = null;
   private HL7SegmentMap         segmentMap = null;

   public static final int    CR = 0x0d;
   public static final String SEGMENT_TERMINATOR = "\r";

   public static final String SEGID_MSH = "MSH";
   public static final String SEGID_BHS = "BHS";
   public static final String SEGID_BTS = "BTS";
   public static final String SEGID_MSA = "MSA";
   public static final String SEGID_NCK = "NCK";

   public static final String MSG_TYPE_ACK = "ACK";
   public static final String MSG_TYPE_NMQ = "NMQ";
   public static final String MSG_TYPE_NMR = "NMR";

   public static final String ACK_CODE_OK = "AA";
   public static final String ACK_CODE_ERROR = "AE";

   /**
    * Constructs an empty HL7Message object.
    */
   public HL7Message() { }


   /**
    * Constructs a HL7Message object from the argument String object.
    * @param hl7Msg The HL7 transaction message from which to construct the object.
    */
   public HL7Message(String hl7Msg) {
      parse(hl7Msg);
   } // HL7Message


   private HL7Encoding extractEncoding(String hl7Msg) {
      return (hl7Msg.startsWith(SEGID_MSH) || hl7Msg.startsWith(SEGID_BHS))
           ? new HL7Encoding(hl7Msg.substring(3, 8))
           : null;
   } // extractEncoding


   /**
    * Adds the segments contained in the argument String object to the context
    * HL7Message object.
    * @param segs A string object containing the segments to be added.
    * <ul>Note that default encoding may be used if all of the following
    * conditions are met:
    * <li>the context message encoding characters are not set.
    * <li>the argument String object does not contain a leading MSH or BSH segment.
    * <li>default encoding properties are set.</ul>
    * @see us.conxio.hl7.hl7properties
    */
   public void addSegments(String segs) {
      if (segs == null) return;

      if (!hasDecoders()) encoders = extractEncoding(segs);
      if (!hasDecoders()) setDefaultEncoding();

      String[] segmentStrings = segs.split(SEGMENT_TERMINATOR);

      int segmentCount = segmentStrings.length;
      for (int index = 0; index < segmentCount; ++index) {
         addSegment(new HL7Segment(segmentStrings[index], encoders));
      } // for
   } // addSegments


   /**
    * Adds a single segment to context HL7Message object.
    * @param seg The HL7 transaction message segment to add, as a HL7Segment object.
    */
   public void addSegment(HL7Segment seg) {
      if (seg == null) return;
      
      if (segmentMap == null) segmentMap = new HL7SegmentMap();
      if (segments == null) segments = new ArrayList<HL7Segment>();

      segments.add(seg);
      segmentMap.put(seg);
   } // addSegment


   /**
    * Adds a single segment to context HL7Message object.
    * @param segStr The HL7 transaction message segment to add, as a String.
    * Note that the segment String is effectively terminated by the first occurrence
    * of the segment terminator character (CR,0x0d).
    */
   public void addSegment(String segStr) {
      if (segStr == null) return;
      int endIndex = segStr.indexOf("\r");
      if (endIndex > 0) segStr = segStr.substring(0, endIndex);
      addSegment(new HL7Segment(segStr, encoders));
   } // addSegment


   /**
    * Adds each of the segments in the argument list to the context HL7Message object.
    * @param segments a List of object type HL7Segment, containing the segments to be added.
    */
   public void addSegments(List<HL7Segment> segments) {
      if (segments == null) return;
      for (HL7Segment seg : segments) if (seg != null) addSegment(seg);
   } // addSegments

   private void parse(String msg) {
      encoders = extractEncoding(msg);
      addSegments(msg);
   } // parse


   HL7SegmentMap segMap() {
      return segmentMap;
   } // segMap

   private boolean hasDecoders() {
      return encoders != null;
   } // hasDecoders

   private void setDefaultEncoding() {
      // TODO:
      // encoders = HL7Properties.getDefaultMessageEncoding();
      throw new UnsupportedOperationException("Not yet implemented");
   }


   /**
    * Creates a HL7 representation of the context HL7 transaction message,
    * using the argument encoding characters.
    * @param encode The encoding characters to use for the construction of the
    * HL7 transaction message String.
    * @return a String containing a HL7 representation of the context HL7
    * transaction message, using the argument encoding characters.
    */
   public String toHL7String(HL7Encoding encode) {
      StringBuilder msgBuffer = new StringBuilder();
      for (HL7Segment seg : segments) {
         msgBuffer.append(seg.toHL7String(encode) ).append(SEGMENT_TERMINATOR);
      } // for

      return msgBuffer.toString();
   } // toHL7String


   /**
    * Creates a HL7 representation of the context HL7 transaction message,
    * using the original encoding characters.
    * @return A String object containing a HL7 representation of the context
    * HL7 transaction message, using the original encoding characters.
    */
   public String toHL7String() {
      return toHL7String(encoders);
   } // toHL7String


   @Override
   /**
    * Overrides the default toString method with to HL7String().
    */
   public String toString() {
      return toHL7String();
   } // toString

   
   private HL7Segment pickSegment(String segID, int segIndex, boolean create) {
      ArrayList<HL7Segment> segs = segmentMap.get(segID);

      if (segs == null) {
         if (!create) return null;

         HL7Segment seg = new HL7Segment(segID, encoders);
         addSegment(seg);
         return seg;
      } // if

      int index = segIndex;
      if (segIndex == HL7Designator.UNSPECIFIED) index = segs.size() - 1;
      if (index >= segs.size()) return null;

      return segs.get(index);
   } // pickSegment


   private HL7Element pick(String segID, int segIndex, int sequence, int repetition, int component, int subComponent, boolean create) {
      HL7Segment segment = this.pickSegment(segID, segIndex, create);
      if (segment == null) return null;

      if (sequence == HL7Designator.UNSPECIFIED) return segment;

      HL7Field field = segment.pickField(sequence, create);
      if (field == null) return null;

      if (repetition == HL7Designator.UNSPECIFIED 
      &&  component  == HL7Designator.UNSPECIFIED) return field;

      HL7FieldRepetition hl7Rep = field.pickRepetition(repetition, create);
      if (hl7Rep == null) return null;

      if (component == HL7Designator.UNSPECIFIED) return hl7Rep;

      HL7Component hl7Comp = hl7Rep.pickComponent(component, create);
      if (hl7Comp == null) return null;

      if (subComponent == HL7Designator.UNSPECIFIED) return hl7Comp;

      return hl7Comp.pickSubComponent(subComponent, create);
   } // pick


   private HL7Element pick(HL7Designator designator) {
      return this.pick( designator.getSegID(),
                        designator.getSegIndex(),
                        designator.getSequence(),
                        designator.getRepetitionIndex(),
                        designator.getComponentIndex(),
                        designator.getSubComponentIndex(),
                        false);
   } // pick


   private HL7Element pick(HL7Designator designator, boolean create) {
      return pick(designator.getSegID(),
                  designator.getSegIndex(),
                  designator.getSequence(),
                  designator.getRepetitionIndex(),
                  designator.getComponentIndex(),
                  designator.getSubComponentIndex(),
                  create);
   } // pick


   /**
    * Retrieves the designated HL7 transaction message item for the context
    * HL7Message object.
    * @param designator A HL7Designator object designating the HL7 transaction
    * message item to be retrieved.
    * @see HL7Designator.
    * @return The retrieved item as a String. Note that the returned String object
    * may contain encoding characters if the designator was not specific down to the
    * sub-component level.
    */
   public String get(HL7Designator designator) {
      HL7Element element = pick(designator);
      if (element == null) return null;
      return element.toHL7String(encoders);
   } // get


   /**
    * Retrieves the specified HL7 transaction message item for the context
    * HL7Message object.
    * @param designatorStr A String object specifying the HL7 transaction
    * message item to be retrieved.
    * @see us.conxio.hl7.hl7message.HL7Designator.
    * @return The retrieved item as a String. Note that the returned String object
    * may contain encoding characters if the designator string was not specific
    * down to the sub-component level.
    */
   public String get(String designatorStr) {
      return get(new HL7Designator(designatorStr));
   } // get


   /**
    * Sets the designated HL7 transaction message item to the argument value.
    * @param designator The designator of the item to be set, as a HL7Designator object.
    * @param valueStr The value to which the designated item is to be set.
    */
   public void set(HL7Designator designator, String valueStr) {
      pick(designator, true).set(valueStr, encoders);
   } // set


   /**
    * Sets the designated HL7 transaction message item to the argument value.
    * @param designatorStr The designator of the item to be set, as a String object.
    * @param valueStr The value to which the designated item is to be set, as a String.
    */
   public void set(String designatorStr, String valueStr) {
      set(new HL7Designator(designatorStr), valueStr);
   } // set


   boolean hasSegment(String segID) {
      if (segmentMap == null || segmentMap.isEmpty()) return false;
      if (segID == null || segID.length() < 3) return false;

      return segmentMap.has(segID.substring(0, 3));
   } // hasSegment


   /**
    * Returns a count of occurrences of the argument segment ID in the context
    * HL7Message object.
    * @param segID The 3 character ID of the subject segment.
    * @return The number of occurrences of the argument segment ID in the context
    * HL7Message object, as a int value.
    */
   public int countSegment(String segID) {
      if (segID == null || segID.length() < 3) return 0;
      if (!hasSegment(segID)) return 0;

      ArrayList<HL7Segment> segs = segmentMap.get(segID);
      if (segs == null) return 0;
      return segs.size();
   } // countSegment


   /**
    * Returns the control ID value of the context HL7Message object.
    * @return A String object containing the control ID value. If no control ID
    * value is found then the returned String object is empty.
    */
   public String controlID() {
      String retnStr = "";
      if (hasSegment("MSH")) {
         retnStr = get("MSH.10");
      } else if (hasSegment("BHS")) {
         retnStr = get("BHS.11");
      } // if - else if

      return retnStr == null ? "" : retnStr;
   } // controlID


   /**
    * Updates the time stamp of the context HL7MEssage object to the current time.
    */
   public void fresh() {
      String tStr = HL7Time.get().toString();
      if (hasSegment("MSH")) {
         set("MSH.7", tStr);
      } else if (hasSegment("BHS")) {
         set("BHS.7", tStr);
      } // if - else if
   } // fresh


   /**
    * Creates an acknowledgment message to the context HL7Message, using the
    * information in the argument items.
    * @param ack A boolean indicating true if the returned message is to be a 
    * positive acknowledgment (ack), or false if it is to be a negative
    * acknowledgment (nAck).
    * @param errorCondition A error condition String. Ignored unless ack is false.
    * @param errorLocation A error location designator. Ignored unless ack is false.
    * @param errorCode A error code String. Ignored unless ack is false.
    * @return a HL7Message object representation of the acknowledgment message.
    */
   public HL7Message acknowledgment(boolean ack,
                                    String errorCondition,
                                    HL7Designator errorLocation,
                                    String errorCode) {
      // TODO: refactor
      String   msgCtlID = null;

      if (!isValid()) {
         ack = false;
         if (errorCondition == null) {
            errorCondition = "Not a valid message.";
         } else {
            errorCondition = new StringBuffer(errorCondition).append(" Not a valid message.").toString();
         } // if - else
      } // if

      if (hasSegment(SEGID_MSH)) {
         msgCtlID = get("MSH.10");
      } else if (hasSegment("BHS")) {
         msgCtlID = get("BHS.11");
      } else if (hasSegment("BTS")) {
         msgCtlID = "";
      } // if - else if
      
      HL7Message ackMsg = new HL7Message(ackMSHSegmentString()); // create new message for acknowledgement
      ackMsg.set("MSH.9.1", MSG_TYPE_ACK); // Message type
      ackMsg.set("MSH.9.2", "");

      ackMsg.fresh();   // Message DateTime

      String msgType = get("MSH.9");
      if (msgType == null) {
         ack = false;
         if (errorCondition == null) {
            errorCondition = "Message Type not defined.";
         } else {
            errorCondition = new StringBuffer(errorCondition).append(" Message Type not defined.").toString();
         } // if
      } // if
      
      ackMsg.set("MSH.10", ackControlID(msgCtlID)); // Control ID
      ackMsg.set(SEGID_MSA, msaSegmentString(ack, msgCtlID, ackMsg.encoders));

      if (msgType.startsWith(MSG_TYPE_NMQ) ) {
         ackMsg.set(SEGID_NCK, nckSegmentString(ackMsg.encoders));  // add the NCK segment.
         ackMsg.set("MSH.9.1", MSG_TYPE_NMR);
         ackMsg.set("MSH.9.2", "");
      } // if

      return(ackMsg);
   } // acknowledgment


   /**
    * A deprecated version of acknowledgment()
    * @deprecated 
    * @param ack A boolean indicating true if the returned message is to be a
    * positive acknowledgment (ack), or false if it is to be a negative
    * acknowledgment (nAck).
    * @param errorCondition A error condition String. Ignored unless ack is false.
    * @param errorLocation A error location designator. Ignored unless ack is false.
    * @param errorCode A error code String. Ignored unless ack is false.
    * @return a HL7Message object representation of the acknowledgment message.
    */
   public HL7Message Acknowledgment(boolean ack,
                                    String errorCondition,
                                    HL7Designator errorLocation,
                                    String errorCode) {
      return acknowledgment(ack, errorCondition, errorLocation, errorCode);
   } // Acknowledgment


   /**
    * Creates a message ID string.
    * TODO: Use a format specification from hl7properties, or a default.
    * eg;
    * hl7.message-id-format.delimiter="."
    * hl7.message-id-format.items="MSH.9.1,MSH.9.2,MSH.10"
    *
    * @return
    */
   public String idString() {
      StringBuilder idStrBuffer = new StringBuilder();

      if (this.hasSegment("MSH")) {
         idStrBuffer.append(this.get("MSH.3"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.4"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.5"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.6"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.7"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.9.1"));

         String eventType = this.get("MSH.9.2");
         if (eventType != null && !eventType.isEmpty() ) {
            idStrBuffer.append(this.encoders.getFieldSeparator());
            idStrBuffer.append(eventType);
         } // if

         String ctlID = this.get("MSH.10");
         if (ctlID != null && !ctlID.isEmpty() ) {
            idStrBuffer.append(this.encoders.getFieldSeparator());
            idStrBuffer.append(ctlID);
         } // if
      } else if (this.hasSegment("BHS")) {
         idStrBuffer.append(this.get("BHS.3"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.4"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.5"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.6"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.7"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.9"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.11"));
      } else if (this.hasSegment("BHS")) {
         idStrBuffer.append("BTS");
      } else {
         idStrBuffer.append("???");
      }
      return(idStrBuffer.toString() );
   } // idString


   /**
    * Removes the content at the argument designation.
    * @param designation A HL7 designation string referring to the content to
    * be removed.
    */
   public void remove(String designation) {
      set(designation, "");
   } // remove


   /**
    * Swaps the content of the two argument designations.
    * @param des1 A HL7 designation string referring to one of the content
    * items to be swapped.
    * @param des2 A HL7 designation string referring to the other of the content
    * items to be swapped.
    */
   public void swap(String des1, String des2) {
      String str1 = get(des1);
      String str2 = get(des2);
      set(des1, str2);
      set(des2, str1);
   } // swap


   /**
    * Creates a XML representation of the context HL7Message object.
    * @return
    */
   public String toXMLString() {
      String tag = "HL7Message";
      StringBuffer returnBuffer =  new StringBuffer("<").append(tag).append(">");
      
      for (HL7Segment segment : segments) {
         returnBuffer.append(segment.getID().equals("MSH")
                           ? segment.toXMLString(encoders)
                           : segment.toXMLString());
      } // for

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString

   private boolean isValid() {
      return hasSegment(SEGID_MSH) || hasSegment(SEGID_BHS) || hasSegment(SEGID_BTS);
   } // isValid

   private String ackControlID(String msgCtlID) {
      StringBuilder builder = new StringBuilder();
      if (StringUtils.isNotEmpty(msgCtlID)) builder.append(msgCtlID);
      return builder.append(".AK").toString();
   } // ackControlID


   private String msaSegmentString(boolean ack, String msgCtlID, HL7Encoding encoders) {
      StringBuilder msaSegmentBuffer = new StringBuilder(SEGID_MSA);
      msaSegmentBuffer.append(encoders.getFieldSeparator());
      msaSegmentBuffer.append(ack ? ACK_CODE_OK : ACK_CODE_ERROR);
      msaSegmentBuffer.append(encoders.getFieldSeparator());
      msaSegmentBuffer.append(msgCtlID == null ? "" : msgCtlID);
      msaSegmentBuffer.append(encoders.getFieldSeparator());
      msaSegmentBuffer.append(encoders.getFieldSeparator());
      return msaSegmentBuffer.toString();
   } // msaSegmentString

   private String nckSegmentString(HL7Encoding encoders) {
      StringBuilder nckSegmentBuffer = new StringBuilder();
      nckSegmentBuffer.append(SEGID_NCK);
      nckSegmentBuffer.append(encoders.getFieldSeparator());
      nckSegmentBuffer.append(HL7Time.get());
      nckSegmentBuffer.append(encoders.getFieldSeparator());
      return nckSegmentBuffer.toString();
   } // nckSegmentString


   private String ackMSHSegmentString() {
      String   sendingApplication   = null,
               sendingFacility      = null,
               receivingApplication = null,
               receivingFacility    = null,
               versionID            = null;

      if (this.hasSegment(SEGID_MSH)) {
         sendingApplication   = get("MSH.3");
         sendingFacility      = get("MSH.4");
         receivingApplication = get("MSH.5");
         receivingFacility    = get("MSH.6");
         versionID            = get("MSH.12");
      } else if (hasSegment(SEGID_BHS)) {
         sendingApplication   = get("BHS.3");
         sendingFacility      = get("BHS.4");
         receivingApplication = get("BHS.5");
         receivingFacility    = get("BHS.6");
         versionID            = "";
      } else if (hasSegment(SEGID_BTS)) {
         sendingApplication   = "";
         sendingFacility      = "";
         receivingApplication = "";
         receivingFacility    = "";
         versionID            = "";
      } // if - else if

      return new StringBuilder(SEGID_MSH)
                  .append(encoders.toString())
                  .append(encoders.getFieldSeparator())  // MSH.3
                  .append(receivingApplication)
                  .append(encoders.getFieldSeparator())  // MSH.4
                  .append(receivingFacility)
                  .append(encoders.getFieldSeparator())  // MSH.5
                  .append(sendingApplication)
                  .append(encoders.getFieldSeparator())  // MSH.6
                  .append(sendingFacility)
                  .append(encoders.getFieldSeparator())  // MSH.7
                  .append(encoders.getFieldSeparator())  // MSH.8
                  .append(encoders.getFieldSeparator())  // MSH.9
                  .append(encoders.getFieldSeparator())  // MSH.10
                  .append(encoders.getFieldSeparator())  // MSH.11
                  .append(encoders.getFieldSeparator())  // MSH.12
                  .append(versionID)
                  .append(encoders.getFieldSeparator())  // MSH.13
                  .append(encoders.getFieldSeparator())  // MSH.14
                  .append(encoders.getFieldSeparator())  // MSH.15
                  .append(encoders.getFieldSeparator())  // MSH.16
                  .append(encoders.getFieldSeparator())  // MSH.17
                  .append(encoders.getFieldSeparator())  // MSH.18
                  .append(encoders.getFieldSeparator())  // MSH.19
                  .toString();
   } // ackMSHSegmentString

   /**
    * @return The encoding character set of the context HL7Message, as a
    * HL7Encoding object.
    */
   HL7Encoding getEncoding() {
      return encoders;
   } // getEncoding

} // HL7Message

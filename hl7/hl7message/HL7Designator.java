/*
 *  $Id$
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Designator.java : An interpreted string class representing the lexical
 *                       location of an item in an HL7 message.
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

import org.apache.commons.lang.StringUtils;

/**
 * An interpreted string class representing the lexical location of an item in an HL7 message. 
 * The normalized string takes the form:<br><br><b>
 * {@code
 *       <segID>:[index].<sequence>:[index].<componentIndex>.<subcomponent>
 * }
 * </b><br><br>
 * Note that all items below the segment ID level are optional, however the dot pre-fixed specifiers above the
 * most subordinate level specified are required. Repetition indices (those values preceded by a colon (':'))
 * are optional, with the default being 0, in the case of multiples.
 * <p>
 * Note also that this designation syntax reflects a commonly practiced ambiguity in specifying
 * components and sub-components using ordinal indexing, while specifications for segment repetitions,
 * field sequences, and field repetitions use zero based indexing. Thus the first segment, field or
 * repetition index is 0, while the first component or sub-component index is 1. This implementation
 * attempts to clarify this ambiguity by referring to indices as such, and differentiating the
 * component and sub-component position values from their indices.
 * <br><br>
 * eg;<ul>
 * <li> <b>PID.3.1</b> is the 1st componentIndex of the 3rd sequence in the PID segment.
 * <li> <b>PID:1.3.1</b> is the 1st componentIndex of the 3rd sequence in the 2nd PID segment.
 * <li> <b>PID.3:2.1</b> is the 1st componentIndex of the 3rd repetitionIndex of the 3rd sequence in the PID segment.
 * </ul>
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7Designator {
   static final int  UNSPECIFIED = -3,
                     ALL = -1,
                     INDEX_SEGMENT = HL7ElementLevel.SEGMENT - 1,
                     INDEX_FIELD = HL7ElementLevel.FIELD - 1;
   /**
    * The HL7Designator represented as a String.
    */
   private String  argString;
   /**
    * The HL7Designator Segment ID.
    */
   private String segID;
   /**
    * The index of the specific occurrence of the subject segment. -1 for all.
    */
   private int segIndex          = UNSPECIFIED;
   private int sequence          = ALL;
   private int repetitionIndex   = UNSPECIFIED;
   private int componentIndex    = ALL;
   private int subComponentIndex = ALL;
   /**
    * A flag to control the explicit representation of segment and repetitionIndex indices
    * in the String representation of the HL7Designator.
    */
   private boolean hasColon = false;
   private boolean hasBracket =  false;

   private static final String COLON = ":";
   private static final String OPEN_BRACKET = "[";
   private static final String CLOSE_BRACKET = "[";
   private static final String WILDCARD = "*";
   private static final String SEPARATOR = ".";
   /**
    * level of specification 
    */
   private HL7ElementLevel level;

   /**
    * Creates an empty HL7Designator()
    */
   private HL7Designator() { } // HL7Designator constructor
   
   
   /**
    * Creates a HL7Designator from the argument string.
    * @param argStr A designator of the form:<br><b>
    * nbsp;&nbsp;&nbsp; &lt;segID&gt;:[index].&lt;sequence&gt;:[index].&lt;componentIndex&gt;.&lt;subcomponent&gt; </b><br>
    */
   public HL7Designator(String argStr) {
      parse(argStr);
   } // HL7Designator constructor


   /**
    * Creates a HL7Designator which is a duplicate of the argument.
    * @param argLocation a HL7Designator to be duplicated.
    */
   HL7Designator(HL7Designator argLocation) {
      componentIndex = argLocation.componentIndex;
      repetitionIndex = argLocation.repetitionIndex;
      segIndex = argLocation.segIndex;
      sequence = argLocation.sequence;
      subComponentIndex = argLocation.subComponentIndex;
      segID = argLocation.segID;
      argString = argLocation.argString;
   } // HL7Designator constructor
   
   
   /**
    * Returns the repetitionIndex value, that is, value expressed between the square brackets of
    * the argument location element.
    * @return the integral value of positive base 10 numeric representations.
    *    -1 if the wildcard ('*'), indicating 'all' is specified.
    *    -3 if the element contains no bracketed value, or the value is not valid.
    */
   private int indexValueOf(String argStr) {
      if (hasBracket) return bracketedIndexValueOf(argStr);
      if (hasColon)   return colonSeparatedIndexValueOf(argStr);
      return UNSPECIFIED;
   } // indexValueOf

   
   /**
    * Returns the value of the non-bracketed portion of the argument location element,
    * or UNSPECIFIED if there is no non-bracketed portion of the argument.
    */
   private int positionValueOf(String str) {
      if (hasColon)     return colonSeparatedPositionValueOf(str);
      if (hasBracket)   return bracketSeparatedPositionValueOf(str);
      return Integer.parseInt(str);
   } // positionValueOf
   
   
   
   private void parse(String str) {
      argString = str;
      if (hasColon() ) hasColon = true;
      if (hasBrackets()) hasBracket = true;

      String[] elementDesignations = str.split("\\.");

      // The first designator componentIndex is the segment ID
      if (elementDesignations[0].length() < 3) return;

      segIndex = 0;
      componentIndex = UNSPECIFIED;
      sequence = UNSPECIFIED;
      subComponentIndex = UNSPECIFIED;

      if (elementDesignations[0].length() > 3) {   // a segment index is specified
         segIndex = indexValueOf(elementDesignations[0]);
      } // if

      // Segment ID is the first three characters.
      segID = elementDesignations[0].substring(0, 3);
      level = new HL7ElementLevel(HL7ElementLevel.SEGMENT);

      
      if (elementDesignations.length > 1) {  // a field sequence is specified.
         sequence = positionValueOf(elementDesignations[1]);
         if (hasIndexIndicator(elementDesignations[1])) repetitionIndex = indexValueOf(elementDesignations[1]);
         
         // Correct for MSH indexing idiosyncracy.
         if (segID.equals("MSH") && sequence >= 0) --sequence;
         level.set(level.next().get());
      } // if      

      if (elementDesignations.length > 2) {  // A componentIndex is specified.
         int tempInt = Integer.parseInt(elementDesignations[2]);
         if (tempInt > 0) --tempInt;  // correct for ordinal designations
         componentIndex =  tempInt;
         level.set(level.next().get());
      } // if 

      if (elementDesignations.length > 3) {
         Integer tempInt = Integer.decode(elementDesignations[3]);
         if (tempInt > 0)  --tempInt; // correct for ordinal designations
         subComponentIndex = tempInt;
         level.set(level.next().get());
      } // if
   } // parse
   

   /**
    * Determines and returns the depth, or precision, of the subject designator.
    * @return the depth, or precision, of the subject designator.
    */
   private int depth() {
      int retnV = 5;
      if (segIndex  < ALL) {
         retnV =  0;
      } else if (sequence < 0) {
         retnV = 1;
      } else if (componentIndex < 0) {
         retnV = 3;
      } else if (subComponentIndex < 0) {
         retnV = 4;
      } // if - else if,,,
      return retnV;
   } // length


   void snip() {
      switch (depth()) {
         case 3 : repetitionIndex = UNSPECIFIED;  break;
         case 4 : componentIndex = ALL;  break;
         default: ;
      } // switch
   } // snip
   
   
   
   /**
    * Creates a representation of the context HL7Designator.
    * @return Returns the representation of the context HL7Designator, as a String.
    */
   @Override
   public String toString() {
      if (segID.length() < 3) return(null);

      StringBuilder builder = new StringBuilder();
      
      builder.append(segID);
      
      if (segIndex > 0) {
         builder.append(COLON).append(Integer.toString(segIndex));
      } else if (segIndex == ALL) {
         builder.append(COLON).append(WILDCARD);
      } else if (segIndex < ALL) {      
         return argString = builder.toString();
      } // if - else if - else
      
      if (sequence >= 0) {
         int seqIndex = sequence;
         if (segID.equals("MSH") ) ++seqIndex;
         builder.append(SEPARATOR).append(Integer.toString(seqIndex));
      } else {
         return argString = builder.toString();
      } // if - else 
      
      if (repetitionIndex == ALL) {
         builder.append(COLON).append(WILDCARD);
      } else if (repetitionIndex >= 0) {
         builder.append(COLON).append(Integer.toString(repetitionIndex));
      } // if - else if
      
      if (componentIndex >= 0) {
         builder.append(SEPARATOR).append(Integer.toString(componentIndex + 1));
      } else {
         return argString = builder.toString();
      } // if - else
      
      if (subComponentIndex >= 0) builder.append(SEPARATOR).append(Integer.toString(subComponentIndex + 1));
      
      return argString = builder.toString();
   } // toString
   
   
   /**
    * @return a representation of the context HL7Designator which is suitable
    * for use in XML, as a String.
    */
   public String toXMLString() {
      if (segID.length() < 3) return(null);
      
      StringBuilder builder = new StringBuilder();

      builder.append(segID);
      
      if (segIndex >= 0) builder.append(COLON).append(Integer.toString(segIndex));
      
      if (sequence >= 0) {
         builder.append(SEPARATOR).append(Integer.toString(sequence));
      } else {
         return argString = builder.toString();
      } // if - else 

      if (repetitionIndex >= 0) builder.append(COLON).append(Integer.toString(repetitionIndex));
      
      if (componentIndex >= 0) {
         builder.append(SEPARATOR).append(Integer.toString(componentIndex + 1));
      } else {
         return argString = builder.toString();
      } // if - else
      
      if (subComponentIndex >= 0) builder.append(SEPARATOR).append(Integer.toString(subComponentIndex + 1));
      
      return argString = builder.toString();
   } // toXMLString
   
      
   /**
    * Creates an extension of the context designator, with the argument index.
    * @param index
    * @return
    */
   private HL7Designator spawn(int index) {
      HL7Designator retn = new HL7Designator(this);
      
      switch (retn.depth()) {
         case 0 : 
            retn.segIndex = index;
            retn.sequence = ALL;
            break;
            
         case 1 : 
            retn.sequence = index;
            retn.repetitionIndex = UNSPECIFIED;
            break;
            
         case 2 : 
            retn.repetitionIndex = index;
            retn.componentIndex = ALL;
            break;
            
         case 3 : 
            retn.componentIndex = index;
            retn.subComponentIndex = ALL;
            break;
            
         case 4 : 
            retn.subComponentIndex= index;
            break;
            
         default: return(null);
      } // switch
      
      retn.argString = retn.toString();
      return(retn);
   } // spawn
   
   /**
    * Retrieve the segment ID value.
    * @return the segID
    */
   public String getSegID() {
      return segID;
   } // getSegID

   /**
    * Retrieve the segment index value.
    * @return the segment index value.
    */
   public int getSegIndex() {
      return segIndex;
   } // getSegIndex

   /**
    * @return the sequence
    */
   int getSequence() {
      return sequence;
   }

   /**
    * @return the repetitionIndex
    */
   int getRepetitionIndex() {
      return repetitionIndex;
   } // getRepetitionIndex

   /**
    * @return the componentIndex
    */
   int getComponentIndex() { return componentIndex; }


   /**
    * Returns the component position, as an int value. Note that this is
    * the value that is actually specified, which is, in general, one more than
    * the component index.
    * @return the component position.
    */
   int getComponent() {
      return componentIndex >= 0 ? componentIndex + 1 : componentIndex;
   } // getComponent

   /**
    * @return the subComponentIndex
    */
   int getSubComponentIndex() {  return subComponentIndex; }


   /**
    * Returns the sub-component position, as an int value. Note that this is
    * the value that is actually specified, which is, in general, one more than
    * the sub-component index value.
    * @return the subComponent
    */
   int getSubComponent() {
      return subComponentIndex >= 0 ? subComponentIndex + 1 : subComponentIndex;
   } // getSubComponent

   
   public boolean isSpecifiedSegmentIndex() {
      return segIndex != UNSPECIFIED;
   } // isSpecifiedSegmentIndex

   public boolean isSpecifiedSequence() {
      return sequence != UNSPECIFIED;
   } // isSpecifiedSequence

   
   public boolean isSpecifiedRepetition() {
      return repetitionIndex != UNSPECIFIED;
   } // isSpecifiedRepetition


   public boolean isSpecifiedComponent() {
      return componentIndex != UNSPECIFIED;
   } // isSpecifiedComponent


   public boolean isSpecifiedSubComponent() {
      return subComponentIndex != UNSPECIFIED;
   } // isSpecifiedSubComponent


   HL7ElementLevel getLevel() {
      return level;
   } // HL7ElementLevel

   private boolean hasColon() {
      return argString.contains(COLON);
   } // hasColon

   private boolean hasBrackets() {
      return   argString.contains(OPEN_BRACKET)
          &&   argString.contains(CLOSE_BRACKET);
   } // hasBrackets

   private int bracketedIndexValueOf(String argStr) {
      int openPosn   = argStr.indexOf(OPEN_BRACKET),
          closePosn  = argStr.indexOf(CLOSE_BRACKET);

      if (openPosn >= 0 && closePosn > openPosn) {
         String vStr = argStr.substring(++openPosn, closePosn);
         return vStr.startsWith("*") ? ALL : Integer.parseInt(vStr);
      } // if

      return UNSPECIFIED;
   } // bracketedIndexValueOf


   private int colonSeparatedIndexValueOf(String argStr) {
      if (!hasColon(argStr)) return UNSPECIFIED;
      return Integer.parseInt(StringUtils.substringAfter(argStr, COLON));
   } // colonSeparatedIndexValueOf


   private boolean hasColon(String str) {
      if (str == null) return false;
      return str.contains(COLON);
   } // hasColon


   private int bracketSeparatedPositionValueOf(String argStr) {
      int openPosn   = argStr.indexOf("[");
      int retnInt = -3;

      if (openPosn > 0) {
         retnInt = Integer.decode(argStr.substring(0, openPosn));
         return(retnInt);
      } else if (openPosn == 0) {
         retnInt = -3;
      } else {
         retnInt = Integer.decode(argStr);
      } // if - else if - else

      return(retnInt);
   }

   private int colonSeparatedPositionValueOf(String str) {
      return Integer.parseInt(hasColon(str)
                           ?  StringUtils.substringBefore(str, COLON)
                           :  str);
   } // colonSeparatedPositionValueOf


   private boolean hasIndexIndicator(String str) {
      return hasColon(str) || hasBrackets();
   } // hasIndexIndicator

   
   public void setSegIndex(int value) {
      segIndex = value;
   } // setSegIndex

} // HL7Designator

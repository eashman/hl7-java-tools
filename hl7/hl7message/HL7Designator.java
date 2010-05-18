/*
 *  $Id: HL7Designator.java 11 2010-01-14 09:51:18Z scott.herman@unconxio.us $
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Designator.java : An interpreted string class representing the lexical
                         location of an item in an HL7 message.
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


package us.conxio.hl7.hl7message;

import us.conxio.hl7.hl7message.HL7ElementLevel;

/**
 * An interpreted string class representing the lexical location of an item in an HL7 message. 
 * The normalized string takes the form:<br><b>
 * &nbsp;&nbsp;&nbsp; &lt;segID&gt;[index].&lt;sequence&gt;[index].&lt;component&gt;.&lt;subcomponent&gt; </b><br>
 * Note that all items below the segment ID level are optional, however the dot pre-fixed specifiers above the
 * most subordinate level specified are required. Bracket enclosed specifiers are optional, with the default 
 * being all (??? I don't think so, although it should be.), in the case of multiples.
 * <br><br>
 * eg;<ul>
 * <li> <b>PID.3.1</b> is the 1st component of the 3rd sequence in the PID segment.
 * <li> <b>PID[1].3.1</b> is the 1st component of the 3rd sequence in the 2nd PID segment.
 * <li> <b>PID.3[2].1</b> is the 1st component of the 3rd repetition of the 3rd sequence in the PID segment.
 * </ul>
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7Designator {
   static final int  UNSPECIFIED = -3,
                     ALL = -1;
   /**
    * The HL7Designator represented as a String.
    */
   private String  argString;
   /**
    * The HL7Designator Segment ID.
    */
   private String segID;
   /**
    * The index of the specific occurence of the subject segment. -1 for all.
    */
   private int segIndex;
   private int sequence;
   private int repetition;
   private int component;
   private int subComponent;
   /**
    * A flag to control the explicit represention of segment and repetition idices
    * in the String representation of the HL7Designator.
    */
   private boolean verbose;
   /**
    * level of specification 
    */
   private HL7ElementLevel level;

   /**
    * Creates an empty HL7Designator()
    */
   public HL7Designator() { 
      this.segIndex = HL7Designator.UNSPECIFIED;
      this.sequence = HL7Designator.ALL;
      this.repetition = HL7Designator.UNSPECIFIED;
      this.component = HL7Designator.ALL;
      this.subComponent = HL7Designator.ALL;
      this.verbose = false;
   } // HL7Designator constructor
   
   
   /**
    * Creates a HL7Designator from the argument string.
    * @param argStr A designator of the form:<br><b>
    * nbsp;&nbsp;&nbsp; &lt;segID&gt;[index].&lt;sequence&gt;[index].&lt;component&gt;.&lt;subcomponent&gt; </b><br>
    */
   public HL7Designator(String argStr) {
      this.argString = argStr;
      this.segIndex = -3;
      this.verbose = false;
      parse();
   } // HL7Designator constructor


   /**
    * Creates a HL7Designator which is a duplicate of the argument.
    * @param argLocation a HL7Designator to be duplicated.
    */
   public HL7Designator(HL7Designator argLocation) {
      this.component = argLocation.component;
      this.repetition = argLocation.repetition;
      this.segIndex = argLocation.segIndex;
      this.sequence = argLocation.sequence;
      this.subComponent = argLocation.subComponent;
      this.segID = new String(argLocation.getSegID());
      this.argString = new String(argLocation.getArgString());
      this.verbose = argLocation.verbose;
   } // HL7Designator constructor
   
   
   /**
    * Returns the repetition value, that is, value expressed between the square brackets of
    * the argument location element.
    * @return the integral value of positive base 10 numeric representations.
    *    -1 if the wildcard ('*'), indicating 'all' is specified.
    *    -3 if the element contains no bracketed value, or the value is not valid.
    */
   private int indexValueOf(String argStr) {
      int openPosn   = argStr.indexOf("["),
          closePosn  = argStr.indexOf("]"),
          retnV      = -3;

      if (openPosn >= 0 && closePosn > openPosn) {
         String vStr = argStr.substring(++openPosn, closePosn);
         if (vStr.startsWith("*")) { 
            retnV = -1;
         } else {
            retnV = Integer.decode(vStr);
         }
      } // if

      return retnV;
   } // indexValueOf

   
   /**
    * Returns the value of the non-bracketed portion of the argument location element,
    * or -3 if there is no non-bracketed portion of the argument.
    */
   private int positionValueOf(String argStr) {
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
      
   } // positionValueOf
   
   
   
   private void parse() {
      String[] args = this.getArgString().split("\\.");

      if (args[0].length() < 3) {
         return;
      } // if

      this.setSegIndex(0);
      this.setRepetition(-3);
      this.setComponent(-3);
      this.setSequence(-3);
      this.setSubComponent(-3);

      // Segment index specification
      if (args[0].length() > 3) {
         this.setSegIndex(indexValueOf(args[0]));
      } // if

      // Segment ID is the first three characters.
      this.setSegID(args[0].substring(0, 3));
      this.level = new HL7ElementLevel(HL7ElementLevel.SEGMENT);

      // Sequence is the second item.
      if (args.length > 1) {
         this.setSequence(this.positionValueOf(args[1]));
         this.setRepetition(indexValueOf(args[1]));
         
         // Correct for MSH indexing idiosyncracy.
         if (this.getSegID().equals("MSH") && this.getSequence() >= 0) {
            this.setSequence(this.getSequence() - 1);
         } // if
         level.set(level.next().get());
      } // if      

      // Component is the third item
      if (args.length > 2) {
         Integer tempInt = Integer.decode(args[2]);
         if (tempInt > 0) { --tempInt; }  // correct for ordinal designations
         this.setComponent((int) tempInt);
         level.set(level.next().get());
      } // if 

      if (args.length > 3) {
         Integer tempInt = Integer.decode(args[3]);
         if (tempInt > 0) { --tempInt; }  // correct for ordinal designations
         this.setSubComponent((int) tempInt);
         level.set(level.next().get());
      } // if
   } // parse
   

   /**
    * Determines and returns the depth, or precision, of the subject designator.
    * @return the depth, or precision, of the subject designator.
    */
   public int depth() {
      int retnV = 5;
      if (this.getSegIndex()  < -1) { 
         retnV =  0;
      } else if (this.getSequence() < 0) {
         retnV = 1;
      } else if (this.getRepetition() < -1) {
         retnV = 2;
      } else if (this.getComponent() < 0) {
         retnV = 3;
      } else if (this.getSubComponent() < 0) {
         retnV = 4;
      } // if - else if,,,
      return retnV;
   } // length


   void snip() {
      switch (this.depth()) {
         case 3 : this.setRepetition(-3);  break;
         case 4 : this.setComponent(-1);  break;
         default: ;
      } // switch
   } // snip
   
   
   
   /**
    * Creates a representation of the context HL7Designator.
    * @return Returns the represention of the context HL7Designator, as a String.
    */
   public String toString() {
      StringBuffer tempStr = new StringBuffer();
      
      if (this.getSegID().length() < 3) {
         return(null);
      } // if
      
      tempStr.append(this.getSegID());
      
      if (this.getSegIndex() == 0 && this.isVerbose() == false) {
         ;
      } else if (this.getSegIndex() >= 0) {
         tempStr.append("[" + Integer.toString(this.getSegIndex()) + "]");
      } else if (this.getSegIndex() == -1) {
         tempStr.append("[*]");
      } else {
         this.setArgString(new String(tempStr.toString()));
         return(this.getArgString());
      } // if - else if - else
      
      if (this.getSequence() >= 0) {
         int seqIndex = this.getSequence();
         if (this.getSegID().equals("MSH") ) {
            ++seqIndex;
         } // if
         
         tempStr.append("." + Integer.toString(seqIndex));
      } else {
         this.setArgString(new String(tempStr.toString()));
         return(this.getArgString());
      } // if - else 
      
      if (this.getRepetition() == 0 && this.isVerbose() == false) {
         ;
      } else if (this.getRepetition() == -1) {
         tempStr.append("[*]");
      } else if (this.getRepetition() >= 0) {
         tempStr.append("[" + Integer.toString(this.getRepetition()) + "]");
      } else {
         this.setArgString(new String(tempStr.toString()));
         return(this.getArgString());
      } // if - else if - else
      
      if (this.getComponent() >= 0) {
         tempStr.append("." + Integer.toString(this.getComponent() + 1));
      } else {
         this.setArgString(new String(tempStr.toString()));
         return(this.getArgString());
      } // if - else
      
      if (this.getSubComponent() >= 0) {
         tempStr.append("." + Integer.toString(this.getSubComponent() + 1));
      } else {
         this.setArgString(new String(tempStr.toString()));
         return(this.getArgString());
      } // if - else
      
      this.setArgString(new String(tempStr.toString()));
      return(this.getArgString());
      
   } // toString
   
   
   /**
    * Creates a representation of the context HL7Designator which is suitable for use in XML.
    * @return a representation of the context HL7Designator which is suitable for use in XML, as a String.
    */
   public String toXMLString() {
      StringBuffer tempStr = new StringBuffer();
      
      if (this.getSegID().length() < 3) {
         return(null);
      } // if
      
      tempStr.append(this.getSegID());
      
      if (this.getSegIndex() < -1) {
         return(this.argString = new String(tempStr.toString()));
      } // if - else if - else
      
      if (this.getSequence() >= 0) {
         tempStr.append("." + Integer.toString(this.getSequence()));
      } else {
         return(this.argString = new String(tempStr.toString()));
      } // if - else 
      
      if (this.getRepetition() < -1) {
         return(this.argString = new String(tempStr.toString()));
      } // if - else if - else
      
      if (this.getComponent() >= 0) {
         tempStr.append("." + Integer.toString(this.getComponent() + 1));
      } else {
         return(this.argString = new String(tempStr.toString()));
      } // if - else
      
      if (this.getSubComponent() >= 0) {
         tempStr.append("." + Integer.toString(this.getSubComponent() + 1));
      } // if
      
      this.setArgString(new String(tempStr.toString()));
      return(this.getArgString());
      
   } // toXMLString
   
      
   /**
    * Creates an extension of the context designator, with the argument index.
    * @param index
    * @return
    */
   HL7Designator spawn(int index) {
      HL7Designator retn = new HL7Designator(this);
      
      switch (retn.depth()) {
         case 0 : 
            retn.setSegIndex(index);
            retn.setSequence(-1);
            break;
            
         case 1 : 
            retn.setSequence(index);
            retn.setRepetition(-3);
            break;
            
         case 2 : 
            retn.setRepetition(index);
            retn.setComponent(-1);
            break;
            
         case 3 : 
            retn.setComponent(index);
            retn.setSubComponent(-1);
            break;
            
         case 4 : 
            retn.setSubComponent(index);
            break;
            
         default: return(null);
      } // switch
      
      retn.setArgString(retn.toString());
      return(retn);
   } // spawn
   
   /**
    * Sets an internal flag in the context HL7Designator, which indicates that segment and repetition indices are to
    * always be represented explicitly.
    */
   public void setVerbose() {
      this.verbose = true;
   } // setVerbose

   /**
    * @return the argString
    */
   public String getArgString() {
      return argString;
   }

   /**
    * @param argString the argString to set
    */
   public void setArgString(String argString) {
      this.argString = argString;
   }

   /**
    * @return the segID
    */
   public String getSegID() {
      return segID;
   }

   /**
    * @param segID the segID to set
    */
   public void setSegID(String segID) {
      this.segID = segID;
   }

   /**
    * @return the segIndex
    */
   public int getSegIndex() {
      return segIndex;
   }

   /**
    * @param segIndex the segIndex to set
    */
   public void setSegIndex(int segIndex) {
      this.segIndex = segIndex;
   }

   /**
    * @return the sequence
    */
   public int getSequence() {
      return sequence;
   }

   /**
    * @param sequence the sequence to set
    */
   public void setSequence(int sequence) {
      this.sequence = sequence;
   }

   /**
    * @return the repetition
    */
   public int getRepetition() {
      return repetition;
   }

   /**
    * @param repetition the repetition to set
    */
   public void setRepetition(int repetition) {
      this.repetition = repetition;
   }

   /**
    * @return the component
    */
   public int getComponent() {
      return component;
   }

   /**
    * @param component the component to set
    */
   public void setComponent(int component) {
      this.component = component;
   }

   /**
    * @return the subComponent
    */
   public int getSubComponent() {
      return subComponent;
   }

   /**
    * @param subComponent the subComponent to set
    */
   public void setSubComponent(int subComponent) {
      this.subComponent = subComponent;
   }

   /**
    * @return the verbose
    */
   public boolean isVerbose() {
      return verbose;
   }

   public boolean isSpecifiedSegmentIndex() {
      return (this.segIndex != -3);
   } // isSpecifiedSegmentIndex

   public boolean isSpecifiedSequence() {
      return (this.sequence != -3);
   } // isSpecifiedSequence

   
   public boolean isSpecifiedRepetition() {
      return (this.repetition != -3);
   } // isSpecifiedRepetition


   public boolean isSpecifiedComponent() {
      return (this.component != -3);
   } // isSpecifiedComponent


   public boolean isSpecifiedSubComponent() {
      return (this.subComponent != -3);
   } // isSpecifiedSubComponent


   public HL7ElementLevel getLevel() {
      return this.level;
   } // HL7ElementLevel

} // HL7Designator

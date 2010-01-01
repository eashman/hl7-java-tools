/*
 *  $Id: HL7Designator.java 14 2009-11-20 22:03:44Z scott $
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


package us.conxio.HL7.HL7Message;

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
   /**
    * The Hl7Designator represented as a String.
    */
   String   argString,
   /**
    * The HL7 segment identifier; The first 3 characters of the subject HL7 segment.
    */           
            segID;
   /**
    * The index of the specific occurence of the subject segment. -1 for all.
    */
   int      segIndex,
   /**
    * The index of the subject sequence, or field.
    */
            sequence,
   /**
    * The index of the subject sequence, or field repetition. -1 for all.
    */
            repetition,
   /**
    * The index of the subject component, in the subject field, or sequence.
    */
            component,
   /**
    * The index of the subject sub-component, in the subject component.
    */         
            subComponent;
   /**
    * A flsg to control the explicit represention of segment and repetition idices 
    * in the String representation of the HL7Designator.
    */
   boolean  verbose;

   /**
    * Creates an empty HL7Designator()
    */
   public HL7Designator() { 
      this.segIndex = -3;
      this.sequence = -1;
      this.repetition = -3;
      this.component = -1;
      this.subComponent = -1;
      this.verbose = false;
   } // HL7Designator constructor
   
   
   /**
    * Creates a HL7Designator from the argument string.
    * @param argStr A designator of the form:<br><b>
    * nbsp;&nbsp;&nbsp; &lt;segID&gt;[index].&lt;sequence&gt;[index].&lt;component&gt;.&lt;subcomponent&gt; </b><br>
    */
   public HL7Designator(String argStr) {
      this.argString = new String( argStr );
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
      this.segID = new String(argLocation.segID);
      this.argString = new String(argLocation.argString);
      this.verbose = argLocation.verbose;
   } // HL7Designator constructor
   
   
   // Returns the repetition value, that is, value expressed between the square brackets of
   // the argument location element. 
   // Returns 
   //    the integral value of positive base 10 numeric representations.
   //    -1 if the wildcard ('*'), indicating 'all' is specified.
   //    -3 if the element contains no bracketed value, or the value is not valid.
   private int indexValueOf(String argStr) {
      int openPosn   = argStr.indexOf("["),
          closePosn  = argStr.indexOf("]");

      if (openPosn >= 0 && closePosn > openPosn) {
         String vStr = argStr.substring(++openPosn, closePosn);
         if (vStr.startsWith("*")) { return(-1); }
         Integer retnV = Integer.decode(vStr);
         return retnV;
      } // if

      return -3;
   } // indexValueOf

   
   // Returns the value of the non-bracketed portion of the argument location element,
   // or -3 if there is no non-bracketed portion of the argument.
   private int positionValueOf(String argStr) {
      int openPosn   = argStr.indexOf("[");
      Integer tempInt;
      int retnInt;
      
      if (openPosn > 0) {
         tempInt = Integer.decode(argStr.substring(0, openPosn));
         retnInt = tempInt;
         return(retnInt);  
      } else if (openPosn == 0) {
         return (-3);
      } // if - else if
      
      tempInt = Integer.decode(argStr);
      retnInt = tempInt;
      return(retnInt);  
      
   } // positionValueOf
   
   
   
   private void parse() {
      String[] args = this.argString.split("\\.");

      if (args[0].length() < 3) {
         return;
      } // if

      this.segIndex = 0;
      this.repetition = -3;
      this.component = -3;
      this.sequence = -3;
      this.subComponent = -3;

      // Segment index specification
      if (args[0].length() > 3) {
         this.segIndex = indexValueOf(args[0]);
         // if (this.segIndex == -1) { ++this.segIndex; }
      } // if

      // Segment ID is the first three characters.
      this.segID = args[0].substring(0, 3);

      // Sequence is the second item.
      if (args.length > 1) {
         this.sequence = this.positionValueOf(args[1]);
         this.repetition = indexValueOf(args[1]);
         
         // Correct for MSH indexing idiosyncracy.
         if (this.segID.equals("MSH") && this.sequence >= 0) {
            --this.sequence;
         } // if
      } // if      

      // Component is the third item
      if (args.length > 2) {
         Integer tempInt = Integer.decode(args[2]);
         if (tempInt > 0) { --tempInt; }  // correct for ordinal designations
         this.component = tempInt;
      } // if 

      if (args.length > 3) {
         Integer tempInt = Integer.decode(args[3]);
         if (tempInt > 0) { --tempInt; }  // correct for ordinal designations
         this.subComponent = tempInt;
      } // if
   } // parse
   

   /**
    * Determines and returns the depth, or precision, of the subject designator.
    * @return the depth, or precision, of the subject designator.
    */
   public int depth() {
      if (this.segIndex       < -1) { return 0; }
      if (this.sequence       < 0) { return 1; }
      if (this.repetition     < -1) { return 2; }
      if (this.component      < 0) { return 3; }
      if (this.subComponent   < 0) { return 4; }
      return 5;
   } // length


   void snip() {
      switch (this.depth()) {
         case 3 : this.repetition = -3;  break;
         case 4 : this.component  = -1;  break;
         default: ;
      } // switch
   } // snip
   
   
   
   /**
    * Creates a representation of the context HL7Designator.
    * @return Returns the represention of the context HL7Designator, as a String.
    */
   public String toString() {
      StringBuffer tempStr = new StringBuffer();
      
      if (this.segID.length() < 3) {
         return(null);
      } // if
      
      tempStr.append(this.segID);
      
      if (this.segIndex == 0 && this.verbose == false) {
         ;
      } else if (this.segIndex >= 0) {
         tempStr.append("[" + Integer.toString(this.segIndex) + "]");
      } else if (this.segIndex == -1) {
         tempStr.append("[*]");
      } else {
         this.argString = new String(tempStr.toString());
         return(this.argString);
      } // if - else if - else
      
      if (this.sequence >= 0) {
         int seqIndex = this.sequence;
         if (this.segID.equals("MSH") ) {
            ++seqIndex;
         } // if
         
         tempStr.append("." + Integer.toString(seqIndex));
      } else {
         this.argString = new String(tempStr.toString());
         return(this.argString);
      } // if - else 
      
      if (this.repetition == 0 && this.verbose == false) {
         ;
      } else if (this.repetition == -1) {
         tempStr.append("[*]");
      } else if (this.repetition >= 0) {
         tempStr.append("[" + Integer.toString(this.repetition) + "]");
      } else {
         this.argString = new String(tempStr.toString());
         return(this.argString);
      } // if - else if - else
      
      if (this.component >= 0) {
         tempStr.append("." + Integer.toString(this.component + 1));
      } else {
         this.argString = new String(tempStr.toString());
         return(this.argString);
      } // if - else
      
      if (this.subComponent >= 0) {
         tempStr.append("." + Integer.toString(this.subComponent + 1));
      } else {
         this.argString = new String(tempStr.toString());
         return(this.argString);
      } // if - else
      
      this.argString = new String(tempStr.toString());
      return(this.argString);
      
   } // toString
   
   
   /**
    * Creates a representation of the context HL7Designator which is suitable for use in XML.
    * @return a representation of the context HL7Designator which is suitable for use in XML, as a String.
    */
   public String toXMLString() {
      StringBuffer tempStr = new StringBuffer();
      
      if (this.segID.length() < 3) {
         return(null);
      } // if
      
      tempStr.append(this.segID);
      
      if (this.segIndex < -1) {
         return(this.argString = new String(tempStr.toString()));
      } // if - else if - else
      
      if (this.sequence >= 0) {
         tempStr.append("." + Integer.toString(this.sequence));
      } else {
         return(this.argString = new String(tempStr.toString()));
      } // if - else 
      
      if (this.repetition < -1) {
         return(this.argString = new String(tempStr.toString()));
      } // if - else if - else
      
      if (this.component >= 0) {
         tempStr.append("." + Integer.toString(this.component + 1));
      } else {
         return(this.argString = new String(tempStr.toString()));
      } // if - else
      
      if (this.subComponent >= 0) {
         tempStr.append("." + Integer.toString(this.subComponent + 1));
      } // if
      
      this.argString = new String(tempStr.toString());
      return(this.argString);
      
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
            retn.segIndex = index;
            retn.sequence = -1;
            break;
            
         case 1 : 
            retn.sequence = index;     
            retn.repetition = -3;   
            break;
            
         case 2 : 
            retn.repetition = index;   
            retn.component = -1;    
            break;
            
         case 3 : 
            retn.component = index;    
            retn.subComponent = -1; 
            break;
            
         case 4 : 
            retn.subComponent = index; 
            break;
            
         default: return(null);
      } // switch
      
      retn.argString = retn.toString();
      return(retn);
   } // spawn
   
   /**
    * Sets an internal flag in the context HL7Designator, which indicates that segment and repetition indices are to
    * always be represented explicitly.
    */
   public void setVerbose() {
      this.verbose = true;
   } // setVerbose

} // HL7Designator

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;

import java.util.NoSuchElementException;

/**
 *
 * @author scott
 */
public class HL7ElementLevel {
   public static final int SEGMENT      = 1,
                           FIELD        = 2,
                           REPETITION   = 3,
                           COMPONENT    = 4,
                           SUBCOMPONENT = 5;
   private int value;

   public HL7ElementLevel(int level) {
      this.set(level);
   } // HL7ElementLevel
   
   public void set(int level) {
      if (level < HL7ElementLevel.SEGMENT || level > HL7ElementLevel.SUBCOMPONENT) {
         throw new IllegalArgumentException("Illegal level:" + Integer.toString(level));
      } // if

      value = level;
   } // set

   public int get() {
      return this.value;
   } // get

   public boolean hasNext() {
      if (this.value > COMPONENT) {
         return false;
      } // if

      return true;
   } // hasNext

   public HL7ElementLevel next() throws NoSuchElementException {
      if (this.hasNext()) {
         return new HL7ElementLevel(this.value + 1);
      }

      throw new NoSuchElementException();
   } // next
} // HL7ElementLevel


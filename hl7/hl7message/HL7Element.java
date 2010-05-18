/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;

/**
 *
 * @author scott
 */
public interface HL7Element {

   String toHL7String(HL7Encoding encoders);
   void set(String msgText, HL7Encoding encoders);
   HL7Element getElement(int index);
   public int getLevel();
   boolean hasContent();
   // boolean hasConstituents();

} // HL7Element

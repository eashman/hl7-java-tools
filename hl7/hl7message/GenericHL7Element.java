/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;

/**
 *
 * @author scott
 */
public class GenericHL7Element extends AbstractHL7Element implements HL7Element {

   public GenericHL7Element(HL7ElementLevel level) {
      super(level.get());
   } // GenericHL7Element constructor

} // GenericHL7Element

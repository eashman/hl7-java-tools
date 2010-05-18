/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7stream;

import us.conxio.hl7.hl7message.HL7Message;

/**
 *
 * @author scott
 */
public class HL7MLLPStream implements HL7Stream {

   public boolean open() throws HL7IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean close() throws HL7IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public HL7Message read() throws HL7IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean write(HL7Message msg) throws HL7IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int status() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isClosed() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isOpen() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isServer() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String description() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public HL7MessageHandler dispatchHandler() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

} // HL7MLLPStream

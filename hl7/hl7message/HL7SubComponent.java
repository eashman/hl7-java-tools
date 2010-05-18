/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;

/**
 *
 * @author scott
 */
public class HL7SubComponent implements HL7Element {
   private   HL7ElementLevel        level;
   protected String                 content;
   private boolean                  touched;

   public HL7SubComponent() {
      this.level = new HL7ElementLevel(HL7ElementLevel.SUBCOMPONENT);
   } // HL7SubComponent


   public HL7SubComponent(String subCompStr, HL7Encoding encoders) {
      this();
      this.content = subCompStr;
   } // HL7SubComponent


   public boolean hasContent() {
      if (this.content != null && !this.content.isEmpty()) {
         return true;
      } // if

      return false;
   } // hasContent


   public boolean hasConstituents() {
      return false;
   } // hasConstituents

   
   public String getContent() {
      return this.content;
   } // getContent


   public int getLevel() {
      return this.level.get();
   } // getLevel


   public HL7Element getElement(int index) {
      return null;
   } // getElement


   public void set(String msgText, HL7Encoding encoders) {
      this.content = msgText;
      touched = true;
   } // set


   public String toHL7String(HL7Encoding encoders) {
      if (this.content == null) {
         return "";
      } // if

      return this.content;
   } // if

   
   public String toXMLString(int subComponentIndex) {
      String tag = "subComponent";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(subComponentIndex))
              .append("\">");

      if (this.content == null) {
         returnBuffer.append("");
      } else {
         returnBuffer.append(this.content);
      } // if - else

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


} // HL7SubComponent

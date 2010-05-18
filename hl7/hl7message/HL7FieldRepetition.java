/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;

import java.util.ArrayList;

/**
 *
 * @author scott
 */
public class HL7FieldRepetition implements HL7Element {
   private   HL7ElementLevel              level;
   protected ArrayList<HL7Component>      components = null;
   private boolean                        touched;


   public HL7FieldRepetition() {
      this.level = new HL7ElementLevel(HL7ElementLevel.REPETITION);
   } // HL7FieldRepetition


   public HL7FieldRepetition(String repetitionStr, HL7Encoding encoders) {
      this();
      this.set(repetitionStr, encoders);
   } // HL7FieldRepetition


   public boolean hasComponent(int index) {
      if (  this.components == null
      ||    index < 0
      ||    index >= this.components.size()) {
         return false;
      } // if

      return true;
   } // hasSubComponent


   public HL7Component getComponent(int index) {
      if (this.hasComponent(index)) {
         return this.components.get(index);
      } // if

      return null;
   } // getComponent


   public HL7Element getElement(int index) {
      return this.getComponent(index);
   } // getElement

   
   public void setLevel(int level) {
      this.level.set(level);
   } // setLevel


   public int getLevel() {
      return this.level.get();
   } // getLevel


   public boolean wasTouched() {
      return this.touched;
   } // wasTouched


   public void set(String msgText, HL7Encoding encoders) {
      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.COMPONENT);
      ArrayList<String>  elements = encoders.hl7Split(msgText, nextLevel);
      this.components = new ArrayList<HL7Component>();
      for (String elementStr : elements) {
         HL7Component element = new HL7Component(elementStr, encoders);
         this.components.add(element);
      } // for

      this.touched = true;
   } // set


   public String toHL7String(HL7Encoding encoders) {
      if (this.components == null || this.components.isEmpty()) {
         return "";
      } // if

      ArrayList<String> elementStrings = new ArrayList<String>();
      for (HL7Component element : this.components) {
         elementStrings.add(element.toHL7String(encoders));
      } // for

      return encoders.hl7Join(elementStrings, this.level.next());
   } // toString


   public String toXMLString(int repIndex) {
      if (!this.hasContent()) return "";

      String tag = "Repetition";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(repIndex))
              .append("\">");

      if (this.hasSimpleContent()) {
         returnBuffer.append(this.getSimpleContent());
      } else {
         int componentIndex = 1;
         for (HL7Component component : this.components) {
            if (component.hasContent() ) {
               returnBuffer.append(component.toXMLString(componentIndex));
            } // if
            ++componentIndex;
         } // for
      } // if - else

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


   public boolean hasContent() {
      if (this.hasConstituents()) {
         for (HL7Component comp : this.components) {
            if (comp.hasContent()) {
               return true;
            } // if
         } // if
      } // if

      return false;
   } // hasContent


   public boolean hasSimpleContent() {
      if (this.hasConstituents() ) {
         if (this.components.size() < 2 && this.components.get(0).hasSimpleContent()) {
            return true;
         } // if
      } // if

      return false;
   } // hasSimpleContent


   public String getSimpleContent() {
      if (this.hasSimpleContent()) {
         return this.components.get(0).getSimpleContent();
      } // if

      return "";
   } // if


   public boolean hasConstituents() {
      if (this.components != null && this.components.size() > 0) {
         return true;
      } // if

      return false;
   } // hasConstituents


   public String getContent() {
      return null;
   } // getContent


   HL7Component pickComponent(int component, boolean create) {
      if (!this.hasComponent(component)) {
         if (!create) {
            return null;
         } // if

         for (int newIndex = this.componentCount(); newIndex <= component; ++newIndex) {
            this.addComponent();
         } // for
      } // if

      return this.getComponent(component);
   } // pickComponent


   private int componentCount() {
      if (this.components == null) {
         return 0;
      } // if

      return this.components.size();
   } // componentCount


   private void addComponent() {
      if (this.components == null) {
         this.components = new ArrayList<HL7Component>();
      } // if

      this.components.add(new HL7Component());
   } // addComponent

} // HL7FieldRepetition

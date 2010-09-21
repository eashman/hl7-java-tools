/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7service;

import java.net.URI;
import java.util.HashMap;

/**
 *
 * @author scott
 */
class TranslationTableDirectory {
   static HashMap<String, TranslationTable> map = null;

   public static boolean has(URI uri) {
      return hasMap() && map.containsKey(uri.toString());
   } // has

   private static boolean hasMap() {
      return map != null && !map.isEmpty();
   } // hasMap

   public static TranslationTable get(URI uri) {
      if (!has(uri)) return null;
      return map.get(uri.toString());
   } // get

   public static TranslationTable add(TranslationTable tab) {
      if (map == null) map = new HashMap<String, TranslationTable>();
      map.put(tab.getURI().toString(), tab);
      return tab;
   } // add
} // TranslationTableDirectory

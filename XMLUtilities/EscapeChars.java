/*
 *  $Id$
 * EscapeChars is a functional utility container class for
 * convenience methods for escaping special characters related to HTML, XML, 
 * and regular expressions.
 * 
 * Adopted and adapted from http://www.javapractices.com under the following BSD
 * licensing provisions shown below. Note that the person (s) designated as author(s)
 * in this and any subsequent adoption(s) and/or modification(s) of this code,
 * do so in explicit agreement with these terms.
 * 
 * Copyright (c) 2002-2009, Hirondelle Systems All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of Hirondelle Systems nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY HIRONDELLE SYSTEMS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL HIRONDELLE SYSTEMS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package us.conxio.XMLUtilities;


import java.net.URLEncoder;

import java.io.UnsupportedEncodingException;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;



/**
 Convenience methods for escaping special characters related to HTML, XML,
 and regular expressions.
*/
public final class EscapeChars {

  /**
    Escape characters for text appearing in HTML markup.

    <P>This method exists as a defence against Cross Site Scripting (XSS) hacks.
    The idea is to neutralize control characters commonly used by scripts, such that
    they will not be executed by the browser. This is done by replacing the control
    characters with their escaped equivalents.
    See {@link hirondelle.web4j.security.SafeText} as well.

    <P>The following characters are replaced with corresponding
    HTML character entities :
    <table border='1' cellpadding='3' cellspacing='0'>
    <tr><th> Character </th><th>Replacement</th></tr>
    <tr><td> < </td><td> &lt; </td></tr>
    <tr><td> > </td><td> &gt; </td></tr>
    <tr><td> & </td><td> &amp; </td></tr>
    <tr><td> " </td><td> &quot;</td></tr>
    <tr><td> \t </td><td> &#009;</td></tr>
    <tr><td> ! </td><td> &#033;</td></tr>
    <tr><td> # </td><td> &#035;</td></tr>
    <tr><td> $ </td><td> &#036;</td></tr>
    <tr><td> % </td><td> &#037;</td></tr>
    <tr><td> ' </td><td> &#039;</td></tr>
    <tr><td> ( </td><td> &#040;</td></tr>
    <tr><td> ) </td><td> &#041;</td></tr>
    <tr><td> * </td><td> &#042;</td></tr>
    <tr><td> + </td><td> &#043; </td></tr>
    <tr><td> , </td><td> &#044; </td></tr>
    <tr><td> - </td><td> &#045; </td></tr>
    <tr><td> . </td><td> &#046; </td></tr>
    <tr><td> / </td><td> &#047; </td></tr>
    <tr><td> : </td><td> &#058;</td></tr>
    <tr><td> ; </td><td> &#059;</td></tr>
    <tr><td> = </td><td> &#061;</td></tr>
    <tr><td> ? </td><td> &#063;</td></tr>
    <tr><td> @ </td><td> &#064;</td></tr>
    <tr><td> [ </td><td> &#091;</td></tr>
    <tr><td> \ </td><td> &#092;</td></tr>
    <tr><td> ] </td><td> &#093;</td></tr>
    <tr><td> ^ </td><td> &#094;</td></tr>
    <tr><td> _ </td><td> &#095;</td></tr>
    <tr><td> ` </td><td> &#096;</td></tr>
    <tr><td> { </td><td> &#123;</td></tr>
    <tr><td> | </td><td> &#124;</td></tr>
    <tr><td> } </td><td> &#125;</td></tr>
    <tr><td> ~ </td><td> &#126;</td></tr>
    </table>

    <P>Note that JSTL's {@code <c:out>} escapes <em>only the first
    five</em> of the above characters.
   */
   public static String forHTML(String aText){
      final StringBuilder result = new StringBuilder();
      final StringCharacterIterator iterator = new StringCharacterIterator(aText);
      char character =  iterator.current();
      while (character != CharacterIterator.DONE) {
         switch (character) {
            case '<' :  result.append("&lt;");  break;
            case '>' :  result.append("&gt;");  break;
            case '&' :  result.append("&amp;");  break;
            case '\"' : result.append("&quot;");  break;
            case '\t' : addCharEntity(9, result);  break;
            case '!' :  addCharEntity(33, result);  break;
            case '#' :  addCharEntity(35, result);  break;
            case '$' :  addCharEntity(36, result);  break;
            case '%' :  addCharEntity(37, result);  break;
            case '\'' : addCharEntity(39, result);  break;
            case '(' :  addCharEntity(40, result);  break;
            case ')' :  addCharEntity(41, result);  break;
            case '*' :  addCharEntity(42, result);  break;
            case '+' :  addCharEntity(43, result);  break;
            case ',' :  addCharEntity(44, result);  break;
            case '-' :  addCharEntity(45, result);  break;
            case '.' :  addCharEntity(46, result);  break;
            case '/' :  addCharEntity(47, result);  break;
            case ':' :  addCharEntity(58, result);  break;
            case ';' :  addCharEntity(59, result);  break;
            case '=' :  addCharEntity(61, result);  break;
            case '?' :  addCharEntity(63, result);  break;
            case '@' :  addCharEntity(64, result);  break;
            case '[' :  addCharEntity(91, result);  break;
            case '\\' : addCharEntity(92, result);  break;
            case ']' :  addCharEntity(93, result);  break;
            case '^' :  addCharEntity(94, result);  break;
            case '_' :  addCharEntity(95, result);  break;
            case '`' :  addCharEntity(96, result);  break;
            case '{' :  addCharEntity(123, result);  break;
            case '|' :  addCharEntity(124, result);  break;
            case '}' :  addCharEntity(125, result);  break;
            case '~' :  addCharEntity(126, result);  break;
            default  :  result.append(character);
         } // switch

       character = iterator.next();
     } // while

     return result.toString();
  } // forHTML


  /**
   Escape all ampersand characters in a URL.

   <P>Replaces all <tt>'&'</tt> characters with <tt>'&amp;'</tt>.

  <P>An ampersand character may appear in the query string of a URL.
   The ampersand character is indeed valid in a URL.
   <em>However, URLs usually appear as an <tt>HREF</tt> attribute, and
   such attributes have the additional constraint that ampersands
   must be escaped.</em>

   <P>The JSTL <c:url> tag does indeed perform proper URL encoding of
   query parameters. But it does not, in general, produce text which
   is valid as an <tt>HREF</tt> attribute, simply because it does
   not escape the ampersand character. This is a nuisance when
   multiple query parameters appear in the URL, since it requires a little
   extra work.
  */
  public static String forHrefAmpersand(String aURL){
    return aURL.replace("&", "&amp;");
  } // forHrefAmpersand

  /**
    Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.

    <P>Used to ensure that HTTP query strings are in proper form, by escaping
    special characters such as spaces.

    <P>It is important to note that if a query string appears in an <tt>HREF</tt>
    attribute, then there are two issues - ensuring the query string is valid HTTP
    (it is URL-encoded), and ensuring it is valid HTML (ensuring the
    ampersand is escaped).
   */
   public static String forURL(String aURLFragment){
     String result = null;
     try {
       result = URLEncoder.encode(aURLFragment, "UTF-8");
     } catch (UnsupportedEncodingException ex){
       throw new RuntimeException("UTF-8 not supported", ex);
     } // try - catch

     return result;
   } // forURL

  /**
   Escape characters for text appearing as XML data, between tags.

   <P>The following characters are replaced with corresponding character entities :
   <table border='1' cellpadding='3' cellspacing='0'>
   <tr><th> Character </th><th> Encoding </th></tr>
   <tr><td> < </td><td> &lt; </td></tr>
   <tr><td> > </td><td> &gt; </td></tr>
   <tr><td> & </td><td> &amp; </td></tr>
   <tr><td> " </td><td> &quot;</td></tr>
   <tr><td> ' </td><td> &#039;</td></tr>
   </table>

   <P>Note that JSTL's {@code <c:out>} escapes the exact same set of
   characters as this method. <span class='highlight'>That is, {@code <c:out>}
    is good for escaping to produce valid XML, but not for producing safe
    HTML.</span>
  */
  public static String forXML(String aText){
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      switch (character) {
         case '<' :  result.append("&lt;");  break;
         case '>' :  result.append("&gt;");  break;
         case '\"' : result.append("&quot;");  break;
         case '\'' : result.append("&#039;");  break;
         case '&' :  result.append("&amp;");  break;
         default  :  result.append(character);
      } // switch

      character = iterator.next();
    } // while

    return result.toString();
  } // forXML

  /**
   Escapes characters for text appearing as data in the
   <a href='http://www.json.org/'>Javascript Object Notation</a>
   (JSON) data interchange format.

   <P>The following commonly used control characters are escaped :
   <table border='1' cellpadding='3' cellspacing='0'>
   <tr><th> Character </th><th> Escaped As </th></tr>
   <tr><td> " </td><td> \" </td></tr>
   <tr><td> \ </td><td> \\ </td></tr>
   <tr><td> / </td><td> \/ </td></tr>
   <tr><td> back space </td><td> \b </td></tr>
   <tr><td> form feed </td><td> \f </td></tr>
   <tr><td> line feed </td><td> \n </td></tr>
   <tr><td> carriage return </td><td> \r </td></tr>
   <tr><td> tab </td><td> \t </td></tr>
   </table>

   <P>See <a href='http://www.ietf.org/rfc/rfc4627.txt'>RFC 4627</a> for more information.
  */
  public static String forJSON(String aText){
    final StringBuilder result = new StringBuilder();
    StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != StringCharacterIterator.DONE){
      if( character == '\"' ){
        result.append("\\\"");
      } else if(character == '\\') {
        result.append("\\\\");
      } else if(character == '/') {
        result.append("\\/");
      } else if(character == '\b') {
        result.append("\\b");
      } else if(character == '\f') {
        result.append("\\f");
      } else if(character == '\n') {
        result.append("\\n");
      } else if(character == '\r') {
        result.append("\\r");
      } else if(character == '\t') {
        result.append("\\t");
      } else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      } // if -else if - else
      character = iterator.next();
    } // while
    return result.toString();
  } // forJSON

  /**
   Return <tt>aText</tt> with all <tt>'<'</tt> and <tt>'>'</tt> characters
   replaced by their escaped equivalents.
  */
  public static String toDisableTags(String aText){
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      if (character == '<') {
        result.append("&lt;");
      } else if (character == '>') {
        result.append("&gt;");
      } else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      } // if - else if - else

      character = iterator.next();
    } // while

    return result.toString();
  } // toDisableTags


  /**
   Replace characters having special meaning in regular expressions
   with their escaped equivalents, preceded by a '\' character.

   <P>The escaped characters include :
  <ul>
  <li>.
  <li>\
  <li>?, * , and +
  <li>&
  <li>:
  <li>{ and }
  <li>[ and ]
  <li>( and )
  <li>^ and $
  </ul>
  */
  public static String forRegex(String aRegexFragment){
    final StringBuilder result = new StringBuilder();

    final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE) {
      /*
       All literals need to have backslashes doubled.
      */
      if (character == '.') {
        result.append("\\.");
      } else if (character == '\\') {
        result.append("\\\\");
      } else if (character == '?') {
        result.append("\\?");
      } else if (character == '*') {
        result.append("\\*");
      } else if (character == '+') {
        result.append("\\+");
      } else if (character == '&') {
        result.append("\\&");
      } else if (character == ':') {
        result.append("\\:");
      } else if (character == '{') {
        result.append("\\{");
      } else if (character == '}') {
        result.append("\\}");
      } else if (character == '[') {
        result.append("\\[");
      } else if (character == ']') {
        result.append("\\]");
      } else if (character == '(') {
        result.append("\\(");
      } else if (character == ')') {
        result.append("\\)");
      } else if (character == '^') {
        result.append("\\^");
      } else if (character == '$') {
        result.append("\\$");
      } else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      } // if - else if - else

      character = iterator.next();
    } // while

    return result.toString();
  } // forRegex

  /**
   Escape <tt>'$'</tt> and <tt>'\'</tt> characters in replacement strings.

   <P>Synonym for <tt>Matcher.quoteReplacement(String)</tt>.

   <P>The following methods use replacement strings which treat
   <tt>'$'</tt> and <tt>'\'</tt> as special characters:
   <ul>
   <li><tt>String.replaceAll(String, String)</tt>
   <li><tt>String.replaceFirst(String, String)</tt>
   <li><tt>Matcher.appendReplacement(StringBuffer, String)</tt>
   </ul>

   <P>If replacement text can contain arbitrary characters, then you
   will usually need to escape that text, to ensure special characters
   are interpreted literally.
  */
  public static String forReplacementString(String aInput){
    return Matcher.quoteReplacement(aInput);
  } // forReplacementString

  /**
   Disable all <tt><SCRIPT></tt> tags in <tt>aText</tt>.

   <P>Insensitive to case.
  */
  public static String forScriptTagsOnly(String aText){
    String result = null;
    Matcher matcher = SCRIPT.matcher(aText);
    result = matcher.replaceAll("&lt;SCRIPT>");
    matcher = SCRIPT_END.matcher(result);
    result = matcher.replaceAll("&lt;/SCRIPT>");
    return result;
  } // forScriptTagsOnly

  // PRIVATE //

  private EscapeChars() {
    //empty - prevent construction
  }

  private static final Pattern SCRIPT = Pattern.compile( "<SCRIPT>", Pattern.CASE_INSENSITIVE);
  private static final Pattern SCRIPT_END = Pattern.compile("</SCRIPT>", Pattern.CASE_INSENSITIVE);

  private static void addCharEntity(Integer aIdx, StringBuilder aBuilder){
    String padding = "";
    if (aIdx <= 9) {
       padding = "00";
    } else if (aIdx <= 99) {
      padding = "0";
    } else {
      //no prefix
    } // if - else if - else

    String number = padding + aIdx.toString();
    aBuilder.append("&#").append(number).append(";");
  } // addCharEntity

} // EscapeChars

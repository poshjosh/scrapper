package com.scrapper.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.StringCharacterIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





























































































public final class EscapeChars
{
  public static String forHTML(String aText)
  {
    StringBuilder result = new StringBuilder();
    StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != 65535) {
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else if (character == '&') {
        result.append("&amp;");
      }
      else if (character == '"') {
        result.append("&quot;");
      }
      else if (character == '\t') {
        addCharEntity(Integer.valueOf(9), result);
      }
      else if (character == '!') {
        addCharEntity(Integer.valueOf(33), result);
      }
      else if (character == '#') {
        addCharEntity(Integer.valueOf(35), result);
      }
      else if (character == '$') {
        addCharEntity(Integer.valueOf(36), result);
      }
      else if (character == '%') {
        addCharEntity(Integer.valueOf(37), result);
      }
      else if (character == '\'') {
        addCharEntity(Integer.valueOf(39), result);
      }
      else if (character == '(') {
        addCharEntity(Integer.valueOf(40), result);
      }
      else if (character == ')') {
        addCharEntity(Integer.valueOf(41), result);
      }
      else if (character == '*') {
        addCharEntity(Integer.valueOf(42), result);
      }
      else if (character == '+') {
        addCharEntity(Integer.valueOf(43), result);
      }
      else if (character == ',') {
        addCharEntity(Integer.valueOf(44), result);
      }
      else if (character == '-') {
        addCharEntity(Integer.valueOf(45), result);
      }
      else if (character == '.') {
        addCharEntity(Integer.valueOf(46), result);
      }
      else if (character == '/') {
        addCharEntity(Integer.valueOf(47), result);
      }
      else if (character == ':') {
        addCharEntity(Integer.valueOf(58), result);
      }
      else if (character == ';') {
        addCharEntity(Integer.valueOf(59), result);
      }
      else if (character == '=') {
        addCharEntity(Integer.valueOf(61), result);
      }
      else if (character == '?') {
        addCharEntity(Integer.valueOf(63), result);
      }
      else if (character == '@') {
        addCharEntity(Integer.valueOf(64), result);
      }
      else if (character == '[') {
        addCharEntity(Integer.valueOf(91), result);
      }
      else if (character == '\\') {
        addCharEntity(Integer.valueOf(92), result);
      }
      else if (character == ']') {
        addCharEntity(Integer.valueOf(93), result);
      }
      else if (character == '^') {
        addCharEntity(Integer.valueOf(94), result);
      }
      else if (character == '_') {
        addCharEntity(Integer.valueOf(95), result);
      }
      else if (character == '`') {
        addCharEntity(Integer.valueOf(96), result);
      }
      else if (character == '{') {
        addCharEntity(Integer.valueOf(123), result);
      }
      else if (character == '|') {
        addCharEntity(Integer.valueOf(124), result);
      }
      else if (character == '}') {
        addCharEntity(Integer.valueOf(125), result);
      }
      else if (character == '~') {
        addCharEntity(Integer.valueOf(126), result);

      }
      else
      {
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  


















  public static String forHrefAmpersand(String aURL)
  {
    return aURL.replace("&", "&amp;");
  }
  










  public static String forURL(String aURLFragment)
  {
    String result = null;
    try {
      result = URLEncoder.encode(aURLFragment, "UTF-8");
    }
    catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
  }
  

















  public static String forXML(String aText)
  {
    StringBuilder result = new StringBuilder();
    StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != 65535) {
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else if (character == '"') {
        result.append("&quot;");
      }
      else if (character == '\'') {
        result.append("&#039;");
      }
      else if (character == '&') {
        result.append("&amp;");

      }
      else
      {
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  



  public static String toDisableTags(String aText)
  {
    StringBuilder result = new StringBuilder();
    StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != 65535) {
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");

      }
      else
      {
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  

















  public static String forRegex(String aRegexFragment)
  {
    StringBuilder result = new StringBuilder();
    
    StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
    

    char character = iterator.current();
    while (character != 65535)
    {


      if (character == '.') {
        result.append("\\.");
      }
      else if (character == '\\') {
        result.append("\\\\");
      }
      else if (character == '?') {
        result.append("\\?");
      }
      else if (character == '*') {
        result.append("\\*");
      }
      else if (character == '+') {
        result.append("\\+");
      }
      else if (character == '&') {
        result.append("\\&");
      }
      else if (character == ':') {
        result.append("\\:");
      }
      else if (character == '{') {
        result.append("\\{");
      }
      else if (character == '}') {
        result.append("\\}");
      }
      else if (character == '[') {
        result.append("\\[");
      }
      else if (character == ']') {
        result.append("\\]");
      }
      else if (character == '(') {
        result.append("\\(");
      }
      else if (character == ')') {
        result.append("\\)");
      }
      else if (character == '^') {
        result.append("\\^");
      }
      else if (character == '$') {
        result.append("\\$");

      }
      else
      {
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  
















  public static String forReplacementString(String aInput)
  {
    return Matcher.quoteReplacement(aInput);
  }
  




  public static String forScriptTagsOnly(String aText)
  {
    String result = null;
    Matcher matcher = SCRIPT.matcher(aText);
    result = matcher.replaceAll("&lt;SCRIPT>");
    matcher = SCRIPT_END.matcher(result);
    result = matcher.replaceAll("&lt;/SCRIPT>");
    return result;
  }
  






  private static final Pattern SCRIPT = Pattern.compile("<SCRIPT>", 2);
  

  private static final Pattern SCRIPT_END = Pattern.compile("</SCRIPT>", 2);
  

  private static void addCharEntity(Integer aIdx, StringBuilder aBuilder)
  {
    String padding = "";
    if (aIdx.intValue() <= 9) {
      padding = "00";
    }
    else if (aIdx.intValue() <= 99) {
      padding = "0";
    }
    


    String number = padding + aIdx.toString();
    aBuilder.append("&#" + number + ";");
  }
}

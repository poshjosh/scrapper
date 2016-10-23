package com.scrapper.util;

import com.bc.util.XLogger;
import com.bc.webdatex.formatter.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;














public class Translator
{
  public static final String WORDS = "words";
  private static Map<Locale, Formatter<String>> formatters;
  
  public static String translate(Locale to, String text)
  {
    return translate(Locale.getDefault(), to, text);
  }
  
  public static String translate(String fromLang, String toLang, String word)
  {
    return translate(getLocaleForLanguage(fromLang), getLocaleForLanguage(toLang), word);
  }
  



  public static String translate(Locale from, Locale to, String text)
  {
    checkBundle(from);
    
    checkBundle(to);
    


    String regex = "\\b[\\p{L}\\p{Nd}]+?\\b";
    

    Pattern pattern = Pattern.compile(regex);
    
    Matcher matcher = pattern.matcher(text);
    
    XLogger.getInstance().log(Level.FINER, "Translating from: {0}, Translatring to: {1}", Translator.class, from, to);
    

    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
      String word = matcher.group();
      String tran = translateWord(from, to, word);
      
      XLogger.getInstance().log(Level.FINER, "{0}={1}", Translator.class, word, tran);
      
      if (tran != null)
        matcher.appendReplacement(buf, tran);
    }
    matcher.appendTail(buf);
    
    return buf.toString();
  }
  
  public static String translateWord(Locale to, String word)
  {
    return translateWord(Locale.getDefault(), to, word);
  }
  
  public static String translateWord(String fromLang, String toLang, String word)
  {
    return translateWord(getLocaleForLanguage(fromLang), getLocaleForLanguage(toLang), word);
  }
  



  public static String translateWord(Locale from, Locale to, String word)
  {
    ResourceBundle frmBundle = checkBundle(from);
    
    ResourceBundle toBundle = checkBundle(to);
    
    word = word.toLowerCase(from);
    
    Formatter<String> fromFmt = getFormatter(from);
    
    if (fromFmt != null) {
      word = (String)fromFmt.format(word);
    }
    
    Set<String> frmKeys = frmBundle.keySet();
    
    String key = null;
    
    for (String frmKey : frmKeys)
    {
      String val = frmBundle.getString(frmKey);
      XLogger.getInstance().log(Level.FINEST, "Word: {0}, Key: {1}, Value: {2}", Translator.class, word, frmKey, val);
      

      if (val != null)
      {
        if (val.equals(word)) {
          key = frmKey;
          break;
        }
      }
    }
    if (key == null) return null;
    XLogger.getInstance().log(Level.FINER, "Word: {0}, Key: {1}", Translator.class, word, key);
    
    return toBundle.getString(key);
  }
  
  public static boolean isSupported(Locale locale) {
    try {
      ResourceBundle.getBundle("words", locale);
      return true;
    } catch (MissingResourceException e) {}
    return false;
  }
  
  public static Locale getLocaleForLanguage(String language)
  {
    Locale locale = null;
    if (language != null) {
      Locale[] arr = Locale.getAvailableLocales();
      for (Locale e : arr) {
        if ((language.equals(e.getISO3Language())) || (language.equals(e.getLanguage())))
        {
          if ((e.getCountry() == null) || (e.getCountry().isEmpty())) {
            locale = e;
            break;
          }
        }
      }
    }
    return locale;
  }
  
  private static ResourceBundle checkBundle(Locale locale) {
    return ResourceBundle.getBundle("words", locale);
  }
  
  private static Formatter<String> getFormatter(Locale locale) {
    if (formatters == null) {
      initDefaultFormatters();
    }
    return (Formatter)formatters.get(locale);
  }
  
  private static void initDefaultFormatters() {
    registerFormatter(Locale.GERMAN, new DeutschFormatter());
  }
  
  public static Formatter<String> registerFormatter(Locale locale, Formatter<String> f) {
    if (formatters == null) {
      formatters = new HashMap()
      {
        public Formatter<String> put(Locale key, Formatter<String> value) {
          if ((key == null) || (value == null)) throw new NullPointerException();
          return (Formatter)super.put(key, value);
        }
      };
    }
    return (Formatter)formatters.put(locale, f);
  }
  
  public static Formatter<String> unregisterFormatter(Locale locale) {
    if (formatters == null) {
      return null;
    }
    return (Formatter)formatters.remove(locale);
  }
  




  public static class DeutschFormatter
    implements Formatter<String>
  {
    public String format(String e)
    {
      e = e.toLowerCase(Locale.GERMAN);
      e = e.replace("ä", "ae");
      e = e.replace("ö", "oe");
      e = e.replace("ü", "ue");
      e = e.replace("ß", "ss");
      return e;
    }
  }
}

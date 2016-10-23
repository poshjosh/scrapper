package com.scrapper.formatter;

import com.bc.io.CharFileIO;
import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import com.bc.webdatex.formatter.Formatter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
















public class GoogleTranslate
  implements Formatter<String>
{
  private String inputLang;
  private String outputLang;
  private CharFileIO fileIO;
  private ConnectionManager mgr;
  
  public GoogleTranslate()
  {
    this.mgr = new ConnectionManager();
    this.mgr.setGenerateRandomUserAgent(true);
    this.fileIO = new CharFileIO("UTF-8");
  }
  

  public String format(String input)
  {
    StringBuilder urlStr = getURL();
    try
    {
      input = URLEncoder.encode(input, this.fileIO.getOutputCharset());
    } catch (UnsupportedEncodingException | RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, "Error encoding: "+input, getClass(), e);
    }
    
    urlStr.append('/').append(input);
    
    URL url = null;
    try {
      url = new URL(urlStr.toString());
    } catch (MalformedURLException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    
    CharSequence output = null;
    
    if (url != null) {
      try {
        InputStream in = this.mgr.getInputStream(url, getDefaultProperties());
        output = this.fileIO.readChars(in);
      } catch (IOException e) {
        XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
      }
    }
    
    return output == null ? null : output.toString();
  }
  
  public StringBuilder getURL() {
    StringBuilder builder = new StringBuilder("http://translate.google.com/");
    builder.append('#').append(getOutputLang());
    builder.append('/').append(getInputLang());
    return builder;
  }
  
  private Map<String, Object> getDefaultProperties() {
    HashMap<String, Object> props = new HashMap();
    props.put("Content-Type", "application/x-www-form-urlencoded");
    props.put("Accept-Charset", this.fileIO.getOutputCharset());
    return props;
  }
  
  public String getInputLang() {
    return this.inputLang;
  }
  
  public void setInputLang(String inputLang) {
    this.inputLang = inputLang;
  }
  
  public String getOutputLang() {
    return this.outputLang;
  }
  
  public void setOutputLang(String outputLang) {
    this.outputLang = outputLang;
  }
  
  public String getCharset() {
    return this.fileIO.getOutputCharset();
  }
  
  public void setCharset(String charset) {
    this.fileIO.setInputCharset(charset);
    this.fileIO.setOutputCharset(charset);
  }
}

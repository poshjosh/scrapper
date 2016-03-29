package com.scrapper;

import com.bc.io.CharFileIO;
import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;











public class LoginManager
  extends ConnectionManager
{
  public LoginManager() {}
  
  public LoginManager(int maxRetrials, long retrialInterval)
  {
    super(maxRetrials, retrialInterval);
  }
  





  public List<String> login(String target, Map<String, String> outputParams)
    throws MalformedURLException, IOException
  {
    URL loginURL = new URL(target);
    XLogger.getInstance().log(Level.FINE, "Login url: {0}", getClass(), target);
    
    HashMap<String, Object> connProps = new HashMap();
    String charset = "UTF-8";
    connProps.put("Accept-Charset", charset);
    connProps.put("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
    
    this.setGenerateRandomUserAgent(true);
    
    setGetCookies(true);
    
    InputStream in = getInputStream(loginURL, connProps, outputParams, true);
    
    int responseCode = getResponseCode();
    XLogger.getInstance().log(Level.FINE, "Login responseCode: {0}", getClass(), Integer.valueOf(responseCode));
    
    if (responseCode != 200) {
      StringBuilder builder = new StringBuilder("Login Failed. Server response: ");
      builder.append(getResponseCode());
      builder.append(' ').append(getResponseMessage());
      CharFileIO ioMgr = new CharFileIO(charset);
      CharSequence cs = ioMgr.readChars(in);
      XLogger.getInstance().log(Level.FINER, "Login Raw response:\n{0}", getClass(), cs);
      throw new IOException(builder.toString());
    }
    
    return getCookies();
  }
}

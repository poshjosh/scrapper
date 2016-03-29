package com.scrapper;

import com.bc.io.CharFileIO;
import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import com.scrapper.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public abstract class DataUploader implements Serializable
{
  private String message;
  private URL url;
  private CharFileIO ioMgr;
  private transient ConnectionManager connMgr;
  
  public DataUploader()
  {
    init(null);
  }
  
  public DataUploader(URL aURL) { init(aURL); }
  
  private void init(URL aURL)
  {
    setUrl(aURL);
    XLogger.getInstance().log(Level.FINE, "URL: {0}", getClass(), aURL);
    
    this.ioMgr = new CharFileIO();
    
    initConnectionManager();
  }
  
  public abstract Map getUploadParameters();
  
  protected void initConnectionManager() {
    this.connMgr = new ConnectionManager(5, 3000L);
    this.connMgr.setAddCookies(true);
    this.connMgr.setGenerateRandomUserAgent(true);
  }
  
  private void writeObject(ObjectOutputStream o) throws IOException
  {
    o.defaultWriteObject();
  }
  
  private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException
  {
    o.defaultReadObject();
    initConnectionManager();
  }
  
  public void stop(long timeout) {
    this.connMgr.stop(timeout);
    XLogger.getInstance().log(Level.FINE, "Stopped", getClass());
  }
  
  public boolean isRunning() {
    return this.connMgr.isRunning();
  }
  
  public boolean isStopped() {
    return this.connMgr.isStopped();
  }
  
  public void setStopped(boolean stopped) {
    this.connMgr.setStopped(stopped);
  }
  
  public void reset(URL aURL) {
    this.url = aURL;
    reset();
  }
  
  public void reset() {
    this.message = null;
    if (this.connMgr != null) {
      this.connMgr.reset();
    }
  }
  




  public int uploadRecord(Map parameters)
  {
    int responseCode = -1;
    
    try
    {
      responseCode = doUploadRecord(parameters);
    } finally { 
      if (responseCode == -1)
      {
        String msg = getMessage();
        
        String extraDetails = (String)parameters.get("extraDetails");
        
        if (extraDetails != null) {
          String[] parts = extraDetails.split("=");
          if (parts.length != 2) {
            XLogger.getInstance().log(Level.WARNING, "Expected format: url=[actualUrlLink]. Found: {0}", getClass(), extraDetails);
          } else {
            XLogger.getInstance().log(Level.WARNING, "Failed to save: {0}", getClass(), parts[1]);
          }
        }
        


        if ((msg != null) && ((msg.contains("java.net.")) || (msg.contains("java.io.")))) {
          XLogger.getInstance().log(Level.WARNING, "{0}. {1}", getClass(), msg);
        }
      }
    }
    
    return responseCode;
  }
  



  private int doUploadRecord(Map parameters)
  {
    reset();
    
    log(parameters);
    
    if ((parameters == null) || (parameters.isEmpty())) {
      throw new IllegalArgumentException();
    }
    
    int responseCode = -1;
    
    URLConnection connection = null;
    OutputStream outputStream = null;
    
    parameters.putAll(getUploadParameters());
    
    try
    {
      HashMap<String, String> files = new HashMap();
      HashMap<String, String> nonFiles = new HashMap();
      
      for (Object entryObj : parameters.entrySet())
      {
        Map.Entry entry = (Map.Entry)entryObj;
        
        String key = entry.getKey().toString();
        
        if (entry.getValue() == null) {
          throw new NullPointerException("Value is null, for key: " + key);
        }
        
        String val = entry.getValue().toString();
        
        if (key.startsWith("image"))
        {
          files.put(key, val);
        }
        else
        {
          nonFiles.put(key, val);
        }
      }
      
      XLogger.getInstance().log(Level.FINE, "After UPDATE Parameter keys: {0}", getClass(), parameters.keySet());
      

      XLogger.getInstance().log(Level.FINER, "After UPDATE Parameters: {0}", getClass(), parameters);
      

      boolean multipart = (isSendMultipart()) && (!files.isEmpty());
      
      XLogger.getInstance().log(Level.FINE, "Send multipart: {0}", getClass(), Boolean.valueOf(multipart));
      
      try
      {
        String charset = getCharset();
        
        if (multipart)
        {

          String BOUNDARY = Long.toHexString(System.currentTimeMillis());
          
          connection = this.connMgr.openConnection(this.url, true, true, charset, "Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
          
          outputStream = this.connMgr.getOutputStream(connection);
          
          postMultipart(outputStream, nonFiles, files, isRemoteFiles(), BOUNDARY);
          
          XLogger.getInstance().log(Level.FINE, "Done posting multipart.", getClass());
        }
        else
        {
          if (!files.isEmpty()) {
            parameters.put("images", Boolean.valueOf(true));
          }
          XLogger.getInstance().log(Level.FINE, "Charset: {0}, URL: {1}", getClass(), charset, this.url);
          
          connection = this.connMgr.openConnection(this.url, true, true, charset, "Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
          
          outputStream = this.connMgr.getOutputStream(connection);
          
          String queryString = getQueryString(parameters, charset);
          
          outputStream.write(queryString.getBytes(charset));
          
          XLogger.getInstance().log(Level.FINE, "Done posting query.", getClass());
        }
        

        if (outputStream != null) { try { outputStream.close();
          } catch (IOException e) { XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
          }
        }
        
        responseCode = this.connMgr.getResponseCode();
      }
      finally
      {
        if (outputStream != null) { try { outputStream.close();
          } catch (IOException e) { XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
          }
        }
      }
      
      XLogger.getInstance().log(Level.INFO, "Server returned response code: {0}", getClass(), Integer.valueOf(responseCode));
      

      if (responseCode >= 400)
      {


        readResponse();
      }
    }
    catch (IOException e)
    {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    finally
    {
      this.connMgr.disconnect();
    }
    
    return responseCode;
  }
  
  public void postMultipart(OutputStream output, Map<String, String> nonFiles, Map<String, String> files, boolean remoteFiles, String boundary)
    throws IOException
  {
    String charset = getCharset();
    
    PrintWriter writer = null;
    

    try
    {
      writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
      
      if (nonFiles != null) {
        postNonFiles(writer, nonFiles, boundary);
      }
      
      postFiles(writer, output, files, remoteFiles, boundary);
    }
    finally {
      if (writer != null) writer.close();
    }
  }
  
  protected void postFiles(PrintWriter writer, OutputStream output, Map<String, String> files, boolean remote, String boundary) throws IOException
  {
    for (String name : files.keySet())
      postFile(writer, output, name, (String)files.get(name), remote, boundary);
  }
  
  protected void postNonFiles(PrintWriter writer, Map nonFiles, String boundary) {
    for (Object name : nonFiles.keySet())
      postNonFile(writer, name.toString(), nonFiles.get(name), boundary);
  }
  
  protected void postNonFile(PrintWriter writer, String name, Object value, String boundary) {
    XLogger.getInstance().log(Level.FINER, "{0}. Posting non file: {1}={2}", getClass(), name, value);
    String CRLF = "\r\n";
    
    String charset = getCharset();
    

    writer.append("--" + boundary).append("\r\n");
    writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append("\r\n");
    writer.append("Content-Type: text/plain; charset=" + charset).append("\r\n");
    writer.append("\r\n");
    writer.append(value.toString()).append("\r\n").flush();
  }
  
  protected void postFile(PrintWriter writer, OutputStream output, String name, String filePath, boolean remote, String boundary) throws IOException
  {
    XLogger.getInstance().log(Level.FINER, "{0}. Posting file: {1}={2}", getClass(), name, filePath);
    
    File binaryFile = new File(filePath);
    
    String CRLF = "\r\n";
    

    try
    {
      writer.append("--" + boundary).append("\r\n");
      writer.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + binaryFile.getName() + "\"").append("\r\n");
      writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append("\r\n");
      writer.append("Content-Transfer-Encoding: binary").append("\r\n");
      writer.append("\r\n").flush();
      
      InputStream input = null;
      try {
        if (remote)
        {
          URL remoteUrl = new URL(filePath);
          
          input = this.connMgr.getInputStream(remoteUrl);
        }
        else
        {
          input = new FileInputStream(binaryFile);
        }
        
        this.ioMgr.copyStream(input, output);
        

        output.flush();
      }
      finally
      {
        if (input != null) try { input.close();
          } catch (IOException e) { System.out.println(getClass().getName() + ". " + e);
          }
        this.connMgr.disconnect();
      }
      
      writer.append("\r\n").flush();
      

      writer.append("--" + boundary + "--").append("\r\n");
    }
    catch (IOException e) {
      XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
    }
  }
  
  private boolean readResponse() throws IOException {
    XLogger.getInstance().entering(getClass(), "@readResponse()", null);
    
    boolean success;
    
    List<String> cookies = this.connMgr.getCookies();

    this.connMgr.setGetCookies(cookies == null);
    
    if ((cookies != null) && (!cookies.isEmpty())) {
      XLogger.getInstance().log(Level.FINE, "Cookies: {0}", getClass(), cookies);
    }
    InputStream in = null;
    try
    {
      in = this.connMgr.getInputStream();
      
      CharSequence cs = this.ioMgr.readChars(in);
      
      this.message = cs.toString();
      
      XLogger.getInstance().log(Level.FINER, "Response: {0}", getClass(), cs);
      

      if (in != null) { try { in.close();
        } catch (IOException e) { XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
        }
      }
      



      success = this.message.contains("<SUCCESS>");
    }
    finally
    {
      if (in != null) { try { in.close();
        } catch (IOException e) { XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
        }
      }
    }

    if (success) {
      XLogger.getInstance().logger(getClass()).info(this.message);
    } else {
      StringBuilder logMsg = new StringBuilder();
      try
      {
        appendTag("id", "myMessage", logMsg);
        if (logMsg.length() == 0)
        {
          appendTag("class", "handWriting errorPageData", logMsg);
        }
      } catch (ParserException e) {
        XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
        if (this.message.contains("<ERROR>")) {
          logMsg.append("Error: ").append(this.message);
        } else {
          logMsg.append("Message: ").append(this.message);
        }
      }
      String s = logMsg.toString().trim();
      if (!s.isEmpty()) {
        Logger.getLogger(getClass().getName()).warning(logMsg.toString());
      } else {
        Logger.getLogger(getClass().getName()).warning(this.message);
      }
    }
    
    return success;
  }
  
  private void appendTag(String attrName, String attrValue, StringBuilder appendTo) throws ParserException
  {
    Parser parser = new Parser(this.message);
    HasAttributeFilter filter = new HasAttributeFilter(attrName, attrValue);
    NodeList list = parser.parse(filter);
    for (int i = 0; i < list.size(); i++) {
      String s = list.elementAt(i).toPlainTextString();
      if ((s != null) && (!s.trim().isEmpty()))
      {

        appendTo.append(s);
        appendTo.append("\n");
      }
    }
  }
  
  private String getQueryString(Map parameters, String charset) throws IOException {
    StringBuilder output = new StringBuilder();
    
    HashMap map = new HashMap(parameters);
    
    ArrayList nulls = new ArrayList();
    nulls.add(null);
    map.values().removeAll(nulls);
    
    Iterator iter = map.keySet().iterator();
    
    while (iter.hasNext())
    {
      Object key = iter.next();
      Object val = map.get(key);
      
      output.append(key);
      output.append('=');
      output.append(URLEncoder.encode(val.toString(), charset));
      
      if (iter.hasNext()) {
        output.append('&');
      }
    }
    XLogger.getInstance().log(Level.FINER, "Query: {0}", getClass(), output);
    return output.toString();
  }
  
  private void log(Map parameters) {
    XLogger xlog = XLogger.getInstance();
    Object toLog;
    if (xlog.isLoggable(Level.FINER, getClass())) {
      toLog = parameters; } else { 
      if (xlog.isLoggable(Level.FINE, getClass())) {
        toLog = Util.toString(parameters, 70); } else { 
        if (xlog.isLoggable(Level.INFO, getClass())) {
          toLog = parameters.keySet();
        } else
          toLog = null;
      } }
    if (toLog != null)
    {
      xlog.log(Level.INFO, "Parameters: {0}", getClass(), toLog);
    }
  }
  
  public String getMessage()
  {
    return this.message;
  }
  
  public boolean isAppendLogo() {
    Object oval = getUploadParameters().get("appendLogo");
    return oval == null ? false : Boolean.parseBoolean(oval.toString());
  }
  
  public String getCharset() {
    return (String)getUploadParameters().get("charset");
  }
  
  public boolean isCheckIfRecordExists() {
    return ((Boolean)getUploadParameters().get("checkIfRecordExists")).booleanValue();
  }
  
  public String getDbActionType() {
    return (String)getUploadParameters().get("dbActionType");
  }
  
  public boolean isLogin() {
    return ((Boolean)getUploadParameters().get("login")).booleanValue();
  }
  
  public boolean isRemoteFiles() {
    return ((Boolean)getUploadParameters().get("remoteFiles")).booleanValue();
  }
  
  public boolean isSendMultipart() {
    return ((Boolean)getUploadParameters().get("sendMultipart")).booleanValue();
  }
  

  public URL getUrl()
  {
    return this.url;
  }
  
  public void setUrl(URL url) {
    this.url = url;
  }
}

package com.scrapper;

import com.bc.util.XLogger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppProperties
{
  public static final String TARGETNODES_TO_TRACK = "targetNodesToTrack";
  public static final String ABOUT = "about";
  public static final String LOG_LEVEL = "logLevel";
  public static final String BATCH_SIZE = "batchSize";
  public static final String BATCH_INTERVAL = "batchInterval";
  public static final String MAXCONCURRENT_PROCESSES = "maxConcurrentProcesses";
  public static final String BACKGROUNDPROCESS_MEMORY_FACTOR = "backgroundProcess.memoryFactor";
  public static final String BACKGROUNDPROCESS_MEMORY_MONITOR_INTERVAL = "backgroundProcess.memoryMonitorInterval";
  public static final String BACKGROUNDPROCESS_MINIMUM_MEMORY = "backgroundProcess.minimumMemory";
  public static final String BACKGROUNDPROCESS_CONTINUE_PREVIOUS = "backgroundProcess.continuePrevious";
  public static final String MESSAGE_DISPLAY_INTERVAL = "messageDisplayInterval";
  public static final String INSERT_URL = "insertUrl";
  public static final String CONFIGS_DIR = "configsDir";
  public static final String CONFIGS_DIR_REMOTE = "configsDirRemote";
  public static final String DEFAULT_CONFIG_NAME = "defaultConfigName";
  public static final String SYNONYMS_PATH = "synonymsPath";
  public static final String LOGFILE_PATTERN = "logFilePattern";
  public static final String LOG_FORMATTER = "logFormatter";
  public static final String FTP_HOST = "ftpHost";
  public static final String FTP_PORT = "ftpPort";
  public static final String FTP_USER = "ftpUser";
  public static final String FTP_PASS = "ftpPass";
  public static final String FTP_DIR = "ftpDir";
  public static final String FTP_MAXRETRIALS = "ftpMaxretrials";
  public static final String FTP_RETRIALINTERVAL = "ftpRetrialInterval";
  public static final String MAX_SITES = "siteSearch.maxSites";
  public static final String SEARCHSITE_MAXCONCURRENTPROCESSES = "siteSearch.maxConcurrentProcesses";
  public static final String SITESEARCH_TIMEOUT = "siteSearch.timeout";
  public static final String MAX_RESULTS = "siteSearch.maxResults";
  public static final String REFRESH_REQUEST_TIMES = "siteSearch.refreshRequestTimes";
  public static final String TABLENAME_KEY = "tablenameKey";
  private static String propertiesFilename;
  
  private static String defaultPropertiesFilename = "META-INF/properties/default.properties";
  private static Properties props;
  private static final String[] publicStaticFinalFields;
  
  static
  {
    try
    {
      publicStaticFinalFields = loadFieldNames();
    } catch (IllegalAccessException e) {
      throw new ExceptionInInitializerError(e);
    }
  }
  
  public static String[] getPropertynames() {
    return publicStaticFinalFields;
  }
  
  public static String getProperty(String name) {
    String value = props.getProperty(name);
    return value == null ? null : resolveReferences(props, value);
  }
  
  private static Pattern referencePattern;
  private static String resolveReferences(Properties props, String value) {
    if(referencePattern == null) {
      referencePattern = Pattern.compile("\\$\\{(.+?)\\}");
    }
    Matcher matcher = referencePattern.matcher(value);
    StringBuffer buff = null;
    while(matcher.find()) {
      String key = matcher.group(1);
      String val = props.getProperty(key);
      if(val == null) {
        val = System.getProperty(key);
      }
      if(val != null) {

// @bug 001 see bugFix method for description          
        val = applyBugFix001(val);
        
        if(buff == null) {
          buff = new StringBuffer();
        }
        matcher.appendReplacement(buff, val);
      }
    }
    String output;
    if(buff != null) {
      matcher.appendTail(buff);
      output = buff.toString();
    }else{
      output = value;
    }
//System.out.println(" Input: "+value);    
//System.out.println("Output: "+output);    
    return output;
  }

  // @bug 001 When we append C:\Users\USER to the StringBuffer we got C:UsersUSER
  // This bug fix is only a temporary measure
  private static String applyBugFix001(String val) {
//    return val.replace('\\', File.separatorChar); This didn't work (At least on windows system)
    return val.replace('\\', '/');
  }

  public static Object setProperty(String name, String val) {
    return props.setProperty(name, val);
  }
  
  public static Properties instance() {
    return props;
  }
  
  private static String[] loadFieldNames() throws IllegalAccessException
  {
    Field[] fields = AppProperties.class.getFields();
    
    HashSet<String> names = new HashSet();
    
    for (int i = 0; i < fields.length; i++)
    {
      if (accept(fields[i]))
      {
        Object fieldValue = fields[i].get(null);
        
        XLogger.getInstance().log(Level.FINER, "Field: {0},  Value: {1}", AppProperties.class, Integer.valueOf(i + 1), fieldValue);
        

        names.add(fieldValue.toString());
      }
    }
    
    return (String[])names.toArray(new String[0]);
  }
  
  private static boolean accept(Field field) {
    int modifiers = field.getModifiers();
    return (Modifier.isPublic(modifiers)) && (Modifier.isStatic(modifiers)) && (Modifier.isFinal(modifiers));
  }
  
  public static void load() throws IOException
  {

    load(null);
  }
  
  public static void load(String propertiesFilename) throws IOException
  {
    setPropertiesFilename(propertiesFilename);

    Properties defaultProps = new Properties();
    
    InputStream in = null;

    try
    {
      
      XLogger.getInstance().log(Level.INFO, "Default properites file: {0}, properties file: {1}", 
              AppProperties.class, defaultPropertiesFilename, propertiesFilename);
      
      in = getInputStream(defaultPropertiesFilename);
      
      if (in == null) {
        throw new NullPointerException();
      }
      
      defaultProps.load(in);
      
      XLogger.getInstance().log(Level.FINE, "Default properties: {0}", AppProperties.class, defaultProps);
    }finally{  
      if (in != null) {
        in.close();
      }
    }
    try{
      
      props = new Properties(defaultProps);

      if(propertiesFilename != null) {
          
        in = getInputStream(propertiesFilename);

        if (in != null) {

          props.load(in);
        }
      }
      
      XLogger.getInstance().log(Level.FINE, "Properties: {0}", AppProperties.class, props);
    }
    finally {
      if (in != null) {
        in.close();
      }
    }
  }
  
  public static void store()
    throws FileNotFoundException, IOException {

    if(propertiesFilename == null) {
        return;
    }
    
    OutputStream out = getOutputStream(propertiesFilename, false);
    
    Throwable localThrowable2 = null;
    try {
      props.store(out, "Application Properties saved on: " + new Date());
    }catch (Throwable localThrowable1) {
      localThrowable2 = localThrowable1;
      throw localThrowable1;
    }finally {
      if (out != null) if (localThrowable2 != null) try { out.close(); } catch (Throwable x2) { localThrowable2.addSuppressed(x2); } else out.close();
    }
  }
  
  private static InputStream getInputStream(String path) {
    InputStream in;
    try { 
        in = new FileInputStream(path);
    } catch (FileNotFoundException e) {
      in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
    return in;
  }
  
  private static OutputStream getOutputStream(String path, boolean append) throws FileNotFoundException
  {
    OutputStream out;
    try {
      out = new FileOutputStream(path, append);
    } catch (FileNotFoundException e) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      try {
        out = new FileOutputStream(Paths.get(url.toURI()).toFile(), false);
      } catch (URISyntaxException use) {
        XLogger.getInstance().log(Level.WARNING, "For URL: " + url, AppProperties.class, e);
        out = null;
      }
    }
    return out;
  }
  
  public static String getDefaultPropertiesFilename() {
    return defaultPropertiesFilename;
  }

  public static String getPropertiesFilename()
  {
    return propertiesFilename;
  }
  
  public static void setPropertiesFilename(String filename) {
    propertiesFilename = filename;
  }
}

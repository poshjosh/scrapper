package com.scrapper;

import com.bc.util.XLogger;
import com.scrapper.config.ScrapperConfigFactory;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class CapturerApp
{
  private boolean remote;
  private boolean initialized;
  private List<String> targetNodesToTrack;
  private LoggerManager loggerManager;
  private static CapturerApp instance;
  private static ScrapperConfigFactory configFactory;
  private static ScrapperConfigFactory searchConfigFactory;
  
  public static CapturerApp getInstance()
  {
    return getInstance(true);
  }
  
  public static CapturerApp getInstance(boolean create) {
    if ((instance == null) && (create)) {
      instance = new CapturerApp();
    }
    return instance;
  }
  
  public static void setDefaultInstance(CapturerApp app) {
    if (app.isInitialized()) {
      throw new IllegalStateException();
    }
    instance = app;
  }
  


  public void init()
    throws IllegalAccessException, InterruptedException, InvocationTargetException
  {
    init(false);
  }
  
  public void init(boolean remote)
    throws IllegalAccessException, InterruptedException, InvocationTargetException
  {
    this.remote = remote;
    
    LoggerManager lMgr = createLoggerManager();
    
    setLoggerManager(lMgr);
    
    XLogger.getInstance().setLogLevel(lMgr.getLogLevel());
    
    String sval = getProperty("targetNodesToTrack");
    if (sval != null) {
      this.targetNodesToTrack = Arrays.asList(sval.split("\\,"));
    }
    XLogger.getInstance().log(Level.FINER, "TargetNodesToTrack: {0}", getClass(), this.targetNodesToTrack);
    
    XLogger.getInstance().log(Level.FINER, "Adding shut down hook", getClass());
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        try {
          XLogger.getInstance().log(Level.FINER, "Running shutdown hook", getClass());
          AppProperties.store();

        }
        catch (IOException|RuntimeException e) {}
      }
      
    });
    XLogger.getInstance().log(Level.FINER, "Creating capturer config factory", getClass());
    
    configFactory = createConfigFactory(remote, getConfigDir(), getDefaultConfigname());
    configFactory.setSearch(false);
    searchConfigFactory = createConfigFactory(remote, getConfigDir(), getDefaultConfigname());
    searchConfigFactory.setSearch(true);
    
    this.initialized = true;
    XLogger.getInstance().log(Level.INFO, "Initialization complete.", getClass());
  }
  
  protected LoggerManager createLoggerManager() {
    return new DefaultLoggerManager();
  }
  
  public URI getConfigDir() {
    String propName = this.remote ? "configsDirRemote" : "configsDir";

    String configsDir = getProperty(propName).trim();
    URI uri;
    try {
      URL url = Thread.currentThread().getContextClassLoader().getResource(configsDir);
      if (url == null) {
        uri = Paths.get(configsDir).toUri();
      } else {
        uri = url.toURI();
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException("Failed to load: " + configsDir, e);
    }
XLogger.getInstance().log(Level.INFO, "{0} = {1}", this.getClass(), propName, uri);
    return uri;
  }
  




  public String getDefaultConfigname()
  {
    return getProperty("defaultConfigName").trim();
  }
  
  protected String getProperty(String name) {
    return AppProperties.getProperty(name);
  }
  
  public void setConfigFactory(ScrapperConfigFactory factory) {
    if (factory.isSearch()) {
      searchConfigFactory = factory;
    } else {
      configFactory = factory;
    }
  }
  

  public ScrapperConfigFactory getConfigFactory() { return getConfigFactory(false); }
  
  public ScrapperConfigFactory getConfigFactory(boolean forSearch) {
    ScrapperConfigFactory factory;
    if (forSearch) {
      factory = searchConfigFactory;
    } else {
      factory = configFactory;
    }
    return factory;
  }
  



  protected ScrapperConfigFactory createConfigFactory(final boolean remote, final URI dir, final String defaultConfigName)
  {
    XLogger.getInstance().log(Level.INFO, "Creating config factory:: Remote: {0}, configs dir: {1}, defaultFilename: {2}", getClass(), Boolean.valueOf(remote), dir, defaultConfigName);
    

    ScrapperConfigFactory factory = new ScrapperConfigFactory()
    {
      public boolean isRemote()
      {
        return remote;
      }
      
      protected URI getConfigDir() {
        return dir;
      }
      
      protected String getDefaultConfigName() {
        return defaultConfigName;
      }
    };
    return factory;
  }
  
  public boolean isRemote() {
    return this.remote;
  }
  
  public boolean isTrackLog(String id) {
    return (this.targetNodesToTrack != null) && (this.targetNodesToTrack.contains(id));
  }
  
  public String[] getSiteNames() {
    return (String[])getConfigFactory().getSitenames().toArray(new String[0]);
  }
  
  public void setLoggerManager(LoggerManager logFileMgr) {
    this.loggerManager = logFileMgr;
  }
  
  public LoggerManager getLoggerManager() {
    return this.loggerManager;
  }
  
  public boolean isInitialized() {
    return this.initialized;
  }
  
  public String getLineSeparator() {
    String s = System.getProperty("line.separator");
    return s == null ? "\n" : s;
  }
}

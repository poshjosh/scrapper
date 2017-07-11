package com.scrapper;

import com.bc.config.CompositeConfig;
import com.bc.config.ConfigService;
import com.bc.config.SimpleConfigService;
import com.bc.util.XLogger;
import com.scrapper.config.ScrapperConfigFactory;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public class CapturerApp {
    
  private boolean remote;
  private boolean initialized;
  private List<String> targetNodesToTrack;
  private ConfigService propertiesService;
  private com.bc.config.Config mergedProperties;
  private LoggerManager loggerManager;
  private static CapturerApp instance;
  private static ScrapperConfigFactory configFactory;
  private static ScrapperConfigFactory searchConfigFactory;
  
  public static CapturerApp getInstance() {
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

  public void init(boolean remote)
    throws IOException, IllegalAccessException, InterruptedException, InvocationTargetException {
      
    this.init(remote, "META-INF/properties/app.properties");  
  }
  
  public void init(boolean remote, String propertiesPath)
    throws IOException, IllegalAccessException, InterruptedException, InvocationTargetException {
  
    this.init(remote, "META-INF/properties/default.properties", propertiesPath);
  }
  
  public void init(boolean remote, String defaultPropertiesPath, String propertiesPath)
    throws IOException, IllegalAccessException, InterruptedException, InvocationTargetException {
      
    if(this.initialized) {
      throw new IllegalStateException();    
    }
      
    if(propertiesPath != null) {  
        
XLogger.getInstance().log(Level.INFO, "Default properties path: {0}\nProperties path: {1}", 
        getClass(), defaultPropertiesPath, propertiesPath);
        
      this.propertiesService = new SimpleConfigService(defaultPropertiesPath, propertiesPath);
    
      this.mergedProperties = new CompositeConfig(this.propertiesService);
    }
      
    this.remote = remote;
    
    LoggerManager lMgr = createLoggerManager();
    
    setLoggerManager(lMgr);
    
    XLogger.getInstance().setLogLevel(lMgr.getLoggerName(), lMgr.getLogLevel());
    
    String sval = getProperty("targetNodesToTrack");
    if (sval != null) {
      this.targetNodesToTrack = Arrays.asList(sval.split("\\,"));
    }
    XLogger.getInstance().log(Level.FINER, "TargetNodesToTrack: {0}", getClass(), this.targetNodesToTrack);
    
    if(this.propertiesService != null) {
        
        XLogger.getInstance().log(Level.FINER, "Adding shut down hook for saving app properties updates", getClass());

        final String threadName = "shutdownHook#Thread@"+this.getClass().getName();
        Runtime.getRuntime().addShutdownHook(new Thread(threadName){
          @Override
          public void run(){
            try {
              XLogger.getInstance().log(Level.FINER, "Running shutdown hook", getClass());
              propertiesService.store();
            } catch (IOException|RuntimeException e) {
              XLogger.getInstance().log(Level.WARNING, "Exception in shutdownHook: "+this.getName(), this.getClass(), e);
            }
          }
        });
    }
    
    XLogger.getInstance().log(Level.FINER, "Creating capturer config factory", getClass());
    
    configFactory = createConfigFactory(remote, getConfigDir(), getDefaultConfigname(), null);
    searchConfigFactory = createConfigFactory(remote, getConfigDir(), getDefaultConfigname(), "searchresults");
    
    this.initialized = true;
    XLogger.getInstance().log(Level.INFO, "Initialization complete.", getClass());
  }
  
  public Properties getProperties() {
    return this.getConfiguration().getProperties();
  }

  public com.bc.config.Config getConfiguration() {
    return this.mergedProperties;
  }
  
  public String getProperty(String key) {
    return this.mergedProperties.getProperty(key);
  }
  
  public void setProperty(String key, Object value) {
    this.mergedProperties.setProperty(key, String.valueOf(value));
  }
  
  public void saveProperty(String key, Object value) throws IOException {
    this.setProperty(key, value);
    this.saveProperties();
  }
  
  public void saveProperties() throws IOException {
    this.propertiesService.store();  
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
XLogger.getInstance().log(Level.FINE, "{0} = {1}", this.getClass(), propName, uri);
    return uri;
  }
  
  public String getDefaultConfigname() {
    return getProperty("defaultConfigName").trim();
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
  
  protected ScrapperConfigFactory createConfigFactory(
          final boolean remote, final URI dir, final String defaultConfigName, String searchNodeName) {
      
    XLogger.getInstance().log(Level.INFO, 
            "Creating config factory:: Remote: {0}, configs dir: {1}, defaultFilename: {2}", 
            getClass(), remote, dir, defaultConfigName);
    
    ScrapperConfigFactory factory = new ScrapperConfigFactory(dir, defaultConfigName, searchNodeName, true, remote);
    
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

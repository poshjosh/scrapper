package com.scrapper.config;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.DefaultCapturerContext;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.logging.Level;

public class ScrapperConfigFactory extends JsonConfigFactory {

  public ScrapperConfigFactory(URI configDir, String defaultConfigName) {
    super(configDir, defaultConfigName);
  }

  public ScrapperConfigFactory(URI configDir, String defaultConfigName, String searchNodeName, boolean useCache, boolean remote) {
    super(configDir, defaultConfigName, searchNodeName, useCache, remote);
  }

  protected JsonConfig createNew() {
    JsonConfig config = new ScrapperConfig();
    return config;
  }
  

  @Override
  public JsonConfigFactory newSyncFactory() {
    JsonConfigFactory factory = new ScrapperConfigFactory(
            this.getConfigDir(), this.getDefaultConfigName(), this.getSearchNodeName(), 
            this.isUseCache(), !this.isRemote());
    return factory;
  }
  
  public CapturerContext getContext(String name) {
      
    return getContext(getConfig(name));
  }
  
  public CapturerContext getContext(JsonConfig config) {
      
    if (config == null) {
      throw new NullPointerException();
    }
    
    String className = getClassName(config.getName());
    
    XLogger.getInstance().log(Level.FINER, "Class name: {0}", getClass(), className);
    
    CapturerContext output = null;
    try
    {
      Class aClass = Class.forName(className);
      
      output = (CapturerContext)aClass.getConstructor(new Class[] { JsonConfig.class }).newInstance(new Object[] { config });
    }
    catch (ClassNotFoundException e)
    {
      output = newDefaultContext(config);
    }
    catch (NoSuchMethodException|SecurityException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
    {
      XLogger.getInstance().log(Level.WARNING, "Failed to create: " + className, getClass(), e);
    }
    
    return output;
  }
  
  protected CapturerContext newDefaultContext(JsonConfig config) {
    return new DefaultCapturerContext(config);
  }
  
  private String getClassName(String sitename){
    String packageName = DefaultCapturerContext.class.getPackage().getName();
    StringBuilder builder = new StringBuilder(packageName);
    builder.append('.').append(toTitleCase(sitename)).append("Context");
    return builder.toString();
  }
  
  private Object toTitleCase(String arg0) {
    char ch0 = arg0.charAt(0);
    if (Character.isUpperCase(ch0)) return arg0;
    return Character.toTitleCase(ch0) + arg0.substring(1);
  }
}

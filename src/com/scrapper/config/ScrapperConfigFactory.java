package com.scrapper.config;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.DefaultCapturerContext;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.logging.Level;

public abstract class ScrapperConfigFactory extends JsonConfigFactory {
    
  protected JsonConfig createNew() {
    JsonConfig config = new ScrapperConfig();
    return config;
  }
  

  @Override
  public JsonConfigFactory newSyncFactory() {
      
    final boolean isRemote = !isRemote();
    
    JsonConfigFactory factory = new ScrapperConfigFactory() {
        
      @Override
      public boolean isRemote() {
        return isRemote;
      }
      
      @Override
      protected URI getConfigDir() {
        return ScrapperConfigFactory.this.getConfigDir();
      }
      
      @Override
      protected String getDefaultConfigName() {
        return ScrapperConfigFactory.this.getDefaultConfigName();
      }
    };
    factory.setSearch(isSearch());
    factory.setUseCache(isUseCache());
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
  


  private String getClassName(String sitename)
  {
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

package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.url.ConfigURLPartList;
import com.scrapper.util.PageNodes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DirectSourcesParser
  extends ResumableUrlParser
{
  private int urlCounterIncrement;
  private boolean urlCounterIncremented;
  private ConfigURLPartList serialPart;
  private transient ScheduledExecutorService urlCounterUpdateService;
  private final Serializable counterIncrementLock = new Serializable() {};
  
  public DirectSourcesParser(CapturerContext context, List<String> urls)
  {
    super(context.getConfig().getName(), urls);
    
    initUrlCounterUpdate(context.getConfig());
  }
  
  private void writeObject(ObjectOutputStream o) throws IOException
  {
    o.defaultWriteObject();
  }
  
  private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException
  {
    o.defaultReadObject();
    JsonConfig config = CapturerApp.getInstance().getConfigFactory().getConfig(getSitename());
    initUrlCounterUpdate(config);
  }
  

  public PageNodes next()
  {
    PageNodes page = super.next();
    
    synchronized (this.counterIncrementLock) {
      this.urlCounterIncrement += 1;
      this.urlCounterIncremented = true;
    }
    
    return page;
  }
  
  public void completePendingActions()
  {
    if (this.urlCounterUpdateService != null) {
      XLogger.getInstance().log(Level.FINE, "Shutting down counter update service for: {0}", getClass(), getSitename());
      Util.shutdownAndAwaitTermination(this.urlCounterUpdateService, 3L, TimeUnit.SECONDS);
    }
  }
  
  private void initUrlCounterUpdate(final JsonConfig config)
  {
    this.serialPart = ConfigURLPartList.getSerialPart(config, "counter");
    
    if (this.serialPart == null) {
      throw new IllegalArgumentException("Invalid format for: url.counter.part");
    }
    
    this.urlCounterUpdateService = Executors.newSingleThreadScheduledExecutor();
    
    this.urlCounterUpdateService.scheduleAtFixedRate(new Runnable()
    {
      public void run() {
        try {
          DirectSourcesParser.this.updateUrlCounter(config);
        } catch (IOException e) {
          XLogger.getInstance().log(Level.WARNING, "Encountered exception while updating url counter for config: " + config.getName(), getClass(), e);
        } catch (RuntimeException e) {
          XLogger.getInstance().log(Level.WARNING, "Encountered exception while updating url counter for config: " + config.getName(), getClass(), e); } } }, 1L, 1L, TimeUnit.MINUTES);
  }
  


  private void updateUrlCounter(JsonConfig cfg)
    throws IOException
  {
    if (!this.urlCounterIncremented) {
      return;
    }
    
    synchronized (cfg)
    {
      synchronized (this.counterIncrementLock)
      {
        this.urlCounterIncremented = false;
        
        Object[] jsonPath = { Config.Site.url, this.serialPart.getType(), "part" + this.serialPart.getPartIndex(), Config.Site.start };
        
        int update = this.serialPart.getStart() + this.urlCounterIncrement;
        
        cfg.setObject(jsonPath, Integer.valueOf(update));
        
        if (XLogger.getInstance().isLoggable(Level.INFO, getClass())) {
          XLogger.getInstance().log(Level.INFO, "Updating {0} from {1} to {2}", getClass(), Arrays.toString(jsonPath), Integer.valueOf(this.serialPart.getStart()), Integer.valueOf(update));
        }
      }
      
      CapturerApp.getInstance().getConfigFactory().saveValues(cfg);
    }
  }
}

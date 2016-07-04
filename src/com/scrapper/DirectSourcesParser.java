package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.bc.util.concurrent.NamedThreadFactory;
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

public class DirectSourcesParser extends ResumableUrlParser {
    
  private int urlCounterIncrement;
  private boolean urlCounterIncremented;
  private transient ScheduledExecutorService urlCounterUpdateService;
  
  private final Serializable counterIncrementLock = new Serializable() {};
  
  public DirectSourcesParser(CapturerContext context, List<String> urls){
      
    super(context.getConfig().getName(), urls);
    
    initUrlCounterUpdate(context.getConfig());
  }
  
  private void writeObject(ObjectOutputStream o) throws IOException {
    o.defaultWriteObject();
  }
  
  private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException {
    o.defaultReadObject();
    JsonConfig config = CapturerApp.getInstance().getConfigFactory().getConfig(getSitename());
    initUrlCounterUpdate(config);
  }
  
  @Override
  public PageNodes next()  {
      
    PageNodes page = super.next();
    
    synchronized (this.counterIncrementLock) {
      this.urlCounterIncrement += 1;
      this.urlCounterIncremented = true;
    }
    
    return page;
  }
  
  @Override
  public void completePendingActions() {
    if (this.urlCounterUpdateService != null) {
      XLogger.getInstance().log(Level.FINE, "Shutting down counter update service for: {0}", getClass(), getSitename());
      Util.shutdownAndAwaitTermination(this.urlCounterUpdateService, 3L, TimeUnit.SECONDS);
    }
  }
  
  private void initUrlCounterUpdate(final JsonConfig config) {
      
    ConfigURLPartList serialPart = ConfigURLPartList.getSerialPart(config, "counter");
    
    if (serialPart == null) {
      throw new IllegalArgumentException("Invalid format for: url.counter.part");
    }
    
    this.urlCounterUpdateService = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory(this.getClass().getSimpleName()+".urlCountUpdater_ThreadPool"));
    
    this.urlCounterUpdateService.scheduleAtFixedRate(new UrlCountUpdateTask(config, serialPart), 1L, 1L, TimeUnit.MINUTES);
  }
  
  private class UrlCountUpdateTask implements Runnable {
    private final JsonConfig cfg;
    private final ConfigURLPartList serialPart;
    private UrlCountUpdateTask(JsonConfig cfg, ConfigURLPartList serialPart) {
      this.cfg = cfg;
      this.serialPart = serialPart;
    }
    @Override
    public void run() {
        
      try {
          
        if (!urlCounterIncremented) {
          return;
        }

        synchronized (cfg) {
            
          synchronized (counterIncrementLock) {
              
            urlCounterIncremented = false;

            Object[] jsonPath = { Config.Site.url, serialPart.getType(), "part" + serialPart.getPartIndex(), Config.Site.start };

            int update = serialPart.getStart() + urlCounterIncrement;

            cfg.setObject(jsonPath, update);

            if (XLogger.getInstance().isLoggable(Level.INFO, getClass())) {
              XLogger.getInstance().log(Level.INFO, "Updating {0} from {1} to {2}", 
              getClass(), Arrays.toString(jsonPath), this.serialPart.getStart(), update);
            }
          }

          CapturerApp.getInstance().getConfigFactory().saveValues(cfg);
        }
      } catch (IOException | RuntimeException e) {
        XLogger.getInstance().log(Level.WARNING, "Encountered exception while updating url counter for config: " + cfg.getName(), getClass(), e);
      } 
    }
  }
}

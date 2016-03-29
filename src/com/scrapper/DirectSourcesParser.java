package com.scrapper;

import com.bc.process.ProcessManager;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.bc.util.XLogger;
import com.scrapper.url.ConfigURLPartList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @(#)DirectSourcesParser.java   15-Feb-2014 15:49:13
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class DirectSourcesParser extends ResumableUrlParser {
    
    private int urlCounterIncrement;
    
    private boolean urlCounterIncremented;
    
    private ConfigURLPartList serialPart;
    
    private transient ScheduledExecutorService urlCounterUpdateService;
    
    private final Serializable counterIncrementLock = new Serializable(){};
    
    public DirectSourcesParser(CapturerContext context, List<String> urls) {
        
        super(context.getConfig().getName(), urls);
        
        this.initUrlCounterUpdate(context.getConfig());
    }
  
    private void writeObject(ObjectOutputStream o)
        throws IOException { 
        o.defaultWriteObject();
    }
  
    private void readObject(ObjectInputStream o)
        throws IOException, ClassNotFoundException {
        o.defaultReadObject();
        JsonConfig config = CapturerApp.getInstance().getConfigFactory().getConfig(this.getSitename());
        this.initUrlCounterUpdate(config);
    }
    
    @Override
    public PageNodes next() {
        
        PageNodes page = super.next();

        synchronized(counterIncrementLock) {
            ++urlCounterIncrement;
            this.urlCounterIncremented = true;
        }
        
        return page;
    }
    
    @Override
    public void completePendingActions() {
        if(this.urlCounterUpdateService != null) {
XLogger.getInstance().log(Level.FINE, "Shutting down counter update service for: {0}", this.getClass(), this.getSitename());            
            ProcessManager.shutdownAndAwaitTermination(urlCounterUpdateService, 3, TimeUnit.SECONDS);
        }
    }
    
    private void initUrlCounterUpdate(final JsonConfig config) {
        
        this.serialPart = ConfigURLPartList.getSerialPart(config, "counter");
        
        if(serialPart == null) {
            throw new IllegalArgumentException("Invalid format for: url.counter.part");
        }
        
        this.urlCounterUpdateService = Executors.newSingleThreadScheduledExecutor();
        
        this.urlCounterUpdateService.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                try{
                    updateUrlCounter(config);
                }catch(IOException e){
                    XLogger.getInstance().log(Level.WARNING, "Encountered exception while updating url counter for config: "+config.getName(), this.getClass(), e);
                }catch(RuntimeException e) {
                    XLogger.getInstance().log(Level.WARNING, "Encountered exception while updating url counter for config: "+config.getName(), this.getClass(), e);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    private void updateUrlCounter(final JsonConfig cfg) throws IOException {
        
        if(!this.urlCounterIncremented) {
            return;
        }
        
        synchronized(cfg) {
            
            synchronized(counterIncrementLock) {
                
                this.urlCounterIncremented = false;

                final Object [] jsonPath = {Config.Site.url, serialPart.getType(), "part"+serialPart.getPartIndex(), Config.Site.start};

                int update = serialPart.getStart() + urlCounterIncrement;
                
                cfg.setObject(jsonPath, update);
                
if(XLogger.getInstance().isLoggable(Level.INFO, this.getClass()))            
XLogger.getInstance().log(Level.INFO, "Updating {0} from {1} to {2}", 
        this.getClass(), Arrays.toString(jsonPath), serialPart.getStart(), update);            
            }
            
            CapturerApp.getInstance().getConfigFactory().saveValues(cfg);
        }
    }
}

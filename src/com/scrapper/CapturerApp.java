package com.scrapper;

import com.scrapper.config.ScrapperConfigFactory;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import com.bc.util.XLogger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @(#)CapturerApp.java   24-Nov-2013 04:57:46
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
public class CapturerApp {
    
    private boolean remote;
    
    private boolean initialized;
    
    private List<String> targetNodesToTrack;
    
    private LoggerManager loggerManager;
    
    private static CapturerApp instance;
    
    private static ScrapperConfigFactory configFactory;
    
    private static ScrapperConfigFactory searchConfigFactory;

    protected CapturerApp() { }
    
    public static CapturerApp getInstance() {
        return getInstance(true);
    }

    public static CapturerApp getInstance(boolean create) {
        if(instance == null && create) {
            instance = new CapturerApp();
        }
        return instance;
    }
    
    public static void setDefaultInstance(CapturerApp app) {
        if(app.isInitialized()) {
            throw new IllegalStateException();
        }
        instance = app;
    }

    public void init() 
            throws IllegalAccessException, 
            InterruptedException,
            InvocationTargetException {
        
        this.init(false);
    }
    
    public void init(boolean remote) 
            throws IllegalAccessException, 
            InterruptedException,
            InvocationTargetException {
    
        this.remote = remote;
        
        LoggerManager lMgr = this.createLoggerManager();
        
        this.setLoggerManager(lMgr);
        
        try {
            Level logLevel = lMgr.getLogLevel();
            XLogger.getInstance().setLogLevel(com.scrapper.CapturerApp.class.getPackage().getName(), logLevel);
            XLogger.getInstance().setLogLevelForConsoleHandlers(logLevel);
        } catch (Exception e) {
            XLogger.getInstance().log(Level.WARNING, "Error setting log level to: " + lMgr.getLogLevel(), getClass(), e);
        }
        
        String sval = this.getProperty(AppProperties.TARGETNODES_TO_TRACK);
        if(sval != null) {
            this.targetNodesToTrack = Arrays.asList(sval.split("\\,"));
        }
XLogger.getInstance().log(Level.FINER, "TargetNodesToTrack: {0}", this.getClass(), targetNodesToTrack);        
        
XLogger.getInstance().log(Level.FINER, "Adding shut down hook", this.getClass());        
        
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try{ 
                    // This ran even when java result :1
XLogger.getInstance().log(Level.FINER, "Running shutdown hook", this.getClass());                    
                    AppProperties.store();
                }catch(IOException | RuntimeException e) {
                    // No place to be logging
                }
            }
        });
        
XLogger.getInstance().log(Level.FINER, "Creating capturer config factory", this.getClass());        

        configFactory = createConfigFactory(remote, this.getConfigDir(), this.getDefaultConfigname());
        configFactory.setSearch(false);
        searchConfigFactory = createConfigFactory(remote, this.getConfigDir(), this.getDefaultConfigname()); 
        searchConfigFactory.setSearch(true);
        
        initialized = true;
XLogger.getInstance().log(Level.INFO, "Initialization complete.", this.getClass());        
    }
    
    protected LoggerManager createLoggerManager() {
        return new DefaultLoggerManager();
    }

    public URI getConfigDir() {
        String propName = remote ? 
                AppProperties.CONFIGS_DIR_REMOTE : 
                AppProperties.CONFIGS_DIR;
        
        // We trim the output, because we have encountered properties 
        // with leading or trailings paces
        String configsDir = this.getProperty(propName).trim();
        URI uri;
        try{
            URL url = Thread.currentThread().getContextClassLoader().getResource(configsDir);
            if(url == null) {
                uri = Paths.get(configsDir).toUri();
            }else{
                uri = url.toURI();
            }
        }catch(URISyntaxException e) {
            throw new RuntimeException("Failed to load: "+configsDir, e);
        }
        
        return uri;
    }
    
    /**
     * The default config file name without the extension
     * @return 
     */
    public String getDefaultConfigname() {
        // I have encountered properties with leading or trailings paces
        return this.getProperty(AppProperties.DEFAULT_CONFIG_NAME).trim();
    }
    
    protected String getProperty(String name) {
        return AppProperties.getProperty(name);
    }
    
    public void setConfigFactory(ScrapperConfigFactory factory) {
        if(factory.isSearch()) {
            searchConfigFactory = factory;
        }else{
            configFactory = factory;
        }
    }
    
    public ScrapperConfigFactory getConfigFactory() {
        return getConfigFactory(false);
    }
    
    public ScrapperConfigFactory getConfigFactory(boolean forSearch) {
        ScrapperConfigFactory factory;
        if(forSearch) {
            factory = searchConfigFactory;
        }else{
            factory = configFactory;
        }
        return factory;
    }
    
    protected ScrapperConfigFactory createConfigFactory(
            final boolean remote, 
            final URI dir, 
            final String defaultConfigName) {
        
XLogger.getInstance().log(Level.INFO, "Creating config factory:: Remote: {0}, configs dir: {1}, defaultFilename: {2}",
        this.getClass(), remote, dir, defaultConfigName);

        ScrapperConfigFactory factory = new ScrapperConfigFactory(){

            @Override
            public boolean isRemote() {
                return remote;
            }
            @Override
            protected URI getConfigDir() {
                return dir;
            }
            @Override
            protected String getDefaultConfigName() {
                return defaultConfigName;
            }
        };
        return factory;
    }

    public boolean isRemote() {
        return remote;
    }
   
    public boolean isTrackLog(String id) {
        return (this.targetNodesToTrack != null && this.targetNodesToTrack.contains(id));
    }
    
    public String [] getSiteNames() {
        return this.getConfigFactory().getSitenames().toArray(new String[0]);
    }
    
    public void setLoggerManager(LoggerManager logFileMgr) {
        this.loggerManager = logFileMgr;
    }

    public LoggerManager getLoggerManager() {
        return loggerManager;
    }
    
    public boolean isInitialized() {
        return initialized;
    }

    public String getLineSeparator() {
        String s = System.getProperty("line.separator");
        return s == null ? "\n" : s;
    }
    
    static{
//        Logger.getLogger(com.scrapper.AbstractDataConsumer.class.getName()).setLevel(Level.FINER);
//        Logger.getLogger(com.scrapper.formatter.DefaultFormatter.class.getName()).setLevel(Level.FINER);
    }
}

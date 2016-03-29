package com.scrapper;

import com.bc.util.XLogger;
import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @(#)AbstractLogMgr.java   03-Nov-2014 10:17:10
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class SimpleLoggerManager implements LoggerManager {
    
    public SimpleLoggerManager() { 
    
        this.addFileHandler();
        
        if(this.getLogLevel() != null) {
            XLogger.getInstance().setLogLevel(this.getLoggerName(), this.getLogLevel());
        }
    }
    
    @Override
    public boolean addFileHandler() {

        boolean output;
        
        if(this.getLogFilePattern() != null) {
            
            try{

                Handler fileHandler = this.createFileHandler();

                this.addHandler(fileHandler);
                
                output = true;

            }catch(IOException e) {
                XLogger.getInstance().log(Level.WARNING, "Error creating log file handler", this.getClass(), e);
                output = false;
            }
        }else{
            output = false;
        }
        
        return output;
    }

    @Override
    public Handler createFileHandler() throws IOException {
        
        return this.createFileHandler(this.getLogFilePattern(), this.getLimit(), this.getCount(), this.isAppend());
    }
        
    private Handler createFileHandler(String logFilePattern, int limit, int count, boolean append) throws IOException {
        
XLogger.getInstance().log(Level.INFO, "Log file pattern: {0}", this.getClass(), logFilePattern);            

        FileHandler fileHandler = new FileHandler(logFilePattern, limit, count, append);
        
        return fileHandler;
    }

    @Override
    public void addHandler(Handler handler) {
        
        handler.setFormatter(this.getFormatter());
        
        this.getLogger().addHandler(handler);
        
    }
    
    @Override
    public Logger getLogger() {
        return Logger.getLogger(this.getLoggerName());
    }
    
    @Override
    public boolean isAppend() {
        return true;
    }
    
    @Override
    public int getCount() {
        return 100;
    }

    @Override
    public int getLimit() {
        return 999000;
    }
    
    @Override
    public String getLogFilePattern() {
        return null;
    }
    
    @Override
    public Level getLogLevel() {
        return Level.INFO;
    }

    private String ln;
    @Override
    public String getLoggerName() {
        if(ln == null) {
            String cn = this.getClass().getName();
            String [] parts = cn.split("\\.");
            if(parts.length > 1) {
                ln = parts[0] + '.' + parts[1];
            }else{
                throw new UnsupportedOperationException("Unexpected class name: "+cn);
            }
        }
        return ln;
    }

    private Formatter f;
    @Override
    public Formatter getFormatter() {
        if(f == null) {
            f = new SimpleFormatter();
        }
        return f;
    }
}

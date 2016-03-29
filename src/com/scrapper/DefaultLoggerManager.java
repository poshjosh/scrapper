package com.scrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 * @(#)DefaultLogMgr.java   03-Nov-2014 10:49:57
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
public class DefaultLoggerManager extends SimpleLoggerManager {
    
    public DefaultLoggerManager() { }
    
    @Override
    public Level getLogLevel() {
        
        String levelString = AppProperties.getProperty(AppProperties.LOG_LEVEL);
        
        Level level;

        if(levelString != null && !levelString.isEmpty()) {
            level = Level.parse(levelString);
        }else{
            level = null;
        }
        
        return level;
    }
    
    @Override
    public String getLogFilePattern() {
        return AppProperties.getProperty(AppProperties.LOGFILE_PATTERN);
    }

    private Formatter formatter;
    @Override
    public Formatter getFormatter() {
        if(formatter == null) {
            String formatterClassname = AppProperties.getProperty(AppProperties.LOG_FORMATTER);
            try{
                Class aClass = Class.forName(formatterClassname);
                formatter = (java.util.logging.Formatter)aClass.getConstructor().newInstance();
            }catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                formatter = new SimpleFormatter();
            }
        }
        return formatter;
    }
}

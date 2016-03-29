package com.scrapper;

import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @(#)LoggerManager.java   18-Jun-2015 12:12:23
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
public interface LoggerManager {

    boolean addFileHandler();

    void addHandler(Handler handler);

    Handler createFileHandler() throws IOException;

    int getCount();

    Formatter getFormatter();

    int getLimit();

    String getLogFilePattern();

    Level getLogLevel();

    Logger getLogger();

    String getLoggerName();

    boolean isAppend();

}

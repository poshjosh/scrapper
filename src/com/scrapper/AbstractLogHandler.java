package com.scrapper;

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * @(#)AbstractLogHandler.java   19-Nov-2013 19:54:35
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
public abstract class AbstractLogHandler extends Handler {

    private boolean closed;

    public AbstractLogHandler() {
        this.setFormatter(new SimpleFormatter());
    }
    
    @Override
    public synchronized void publish(LogRecord record) {

        if (closed || !isLoggable(record)) {
            return;
        }

        try {

            doPublish(record);

        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        flush();
    }

    protected abstract void doPublish(LogRecord record) throws Exception;

    @Override
    public void flush() { }

    @Override
    public void close() {
        closed = true;
        flush();
    }
}//END

package com.scrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @(#)SimpleLogFormatter.java   01-Feb-2013 14:33:04
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Overrides Formatter to provide simple logging without Date, Time, 
 * Class and Method name prefixes. 
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class SimpleLogFormatter extends Formatter {

    private final static String format = "{0,date} {0,time}";

    private boolean appendMetaData;
    
    private Date date;
    
    private Object [] args;
    
    private String lineSeparator;

    private MessageFormat formatter;
    
    public SimpleLogFormatter() { 
        date = new Date();
        args = new Object[1];
        lineSeparator = System.getProperty("line.separator");
    }
    
    protected synchronized void appendMetaData(LogRecord record, StringBuilder sb) {
	// Minimize memory allocations here.
	date.setTime(record.getMillis());
	args[0] = date;
	StringBuffer text = new StringBuffer();
	if (formatter == null) {
	    formatter = new MessageFormat(format);
	}
	formatter.format(args, text, null);
	sb.append(text);
	sb.append(" ");
	if (record.getSourceClassName() != null) {	
	    sb.append(record.getSourceClassName());
	} else {
	    sb.append(record.getLoggerName());
	}
	if (record.getSourceMethodName() != null) {	
	    sb.append(" ");
	    sb.append(record.getSourceMethodName());
	}
	sb.append(lineSeparator);
    }
    
    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public synchronized String format(LogRecord record) {

	StringBuilder sb = new StringBuilder();
        
        if(this.appendMetaData) {
            this.appendMetaData(record, sb);
        }
        
	String message = formatMessage(record);
	sb.append(record.getLevel().getLocalizedName());
	sb.append(": ");
	sb.append(message);
	sb.append(lineSeparator);
	if (record.getThrown() != null) {
	    try {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        record.getThrown().printStackTrace(pw);
	        pw.close();
		sb.append(sw.toString());
	    } catch (Exception ex) {
                ex.printStackTrace();
	    }
	}
	return sb.toString();
    }

    public boolean isAppendMetaData() {
        return appendMetaData;
    }

    public void setAppendMetaData(boolean appendMetaData) {
        this.appendMetaData = appendMetaData;
    }
}

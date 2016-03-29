package com.scrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

















public class SimpleLogFormatter
  extends Formatter
{
  private static final String format = "{0,date} {0,time}";
  private boolean appendMetaData;
  private Date date;
  private Object[] args;
  private String lineSeparator;
  private MessageFormat formatter;
  
  public SimpleLogFormatter()
  {
    this.date = new Date();
    this.args = new Object[1];
    this.lineSeparator = System.getProperty("line.separator");
  }
  
  protected synchronized void appendMetaData(LogRecord record, StringBuilder sb)
  {
    this.date.setTime(record.getMillis());
    this.args[0] = this.date;
    StringBuffer text = new StringBuffer();
    if (this.formatter == null) {
      this.formatter = new MessageFormat("{0,date} {0,time}");
    }
    this.formatter.format(this.args, text, null);
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
    sb.append(this.lineSeparator);
  }
  






  public synchronized String format(LogRecord record)
  {
    StringBuilder sb = new StringBuilder();
    
    if (this.appendMetaData) {
      appendMetaData(record, sb);
    }
    
    String message = formatMessage(record);
    sb.append(record.getLevel().getLocalizedName());
    sb.append(": ");
    sb.append(message);
    sb.append(this.lineSeparator);
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
    return this.appendMetaData;
  }
  
  public void setAppendMetaData(boolean appendMetaData) {
    this.appendMetaData = appendMetaData;
  }
}

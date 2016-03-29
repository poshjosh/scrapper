package com.scrapper;

import com.scrapper.util.Util;
import java.util.logging.LogRecord;
import org.htmlparser.Node;
import org.htmlparser.Tag;










public class MyLogFormatter
  extends SimpleLogFormatter
{
  public MyLogFormatter()
  {
    setAppendMetaData(true);
  }
  

  public synchronized String formatMessage(LogRecord record)
  {
    format(record, 70);
    
    return super.formatMessage(record);
  }
  
  private void format(LogRecord record, int maxLengthNode)
  {
    Object[] params = record.getParameters();
    
    if (params == null) { return;
    }
    for (int i = 0; i < params.length; i++)
    {
      if ((params[i] instanceof Tag))
      {
        String sval = ((Tag)params[i]).toTagHtml();
        int len = maxLengthNode > sval.length() ? sval.length() : maxLengthNode;
        
        params[i] = sval.substring(0, len);

      }
      else if ((params[i] instanceof Node))
      {
        params[i] = Util.appendTag((Node)params[i], maxLengthNode);
      }
    }
  }
}

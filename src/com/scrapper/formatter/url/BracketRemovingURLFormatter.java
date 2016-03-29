package com.scrapper.formatter.url;

import com.scrapper.Formatter;

























public class BracketRemovingURLFormatter
  implements Formatter<String>
{
  public String format(String e)
  {
    int off = e.indexOf('(');
    
    if (off != -1)
    {
      e = e.substring(0, off);
    }
    
    return e.trim();
  }
}

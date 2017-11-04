package com.scrapper.formatter.url;

import com.bc.webdatex.formatter.Formatter;

























public class BracketRemovingURLFormatter
  implements Formatter<String>
{
  public String apply(String e)
  {
    int off = e.indexOf('(');
    
    if (off != -1)
    {
      e = e.substring(0, off);
    }
    
    return e.trim();
  }
}

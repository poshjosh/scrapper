package com.scrapper.formatter.url;

























public class DivinefortuneURLFormatter
  extends BracketRemovingURLFormatter
{
  public String format(String e)
  {
    int off = e.indexOf("&amp;zendid");
    
    if (off == -1)
    {
      off = e.indexOf("&zenid");
    }
    
    if (off != -1)
    {
      e = e.substring(0, off);
    }
    
    e = super.format(e);
    
    return e.trim();
  }
}

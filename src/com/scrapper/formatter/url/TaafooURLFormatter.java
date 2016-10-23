package com.scrapper.formatter.url;

import com.bc.webdatex.formatter.Formatter;

























public class TaafooURLFormatter
  implements Formatter<String>
{
  public String format(String e)
  {
    int lenB4 = e.length();
    
    e = e.replace("searchresults.aspx/viewdetails.aspx?productId=", "viewdetails.aspx?productId=");
    

    if (e.length() == lenB4)
    {
      e = e.replace("searchresults.aspx/ViewDetails.aspx?productId=", "ViewDetails.aspx?productId=");
    }
    


    String elower = e.toLowerCase();
    
    int n = elower.indexOf("&amp;prodname=");
    
    if (n == -1) {
      n = elower.indexOf("&prodname=");
    }
    
    if (n != -1) {
      e = e.substring(0, n);
    }
    
    return e.trim();
  }
}

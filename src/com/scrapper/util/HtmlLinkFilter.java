package com.scrapper.util;

import com.scrapper.Filter;











public class HtmlLinkFilter
  implements Filter<String>
{
  public boolean accept(String link)
  {
    String lowercaseLink = link.toLowerCase();
    
    boolean htmlLink = (accept(lowercaseLink, ".html")) || (accept(lowercaseLink, ".php")) || (accept(lowercaseLink, ".htm")) || (accept(lowercaseLink, ".xhtml")) || (accept(lowercaseLink, ".asp")) || (accept(lowercaseLink, ".aspx")) || (accept(lowercaseLink, ".jsp")) || (accept(lowercaseLink, ".jspx")) || (accept(lowercaseLink, ".php")) || (accept(lowercaseLink, ".xml"));
    










    return htmlLink;
  }
  
  private boolean accept(String link, String extension) {
    boolean accept = false;
    if (link.endsWith(extension))
    {
      accept = true;
    }
    else {
      int n = link.lastIndexOf('/');
      if (link.indexOf(extension + "?", n) != -1) {
        accept = true;
      }
    }
    return accept;
  }
}

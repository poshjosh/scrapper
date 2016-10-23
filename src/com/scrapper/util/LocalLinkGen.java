package com.scrapper.util;

import com.bc.webdatex.formatter.Formatter;

public class LocalLinkGen
  implements Formatter<String>
{
  private String rootPageAlias;
  private String baseURL;
  private String referrer;
  
  public LocalLinkGen()
  {
    this.rootPageAlias = "index.html";
  }
  
  public String format(String link)
  {

    String ret;

    if ((link.equals(getBaseURL())) || ((!getBaseURL().endsWith("/")) && (link.equals(getBaseURL() + "/")))) {
      ret = getRootPageAlias(); } else {
      if ((link.startsWith(getBaseURL())) && (link.length() > getBaseURL().length()))
      {
        ret = link.substring(getBaseURL().length() + 1);
      } else {
        ret = link;
      }
    }
    
    if ((null != this.referrer) && (link.startsWith(getBaseURL())) && (this.referrer.length() > getBaseURL().length()))
    {


      this.referrer = this.referrer.substring(getBaseURL().length() + 1);
      int i = 0;
      int j; while (-1 != (j = this.referrer.indexOf('/', i)))
      {
        ret = "../" + ret;
        i = j + 1;
      }
    }
    
    return ret;
  }
  
  private String getBaseURL() {
    return this.baseURL;
  }
  
  public void setBaseURL(String baseURL) {
    this.baseURL = baseURL;
  }
  
  public String getReferrer() {
    return this.referrer;
  }
  
  public void setReferrer(String referrer) {
    this.referrer = referrer;
  }
  
  public String getRootPageAlias() {
    return this.rootPageAlias;
  }
  
  public void setRootPageAlias(String rootPageAlias) {
    this.rootPageAlias = rootPageAlias;
  }
}

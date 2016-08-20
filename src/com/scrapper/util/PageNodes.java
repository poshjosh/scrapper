package com.scrapper.util;

import com.scrapper.tag.Link;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public abstract interface PageNodes{
    
  public abstract String getURL();
  
  public abstract String getFormattedURL();
  
  public abstract NodeList getNodeList();
  
  public abstract MetaTag getRobots();
  
  public abstract MetaTag getKeywords();
  
  public abstract MetaTag getDescription();
  
  public abstract Link getIco();
  
  public abstract Link getIcon();
  
  public abstract TitleTag getTitle();
  
  public abstract BodyTag getBody();
}

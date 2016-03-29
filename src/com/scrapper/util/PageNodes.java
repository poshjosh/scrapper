package com.scrapper.util;

import com.scrapper.tag.Link;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public abstract interface PageNodes
{
  public abstract void setURL(String paramString);
  
  public abstract String getURL();
  
  public abstract void setFormattedURL(String paramString);
  
  public abstract String getFormattedURL();
  
  public abstract void setNodeList(NodeList paramNodeList);
  
  public abstract NodeList getNodeList();
  
  public abstract void setRobots(MetaTag paramMetaTag);
  
  public abstract MetaTag getRobots();
  
  public abstract void setKeywords(MetaTag paramMetaTag);
  
  public abstract MetaTag getKeywords();
  
  public abstract void setDescription(MetaTag paramMetaTag);
  
  public abstract MetaTag getDescription();
  
  public abstract Link getIco();
  
  public abstract void setIco(Link paramLink);
  
  public abstract Link getIcon();
  
  public abstract void setIcon(Link paramLink);
  
  public abstract void setTitle(TitleTag paramTitleTag);
  
  public abstract TitleTag getTitle();
  
  public abstract void setBody(BodyTag paramBodyTag);
  
  public abstract BodyTag getBody();
}

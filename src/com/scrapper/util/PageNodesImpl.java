package com.scrapper.util;

import com.bc.util.XLogger;
import com.scrapper.tag.Link;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;













public class PageNodesImpl
  implements PageNodes
{
  private String url;
  private String formattedUrl;
  private MetaTag robots;
  private MetaTag keywords;
  private MetaTag description;
  private Link ico;
  private Link icon;
  private TitleTag title;
  private BodyTag body;
  private NodeList nodes;
  
  public PageNodesImpl(String url, NodeList nodes)
  {
    this(url, url, nodes);
  }
  
  public PageNodesImpl(String url, String formattedUrl, NodeList nodes) {
    this.url = url;
    this.formattedUrl = formattedUrl;
    this.nodes = nodes;
    init(nodes);
  }
  
  private void init(NodeList nodes)
  {
    NodeList metas = nodes.extractAllNodesThatMatch(new TagNameFilter("META"), true);
    
    for (Node node : metas)
    {
      MetaTag metaTag = (MetaTag)node;
      
      String name = metaTag.getAttribute("name");
      if ((this.robots == null) && ("robots".equals(name))) {
        this.robots = metaTag;
      } else if ((this.keywords == null) && ("keywords".equals(name))) {
        this.keywords = metaTag;
      } else if ((this.description == null) && ("description".equals(name))) {
        this.description = metaTag;
      }
    }
    
    NodeList titles = nodes.extractAllNodesThatMatch(new TagNameFilter("TITLE"), true);
    
    if ((titles != null) && (!titles.isEmpty())) {
      this.title = ((TitleTag)titles.get(0));
    }
    
    NodeList links = nodes.extractAllNodesThatMatch(new TagNameFilter("LINK"), true);
    for (Node node : links)
    {
      Link link = (Link)node;
      String rel = link.getAttribute("rel");
      if (rel != null)
      {

        String lower = rel.toLowerCase().trim();
        if ((this.ico == null) && ("shortcut icon".equals(lower))) {
          this.ico = link;
        } else if ((this.icon == null) && ("icon".equals(lower))) {
          this.icon = link;
        }
        
        if ((this.ico != null) && (this.icon != null)) {
          break;
        }
      }
    }
    

    NodeList bodies = nodes.extractAllNodesThatMatch(new TagNameFilter("BODY"), true);
    
    if ((bodies == null) || (bodies.isEmpty())) {
      XLogger.getInstance().log(Level.WARNING, "No HTML body found for content at: {0}", getClass(), getURL());
    } else {
      this.body = ((BodyTag)bodies.get(0));
    }
  }
  
  public String getURL()
  {
    return this.url;
  }
  
  public void setURL(String url)
  {
    this.url = url;
  }
  
  public String getFormattedURL()
  {
    return this.formattedUrl;
  }
  
  public void setFormattedURL(String formattedUrl)
  {
    this.formattedUrl = formattedUrl;
  }
  
  public MetaTag getRobots()
  {
    return this.robots;
  }
  
  public void setRobots(MetaTag robots)
  {
    this.robots = robots;
  }
  
  public MetaTag getKeywords()
  {
    return this.keywords;
  }
  
  public void setKeywords(MetaTag keywords)
  {
    this.keywords = keywords;
  }
  
  public MetaTag getDescription()
  {
    return this.description;
  }
  
  public void setDescription(MetaTag description)
  {
    this.description = description;
  }
  
  public Link getIco()
  {
    return this.ico;
  }
  
  public void setIco(Link ico)
  {
    this.ico = ico;
  }
  
  public Link getIcon()
  {
    return this.icon;
  }
  
  public void setIcon(Link icon)
  {
    this.icon = icon;
  }
  
  public TitleTag getTitle()
  {
    return this.title;
  }
  
  public void setTitle(TitleTag title)
  {
    this.title = title;
  }
  
  public BodyTag getBody()
  {
    return this.body;
  }
  
  public void setBody(BodyTag body)
  {
    this.body = body;
  }
  
  public NodeList getNodeList()
  {
    return this.nodes;
  }
  
  public void setNodeList(NodeList nodes)
  {
    this.nodes = nodes;
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getName());
    builder.append(". URL: ").append(this.url);
    builder.append(", Nodes: ").append(this.nodes == null ? null : Integer.valueOf(this.nodes.size()));
    return builder.toString();
  }
}

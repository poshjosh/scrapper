package com.scrapper.util;

import com.scrapper.tag.Link;
import java.io.Serializable;
import org.htmlparser.Node;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public class PageNodesImpl implements PageNodes, Serializable {
    
  private String url;
  private String formattedUrl;
  private MetaTag robots;
  private MetaTag keywords;
  private MetaTag description;
  private Link ico;
  private Link icon;
  private TitleTag title;
  private BodyTag body;
  private NodeList nodeList;
  
  public PageNodesImpl(String url, NodeList nodes) {
    this(url, url, nodes);
  }
  
  public PageNodesImpl(String url, String formattedUrl, NodeList nodes) {
    this.url = url;
    this.formattedUrl = formattedUrl;
    this.nodeList = nodes;
    init(nodes);
  }
  
  private void init(NodeList nodes) {
      
    NodeList metas = nodes.extractAllNodesThatMatch(new TagNameFilter("META"), true);
    
    for (Node node : metas) {
        
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
    for (Node node : links) {
        
      Link link = (Link)node;
      String rel = link.getAttribute("rel");
      if (rel != null)  {

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
    
    if (bodies != null && !bodies.isEmpty()) {
      this.body = ((BodyTag)bodies.get(0));
    }
  }
  
  @Override
  public String getURL() {
    return this.url;
  }
  
  @Override
  public String getFormattedURL() {
    return this.formattedUrl;
  }
  
  @Override
  public MetaTag getRobots() {
    return this.robots;
  }
  
  @Override
  public MetaTag getKeywords() {
    return this.keywords;
  }
  
  @Override
  public MetaTag getDescription() {
    return this.description;
  }
  
  @Override
  public Link getIco(){
    return this.ico;
  }
  
  @Override
  public Link getIcon() {
    return this.icon;
  }
  
  @Override
  public TitleTag getTitle() {
    return this.title;
  }
  
  @Override
  public BodyTag getBody() {
    return this.body;
  }
  
  @Override
  public NodeList getNodeList(){
    return this.nodeList;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getName());
    builder.append(". URL: ").append(this.url);
    builder.append(", Nodes: ").append(this.nodeList == null ? null : Integer.valueOf(this.nodeList.size()));
    return builder.toString();
  }
}

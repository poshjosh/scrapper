package com.scrapper.filter;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.HasBounds;
import com.scrapper.config.Config;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

public class BoundsMarker
  implements HasBounds, Serializable
{
  private boolean started;
  private boolean done;
  private String id;
  private NodeFilter startAtFilter;
  private NodeFilter stopAtFilter;
  
  public BoundsMarker() {}
  
  public BoundsMarker(String id, NodeFilter startAt, NodeFilter stopAt)
  {
    this.id = id;
    this.startAtFilter = startAt;
    this.stopAtFilter = stopAt;
  }
  
  public static BoundsMarker newInstance(JsonConfig config, String propertyKey)
  {
    if ((propertyKey == null) || (propertyKey.isEmpty())) {
      throw new NullPointerException();
    }
    
    FilterFactory.FilterType[] types = FilterFactory.getTagNameAndAttributesFilter(config, new Object[] { propertyKey });
    
    NodeFilter startAtFilter = getGroup(config, propertyKey, "startAtNode", types);
    
    NodeFilter stopAtFilter = getGroup(config, propertyKey, "stopAtNode", types);
    
    if ((startAtFilter == null) && (stopAtFilter == null)) {
      return null;
    }
    
    BoundsMarker instance = new BoundsMarker(propertyKey, startAtFilter, stopAtFilter);
    
    return instance;
  }
  
  public void reset()
  {
    this.done = false;
    this.started = false;
  }
  
  public void visitStartTag(Node node)
  {
    if (isDone()) { return;
    }
    if (!isStarted()) {
      this.started = ((this.startAtFilter == null) || (this.startAtFilter.accept(node)));
      if (this.started) {
        XLogger.getInstance().log(Level.FINER, "Started at Node: {0}", getClass(), node);
      }
    }
    else {
      this.done = ((this.stopAtFilter != null) && (this.stopAtFilter.accept(node)));
      if (this.done) {
        XLogger.getInstance().log(Level.FINER, "Ended at Node: {0}", getClass(), node);
      }
    }
  }

  private static NodeFilter getGroup(JsonConfig props, String pathElement0, Object pathElement1, FilterFactory.FilterType[] types)
  {
    int maxFiltersPerKey = props.getInt(new Object[] { Config.Extractor.maxFiltersPerKey }).intValue();
    
    ArrayList<NodeFilter> filters = new ArrayList();
    
    Object[] fullPath = { pathElement0, null };
    
    for (int i = 0; i < maxFiltersPerKey; i++)
    {

      fullPath[(fullPath.length - 1)] = (pathElement1.toString() + i);
      
      NodeFilter filter = FilterFactory.get(props, fullPath, types, false);
      
      if (filter != null)
      {
        filters.add(filter);
      }
    }
    return FilterFactory.get(filters, true);
  }
  
  public boolean isDone()
  {
    return this.done;
  }
  
  public boolean isStarted()
  {
    return this.started;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return this.id;
  }
  
  public NodeFilter getStartAtFilter() {
    return this.startAtFilter;
  }
  
  public void setStartAtFilter(NodeFilter startAtFilter) {
    this.startAtFilter = startAtFilter;
  }
  
  public NodeFilter getStopAtFilter() {
    return this.stopAtFilter;
  }
  
  public void setStopAtFilter(NodeFilter stopAtFilter) {
    this.stopAtFilter = stopAtFilter;
  }
}

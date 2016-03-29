package com.scrapper.filter;

import com.bc.util.XLogger;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

public class BoundsFilter
  extends BoundsMarker
  implements FilterHasBounds
{
  private boolean strict;
  private NodeFilter filter;
  
  public BoundsFilter() {}
  
  public BoundsFilter(String id, NodeFilter f0, NodeFilter startAt, NodeFilter stopAt)
  {
    super(id, startAt, stopAt);
    this.filter = f0;
  }
  
  public boolean accept(Node node)
  {
    visitStartTag(node);
    
    if (isDefaultAccept(node)) {
      return true;
    }
    
    boolean boundsAccept = (isStarted()) && (!isDone());
    
    boolean filterAccept = (this.filter == null) || (this.filter.accept(node));

    boolean accepted;
    if (isStrict())
    {
      accepted = (filterAccept) && (boundsAccept);
    }
    else
    {
      accepted = (filterAccept) || (boundsAccept);
    }
    
    XLogger.getInstance().log(Level.FINER, "{0}-BoundsFilter, Accepted by, Filter: {1}, BoundsMarker: {2}, Node: {3}", getClass(), getId(), Boolean.valueOf(filterAccept), Boolean.valueOf(boundsAccept), node);
    


    return accepted;
  }
  
  protected boolean isDefaultAccept(Node node) {
    return false;
  }
  
  public boolean isStrict() {
    return this.strict;
  }
  
  public void setStrict(boolean strict) {
    this.strict = strict;
  }
  
  public NodeFilter getFilter() {
    return this.filter;
  }
  
  public void setFilter(NodeFilter filter) {
    this.filter = filter;
  }
}

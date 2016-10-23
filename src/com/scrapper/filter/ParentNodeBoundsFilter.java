package com.scrapper.filter;

import com.bc.json.config.JsonConfig;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;

public class ParentNodeBoundsFilter extends BoundsFilter
{
  public ParentNodeBoundsFilter(JsonConfig props, boolean parentFilterIsDirectMode)
  {
    setId("parentNode");
    
    NodeFilter filter = FilterFactory.newDefaultFilter(props, new Object[] { getId() });
    setFilter(filter);
    
    BoundsMarker boundsMarker = BoundsMarker.newInstance(props, getId());
    if (boundsMarker != null) {
      setStartAtFilter(boundsMarker.getStartAtFilter());
      setStopAtFilter(boundsMarker.getStopAtFilter());
    }
    
    setStrict(false);
    
    if (!parentFilterIsDirectMode)
    {
      NodeFilter target = getFilter();
      
      HasChildFilter hasChild = new HasChildFilter();
      hasChild.setChildFilter(target);
      hasChild.setRecursive(true);
      
      HasParentFilter hasParent = new HasParentFilter();
      hasParent.setParentFilter(target);
      hasParent.setRecursive(true);
      
      OrFilter orFilter = new OrFilter();
      orFilter.setPredicates(new NodeFilter[] { target, hasChild, hasParent });
      

      setFilter(orFilter);
    }
  }
  
  /**
   * @deprecated
   */
  public NodeFilter getParentFilter()
  {
    return getFilter();
  }
  
  /**
   * @deprecated
   */
  public void setParentFilter(NodeFilter filter)
  {
    setFilter(filter);
  }
}

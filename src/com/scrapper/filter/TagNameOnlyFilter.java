package com.scrapper.filter;

import java.util.Collection;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;

















public class TagNameOnlyFilter
  extends TagNameFilter
{
  public TagNameOnlyFilter()
  {
    this("");
  }
  




  public TagNameOnlyFilter(String name)
  {
    super(name);
  }
  





  public boolean accept(Node node)
  {
    if ((node instanceof Tag)) {
      Collection attributes = ((Tag)node).getAttributesEx();
      

      if ((attributes != null) && (attributes.size() > 1)) return false;
    }
    return super.accept(node);
  }
}

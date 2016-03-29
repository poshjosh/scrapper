package com.scrapper.tag;

import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.IsEqualFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.TableRow;
import org.htmlparser.util.NodeList;

public class NoscriptTag
  extends CompositeTag
{
  private static final String[] mIds = { "NOSCRIPT" };
  
  private static final String[] mEndTagEnders = { "BODY", "HTML" };
  
  public String[] getIds()
  {
    return mIds;
  }
  
  public String[] getEndTagEnders()
  {
    return mEndTagEnders;
  }
  
  public String toString()
  {
    return "Noscript\n********\n" + toHtml();
  }
}

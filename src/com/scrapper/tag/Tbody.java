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

public class Tbody
  extends CompositeTag
{
  private static final String[] mIds = { "TBODY" };
  



  private static final String[] mEnders = { "TBODY", "TFOOT", "THEAD", "BODY", "HTML" };
  



  private static final String[] mEndTagEnders = { "TFOOT", "THEAD", "TABLE", "BODY", "HTML" };
  









  public String[] getIds()
  {
    return mIds;
  }
  




  public String[] getEndTagEnders()
  {
    return mEndTagEnders;
  }
  









  public TableRow[] getRows()
  {
    NodeList kids = getChildren();
    TableRow[] ret; if (null != kids)
    {
      NodeClassFilter cls = new NodeClassFilter(Tbody.class);
      HasParentFilter recursion = new HasParentFilter(null);
      NodeFilter filter = new OrFilter(new AndFilter(cls, new IsEqualFilter(this)), new AndFilter(new NotFilter(cls), recursion));
      
      recursion.setParentFilter(filter);
      kids = kids.extractAllNodesThatMatch(new AndFilter(new NodeClassFilter(TableRow.class), filter), true);
      
      ret = new TableRow[kids.size()];
      kids.copyToNodeArray(ret);
    }
    else {
      ret = new TableRow[0];
    }
    return ret;
  }
  





  public int getRowCount()
  {
    return getRows().length;
  }
  







  public TableRow getRow(int index)
  {
    TableRow[] rows = getRows();
    TableRow ret; 
    if (index < rows.length) {
      ret = rows[index];
    } else {
      ret = null;
    }
    return ret;
  }
  




  public String toString()
  {
    return "Tbody\n********\n" + toHtml();
  }
}

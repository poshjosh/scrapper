package com.scrapper.tag;

import org.htmlparser.tags.CompositeTag;

public class Tfoot
  extends CompositeTag
{
  private static final String[] mIds = { "TFOOT" };
  
  private static final String[] mEnders = { "TFOOT", "TABLE", "BODY", "HTML" };
  
  private static final String[] mEndTagEnders = { "TABLE", "BODY", "HTML" };
  
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
    return "Tbody\n********\n" + toHtml();
  }
}

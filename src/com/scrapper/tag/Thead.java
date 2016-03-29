package com.scrapper.tag;

import org.htmlparser.tags.CompositeTag;

public class Thead
  extends CompositeTag
{
  private static final String[] mIds = { "THEAD" };
  



  private static final String[] mEnders = { "TFOOT", "THEAD", "TABLE", "BODY", "HTML" };
  



  private static final String[] mEndTagEnders = { "TFOOT", "TABLE", "BODY", "HTML" };
  









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

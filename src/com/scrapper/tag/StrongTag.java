package com.scrapper.tag;

import org.htmlparser.tags.CompositeTag;














public class StrongTag
  extends CompositeTag
{
  private static final String[] mIds = { "STRONG" };
  



  private static final String[] mEndTagEnders = { "BODY", "HTML" };
  














  public String[] getIds()
  {
    return mIds;
  }
  




  public String[] getEndTagEnders()
  {
    return mEndTagEnders;
  }
}

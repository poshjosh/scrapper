package com.scrapper.tag;

import org.htmlparser.tags.CompositeTag;















public class ArticleTag
  extends CompositeTag
{
  private static final String[] mIds = { "ARTICLE" };
  



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

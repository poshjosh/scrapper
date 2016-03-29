package com.scrapper.tag;

import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.ParserUtils;













public class Link
  extends TagNode
{
  private static final String[] mIds = { "LINK" };
  





  protected String mLink;
  





  public String[] getIds()
  {
    return mIds;
  }
  







  public String getLink()
  {
    if (null == this.mLink)
    {
      this.mLink = extractLink();
    }
    return this.mLink;
  }
  




  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Link to : " + getLink() + "; begins at : " + getStartPosition() + "; ends at : " + getEndPosition() + "\n");
    return sb.toString();
  }
  




  public void setLink(String link)
  {
    this.mLink = link;
    setAttribute("HREF", link);
  }
  







  public String extractLink()
  {
    String ret = getAttribute("HREF");
    if (null != ret)
    {
      ret = ParserUtils.removeChars(ret, '\n');
      ret = ParserUtils.removeChars(ret, '\r');
    }
    if (null != getPage()) {
      ret = getPage().getAbsoluteURL(ret);
    }
    return ret;
  }
}

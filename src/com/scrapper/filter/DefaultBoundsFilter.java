package com.scrapper.filter;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;

public class DefaultBoundsFilter
  extends ParentNodeBoundsFilter
{
  private boolean withinHtml;
  private boolean withinHead;
  private boolean withinTitle;
  private boolean withinBody;
  
  public DefaultBoundsFilter(JsonConfig site)
  {
    super(site, false);
  }
  
  public void reset()
  {
    super.reset();
    this.withinBody = false;
    this.withinHead = false;
    this.withinHtml = false;
    this.withinTitle = false;
  }
  

  public boolean accept(Node node)
  {
    boolean accept = super.accept(node);
    
    if (accept) {
      accept = isValidContent(node);
    }
    
    return accept;
  }
  

  protected boolean isDefaultAccept(Node node)
  {
    if (!(node instanceof Tag)) {
      return false;
    }
    
    Tag tag = (Tag)node;
    















    String tagName = tag.getTagName();
    
    boolean defaultAccept = (tagName.equals("HTML")) || (tagName.equals("HEAD")) || (tagName.equals("TITLE"));
    



    if (defaultAccept) {
      XLogger.getInstance().log(Level.FINER, "DEFAULT Accept: {0}", getClass(), tagName);
    }
    

    return defaultAccept;
  }
  



  private boolean isComment(Node node)
  {
    if (((node instanceof Text)) || ((node instanceof Remark))) {
      String text = node.getText();
      int n = text.indexOf("<!--");
      return (n != -1) && (text.indexOf("-->", n) != -1);
    }
    return false;
  }
  

  private boolean isValidContent(Node node)
  {
    if (isDefaultAccept(node)) {
      return true;
    }
    
    Tag tag;
    
    if ((node instanceof Tag)) {
      tag = (Tag)node;
    } else {
      return !isComment(node);
    }

    if (this.withinTitle) {
      this.withinTitle = false;
    }
    
    if ((!this.withinHtml) && (tag.getTagName().equals("HTML"))) {
      this.withinHtml = true;
    } else if ((!this.withinHead) && (tag.getTagName().equals("HEAD"))) {
      this.withinHead = true;
    } else if ((!this.withinTitle) && (tag.getTagName().equals("TITLE"))) {
      this.withinTitle = true;
    } else if ((!this.withinBody) && (tag.getTagName().equals("BODY"))) {
      this.withinTitle = false;
      this.withinHead = false;
      this.withinBody = true;
    }
    


    boolean accept = (this.withinTitle) || (this.withinBody);
    
    XLogger.getInstance().log(Level.FINER, "Within title: {0}, body: {1}, Node: {2}", getClass(), Boolean.valueOf(this.withinTitle), Boolean.valueOf(this.withinBody), tag);
    

    return accept;
  }
}

package com.scrapper.extractor;

import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.ParserException;








public abstract class TargetNodeExtractor
  extends PageExtractor
{
  private Tag currentTargetNode;
  private boolean doneCurrentTargetNode;
  private transient NodeFilter filter;
  
  public TargetNodeExtractor(CapturerContext context)
  {
    super(context);
  }
  
  public void reset()
  {
    super.reset();
    this.currentTargetNode = null;
    this.doneCurrentTargetNode = false;
  }
  

  protected abstract NodeExtractorOld getTargetNodeVisitor();
  
  protected abstract NodeFilter getTargetNodeFilter();
  
  public void visitTag(Tag tag)
  {
    if (isDone()) { return;
    }
    

    super.visitTag(tag);
    
    if (isNode("targetNode", tag))
    {
      this.currentTargetNode = tag;
      
      extractData(this.currentTargetNode);
    }
  }
  




  public void visitEndTag(Tag tag)
  {
    if (isDone()) { return;
    }
    

    super.visitEndTag(tag);
    
    if (isNode("targetNode", tag))
    {
      this.currentTargetNode = null;
    }
  }
  


  public void visitStringNode(Text node)
  {
    if (isDone()) { return;
    }
    




    super.visitStringNode(node);
  }
  

  public void visitRemarkNode(Remark remark)
  {
    if (isDone()) return;
    super.visitRemarkNode(remark);
  }
  
  protected void extractData(Tag node)
  {
    this.doneCurrentTargetNode = false;
    
    if (!accept(node)) { return;
    }
    doExtractData(node);
    
    this.doneCurrentTargetNode = true;
  }
  


  protected boolean accept(Tag node)
  {
    boolean accepted = (withinTargetNode()) && (!this.doneCurrentTargetNode);
    



    return accepted;
  }
  




  protected void doExtractData(Tag node)
  {
    Map data = null;
    try {
      data = getTargetNodeVisitor().extractData(node);
    } catch (ParserException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    

    if ((data == null) || (data.isEmpty())) { return;
    }
    Iterator iter = data.keySet().iterator();
    
    while (iter.hasNext())
    {
      String key = (String)iter.next();
      Object val = data.get(key);
      
      add(key, val);
    }
  }
  
  protected boolean withinTargetNode() {
    return this.currentTargetNode != null;
  }
  
  protected boolean isNode(String key, Tag node)
  {
    if (this.filter == null) {
      this.filter = getTargetNodeFilter();
    }
    if (this.filter == null) { return true;
    }
    

    return false;
  }
}

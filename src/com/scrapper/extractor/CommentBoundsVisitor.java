package com.scrapper.extractor;

import com.bc.util.XLogger;
import com.scrapper.HasBounds;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.visitors.AbstractNodeVisitor;













public class CommentBoundsVisitor
  extends AbstractNodeVisitor
  implements HasBounds, Serializable
{
  private boolean doneAtNextNode;
  private boolean started;
  private boolean done;
  private String id;
  
  public CommentBoundsVisitor() {}
  
  public CommentBoundsVisitor(String id)
  {
    this.id = id;
  }
  
  public void reset()
  {
    this.doneAtNextNode = false;
    this.done = false;
    this.started = false;
  }
  
  public void visitTag(Tag tag)
  {
    updateComment(tag);
  }
  
  public void visitEndTag(Tag tag)
  {
    updateComment(tag);
  }
  
  public void visitStringNode(Text string)
  {
    updateComment(string);
  }
  
  public void visitRemarkNode(Remark remark)
  {
    updateComment(remark);
  }
  
  private void updateComment(Node node)
  {
    if (this.doneAtNextNode) {
      this.done = true;
      this.doneAtNextNode = false;
    }
    
    String text = node.getText();
    
    int n = text.indexOf("<!--");
    
    if ((!this.started) && (n != -1))
    {
      XLogger.getInstance().log(Level.FINER, "Entering Comment: {0}", getClass(), node);
      

      this.started = true;
      
      if (text.indexOf("-->", n) != -1) {
        this.doneAtNextNode = true;
      }
    }
    else if ((this.started) && (text.contains("-->"))) {
      XLogger.getInstance().log(Level.FINER, "Exiting Comment: {0}", getClass(), node);
      

      this.done = true;
    }
  }
  
  public boolean isComment(Node node) {
    if (((node instanceof Text)) || ((node instanceof Remark))) {
      String text = node.getText();
      int n = text.indexOf("<!--");
      return (n != -1) && (text.indexOf("-->", n) != -1);
    }
    return false;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public boolean isDone()
  {
    return this.done;
  }
  
  public boolean isStarted()
  {
    return this.started;
  }
}

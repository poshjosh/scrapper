package com.scrapper.extractor;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.HasBounds;
import com.scrapper.filter.BoundsMarker;
import com.scrapper.filter.FilterFactory;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.visitors.NodeVisitor;

public class BoundsVisitor
  extends NodeVisitor
  implements HasBounds, Serializable
{
  private boolean strict;
  private boolean started;
  private boolean done;
  private boolean endAtNext;
  private String id;
  private Tag startTag;
  private Tag endTag;
  private NodeFilter filter;
  private BoundsMarker boundsMarker;
  
  public BoundsVisitor(JsonConfig props, String propertyKey, FilterFactory.FilterType[] types)
  {
    this.id = propertyKey;
    
    this.filter = FilterFactory.get(props, propertyKey, types);
    
    this.boundsMarker = BoundsMarker.newInstance(props, propertyKey);
    
    XLogger.getInstance().log(Level.FINER, "{0}-BoundsFilter filter: {1}, startStopFilter: {2}", getClass(), propertyKey, this.filter, this.boundsMarker);
  }
  
  public BoundsVisitor(NodeFilter f0, BoundsMarker f1)
  {
    this.filter = f0;
    this.boundsMarker = f1;
    if (this.boundsMarker != null) {
      this.id = f1.getId();
    }
  }
  
  public BoundsVisitor(String id, NodeFilter f0, NodeFilter startAt, NodeFilter stopAt) {
    this.id = id;
    this.filter = f0;
    if ((startAt != null) && (stopAt != null)) {
      this.boundsMarker = new BoundsMarker(id, startAt, stopAt);
    }
  }
  
  public void reset()
  {
    if (this.boundsMarker != null) {
      this.boundsMarker.reset();
    }
    this.done = false;
    this.endAtNext = false;
    this.endTag = null;
    this.startTag = null;
    this.started = false;
  }
  
  public void visitTag(Tag tag)
  {
    if (this.endAtNext) {
      this.done = true;
    }
    
    if (isDone()) {
      return;
    }
    
    if (this.boundsMarker != null) {
      this.boundsMarker.visitStartTag(tag);
    }
    
    if (isStarted()) {
      return;
    }
    
    boolean boundsAccept;
    if (this.boundsMarker != null) {
      boundsAccept = (this.boundsMarker.isStarted()) && (!this.boundsMarker.isDone());
    } else {
      boundsAccept = true;
    }
    
    boolean filterAccept = (this.filter == null) || (this.filter.accept(tag));
    
    XLogger.getInstance().log(Level.FINEST, "{0}-BoundsVisitor, Accepted by, Filter: {1}, BoundsMarker: {2}, Node: {3}", getClass(), this.id, Boolean.valueOf(filterAccept), Boolean.valueOf(boundsAccept), tag.getTagName());
    


    if (isStrict())
    {
      this.started = ((filterAccept) && (boundsAccept));
    }
    else
    {
      this.started = ((filterAccept) || (boundsAccept));
    }
    
    if (this.started)
    {
      XLogger.getInstance().log(Level.FINER, "{0}-BoundsVisitor Started at: {1}", getClass(), this.id, tag);
      

      this.startTag = tag;
      
      this.endTag = tag.getEndTag();
      




      if (this.endTag == null) {
        this.endAtNext = true;
      }
    }
  }
  



  public void visitEndTag(Tag tag)
  {
    if (this.endAtNext) {
      this.done = true;
    }
    
    if (isDone()) {
      return;
    }
    
    if (!isStarted()) {
      return;
    }
    
    boolean foundEnd = false;
    if (tag.equals(this.endTag)) {
      foundEnd = true;
    }
    
    XLogger.getInstance().log(Level.FINER, "{0}-BoundsVisitor Done at: {1}", getClass(), this.id, Boolean.valueOf(foundEnd));
    

    this.done = foundEnd;
  }
  
  public void visitStringNode(Text string)
  {
    if (this.endAtNext) {
      this.done = true;
    }
  }
  
  public void visitRemarkNode(Remark remark)
  {
    if (this.endAtNext) {
      this.done = true;
    }
  }
  
  public boolean isDone()
  {
    return this.done;
  }
  
  public boolean isStarted()
  {
    return this.started;
  }
  
  protected void setEndTag(Tag endTag) {
    this.endTag = endTag;
  }
  
  public Tag getEndTag() {
    return this.endTag;
  }
  
  protected void setStartTag(Tag startTag) {
    this.startTag = startTag;
  }
  
  public Tag getStartTag() {
    return this.startTag;
  }
  
  public BoundsMarker getBoundsMarker() {
    return this.boundsMarker;
  }
  
  public NodeFilter getFilter() {
    return this.filter;
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public boolean isStrict() {
    return this.strict;
  }
  
  public void setStrict(boolean strict) {
    this.strict = strict;
  }
  
  public boolean isEndAtNext() {
    return this.endAtNext;
  }
  
  protected void setEndAtNext(boolean endAtNext) {
    this.endAtNext = endAtNext;
  }
}

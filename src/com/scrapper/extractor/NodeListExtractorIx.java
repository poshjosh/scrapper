package com.scrapper.extractor;

import com.bc.webdatex.extractor.DataExtractor;
import com.bc.task.StoppableTask;
import java.io.Serializable;
import java.util.Map;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public interface NodeListExtractorIx 
    extends DataExtractor<NodeList>, Serializable, StoppableTask {
    
  public abstract Map extractData(NodeList paramNodeList)
    throws ParserException;
  
  public abstract NodeList getSource();
  
  public abstract String getTaskName();
  
  public abstract boolean isCompleted();
  
  public abstract boolean isStarted();
  
  public abstract boolean isStopRequested();
  
  public abstract boolean isStopped();
  
  public abstract void reset();
  
  public abstract void run();
  
  public abstract void setSource(NodeList paramNodeList);
  
  public abstract void stop();
  
  public abstract String toString();
  
  public abstract void visitEndTag(Tag paramTag);
  
  public abstract void visitRemarkNode(Remark paramRemark);
  
  public abstract void visitStringNode(Text paramText);
  
  public abstract void visitTag(Tag paramTag);
}

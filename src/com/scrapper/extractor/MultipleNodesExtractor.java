package com.scrapper.extractor;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.bc.webdatex.extractor.NodeExtractor;
import com.bc.webdatex.extractor.NodeExtractorIx;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.CapturerSettings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;

public class MultipleNodesExtractor
  extends PageExtractor
  implements MultipleNodesExtractorIx
{
  private Map<String, NodeExtractor> nodeVisitors;
  private Map<String, Object[]> columns;
  private Map<String, MappingsExtractor> mappingsExtractors;
  private final CommentBoundsVisitor withinComments;
  
  public MultipleNodesExtractor(CapturerContext context)
  {
    super(context);
    
    JsonConfig config = context.getConfig();
    

    this.nodeVisitors = new HashMap()
    {
      public NodeExtractor put(String key, NodeExtractor value) {
        if ((key == null) || (value == null)) throw new NullPointerException();
        return (NodeExtractor)super.put(key, value);
      }
      
    };
    this.columns = new HashMap()
    {
      public Object[] put(String key, Object[] value) {
        if ((key == null) || (value == null)) throw new NullPointerException();
        return (Object[])super.put(key, value);

      }
      

    };
    String parentNode = config.getString(new Object[] { "parentNode", "value" });
    if (parentNode != null) {
      addExtractor("parentNode");
    }
    
    int maxFiltersPerKey = config.getInt(new Object[] { Config.Extractor.maxFiltersPerKey }).intValue();
    


    for (int i = 0; i < maxFiltersPerKey; i++)
    {
      String propertyKey = "targetNode" + i;
      
      addExtractor(propertyKey);
      
      MappingsExtractor mappingsExt = MappingsExtractor.getInstance(propertyKey, config);
      
      if (mappingsExt != null)
      {


        if (this.mappingsExtractors == null) {
          this.mappingsExtractors = new HashMap();
        }
        this.mappingsExtractors.put(propertyKey, mappingsExt);
      }
    }
    this.withinComments = new CommentBoundsVisitor();
  }
  

  public void reset()
  {
    super.reset();
    
    this.withinComments.reset();
    
    for (NodeExtractorIx extractor : this.nodeVisitors.values()) {
      extractor.reset();
    }
  }
  
  public boolean isSuccessfulCompletion()
  {
    Set<String> cols = null;
    try {
      cols = ((ScrapperConfig)getCapturerConfig()).getColumns(); 
    } catch (ClassCastException ignored) {}
    boolean output;
    if (cols != null) {
      output = getExtractedData().size() >= cols.size();
    } else {
      output = getFailedNodeExtractors().isEmpty();
    }
    return output;
  }
  

  public void finishedParsing()
  {
    for (String name : this.nodeVisitors.keySet())
    {
      NodeExtractorIx extractor = (NodeExtractorIx)this.nodeVisitors.get(name);
      

      extractor.finishedParsing();
      
      Object[] cols = (Object[])this.columns.get(name);
      
      boolean append = extractor.isConcatenateMultipleExtracts();
      
      if (cols != null)
      {
        for (Object column : cols)
        {
          XLogger.getInstance().log(Level.FINEST, "Extractor: {0}", getClass(), name);
          
          add(column.toString(), extractor.getExtract(), append, false);
        }
      }
    }
    XLogger.getInstance().log(Level.FINER, "Extractors: {0}, Extracted data: {1}", getClass(), Integer.valueOf(this.nodeVisitors.size()), Integer.valueOf(getExtractedData().size()));
  }
  

  public Set<String> getFailedNodeExtractors()
  {
    HashSet<String> failed = new HashSet();
    Set keys = getExtractedData().keySet();
    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractorIx extractor = (NodeExtractorIx)this.nodeVisitors.get(key);
      Object[] cols = (Object[])this.columns.get(key);
      if (cols != null) {
        for (Object col : cols) {
          if (!keys.contains(col)) {
            failed.add(extractor.getId());
            break;
          }
        }
      }
    }
    return failed;
  }
  
  public Set<String> getSuccessfulNodeExtractors()
  {
    Set<String> failed = getFailedNodeExtractors();
    Set<String> all = getNodeExtractorIds();
    all.removeAll(failed);
    return all;
  }
  
  public Set<String> getNodeExtractorIds()
  {
    return new HashSet(this.nodeVisitors.keySet());
  }
  

  public void visitTag(Tag tag)
  {
    XLogger.getInstance().log(Level.FINER, "visitTag: {0}", getClass(), tag);
    
    this.withinComments.visitTag(tag);
    
    if (rejectComment(tag)) {
      return;
    }
    XLogger.getInstance().log(Level.FINER, "Extracting with: {0}", getClass(), this.nodeVisitors.keySet());
    

    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractorIx extractor = (NodeExtractorIx)this.nodeVisitors.get(key);
      extractor.visitTag(tag);
    }
  }
  

  public void visitEndTag(Tag tag)
  {
    XLogger.getInstance().log(Level.FINER, "visitEndTag: {0}", getClass(), tag);
    
    this.withinComments.visitEndTag(tag);
    
    if (rejectComment(tag)) {
      return;
    }
    
    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractorIx extractor = (NodeExtractorIx)this.nodeVisitors.get(key);
      extractor.visitEndTag(tag);
    }
  }
  

  public void visitStringNode(Text node)
  {
    XLogger.getInstance().log(Level.FINER, "visitStringNode: {0}", getClass(), node);
    
    this.withinComments.visitStringNode(node);
    
    if (rejectComment(node)) {
      return;
    }
    
    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractorIx extractor = (NodeExtractorIx)this.nodeVisitors.get(key);
      extractor.visitStringNode(node);
    }
    
    if (this.mappingsExtractors == null) {
      return;
    }
    
    Map m;
    for (String id : this.mappingsExtractors.keySet())
    {
      MappingsExtractor extractor = (MappingsExtractor)this.mappingsExtractors.get(id);
      
      String text = node.getText();
//      text = Translate.decode(text);
      
      m = extractor.extractData(text);
      
      if (m != null)
      {
        for (Object key : m.keySet())
        {
          add(key.toString(), m.get(key), false, false);
        }
      }
    }
  }
  
  public void visitRemarkNode(Remark node)
  {
    XLogger.getInstance().log(Level.FINER, "visitRemarkNode: {0}", getClass(), node);
    
    this.withinComments.visitRemarkNode(node);
  }
  
  public NodeExtractor getExtractor(String id)
  {
    return (NodeExtractor)this.nodeVisitors.get(id);
  }
  

  public NodeExtractor createExtractor(String id)
  {
    NodeExtractor extractor = new NodeExtractor();
    
    CapturerSettings ss = getCapturerContext().getSettings();
    
    extractor.setAcceptScripts(false);
    extractor.setAttributesToAccept(ss.getAttributesToAccept(id));
    extractor.setAttributesToExtract(ss.getAttributesToExtract(id));
    extractor.setConcatenateMultipleExtracts(ss.isConcatenateMultipleExtracts(id).booleanValue());
    extractor.setEnabled(true);
    extractor.setExtractAttributes(ss.isExtractAttributes(id));
    extractor.setId(id);
    extractor.setNodesToRetainAttributes(ss.getNodesToRetainAttributes(id));
    extractor.setNodeTypesToAccept(ss.getNodeTypesToAccept(id));
    extractor.setNodeTypesToReject(ss.getNodeTypesToReject(id));
    extractor.setNodesToAccept(ss.getNodesToAccept(id));
    extractor.setNodesToReject(ss.getNodeToReject(id));
    extractor.setPath(ss.getTransverse(id));
    extractor.setReplaceNonBreakingSpace(ss.isReplaceNonBreakingSpace(id));
    
    extractor.setTextToAccept(null);
    extractor.setTextToDisableOn(ss.getTextToDisableOn(id));
    extractor.setTextToReject(ss.getTextToReject(id));
    
    return extractor;
  }
  
  private void addExtractor(String id)
  {
    CapturerSettings cs = getCapturerContext().getSettings();
    
    Object[] cols = cs.getColumns(id);
    
    if (cols == null) {
      XLogger.getInstance().log(Level.FINE, "{0}.{1} == null", getClass(), id, Config.Extractor.columns);
    } else {
      this.columns.put(id, cols);
      NodeExtractor extractor = createExtractor(id);
      this.nodeVisitors.put(id, extractor);
    }
    
    XLogger.getInstance().log(Level.FINER, "Added Extractor for property key: {0}", getClass(), id);
  }
  
  private boolean rejectComment(Node node) {
    boolean reject = (this.withinComments.isStarted()) && (!this.withinComments.isDone());
    if (!reject) {
      reject = this.withinComments.isComment(node);
    }
    return reject;
  }
}

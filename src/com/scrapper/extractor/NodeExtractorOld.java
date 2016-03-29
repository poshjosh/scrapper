package com.scrapper.extractor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.htmlparser.Tag;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;












public class NodeExtractorOld
  extends NodeVisitor
  implements DataExtractor<Tag>, Serializable
{
  private Map extractedData;
  
  public NodeExtractorOld()
  {
    this.extractedData = new HashMap();
  }
  

  public void reset()
  {
    this.extractedData = new HashMap();
  }
  
  public Map extractData(Tag tag) throws ParserException
  {
    reset();
    tag.accept(this);
    return this.extractedData;
  }
  
  protected Map getExtractedData() {
    return this.extractedData;
  }
}

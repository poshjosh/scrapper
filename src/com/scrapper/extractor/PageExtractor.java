package com.scrapper.extractor;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.CapturerSettings;
import com.scrapper.util.Util;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
















public class PageExtractor
  extends NodeListExtractor
  implements PageExtractorIx
{
  private boolean done;
  private String pageTitle;
  private Tag titleTag;
  private boolean titleExtracted;
  private final CapturerContext context;
  
  public PageExtractor(CapturerContext context)
  {
    this.context = context;
    
    XLogger.getInstance().log(Level.FINE, "Site name: {0}", getClass(), context.getConfig().getName());
  }
  

  public void reset()
  {
    super.reset();
    this.done = false;
    this.pageTitle = null;
    this.titleTag = null;
    this.titleExtracted = false;
  }
  

  public void visitTag(Tag tag)
  {
    if (this.done) { return;
    }
    XLogger.getInstance().log(Level.FINER, "visitTag: {0}", getClass(), tag);
    
    super.visitTag(tag);
    
    if (tag.getTagName().equals("TITLE")) {
      this.titleTag = tag;
    }
  }
  

  public void visitEndTag(Tag tag)
  {
    if (this.done) { return;
    }
    XLogger.getInstance().log(Level.FINER, "visitEndTag: {0}", getClass(), tag);
    
    super.visitEndTag(tag);
    
    if (tag.getTagName().equals("TITLE")) {
      this.titleTag = null;
    }
  }
  

  public void visitStringNode(Text node)
  {
    if (this.done) { return;
    }
    XLogger.getInstance().log(Level.FINER, "#visitStringNode: {0}", getClass(), node);
    
    super.visitStringNode(node);
    
    extractTitle(node);
  }
  
  public void visitRemarkNode(Remark remark)
  {
    if (this.done) return;
    super.visitRemarkNode(remark);
  }
  


  private boolean extractTitle(Text node)
  {
    if ((!this.titleExtracted) && (withinTitleTag()))
    {


      this.titleExtracted = true;
      
      doExtractTitle(node);
      
      this.titleTag = null;
      
      return true;
    }
    
    return false;
  }
  



  protected String add(String key, Object val)
  {
    return add(key, val, getCapturerSettings().isConcatenateMultipleExtracts().booleanValue(), true);
  }
  




  protected String add(String key, Object val, boolean append, boolean guessColumnNameFromKey)
  {
    XLogger.getInstance().log(Level.FINER, "#add. Append: {0}, Key: {1}, Val: {2}", getClass(), Boolean.valueOf(append), key, val);
    

    if ((key == null) || (val == null)) { return null;
    }
    if ((key.trim().isEmpty()) || (val.toString().trim().isEmpty())) { return null;
    }
    String col = key;
    
    if (guessColumnNameFromKey)
    {
      Map keys = getCapturerConfig().getMap(new Object[] { "keys" });
      
      col = Util.findValueWithMatchingKey(keys, key);
      
      XLogger.getInstance().log(Level.FINER, "#add. Key: {0}, Matching col: {1}", getClass(), key, col);
    }
    

    if (col == null) {
      return null;
    }
    
    doAdd(col, val, append);
    
    return col;
  }
  
  private String doAdd(String col, Object val, boolean append)
  {
    Object oldVal = getExtractedData().get(col);
    
    if (oldVal == null)
    {


      getExtractedData().put(col, val);
      
      XLogger.getInstance().log(Level.FINE, "#doAdd. Added: [{0}={1}]", getClass(), col, val);



    }
    else if (append)
    {
      if (!oldVal.equals(val))
      {
        String lineSep = getCapturerSettings().getLineSeparator();
        String partSep = getCapturerSettings().getPartSeparator();
        if (lineSep != null) {
          val = val.toString().replace("\n", lineSep);
        }
        
        String s = partSep != null ? partSep : "";
        
        String newVal = oldVal + s + val;
        


        getExtractedData().put(col, newVal);
        
        XLogger.getInstance().log(Level.FINE, "#doAdd. Appended: [{0}={1}]", getClass(), col, val);
      }
    }
    

    return col;
  }
  
  protected boolean withinTitleTag() {
    return this.titleTag != null;
  }
  
  protected void doExtractTitle(Text node)
  {
    String title = getTitle(node);
    
    setPageTitle(title);
    
    node.setText(title);
  }
  
  protected String getTitle(Text node)
  {
    String val = node.getText();
    
    String defaultTitle = getCapturerSettings().getDefaultTitle();
    
    if ((val == null) || (val.isEmpty())) { return defaultTitle;
    }
    if ((defaultTitle == null) || (defaultTitle.isEmpty())) { return val;
    }
    return defaultTitle + " | " + val;
  }
  
  public boolean isDone()
  {
    return this.done;
  }
  
  public boolean isTitleExtracted()
  {
    return this.titleExtracted;
  }
  
  public Tag getTitleTag()
  {
    return this.titleTag;
  }
  
  public CapturerContext getCapturerContext()
  {
    return this.context;
  }
  
  public CapturerSettings getCapturerSettings()
  {
    return this.context.getSettings();
  }
  
  public JsonConfig getCapturerConfig()
  {
    return this.context.getConfig();
  }
  
  public void setPageTitle(String pageTitle)
  {
    this.pageTitle = pageTitle;
  }
  
  public String getPageTitle()
  {
    return this.pageTitle;
  }
  
  public String getTaskName()
  {
    return getClass().getName();
  }
}

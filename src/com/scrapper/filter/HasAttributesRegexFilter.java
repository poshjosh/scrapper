package com.scrapper.filter;

import com.bc.util.XLogger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;

public class HasAttributesRegexFilter
  extends HasAttributeFilter
{
  private Pattern pattern;
  
  public HasAttributesRegexFilter(String attribute, String regex)
  {
    super(attribute, regex);
    if (regex != null) {
      this.pattern = Pattern.compile(regex, 2);
    }
  }
  

  public boolean accept(Node node)
  {
    if (!(node instanceof Tag)) { return false;
    }
    Attribute attribute = ((Tag)node).getAttribute(this.mAttribute);
    
    boolean accept = false;
    
    if ((attribute != null) && (this.pattern != null)) {
      accept = this.pattern.matcher(attribute.getValue()).find();
    }
    

    XLogger.getInstance().log(Level.FINEST, "{0}={1}, Accept: {2}, Node: {3}", getClass(), this.mAttribute, this.pattern.pattern(), Boolean.valueOf(accept), node);
    
    return accept;
  }
}

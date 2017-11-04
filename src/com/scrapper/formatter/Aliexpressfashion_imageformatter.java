package com.scrapper.formatter;

import com.bc.util.XLogger;
import com.bc.webdatex.formatter.Formatter;
import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
















public class Aliexpressfashion_imageformatter
  implements Formatter<String>, Serializable
{
  public String apply(String html)
  {
    XLogger.getInstance().log(Level.FINEST, "{0}. HTML: {1}", getClass(), html);
    String link = null;
    
    try
    {
      Parser parser = new Parser(html);
      
      NodeList list = parser.parse(new TagNameFilter("A"));
      


      for (int i = 0; i < list.size(); i++)
      {
        Node node = list.elementAt(i);
        XLogger.getInstance().log(Level.FINER, "{0}. Node: {1}", getClass(), Util.appendTag(node, null));
        if ((node instanceof LinkTag))
        {
          LinkTag linkTag = (LinkTag)node;
          
          link = linkTag.getLink();
          
          break;
        }
      }
    } catch (ParserException e) { XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
    }
    XLogger.getInstance().log(Level.FINE, "{0}. Link; {1}", getClass(), link);
    if (link == null) { return null;
    }
    



    int a = link.lastIndexOf("/");
    int b = link.lastIndexOf(".");
    String number = link.substring(a + 1, b);
    link = link.replace("http://www.aliexpress.com/item-img", "http://1.1.1.4/bmi/i00.i.aliimg.com/wsphoto/v0/" + number);
    
    link = link.replace("/" + number + ".html", ".jpg");
    XLogger.getInstance().log(Level.FINE, "{0}. Formatted link: {1}", getClass(), link);
    return link;
  }
}

package com.scrapper.filter;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;







public class UppercaseFilter
  implements NodeFilter
{
  public boolean accept(Node node)
  {
    String text = node.getText();
    return text.toUpperCase().equals(text);
  }
}

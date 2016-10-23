package com.scrapper.util;

import com.bc.json.config.JsonConfig;
import com.bc.util.QueryParametersConverter;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;

public final class Util {
    
  private static final transient Logger logger = Logger.getLogger(Util.class.getName());
  
  private static final Pattern[] datePatterns;
  
  static {
      
    String months = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec";
    
    Pattern dp1 = Pattern.compile("\\d{1,2}[st|nd|rd|th]*\\s*[of]{0,1}[\\s]*[" + months + "][a-zA-Z]*[\\p{Punct}]?\\s*[\\d]{4}\\.*", 2);
    
    Pattern dp2 = Pattern.compile("[" + months + "][a-zA-Z]*\\s*[\\d]{1,2}[st|nd|rd|th]*[\\p{Punct}]?\\s*[\\d]{4}\\.*", 2);
    
    datePatterns = new Pattern[] { dp1, dp2 }; 
  }
  
  private static final Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
  
  public static String getTableValue(Map parameters, String defaultTableName) {
      
    String[] tableKeys = { "table", "productTable", "producttable", "tableName", "tablename" };
    
    Object oval = null;
    for (String tableKey : tableKeys) {
      oval = parameters.get(tableKey);
      if (oval != null) {
        break;
      }
    }
    XLogger.getInstance().log(Level.FINEST, "Selected {0} from: {1}", Util.class, oval, parameters);
    
    return oval != null ? oval.toString() : defaultTableName;
  }
  
  public static Node deepClone(Node node) throws CloneNotSupportedException {
    return deepClone(node, true, true);
  }
  
  public static Node deepClone(
          Node node, boolean parents, boolean children) 
          throws CloneNotSupportedException {
      
    Node clone = (Node)node.clone();
    
    if (parents) {
        
      Node nodeParent = node.getParent();
      
      Node cloneParent;
      if (nodeParent == null) {
        cloneParent = null;
      } else {
        cloneParent = deepClone(nodeParent, true, false);
      }
      
      clone.setParent(cloneParent);
      
      if (nodeParent != null) {
          
        NodeList nodeSiblings = nodeParent.getChildren();
        
        NodeList cloneSiblings = new NodeList();
        
        for (Node nodeSibling : nodeSiblings) {
            
          Node cloneSibling;
          if (nodeSibling.equals(node)) {
            cloneSibling = clone;
          } else {
            cloneSibling = deepClone(nodeSibling, false, true);
          }
          
          cloneSiblings.add(cloneSibling);
        }
        
        cloneParent.setChildren(cloneSiblings);
      }
    }
    
    if (children) {
        
      NodeList nodeChildren = node.getChildren();
      
      NodeList cloneChildren;
      if (nodeChildren == null) {
          
        cloneChildren = null;
        
      } else {
          
        cloneChildren = new NodeList();
        
        for (Node child : nodeChildren) {
            
          Node childClone = deepClone(child, false, true);
          
          childClone.setParent(clone);
          
          cloneChildren.add(childClone);
        }
      }
      
      clone.setChildren(cloneChildren);
    }
    
    return clone;
  }
  
  public static void insertBefore(Node node, Node toInsert, NodeFilter filter) {
      
    if ((filter != null) && (filter.accept(node))) {
      XLogger.getInstance().log(Level.FINER, "Accepted: {0}", Util.class, node);
      insertBefore(node, toInsert);
    }
    else
    {
      XLogger.getInstance().log(Level.FINER, "Rejected: {0}", Util.class, node);
    }
    

    NodeList children = node.getChildren();
    
    if ((children == null) || (children.size() == 0))
    {
      return;
    }
    

    for (int i = 0; i < children.size(); i++)
    {
      insertBefore(children.elementAt(i), toInsert, filter);
    }
  }
  
  private static boolean insertBefore(Node node, Node toInsert)
  {
    boolean output = false;
    Node parent = node.getParent();
    if (parent != null) {
      NodeList siblings = parent.getChildren();
      int tgtIndex = siblings.indexOf(node);
      NodeList update = new NodeList();
      for (int i = 0; i < siblings.size(); i++) {
        if (i == tgtIndex) {
          update.add(toInsert);
          output = true;
        }
        update.add(siblings.elementAt(i));
      }
      parent.setChildren(update);
    }
    return output;
  }
  
  public static void printIfEquals(Node node, String expectedNodeName)
  {
    Tag tag = null;
    try {
      tag = (Tag)node;
    }
    catch (ClassCastException ignored) {}
    if (tag == null) {
      return;
    }
    
    if (tag.getTagName().equals(expectedNodeName.toUpperCase())) {
      XLogger.getInstance().log(Level.FINEST, "@Util.printIfEquals Tag: {0}", Util.class, tag);
    }
  }
  

  public static int lookForInNode(Node node, String expectedNode, final String expectedChild, final String childAttrKey, final String childAttrVal)
  {
    if (!(node instanceof Tag)) { return -1;
    }
    Tag tag = (Tag)node;
    
    if (tag.getTagName().equals(expectedNode.toUpperCase()))
    {
      NodeList list = node.getChildren();
      
      if (list != null) {
        XLogger.getInstance().log(Level.INFO, "Child count: {0}", Util.class, Integer.valueOf(list.size()));
        NodeList extr = list.extractAllNodesThatMatch(new NodeFilter()
        {
          public boolean accept(Node node) {
            if (!(node instanceof Tag)) return false;
            Tag tag = (Tag)node;
            String name = tag.getTagName();
            if (!name.equals(expectedChild.toUpperCase())) { 
                return false;
            }
            Attribute attr = tag.getAttribute(childAttrKey);
            
            return (attr != null) && (childAttrVal.equals(attr.getValue())); } }, true);
        

        XLogger.getInstance().log(Level.FINEST, "Extract count: {0}", Util.class, Integer.valueOf(extr.size()));
        return extr == null ? -1 : extr.size();
      }
    }
    
    return -1;
  }
  



  public static String toWWWFormat(String url)
    throws MalformedURLException
  {
    String x = "//";
    int n = url.indexOf(x);
    String b;
    String a;
    if (n == -1) {
      a = "";
      b = url;
    } else {
      n += x.length();
      a = url.substring(0, n);
      b = url.substring(n);
    }
    
    String[] parts = b.split("\\.");
    
    if (parts.length == 1) {
      throw new MalformedURLException("Not a URL: " + url);
    }
    
    StringBuilder builder = new StringBuilder(a);
    
    if (parts.length == 2) {
      builder.append("www.").append(parts[0]).append('.').append(parts[1]);
    } else {
      for (int i = 0; i < parts.length; i++) {
        String part = i == 0 ? "www" : parts[i];
        builder.append(part);
        if (i < parts.length - 1) {
          builder.append('.');
        }
      }
    }
    
    return builder.toString();
  }
  
  public static String createURL(String parent, String child)
  {
    child = prepareLink(child);
    

    if (child.startsWith("//")) {
      try
      {
        String s = "http:" + child;
        URL url = new URL(s);
        return s;
      } catch (MalformedURLException e) {
        String base = com.bc.util.Util.getBaseURL(parent);
        if (base == null) {
          base = parent;
        }
        return base + child;
      }
    }
    String base = com.bc.util.Util.getBaseURL(parent);
    if (base == null) {
      base = parent;
    }
    return base + child;
  }
  





  public static String prepareLink(String link)
  {
    link = link.toLowerCase();
    if ((link.startsWith("http://")) || (link.startsWith("file://"))) { return link;
    }
    while (link.startsWith(".")) {
      link = link.substring(1);
    }
    
    if (!link.startsWith("/")) {
      link = "/" + link;
    }
    return link;
  }
  









  public static List<String> getBaseURLs(String urlString)
  {
    String baseURL = com.bc.util.Util.getBaseURL(urlString);
    logger.log(Level.FINER, "{0}. Base url: {1}", new Object[] { logger.getName(), baseURL });
    LinkedList<String> urls = new LinkedList();
    urls.add(baseURL);
    if (baseURL.equals(urlString)) return urls;
    String s = urlString.substring(baseURL.length());
    logger.log(Level.FINER, "{0}. URL file: {1}", new Object[] { logger.getName(), s });
    
    if (s.startsWith("/")) { s = s.substring(1);
    }
    String[] parts = s.split("/");
    logger.log(Level.FINER, "{0}. URL file parts: {1}", new Object[] { logger.getName(), Arrays.toString(parts) });
    StringBuilder builder = new StringBuilder();
    for (String part : parts) {
      builder.setLength(0);
      baseURL = baseURL + '/' + part;
      urls.add(baseURL);
    }
    return urls;
  }
  
  public static String getPropertyKeyForAttributes(JsonConfig props, Map<String, String> attributes, boolean isValue)
  {
    int max = props.getInt(new Object[] { Config.Extractor.maxFiltersPerKey }).intValue();
    
    for (int i = 0; i < max; i++)
    {
      String propertyKey = "targetNode" + i + "Value";
      
      Map val = props.getMap(new Object[] { propertyKey, Config.Extractor.attributes });
      if (val != null)
      {


        if (val.equals(attributes))
          return propertyKey;
      }
    }
    return null;
  }
  

  public static String getNextNodeName(JsonConfig props, String propertyKey, boolean isValue) {
      
    if (propertyKey == null) {
      throw new NullPointerException("propertyKey == null");
    }
    
    if ("parentNode".equals(propertyKey)) {
      return "parentNode";
    }
    
    final int max = props.getInt(Config.Extractor.maxFiltersPerKey);
    
    for (int i = 0; i < max; i++) {
        
      final String key = propertyKey + i;
      
      final Object val = props.getObject(key);
      
      if (val == null) {
        if(!isValue) {  
          return key;
        }else{
          return key + "Value";  
        }
      }
    }
    return null;
  }
  
  public static StringBuilder appendQuery(Map params, StringBuilder builder) {
    return appendQuery(params, builder, "&");
  }
  
  public static StringBuilder appendQuery(Map params, StringBuilder builder, String separator)
  {
    if (builder == null) {
      builder = new StringBuilder();
    }
    
    QueryParametersConverter c = new QueryParametersConverter(separator);
    
    return builder.append(c.convert(params));
  }
  
  public static Map<String, String> getParameters(String input, String separator)
  {
    return getParameters(input, separator, false);
  }
  

  public static Map<String, String> getParameters(String input, String separator, boolean emptyStringAllowed)
  {
    QueryParametersConverter c = new QueryParametersConverter(emptyStringAllowed, separator);
    
    return c.reverse(input);
  }
  











  public static String getDirectTextContents(Text node)
  {
    int startpos = node.getStartPosition();
    int endpos = node.getEndPosition();
    StringBuffer ret = new StringBuffer(endpos - startpos + 20);
    String s = node.toHtml();
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      switch (c)
      {
      case '\t': 
        ret.append("\\t");
        break;
      case '\n': 
        ret.append("\\n");
        break;
      case '\r': 
        ret.append("\\r");
        break;
      case '\013': case '\f': default: 
        ret.append(c);
      }
      
    }
    return ret.toString();
  }
  
  public static StringBuilder appendTag(Node node, int maxLen) {
    StringBuilder builder = new StringBuilder();
    return appendTag(node, builder, false, maxLen);
  }
  
  public static StringBuilder appendTag(Node node, StringBuilder builder) { if (builder == null) builder = new StringBuilder();
    return appendTag(node, builder, false, 100);
  }
  
  public static StringBuilder appendTag(Node node, StringBuilder builder, boolean recurse, int maxLength) { doAppendTag(node, builder, maxLength);
    if (!recurse) return builder;
    NodeList list = node.getChildren();
    if (list == null) return builder;
    for (int i = 0; i < list.size(); i++) {
      Node child = list.elementAt(i);
      appendTag(child, builder, recurse, maxLength);
    }
    return builder;
  }
  
  private static StringBuilder doAppendTag(Node node, StringBuilder builder, int maxLength) { Tag tag = null;
    if ((node instanceof Tag)) {
      tag = (Tag)node;
      for (Object attr : tag.getAttributes()) {
        builder.append(toString(attr, maxLength));
      }
    }
    else if (node == null) {
      builder.append("NULL");
    } else {
      builder.append(toString(node.getText(), maxLength));
    }
    
    return builder;
  }
  
  public static boolean hasEmail(String str) {
    return emailPattern.matcher(str).find();
  }
  
  public static boolean isEmail(String str) {
    return emailPattern.matcher(str).matches();
  }
  
  public static boolean hasDate(String str) {
    for (Pattern p : datePatterns) {
      if (p.matcher(str).find()) return true;
    }
    return false;
  }
  
  public static boolean isDate(String str) {
    for (Pattern p : datePatterns) {
      if (p.matcher(str).matches()) return true;
    }
    return false;
  }
  
  public static Pattern[] getDatePatterns() {
    return datePatterns;
  }
  
  public static Pattern getEmailPattern() {
    return emailPattern;
  }
  
  public static boolean hasImage(NodeList nodeList, String name, boolean recurse)
  {
    for (int i = 0; i < nodeList.size(); i++)
    {
      Node child = nodeList.elementAt(i);
      
      if (recurse) {
        boolean hasChildren = child.getFirstChild() != null;
        if (hasChildren) {
          if (hasImage(child.getChildren(), name, recurse)) {
            return true;
          }
        }
        else if (isRequiredImage(child, name)) {
          return true;
        }
        
      }
      else if (isRequiredImage(child, name)) {
        return true;
      }
    }
    

    return false;
  }
  





  private static boolean isRequiredImage(Node node, String name)
  {
    if (!(node instanceof Tag)) return false;
    Tag tag = (Tag)node;
    if (!tag.getTagName().equals("IMG")) return false;
    String src = tag.getAttributeValue("src");
    if (src == null) return false;
    return src.endsWith(name);
  }
  
  public static String toString(Object input, int maxLength)
  {
    if (input == null) {
      return "NULL";
    }
    String output;
    if ((input instanceof Collection)) {
      output = toString((Collection)input, maxLength); } else { 
      if ((input instanceof Map)) {
        output = toString((Map)input, maxLength);
      } else {
        output = toString(input.toString(), maxLength);
      }
    }
    return output;
  }
  
  public static String toString(String str, int maxLength)
  {
    if (str == null) {
      return "NULL";
    }
    
    int len = str.length();
    
    return (maxLength < 0) || (len <= maxLength) ? str : str.substring(0, maxLength);
  }
  
  public static String toString(Collection input, int maxLength)
  {
    if (input == null) {
      return "NULL";
    }
    
    Iterator i = input.iterator();
    if (!i.hasNext()) {
      return "[]";
    }
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (;;) {
      Object e = i.next();
      sb.append(e == input ? "(this Collection)" : toString(e, maxLength));
      if (!i.hasNext()) {
        sb.append(']');
        break;
      }
      sb.append(", ");
    }
    return sb.toString();
  }
  
  public static String toString(Map input, int maxLength)
  {
    if (input == null) {
      return "NULL";
    }
    
    Iterator<Map.Entry> i = input.entrySet().iterator();
    if (!i.hasNext()) {
      return "{}";
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (;;) {
      Map.Entry e = (Map.Entry)i.next();
      Object key = e.getKey();
      Object value = e.getValue();
      sb.append(key == input ? "(this Map)" : toString(key, maxLength));
      sb.append('=');
      sb.append(value == input ? "(this Map)" : toString(value, maxLength));
      if (!i.hasNext()) {
        sb.append('}');
        break;
      }
      sb.append(", ");
    }
    return sb.toString();
  }
  






  public static String findValueWithMatchingKey(Map m, String text)
  {
    Object column = null;
    
    Iterator iter = m.keySet().iterator();
    
    while (iter.hasNext())
    {
      String key = iter.next().toString();
      
      String tgt = null;
      String input = null;
      
      if (key.length() > text.length()) {
        tgt = text.toLowerCase();
        input = key.toLowerCase();
      } else {
        tgt = key.toLowerCase();
        input = text.toLowerCase();
      }
      
      if (input.contains(tgt)) {
        column = m.get(key);
        break;
      }
    }
    
    return column == null ? null : column.toString();
  }
  









  /**
   * @deprecated
   */
  private static void keepAllNodesThatMatch(NodeList nodeList, NodeFilter filter)
  {
    keepAllNodesThatMatch(nodeList, filter, false);
  }
  














  /**
   * @deprecated
   */
  private static void keepAllNodesThatMatch(NodeList nodeList, NodeFilter filter, boolean recursive)
  {
    for (int i = 0; i < nodeList.size(); i++)
    {
      Node node = nodeList.elementAt(i);
      
      if (!recursive)
      {
        if (!filter.accept(node)) {
          nodeList.remove(i);
        }
      }
      else {
        NodeList children = node.getChildren();
        
        if (children == null)
        {
          if (!filter.accept(node)) {
            nodeList.remove(i);
          }
          
        }
        else {
          keepAllNodesThatMatch(nodeList, filter, recursive);
        }
      }
    }
  }
}

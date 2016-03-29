package com.scrapper.url;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;










public class ConfigURLList
  extends ArrayList<String>
  implements Serializable
{
  public static void main(String[] args)
  {
    List letters = new ArrayList();
    letters.addAll(Arrays.asList(new String[] { "A", "B", "C" }));
    List numbers = new ArrayList();
    numbers.addAll(Arrays.asList(new String[] { "1", "2", "3", "4" }));
    List symbols = new ArrayList();
    symbols.addAll(Arrays.asList(new String[] { "*", "+" }));
    
    List source = new ArrayList();
    source.add(letters);source.add(numbers);source.add(symbols);
    
    ConfigURLList list = new ConfigURLList();
    
    list.generatePermutations("http://www.abc.com", source, list);
    
    System.out.println(list);
  }
  


  public void update(JsonConfig config, String type)
  {
    int max = 20;
    
    List<List> parts = null;
    
    for (int i = 0; i < max; i++)
    {
      if (ConfigURLPartList.accept(config, type, i))
      {


        ConfigURLPartList part = new ConfigURLPartList(config, type, i);
        
        if (parts == null)
        {
          parts = new ArrayList();
        }
        
        parts.add(part);
      }
    }
    if (parts == null) {
      return;
    }
    
    assert (parts.size() <= 10) : (getClass() + " does not support > 10 parts");
    
    String baseUrl = com.bc.util.Util.getBaseURL(config.getString(new Object[] { "url", "value" }));
    
    generatePermutations(baseUrl, parts, this);
    
    if (XLogger.getInstance().isLoggable(Level.FINER, getClass())) {
      StringBuilder builder = new StringBuilder();
      builder.append("URLs:\n");
      for (String url : this) {
        builder.append(url).append('\n');
      }
      XLogger.getInstance().log(Level.FINER, "{0}", getClass(), builder);
    }
  }
  


  private void generatePermutations(String baseUrl, List<List> source, List<String> result)
  {
    generatePermutations(baseUrl, source, result, 0, "");
  }
  

  private void generatePermutations(String baseUrl, List<List> source, List<String> result, int depth, String current)
  {
    if (depth == source.size())
    {
      String url = baseUrl + current;
      

      url = url.replaceAll("\\s", "");
      
      result.add(url);
      
      return;
    }
    
    for (int i = 0; i < ((List)source.get(depth)).size(); i++)
    {


      generatePermutations(baseUrl, source, result, depth + 1, current + ((List)source.get(depth)).get(i));
    }
  }
}

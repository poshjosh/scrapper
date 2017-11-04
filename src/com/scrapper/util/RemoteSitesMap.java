package com.scrapper.util;

import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import com.bc.webdatex.filter.Filter;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class RemoteSitesMap
  extends HashMap<String, Set<String>>
  implements Filter<JsonConfig>
{
  public RemoteSitesMap()
  {
    init();
  }
  
  public RemoteSitesMap(int initialCapacity, float loadfactor)
  {
    super(initialCapacity, loadfactor);
    
    init();
  }
  
  private void init()
  {
    String name;
    ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
    
    Set<String> names = factory.getConfigNames();
    
    for (Iterator i$ = names.iterator(); i$.hasNext();) { name = (String)i$.next();

      JsonConfig config = factory.getConfig(name, true);
      
      if (config != null)
      {

        if (!test(config))
        {

          factory.removeConfig(name);

        }
        else
        {

          Map map = config.getMap(new Object[] { Config.Site.url, "mappings", Config.Extractor.table.name() });
          Collection tables;
          if (map == null)
          {
            tables = config.getList(new Object[] { Config.Site.tables });
          }
          else
          {
            tables = map.values();
          }
          
          for (Object table : tables)
          {
            add(table.toString(), name); }
        } }
    }
  }
  
  public boolean test(JsonConfig config) {
    return true;
  }
  
  private void add(String table, String sitenameToAdd)
  {
    Set<String> tableSitenames = (Set)get(table);
    
    if (tableSitenames == null)
    {
      tableSitenames = new TreeSet();
      
      put(table, tableSitenames);
    }
    
    tableSitenames.add(sitenameToAdd);
  }
}

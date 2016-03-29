package com.scrapper.config;

import com.bc.json.config.JsonData;
import com.bc.json.config.SimpleJsonConfig;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;











public class ScrapperConfig
  extends SimpleJsonConfig
{
  public ScrapperConfig() {}
  
  public ScrapperConfig(String name)
  {
    super(name);
  }
  
  public ScrapperConfig(String name, Map jsonData) {
    super(name, jsonData);
  }
  
  public ScrapperConfig(String name, JsonData parent, Object... path) {
    super(name, parent, path);
  }
  
  public Set getColumns() {
    TreeSet columns = new TreeSet();
    int max = getInt(new Object[] { Config.Extractor.maxFiltersPerKey }).intValue();
    for (int i = 0; i < max; i++) {
      List nodeCols = getList(new Object[] { Config.Extractor.targetNode + "" + i, Config.Extractor.columns });
      

      if ((nodeCols != null) && (!nodeCols.isEmpty()))
      {

        columns.addAll(nodeCols); }
    }
    return columns;
  }
}

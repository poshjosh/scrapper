package developer_notes;

import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import com.scrapper.config.ScrapperConfigFactory;
import java.util.Set;

/**
 * @(#)UpdateConfigs.java   03-Apr-2014 19:57:49
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class UpdateConfigs {
    
    /**
url.counter.ascending
url.counter.part1.ascending

url.counter.start
url.counter.part1.start

url.counter.end
url.counter.part1.end

url.counter.prefix.value
url.counter.part0.value

url.counter.suffix.value
url.counter.part2.value   If there is a start and end otherwise replace part2 with part1

url.counter.prefix.replacements
url.counter.part0.replacements

url.counter.suffix.replacements
url.counter.part2.replacements   If there is a start and end otherwise replace part2 with part1

THIS PART WAS DONE MANUALLY

url.counter.prefix.mappings.columns= 
Get the above value = Map with one entry
The key in the map should be replaced with XXXXXXX in the query
Create a Map<String,List>
The only key of this new map should be XXXXXXX 
The values of the only key of this new map = List of type values 
url.counter.part0.replacements=

url.part0.value=String
url.part0.replacements=Map<String,List>
url.part1.replacements={"pleaseReplaceMe",["0","1","2","3"]};
// type.values, type.keys, type.both
url.part1.replacements={"pleaseReplaceMe",["@url.counter.mappings.type.values"]}
     */
    public static void main(String [] args) {
        
        try{
            
            CapturerApp.getInstance().init(true);
            
            ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
            
            Set<String> names = factory.getSitenames();
            
            for(String name:names) {
            
                JsonConfig config = factory.getConfig(name);
                
                if(config.getObject("url", "counter") == null) {
                    continue;
                }

                boolean hasStartOrEnd = false;
                Object removed = config.remove("url","counter","ascending");
                if(removed != null) config.setObject(new Object[]{"url","counter","part1","ascending"}, removed);

                removed = config.remove("url","counter","start");
                hasStartOrEnd = removed != null;
                if(removed != null) config.setObject(new Object[]{"url","counter","part1","start"}, removed);
                
                removed = config.remove("url","counter","end");
                hasStartOrEnd = hasStartOrEnd || removed != null;
                if(removed != null) config.setObject(new Object[]{"url","counter","part1","end"}, removed);
                
                removed = config.remove("url","counter","prefix","value");
                if(removed != null) config.setObject(new Object[]{"url","counter","part0","value"}, removed);

                removed = config.remove("url","counter","suffix","value");
                String partIndex = hasStartOrEnd ? "part2" : "part1"; 
                if(removed != null) config.setObject(new Object[]{"url","counter",partIndex,"value"}, removed);
                
                removed = config.remove("url","counter","prefix","replacements");
                if(removed != null) config.setObject(new Object[]{"url","counter","part0","replacements"}, removed);
                
                removed = config.remove("url","counter","suffix","replacements");
                if(removed != null) config.setObject(new Object[]{"url","counter",partIndex,"replacements"}, removed);

                factory.saveValues(config);
                
//                factory.sync(config);
                
System.out.println("Done config");                    
            }    
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void replace(JsonConfig config, Object [] path0, Object [] path1) {
        Object oval = config.remove(path0);
        config.setObject(path1, oval);
    }
}

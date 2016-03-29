package developer_notes;

import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @(#)UpdateFormatters.java   21-Feb-2014 15:54:51
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.1
 */
public class UpdateFormatters {
    
    public static void main(String [] args) {
        try{
            CapturerApp.getInstance().init(true);
            UpdateFormatters x = new UpdateFormatters();
            x.update();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void update() throws IOException {
        
        ScrapperConfigFactory configFactory = CapturerApp.getInstance().getConfigFactory();
        
        Set<String> sitenames = configFactory.getSitenames();
        
        StringBuilder messages = new StringBuilder();
        
        for(String sitename:sitenames) {
            
            messages.setLength(0);
            
messages.append("Site: ").append(sitename);            
            
            JsonConfig config = configFactory.getConfig(sitename);
            
            // Change property name formatterClassNames to formatter
            // and property value to type String from type String[]
            //
            Object oval = config.remove("formatterClassNames");
            
            boolean updated = false;
            
            if(oval != null) {
                
                oval = (String)((List)oval).get(0);

                config.setObject("formatter", oval);
                
                updated = true;
messages.append(", formatter=").append(oval);            
            }
            
            int maxFilters = config.getInt(Config.Extractor.maxFiltersPerKey);
            
            // Change property value for targetNode{n}.formatter to type 
            // String from type String[]
            //
            for(int i=0; i<maxFilters; i++) {
                
                Object [] key = {"targetNode"+i, "formatter"};
                
                oval = config.getObject(key);
                
                if(oval != null) {
                    
                    oval = ((List)oval).get(0);

                    config.setObject(key, oval);
                    
                    updated = true;
messages.append(", ").append(Arrays.toString(key)).append("=").append(oval);            
                }
            }
            
            if(updated) {
                CapturerApp.getInstance().getConfigFactory().saveValues(config);
messages.append("\nSaved ").append(sitename);
            }
System.out.println(messages);
messages.setLength(0);
        }
    }    
}

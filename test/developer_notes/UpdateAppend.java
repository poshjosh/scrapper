package developer_notes;

import com.bc.json.config.JsonConfig;
import com.bc.net.UrlUtil;
import com.scrapper.CapturerApp;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @(#)UpdateAppend.java   11-Jan-2014 13:47:12
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class UpdateAppend {
    
    public static void main(String [] args) {
        try{
//            CapturerApp.getInstance().init(true);
            UpdateAppend obj = new UpdateAppend();
//            obj.update();
            obj.testUrl();
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
    
    private void testUrl() throws Exception {
        URL url = new URL("http://adibba.com");
System.out.println("Authority: "+url.getAuthority());
System.out.println("Host: "+url.getHost());
System.out.println("Ref: "+url.getRef());
System.out.println(UrlUtil.toWWWFormat(url.toString()));
System.out.println(UrlUtil.toWWWFormat("download.adibba.com"));
    }
    
    private void update() throws IOException {
        
        Set<String> sitenames = CapturerApp.getInstance().getConfigFactory().getConfigNames();
        
        for(String s:sitenames) {
            update(s);
        }
    }
    
    private void update(String sitename) throws IOException {
        
        ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
        
        JsonConfig config = factory.getConfig(sitename);
        
        boolean isDefault = config.getName().equals("default");
        
        if(isDefault) {
            
            config.setObject("append", false);
            
System.out.println("Updated: "+sitename+".append=false");

            factory.saveValues(config);
            
            return;
        }
        
        boolean updated = false;
        
        int max = config.getInt(Config.Extractor.maxFiltersPerKey);
        
        for(int i=0; i<=max; i++) {
            
            Map m = config.getMap("targetNode"+i);
            
            if(m == null) {
                continue;
            }
            
            List columns = (List)m.get("columns");
            
            if(columns == null) {
                continue;
            }
            
            boolean append = columns.contains("description");
            
            m.put("append", append);
            
            updated = true;
System.out.println("Updated: "+sitename+".targetNode"+i+".append="+append);            
        }
        
        if(updated) {
            factory.saveValues(config);
        }
    }
}

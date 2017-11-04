package developer_notes;

import com.bc.json.config.JsonConfig;
import com.bc.util.JsonFormat;
import com.scrapper.CapturerApp;
import com.scrapper.config.ScrapperConfigFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @(#)PrintJson.java   15-Feb-2014 12:17:49
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
public class PrintJson {
    
    public PrintJson() { }
    
    private static Map<String, Object> getDefaultProperties() {
        HashMap<String, Object> props = new HashMap<>();
        props.put("User-Agent", new com.bc.net.UserAgents().getAny(false));
        props.put("Content-Type", "application/x-www-form-urlencoded");
        props.put("Accept-Charset", "utf-8");
        return props;
    }
    
    public static void main(String [] args) {
        int status = 0;
        try{
            CapturerApp.getInstance().init(true);
            PrintJson printJson = new PrintJson();
            printJson.print(new String[]{"url", "counter"}, true);
        }catch(Exception e) {
            e.printStackTrace();
            status = 1;
        }finally{
            System.exit(status);
        }
    }
    
    public void print(Object [] jsonPath, boolean tidy) {
        Set<String> sitenames = CapturerApp.getInstance().getConfigFactory().getConfigNames();
        for(String sitename:sitenames) {
            print(sitename, jsonPath, tidy);
        }
    }
    
    public void print(String sitename, Object[] jsonPath, boolean tidy) {
        ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
        JsonConfig config = factory.getConfig(sitename);
        Object jsonObj = config.getObject(jsonPath);
System.out.println("= = = = = = = : "+sitename+"."+Arrays.toString(jsonPath));
        if(jsonObj == null) {
System.out.println("NULL");            
        }else{
            StringBuilder jsonStr = new StringBuilder();
            JsonFormat jsonFmt = new JsonFormat(tidy);
            jsonFmt.appendJSONString(jsonObj, jsonStr);
System.out.println(jsonStr);
        }
    }
}

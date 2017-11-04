package com.scrapper.search;

import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class URLProducerTest {
    
    public URLProducerTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception { }

    @AfterClass
    public static void tearDownClass() throws Exception { }

    @Test
    public void testMain() {
        
        try{
            
            CapturerApp app = CapturerApp.getInstance();

            app.init(false);

            // Note we use the search instance of ScrapperConfigFactory
            //
            ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory(true);
            
            Set<String> sites = factory.getConfigNames();
            
            for(String site:sites) {
                
                if(!site.equals("kiramu")) {
                    continue;
                }
                
                testMain(factory, site);
            }
        }catch(Exception e) {
            System.err.println(e);
        }
    }
    
    private void testMain(ScrapperConfigFactory factory, String site) 
            throws Exception {

        JsonConfig config = factory.getConfig(site);

        Boolean disabled = config.getBoolean(Config.Extractor.disabled);        
        
        if(disabled != null && disabled) {
System.out.println("\nSite: "+site+" IS DISABLED");
        }        
        
        CapturerContext context = factory.getContext(config);

        URLProducer producer = context.getUrlProducer();
        
        if(producer == null) {
            return;
        }
System.out.println("\nSite: "+site+", URLProducer: "+producer);

        String table = context.getConfig().getArray("tables")[0].toString();
        
        table = "fashion";
        
        String searchText = this.getSearchText(table);
        
        List<String> urls = producer.getSearchURLs(context, table, null, searchText);

System.out.println(urls.toString().replace(", ", "\n"));

        List<String> subs =  null;
        if(urls instanceof HasUrlSubs) {
            subs = ((HasUrlSubs)urls).getUrlSubs();
        }
if(subs != null) {            
System.out.println("Sub urls:\n"+subs.toString().replace(", ", "\n"));    
}
    }
    
    private String getSearchText(String table) {
        if("classifieds".equals(table)) {
            return "dvd";
        }else if("gadgets".equals(table)) {
            return "ipad 3G";
        }else if("autos".equals(table)) {
            return "yamaha";
        }else if("fashion".equals(table)) {
            return "dress";
        }else if("household_items".equals(table)) {
            return "cooker";
        }else if("gifts".equals(table)) {
            return "body spray";
        }else if("jobs".equals(table)) {
            return "teacher";
        }else if("property".equals(table)) {
            return "2 bedroom bungalow";
        }else{
            throw new IllegalArgumentException("Unexpected table: "+table);
        }
    }
}

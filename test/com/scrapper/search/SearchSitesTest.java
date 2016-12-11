package com.scrapper.search;

import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import com.scrapper.PageDataConsumer;
import com.scrapper.search.url.SampleSearchURL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.dom.HtmlPageDom;

/**
 * @author Josh
 */
public class SearchSitesTest {
    
    public SearchSitesTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
        CapturerApp app = CapturerApp.getInstance();
        app.init(false);

        Level logLevel = Level.FINE;
        String packageLoggerName = com.scrapper.CapturerApp.class.getPackage().getName();
        if(logLevel.intValue() <= Level.FINE.intValue()) {
            XLogger.getInstance().transferConsoleHandler("", packageLoggerName, true);
        }
        XLogger.getInstance().setLogLevel(packageLoggerName, logLevel);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of main method, of class SearchSites.
     */
    @Test
    public void testMain() {
        try{
            
//            this.testLocal("to this");
            this.testFashion("dress");
            
//            this.testHousehold("inverter");
            
        }catch(Exception e) {
            System.err.println(e);
        }
    }

    private void testLocal(String text) {
        try{
            
            List<String> names = new ArrayList<>();
            
            names.add("sample"); 
            
            this.testMain("quanda", text, names, null, new SampleSearchURL());
            
        }catch(Exception e) {
            System.err.println(e);
        }
    }
    
    private void testFashion(String text) {
        try{
            
            List<String> names = new ArrayList<>();
            names.add("kiramu"); 
            names.add("second"); 
            names.add("Adibba");            
            names.add("jumia_ng");
            names.add("konga");
            
            Map parameters;
            final int type = this.getFashionType(text);
            if(type == -1) {
                parameters = null;
            }else{
                parameters = new HashMap();
                parameters.put("type", type);
            }
            
            this.testMain("fashion", text, names, parameters, null);
            
        }catch(Exception e) {
            System.err.println(e);
        }
    }
    
    private void testHousehold(String text) {
        try{
            
            List<String> names = new ArrayList<>();
            names.add("kaymu"); names.add("kara"); names.add("fouani");            
            names.add("jumia_ng");
            names.add("second");
            names.add("Adibba");
            
            this.testMain("household_items", text, names, null, null);
            
        }catch(Exception e) {
            System.err.println(e);
        }
    }
    
    private void testMain(String table, String searchText, 
            List<String> sitenames, Map parameters, final URLProducer up) {
        
        try{
System.out.println("Category: "+table+", search text: "+searchText+", params: "+parameters);            
System.out.println("Sites: "+(sitenames==null?null:sitenames.size()));

            SearchSites ss = new SearchSitesImpl(
                    sitenames, table, parameters, searchText){

                @Override
                protected SearchSite newTask(String site) {
                    SearchSite task =  super.newTask(site);
                    if(up != null) {
                        task.setUrlProducer(up);
                    }
                    return task;
                }
                    
            };

            ss.run();
            
            synchronized(this) {
                int n = 0;
                // We have to wait for the search process
                // this is because the test will end even if
                // these processes are running
                while(!ss.isCompleted() && ++n<90) {
                    this.wait(1000);
                }
            }
            
System.out.println("Finally: "+ss);            
            
        }catch(Exception e) {
            System.err.println(e);
        }
    }

    public class SearchSitesImpl extends SearchSites {

        public SearchSitesImpl() { }
        
        public SearchSitesImpl(List<String> sitenames, 
                final String productTable, Map parameters, String searchText) {
            
            super(sitenames, productTable, parameters, searchText);
        }

        @Override
        protected PageDataConsumer newDataConsumer(SearchSite searchSite) {
            DefaultSearchDataConsumer consumer = new DefaultSearchDataConsumer(
                    null, searchSite.getProductTable(), searchSite.getName()) {
                @Override
                protected Map findMatch(Map data) throws SQLException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected EntityController getEntityController() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                @Override
                protected boolean doConsume(HtmlPageDom page, Map data) {
System.out.println("= x = x = x = x = x = x Consuming: "+data);
                    return true;
                }
            };
            return consumer;
        }
    }

    private int getFashionType(String text) {
        // For fashion type of 1 = womens, clothing
        if("dress".equalsIgnoreCase(text)) {
            return 1;
        }
        return -1;
    }
}

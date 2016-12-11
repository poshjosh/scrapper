package com.scrapper.search;

import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import com.scrapper.PageDataConsumer;
import com.scrapper.context.CapturerContext;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.dom.HtmlPageDom;

/**
 *
 * @author Josh
 */
public class SearchSiteTest {
    
    public SearchSiteTest() { }

    /**
     * Test of main method, of class SearchSite.
     */
    @Test
    public void testMain() {
        try{
            
            CapturerApp.getInstance().init(false);
            
            Level logLevel = Level.FINE;
            String packageLoggerName = com.scrapper.CapturerApp.class.getPackage().getName();
            if(logLevel.intValue() <= Level.FINE.intValue()) {
                XLogger.getInstance().transferConsoleHandler("", packageLoggerName, true);
            }
            XLogger.getInstance().setLogLevel(packageLoggerName, logLevel);
            
// kaymu, kara, fouani
            
            String sitename = "fouani";
            String table = "household_items";
            String searchText = "LG";
            sitename = "taafoo";
            table = "fashion";
            searchText = "polo";
            
            // Note we use the search instance of ScrapperConfigFactory
            //
            CapturerContext context = CapturerApp.getInstance().getConfigFactory(true).getContext(sitename);

            SearchSite searchSite = new SearchSite(context);
            
            searchSite.update(table, null, searchText);
            
            PageDataConsumer dataConsumer = this.newDataConsumer(searchSite);

            searchSite.setDataConsumer(dataConsumer);
            
            searchSite.setScrappLimit(10);

            searchSite.run();
            
        }catch(Exception e) {
e.printStackTrace();
        }
    }

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

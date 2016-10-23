package com.scrapper;

import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import java.awt.Color;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.webdatex.nodedata.Dom;

/**
 * @author Josh
 */
public class DefaultSiteCapturerTest {
    
    private URL insertURL;
    
    public DefaultSiteCapturerTest() {
        try{
            insertURL = new URL("http://localhost:8080/gx?rh=tempadmin");
        }catch(MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        AppProperties.load(System.getProperty("user.home")+"/Documents/NetBeansProjects/scrapper/src/META-INF/properties/app.properties");
        CapturerApp.getInstance().init(false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testAll() throws Exception {
        
        this.testLocal();
        
//        this.testRemote();
    }

    private void testLocal() throws Exception {

        final String sitename = "sample";
        
        ArrayList<String> urls = new ArrayList<>(1);
        
        urls.add("file:/"+System.getProperty("user.home")+"/Desktop/sample.htm");
        
        DefaultSiteCapturer capturer = new DefaultSiteCapturer(
                sitename, urls, false, false){
            @Override
            protected PageDataConsumer createDataConsumer(CapturerContext context, List<String> urlList) {
                // We use our own data consumer
                return DefaultSiteCapturerTest.this.getDataConsumer(sitename);
            }
        };
        
        capturer.run();
    }
    
    private void testRemote() throws Exception {
        
        ArrayList<String> urls = new ArrayList<>(1);
        
//        urls.add("http://www.jumia.com.ng/Embellished-Asymmetric-Hem-Dress---Cream-Black-88515.html");
        urls.add("http://www.jumia.com.ng/Sleeveless-Skater-Dress---White-%26-Black-88540.html");
//        urls.add("http://www.jumia.com.ng/Maxi-Corsage-Slinky-Dress---Navy-Blue-87882.html");
        
        DefaultSiteCapturer capturer = new DefaultSiteCapturer(
                "jumia_womensclothing", urls, false, false);
        
        capturer.run();
    }
    
    private PageDataConsumer getDataConsumer(String sitename) {
        
        ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
        
        PageDataConsumer dataConsumer = new ContextDataConsumer(
                factory.getContext(sitename)){
            @Override
            public boolean doConsume(Dom page, Map data) {
                
                Object productTable = data.get(this.getTableNameKey());
                
                JLabel label = new JLabel();
                label.setBackground(Color.WHITE);
                StringBuilder labelText = new StringBuilder();
                labelText.append("<html><h2>");
                labelText.append(productTable).append(" Extracts</h2>");
                Set entrySet = data.entrySet();
                for(Object oval:entrySet) {
                    Map.Entry entry = (Map.Entry)oval;
                    labelText.append("<br/><b>");
                    labelText.append(entry.getKey());
                    labelText.append("</b> = <tt>");
                    labelText.append(entry.getValue());
                    labelText.append("</tt>");
                }
                labelText.append("</html>");
                label.setText(labelText.toString());
                
                JScrollPane view = new JScrollPane(label);
                Dimension dim = new Dimension(600, 500);
                view.setPreferredSize(dim);
                
                JOptionPane.showMessageDialog(null, view);
                
                return true;
            }
        };
        return dataConsumer;
    }
}

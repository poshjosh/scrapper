package com.scrapper.config;

import com.bc.json.config.JsonConfig;
import java.io.IOException;
import com.scrapper.CapturerApp;
import com.scrapper.context.CapturerContext;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class CapturerConfigFactoryTest {
    
    public CapturerConfigFactoryTest() { 
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    /**
     * Test of getInstance method, of class ScrapperConfigFactory.
     */
    @Test
    public void testGetInstance() {
System.out.println("getInstance");
        ScrapperConfigFactory result = CapturerApp.getInstance().getConfigFactory();
        assertFalse(result.isRemote());
    }

    /**
     * Test of syncAll method, of class ScrapperConfigFactory.
     */
    @Test
    public void testSyncAll() throws Exception {
System.out.println("syncAll");
// Memory consuming
//        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
//        instance.syncAll();
    }

    /**
     * Test of sync method, of class ScrapperConfigFactory.
     */
    @Test
    public void testSync_CapturerConfig() throws Exception {
System.out.println("sync");
//        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
//        String sitename = instance.getSitenames().iterator().next();
//        JsonConfig from = instance.getConfig(sitename);
//        instance.sync(from);
    }

    /**
     * Test of sync method, of class ScrapperConfigFactory.
     */
    @Test
    public void testSync_CapturerConfig_CapturerConfigPropertiesType() throws Exception {
System.out.println("sync");
    }

    /**
     * Test of getConfig method, of class ScrapperConfigFactory.
     */
    @Test
    public void testLoad_String() {
System.out.println("load");
        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
        String sitename = instance.getSitenames().iterator().next();
        JsonConfig from = instance.getConfig(sitename);
    }

    /**
     * Test of getConfig method, of class ScrapperConfigFactory.
     */
    @Test
    public void testLoad_String_boolean() throws Exception {
System.out.println("load");
    }

    /**
     * Test of newConfig method, of class ScrapperConfigFactory.
     */
    @Test
    public void testNewConfig() throws Exception {
System.out.println("newConfig");
    }

    /**
     * Test of newSyncPair method, of class ScrapperConfigFactory.
     */
    @Test
    public void testNewSyncPair() throws IOException {
System.out.println("newSyncPair");
//        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
//        String sitename = instance.getSitenames().iterator().next();
//        JsonConfig from = instance.getConfig(sitename);
//        JsonConfig to = instance.newSyncPair(from.getName());
    }

    /**
     * Test of getSiteNames method, of class ScrapperConfigFactory.
     */
    @Test
    public void testGetAvailableSitenames() {
System.out.println("getAvailableSitenames");
        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
        Set result = instance.getSitenames();
    }

    /**
     * Test of getContext method, of class ScrapperConfigFactory.
     */
    @Test
    public void testGetContext_String() {
System.out.println("getContext");
        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
        String name = instance.getSitenames().iterator().next();
        CapturerContext result = instance.getContext(name);
    }

    /**
     * Test of getContext method, of class ScrapperConfigFactory.
     */
    @Test
    public void testGetContext_CapturerConfig() {
System.out.println("getContext");
        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
        String name = instance.getSitenames().iterator().next();
        JsonConfig config = instance.getConfig(name);
        CapturerContext result = instance.getContext(config);
    }

    /**
     * Test of isRemote method, of class ScrapperConfigFactory.
     */
    @Test
    public void testIsRemote() {
System.out.println("isRemote");
        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
        boolean expResult = false;
        boolean result = instance.isRemote();
        assertEquals(expResult, result);
    }

    /**
     * Test of isUseCache method, of class ScrapperConfigFactory.
     */
    @Test
    public void testIsUseCache() {
System.out.println("isUseCache");
    }

    /**
     * Test of setUseCache method, of class ScrapperConfigFactory.
     */
    @Test
    public void testSetUseCache() {
System.out.println("setUseCache");
    }

    /**
     * Test of toString method, of class ScrapperConfigFactory.
     */
    @Test
    public void testToString() {
System.out.println("toString");
        ScrapperConfigFactory instance = CapturerApp.getInstance().getConfigFactory();
        String result = instance.toString();
System.out.println(result);        
    }
}

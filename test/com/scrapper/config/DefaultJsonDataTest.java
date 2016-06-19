package com.scrapper.config;

import com.bc.json.config.DefaultJsonData;
import com.bc.util.JsonFormat;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class DefaultJsonDataTest {
    
    public DefaultJsonDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testAll() {
        
        DefaultJsonData data = new DefaultJsonData();
        Map root = data.createObjectContainer();
        root.put("boolean", true);
        root.put("number", 12);
        root.put("string", "Chinomso");
        root.put("list", new String[]{"FirstName", "LastName"});
        Map group0 = data.createObjectContainer();
        group0.put("map.boolean", false);
        group0.put("map.number", 23);
        root.put("group0", group0);
        data.setRootContainer(root);
JsonFormat fmt = new JsonFormat(true, true);

System.out.println("===== ==== === == = ROOT = == === ==== =====");
System.out.println(data.getRootContainer());
        Map ss = data.getMap("group0");
System.out.println("===== ==== === == = GROUP 0 = == === ==== =====");
System.out.println(fmt.toJSONString(ss));
        DefaultJsonData subset = new DefaultJsonData(data, "group0");
System.out.println("===== ==== === == = SUBSET = == === ==== =====");
System.out.println(subset.getRootContainer());
    }
}

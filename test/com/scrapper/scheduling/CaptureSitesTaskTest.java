package com.scrapper.scheduling;

import java.util.HashSet;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class CaptureSitesTaskTest {
    
    public CaptureSitesTaskTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception { }

    @AfterClass
    public static void tearDownClass() throws Exception { }

    /**
     * Test of main method, of class CaptureSitesTask.
     */
    @Test
    public void testAll() { 
        
        final HashSet<String> set = new HashSet<String>();
        set.add("Adibba");
        set.add("taafoo");
        set.add("second");

        CaptureSitesManager taskManager = new CaptureSitesManager(){
            @Override
            public Set<String> getSitenames() {
                return set;
            }
        };
        
        taskManager.setScrappLimit(3);
        
        CaptureSitesTask task = new CaptureSitesTask(taskManager);
        
        Thread taskThread = new Thread(task);
        
        taskThread.start();
        
        // THIS MAY TAKE A WHILE
        //
        final long timeout = taskManager.getSitenames().size() * 60000 * taskManager.getScrappLimit();
        
        // wait for this thread to die
        //
        try{
            taskThread.join(timeout);
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

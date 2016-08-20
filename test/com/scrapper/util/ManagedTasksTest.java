package com.scrapper.util;

import com.bc.task.AbstractStoppableTask;
import com.bc.task.StoppableTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class ManagedTasksTest {
    
    public ManagedTasksTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception { }

    @AfterClass
    public static void tearDownClass() throws Exception { }
    
    @Test
    public void testAll() throws Exception {
        
        String tableName = "fashion";
        
        List<String> sites = new ArrayList<String>();
        sites.add("kiramu");
        sites.add("jumia_ng");
        sites.add("aboki na");
        sites.add("tulemon");
        sites.add("Na wa o");
        
        ManagedTasks m = new ManagedTasksImpl(){
            @Override
            public int getMaxResults() {
                return 4;
            }
        };
        
        m.loadTasks(tableName, sites);
        
        m.setMaxConcurrentProcesses(2);
        
long start = System.currentTimeMillis();
System.out.println("========================================"+new Date());

        m.run();

        m.shutdownAndAwaitTermination(7, TimeUnit.SECONDS);
        
System.out.println("========================================Time spent: "+(System.currentTimeMillis()-start));        
    }

    public class ManagedTasksImpl<T extends StoppableTask> extends ManagedTasks<T> {
        @Override
        public T newTask(String name) {
            return (T)new StoppableTaskImpl(name);
        }
        private class StoppableTaskImpl extends AbstractStoppableTask<String> {
            private String name;
            public StoppableTaskImpl(String name) { 
                this.name = name;
            }
            @Override
            public String doCall() {
                for(int i=0; i<5; i++) {
System.out.println(name+" . . . . . . . . . . "+i);    
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                return name;
            }
            @Override
            public String getTaskName() {
                return name;
            }
        }
    }
}

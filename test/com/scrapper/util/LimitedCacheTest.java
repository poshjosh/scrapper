package com.scrapper.util;

import com.bc.task.AbstractStoppableTask;

import com.bc.task.StoppableTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class LimitedCacheTest {
    
    public LimitedCacheTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception { }

    @AfterClass
    public static void tearDownClass() throws Exception { }

    @Test
    public void testAll() { 

        this.test(3, 2, false, false);
        
//        this.test(3, 2, false, true);
        
//        this.test(3, 2, true, false);
    }
    
    private void test(int limit, int maxActive, boolean simulateException, boolean simulateExceptionInRun) {
        
        List<String> taskNames = new ArrayList<String>();
        taskNames.add("Task A"); taskNames.add("Task B"); taskNames.add("Task C");
        
        final List<StoppableTask> tasks = new ArrayList<StoppableTask>() ;
        
        final TaskCacheImpl cache = new TaskCacheImpl(null, taskNames, limit, maxActive);
        
        for(String name:taskNames) {
        
            tasks.add(cache.newObject(name));
        }

        cache.simulateException = simulateException;

        cache.simulateExceptionInRun = simulateExceptionInRun;

        try{
            
            final ConcurrentProgressTaskList mgr = new ConcurrentProgressTaskList(){
                @Override
                protected List<StoppableTask> getList() {
                    return tasks;
                }
                @Override
                public String getTaskName() {
                    return this.getClass().getName();
                }
            };

            final long timeout = 13000;
            
            Thread printStatsThread = new Thread() {
                @Override
                public void run() {
                    final long ONE_SECOND = 1000;
                    for(int i=0; i<timeout/ONE_SECOND; i++) {
    if(i==3 || i == 6 || i == 9 || i == 12) {
        LimitedCacheTest.this.print(i, cache, mgr);
    }                    
                        try{
                            Thread.sleep(ONE_SECOND);
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                            this.interrupt();
                        }
                    }
                }
            };

            printStatsThread.start();

            mgr.setMaxConcurrentProcesses(cache.getMaxActive());
//        mgr.setMaxConcurrentProcesses(cache.getLimit()); // Test with this too, see the difference

            mgr.run();
            
//            try{
//                mgr.waitForTasks(timeout, TimeUnit.MILLISECONDS, true);
//            }catch(InterruptedException e) {
//                e.printStackTrace();
//            }
            
            mgr.shutdownAndAwaitTermination(timeout, TimeUnit.MILLISECONDS);
            
        }finally{

if(cache != null) {            
    System.out.println("Shutdown: "+cache.isShutdown());        

    assert cache.isShutdown() : "Lock releaser service in class: "+cache.getClass().getName()+" is not yet shutdown";
}else{
    System.out.println("Lock releaser service in class: "+cache.getClass().getName()+" was never instantiated. It is only instantiated as required");
}
        }
    }
    
    private void print(int index, TaskCache cache, ConcurrentProgressTaskList mgr) {
StringBuilder builder = new StringBuilder();        
builder.append("================================================================\n");
builder.append("@index: ").append(index);
builder.append(", Cache.released: ").append(cache.getReleased());
builder.append("\nProgressManager::\nSubmitted: ").append(mgr.getSubmitted());
builder.append(", Running: ").append(mgr.countRunning());
builder.append(", Stopped: ").append(mgr.countStopped());
builder.append(", Max active: ").append(mgr.getMaxConcurrentProcesses());
builder.append(", Pos: ").append(mgr.getPos());
builder.append(", Max: ").append(mgr.getMax());
builder.append("\nStarted: ").append(mgr.isStarted());
builder.append(", Stop initiated: ").append(mgr.isStopInitiated());
builder.append(", Stopped: ").append(mgr.isStopped());
builder.append(", Completed: ").append(mgr.isCompleted());
builder.append("\n================================================================");
System.out.println(builder);
    }

    public class TaskCacheImpl extends TaskCache<StoppableTask> {
        
        private boolean simulateException;
        
        private boolean simulateExceptionInRun;

        private int limit;
        
        private int maxActive;
        
        public TaskCacheImpl(String category, List<String> names, int limit, int maxActive) {
            super(category, names);
            this.limit = limit;
            this.maxActive = maxActive;
        }

        @Override
        protected StoppableTask newObject(final String name) {
            if(simulateException && name.contains("C")) {
                throw new RuntimeException("Intentionally added in: "+this.getClass()+"#newObject(java.lang.String)");
            }
            return new AbstractStoppableTask() {
                @Override
                protected void doRun() {
                    int n = name.contains("A") ? 20 : 10;
                    for(int i=0; i<n; i++) {
System.out.println(name+" . . . . . . . . . . "+i);    
                        if(simulateExceptionInRun && i >= n-2) {
                            throw new RuntimeException("Intentionally added in: "+this.getClass()+"#doRun()");
                        }
                        try{
                            Thread.sleep(1000);
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
                @Override
                public String getTaskName() {
                    return name;
                }
            };
        }

        @Override
        public int getMaxActive() {
            return maxActive;
        }

        @Override
        public int getLimit() {
            return limit;
        }
    }
}

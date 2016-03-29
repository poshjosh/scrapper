package developer_notes;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @(#)TestExecutorService.java   03-Mar-2014 15:28:44
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class TestExecutorService {

    private static class MyTask extends TimerTask {
        int offset;
        int size;
        private String name;
        private MyTask(String name, int size) {
            this.name = name;
            this.size = size;
        }
        @Override
        public void run() {
            for(; offset<size; offset++) {
System.out.println(name+" @ index "+offset);
                this.waitBetween();
            }
        }
        private synchronized void waitBetween() {
            try{
                wait(500);
            }catch(Exception e) {
                System.err.println(e);
            }finally{
                notifyAll();
            }
        }
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static void main(String [] args) {
        
        MyTask t1 = new MyTask("Task A", 5);
        MyTask t2 = new MyTask("Task B", 10);
        MyTask t3 = new MyTask("Task C", 5);
        
        // A timer runs the tasks one at a time, in any order
        // E.g Task B may run before Task C
        //
        Timer timer = new Timer();
        
//        timer.schedule(t1, 0);
//        timer.schedule(t2, 0);
//        timer.schedule(t3, 0);

        ScheduledExecutorService svc = Executors.newScheduledThreadPool(2);
        
        svc.submit(t1);
        svc.submit(t2);
        svc.submit(t3);
        
//        svc.shutdown();
    }
}

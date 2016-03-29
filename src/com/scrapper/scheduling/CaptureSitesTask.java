package com.scrapper.scheduling;

import com.bc.process.AbstractTaskList;
import com.bc.util.XLogger;
import com.scrapper.SiteCapturer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @(#)CaptureSitesTask.java   11-Jul-2014 11:36:17
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * If ${@linkplain #loop} is true, then this task will run coninuosly.
 * Once the task is completed, reset() is called and run() is recalled. 
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class CaptureSitesTask extends AbstractTaskList<String> {
    
    private boolean loop = true;

    private CaptureSitesManager taskManager;
    
    private final List<String> sitenames;
    
    private SiteCapturer current;
    
    private int timeoutHours;
    
    public CaptureSitesTask() {
        this(new CaptureSitesManager());        
    }
    
    public CaptureSitesTask(CaptureSitesManager taskManager) {
        
        this.timeoutHours = 24;
        
        this.taskManager = taskManager;
        
        sitenames = new ArrayList<String>(taskManager.getSitenames());
    }
    
    @Override
    public void reset() {
        super.reset();
        loop = true;
    }

    @Override
    public void doRun() {
        try{
            super.doRun();
        }catch(RuntimeException e) {
            this.loop = false;
            throw e;
        }
    }
    
    /**
     * This method is called once on completion of the Process.
     * The default implementation does nothing. In this implementation
     * we restart the whole process again
     */
    @Override
    protected void post() {

        if(!this.loop) {
            return;
        }
        
XLogger.getInstance().log(Level.INFO, "= x = x = x = RESETTING. size: {0}, pos: {1}", 
this.getClass(), this.getTaskCount(), this.getPos());                            
        // Reset
        this.reset();
        
XLogger.getInstance().log(Level.INFO, "= x = x = x = RERUNNING. size: {0}, pos: {1}", 
this.getClass(), this.getTaskCount(), this.getPos());                            

        // Start all over
        this.run();
    }
    
    @Override
    protected List<String> getList() {
        return sitenames;
    }

    @Override
    public void execute(String sitename) {
XLogger.getInstance().log(Level.INFO, "Executing: {0}", this.getClass(), sitename);                            
        
        try{

            boolean createnew = false;

            if(current != null) {
                String currentName = current.getContext().getConfig().getName();
                if(!currentName.equals(sitename)) {
                    createnew = true;
                }else{
                    Date startTime = current.getStartTime();
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.HOUR_OF_DAY, -this.timeoutHours);
                    if(startTime.before(cal.getTime())) {
                        createnew = true;
                    }
                }
            }else{
                createnew = true;
            }

            if(createnew) {
                if(this.current != null) {
                    this.current.stop();
                }
                current = taskManager.newTask(sitename);
XLogger.getInstance().log(Level.INFO, "Created task: {0}", this.getClass(), current);                            
            }

XLogger.getInstance().log(Level.INFO, "Running task: {0}", this.getClass(), current);                            
            current.run();
            
        }catch(RuntimeException e) {
            XLogger.getInstance().log(Level.WARNING, "Task failed: "+current, this.getClass(), e);
        }
XLogger.getInstance().log(Level.INFO, "DONE Executing: {0}", this.getClass(), this.current);                                    
    }

    @Override
    public void stop(String sitename) {
        if(this.current != null && this.current.getContext().getConfig().getName().equals(sitename)) {
XLogger.getInstance().log(Level.INFO, "Stopping: {0}", this.getClass(), this.current);                                    
            this.current.stop();
        }
    }

    public CaptureSitesManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(CaptureSitesManager taskManager) {
        this.taskManager = taskManager;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    public String getTaskName() {
        return CaptureSitesTask.class.getName();
    }
    
    @Override
    public void print(StringBuilder builder) {
        super.print(builder);
        builder.append(", timeout (hours): ").append(this.timeoutHours);
        builder.append(", crawlLimit: ").append(this.taskManager==null?null:this.taskManager.getCrawlLimit());
        builder.append(", parseLimit: ").append(this.taskManager==null?null:this.taskManager.getParseLimit());
        builder.append(", scrappLimit: ").append(this.taskManager==null?null:this.taskManager.getScrappLimit());
    }
}

package com.scrapper.util;

import com.bc.process.ConcurrentProgressList;
import com.bc.process.StoppableTask;
import com.bc.util.XLogger;
import com.scrapper.AppProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @(#)ManagedTasks.java   23-Oct-2014 16:27:22
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * This task shuts down after the specified {@link #getTimeout()}
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @param <T>
 * @since    0.2
 */
public abstract class ManagedTasks<T extends StoppableTask> 
        extends ConcurrentProgressList {

    private int timeout;
    
    private List<T> tasks;
    
    private final SortByFrequency sortByFrequency;
        
    public ManagedTasks() {

        sortByFrequency = new SortByFrequency();
        
        this.init();
    }

    public ManagedTasks(List<String> sitenames, final String category) {

        sortByFrequency = new SortByFrequency();
        
        this.init();
        
        ManagedTasks.this.loadTasks(category, sitenames);
    }

    private void init() {
        this.timeout = getInt(AppProperties.SITESEARCH_TIMEOUT);
XLogger.getInstance().log(Level.FINER, "Setting timeout to: {0} for {1}", this.getClass(), timeout, this);
        final int maxConcurrent = getInt(AppProperties.SEARCHSITE_MAXCONCURRENTPROCESSES);
        this.setMaxConcurrentProcesses(maxConcurrent);
    }
    
    protected abstract T newTask(String name);
    
    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported");
    }

    public void loadTasks(String category, List<String> sitenames) {
        
        if(category == null || sitenames == null) {
            throw new NullPointerException();
        }
        
        sortByFrequency.setCategory(category);
        
        Collections.sort(sitenames, sortByFrequency);

        // If we don't do this, some sites may never get airtime
        //
        // Factor: 0.2f
        //  Input: [a, b, c, d, e, f, g, h, i, j]
        // Output: [a, b, i, j, c, d, e, f, g, h]
        //
        sortByFrequency.rearrange(sitenames, 0.2f);
        
        this.tasks = new ArrayList<T>(sitenames.size());
        
        for(String sitename:sitenames) {
            T task = this.newTask(sitename);
            this.tasks.add(task);
        }
    }

    @Override
    public void doRun() {

        this.checkTimeout();
        
        try{
            
            super.doRun();
            
        }finally{

            this.shutdownAndAwaitTermination(timeout, TimeUnit.MILLISECONDS);
        }
    }
    
    private void checkTimeout() {
        assert this.getTimeout() != 0 : "timeout cannot be equal to 0";
    }
    
    public int computeLimitPerSite() {
        int maxResults = this.getMaxResults();
        final int maxConcurr = this.getMaxConcurrentProcesses();
        int limit = maxResults / maxConcurr;
        int rem = maxResults % maxConcurr;
        if(rem > 0) {
            ++limit;
        }
        if(limit <= 0) {
            limit = 1;
        }
XLogger.getInstance().log(Level.FINE, "Limit: {0}", this.getClass(), limit);                
        return limit;
    }
    
    protected void preTaskUpdateTimeTaken(String name) {
        
        this.checkTimeout();

        // Update time taken with a large value (in this case timeout)
        // This way, if the request is not completed, we would still
        // be able to compare this time taken in a fairly consistent way
        // AN ADJUSTMENT HAS TO BE MADE if the request completes.
        //
        this.updateTimeTaken(name, this.getTimeout());
    }
    
    protected void postTaskUpdateTimeTaken(String name, long lastTaskTime, int lastTaskTimeTaken) {
        
        this.checkTimeout();
XLogger.getInstance().log(Level.FINER, "Tasks: {0}", this.getClass(), tasks);

        // Adjust time taken. This was done earlier.
        // c = (previousAverage + timeout) / 2;
        //
        // We have to now solve for the previous average
        // previousAverage = (c * 2) - timeout;
        //  
        Map<String, Integer> rt = sortByFrequency.getRequestTimes();
        
XLogger.getInstance().log(Level.FINER, "Product table: {0}, Request times: {1}", 
        this.getClass(), sortByFrequency.getCategory(), sortByFrequency.getRequestTimes());        

        if(rt == null) {
            return;
        }
        
        Integer i = rt.get(name);
        
        int adjustment = (i * 2) - this.getTimeout();
        
        this.updateTimeTaken(name, adjustment);

        // Ensure this is available
        assert lastTaskTime > 0 : ManagedTasks.class.getName()+".lastRequestTime == 0. This value should be set each time a request is sent to a URL";

        // Update time taken
        this.updateTimeTaken(name, lastTaskTimeTaken);

XLogger.getInstance().log(Level.FINER, "Average request times for{0}={1}", 
this.getClass(), sortByFrequency.getCategory(), sortByFrequency.getRequestTimes());        
    }
    
    private void updateTimeTaken(String sitename, int timeTaken) {
        
        Map<String, Integer> rt = sortByFrequency.getRequestTimes();

XLogger.getInstance().log(Level.FINER, "Product table: {0}, Request times: {1}", 
        this.getClass(), sortByFrequency.getCategory(), sortByFrequency.getRequestTimes());        
        
        if(rt == null) {
            return;
        }
        
        Integer previousTimetaken = rt.get(sitename);
        
        // Not actual average
        //
        long average = previousTimetaken == null ? timeTaken : (previousTimetaken + timeTaken) / 2;
        
        rt.put(sitename, (int)average);
    }

    @Override
    protected List<StoppableTask> getList() {
        return (List<StoppableTask>)tasks;
    }

    private int getInt(String name) {
        String value = AppProperties.getProperty(name);
        return Integer.parseInt(value);
    }

    private int mr = - 1;
    public int getMaxResults() {
        if(mr == -1) {
            mr = getInt(AppProperties.MAX_RESULTS);
        }
        return mr;
    }

    public String getProductTable() {
        return sortByFrequency.getCategory();
    }

    public void setProductTable(String productTable) {
        this.sortByFrequency.setCategory(productTable);
    }

    public List<T> getTasks() {
        return tasks;
    }

    @Override
    public String getTaskName() {
        return this.getClass().getName();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

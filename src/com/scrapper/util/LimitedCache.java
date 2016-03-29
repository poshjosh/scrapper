package com.scrapper.util;

import com.bc.process.ProcessManager;
import com.bc.util.XLogger;
import com.scrapper.search.SearchSite;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @(#)LimitedCache.java   22-Oct-2014 20:55:10
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * The elements in this objects are created as need. In total, only 
 * {@link #getLimit()} amount of objects may be created. In addition, 
 * only {@link #getMaxActive()} elements may be active at any given time.
 * When this amount of elements are active, calls to {@link #get(int)}
 * block until the number of active elements are less than this amount.
 * Wether an element is active is decided by invoking {@link #isActive(int)}
 * @see #get(int) 
 * @see #isActive(int) 
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public abstract class 
        LimitedCache<T extends Serializable> 
        extends AbstractList<T> 
        implements Serializable,
        Comparator<String> {

    private int released;
    
    private final String [] names;

    private Object [] cache;
    
    private boolean shutdown;
    
    private final transient Serializable lock = new Serializable(){};

    private transient ScheduledExecutorService lockReleaserService;
    
    public LimitedCache(List<String> names) {
        
        this.names = LimitedCache.this.sort(names);
    }
    
    protected abstract T newObject(String name);
    
    public abstract int getMaxActive();
    
    public abstract boolean isDone(T type);

    public int getLimit() {
        return this.getNames().length;
    }
    
    public boolean isReleased(int index) {
        
        return index < released;
    }
    
    /**
     * A task that has not been released is NOT active.<br/>
     * A task that is release but not yet started is active.<br/>
     * A task that is started but not yet stopped is active<br/>
     * A task that is started but stopped is NOT active.
     */
    public boolean isActive(int index) {
        
        boolean isActive;
        
        if(!this.isReleased(index)) {
            
            isActive = false;
            
        }else{
        
            // We call doGet because we don't want to be subject to the
            // waiting procedure
            //
            T type = this.doGet(index);

            // A 
            isActive = !this.isDone(type);
        }
        
        return isActive;
    }

    protected String [] sort(List<String> toSort) {
        
        Collections.sort(toSort, LimitedCache.this);
        
        return toSort.toArray(new String[0]);
    }

    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }

    /**
     * When {@link #getMaxActive()} elements are active, calls to this method.
     * block until the number of active elements are less than {@link #getMaxActive()}.
     * Wether an element is active is decided by invoking {@link #isActive(int)}.
     * <br/>
     * <b>NOTE</b> This method returns <tt>null</t> if interrupted exception is thrown
     * while blocking (waiting) till number of active elements are less than 
     * {@link #getMaxActive()}.
     * @see #isActive(int) 
     */
    @Override
    public T get(int index) {

        try{
            
            T type;

            if(!this.needsRegulation()) {

                type = this.doGet(index);

            }else{

                boolean mayGetMore = this.waitForLimit();
                
                if(!mayGetMore) {
                    return null;
                }

                type = this.doGet(index);

                if(index >= released) {
                    ++released;
XLogger.getInstance().log(Level.FINER, "Total released: {0}, last released: {1}={2}",
        this.getClass(), released, index, type);        
                }
            }

            return type;
            
        }catch(RuntimeException e) {
            
            this.shutdownLockReleaserService();
            
            throw e;
        }
    }
    
    private T doGet(int index) {
        
        try{

XLogger.getInstance().log(Level.FINER, 
"Index: {0}, Objects created: {1}, size: {2} names supplied: {3}", 
this.getClass(), index, names==null?0:names.length, size(), names);            

            if(index < 0 || index >= size()) {
                throw new IndexOutOfBoundsException("Index: "+index+", size: "+size());
            }

            String name = names[index];

XLogger.getInstance().log(Level.FINER, 
"Index: {0}, Name: {1}", this.getClass(), index, name);            

            Object type;

            if(cache != null) {

                type = cache[index];

XLogger.getInstance().log(Level.FINER, 
"Loaded from cache: {0}", this.getClass(), type);            

            }else{

                type = null;
            }

            if(type == null) {

                type = this.newObject(name);

                if(type == null) {
                    throw new IllegalArgumentException("Failed to create an instanceof "+SearchSite.class.getName()+" for: "+name);
                }

XLogger.getInstance().log(Level.FINER, "Created: {0}", this.getClass(), type);            

                if(cache == null) {
                    cache = new Object[size()];
                }

                cache[index] = type;
            }

XLogger.getInstance().log(Level.FINER, "Index: {0}, object: {1}", 
    this.getClass(), index, type);  

            return (T)type;
            
        }catch(RuntimeException e) {
            
            this.shutdownLockReleaserService();
            
            throw e;
        }    
    }

    public int countActive() {

        int active = 0;
        
        for(int i=0; i<released; i++) {
            
            if(this.isActive(i)) {
                ++active;
            }
        }
XLogger.getInstance().log(Level.FINER, "Active: {0}", this.getClass(), active);        
        return active;
    }
    
    private boolean waitForLimit() {
        
        synchronized(lock) {
            
            try{
                
                this.initLockReleaserService(1, TimeUnit.SECONDS);
        
XLogger.getInstance().log(Level.FINER, "Waiting for next", this.getClass());        
                while(!this.mayHaveMore()) {
                    
                    lock.wait();
                }

XLogger.getInstance().log(Level.FINER, "Done waiting for next", this.getClass());        
                return true;
                
            }catch(InterruptedException e) {

// This exception is thrown because com.idisc.MultiFeedTask.waitForFuture calls cancel on all the running 
//                
                XLogger.getInstance().log(Level.WARNING, "Interrupted while waiting for active element count to reduce below: "+this.getMaxActive(), this.getClass(), e);
                
// Probably because of this, ElipseLink operation was getting interrupted.
// 
//                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    private void initLockReleaserService(long interval, TimeUnit timeUnit) {
        
        if(lockReleaserService != null) {
            return;
        }
        
        lockReleaserService = Executors.newSingleThreadScheduledExecutor();
        
        lockReleaserService.scheduleWithFixedDelay(
                new LockReleaser(), interval, interval, timeUnit);
    }
    
    @Override
    public int size() {
        int limit = this.getLimit();
        int len = names.length;
        return len > limit ? limit : len;        
    }

    private boolean needsRegulation() {
        int size = size();
XLogger.getInstance().log(Level.FINER, "Needs regulation: {0}, released: {1}, size: {2}", 
        this.getClass(), released<size, released, size);        
        return released < size;
    }
    
    private boolean mayHaveMore() {
        int maxActive = this.getMaxActive();
        if(maxActive <= 0) {
            throw new UnsupportedOperationException("Max active must be > 0 : "+maxActive);
        }
        if(maxActive > this.getLimit()) {
            throw new UnsupportedOperationException("Max active must be < limit : "+this.getLimit());
        }
        int active = this.countActive();
XLogger.getInstance().log(Level.FINER, "May have more: {0}, active: {1}, max active: {2}", 
        this.getClass(), active<maxActive, active, maxActive);        
        return active < maxActive;
    }
    
    public int getReleased() {
        return released;
    }

    private class LockReleaser implements Runnable, Serializable {
        @Override
        public void run() {
            try{
                this.doRun();
            }catch(RuntimeException e) {
                XLogger.getInstance().log(Level.WARNING, "Exception in run method of: "+this.getClass().getName(), this.getClass(), e);
            }
        }
        
        private void doRun() {

            if(!LimitedCache.this.needsRegulation()) {
XLogger.getInstance().log(Level.FINER, "Releasing lock and ending lock watch", this.getClass());        
                synchronized(lock) {
                    lock.notifyAll(); 
                }
                LimitedCache.this.shutdownLockReleaserService();
                return;
            }

            if(LimitedCache.this.mayHaveMore()) {

XLogger.getInstance().log(Level.FINER, "Releasing lock", this.getClass());        
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        }
    }
    
    private void shutdownLockReleaserService() {
        if(lockReleaserService == null) {
            return;
        }
        try{
            shutdown = true;
            ProcessManager.shutdownAndAwaitTermination(lockReleaserService, 100, TimeUnit.MILLISECONDS);
        }finally{
            lockReleaserService = null;
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }
    
    public String [] getNames() {
        return names;
    }

    public ScheduledExecutorService getLockReleaserService() {
        return lockReleaserService;
    }
}

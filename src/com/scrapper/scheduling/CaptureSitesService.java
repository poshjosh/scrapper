package com.scrapper.scheduling;

import com.bc.process.AbstractStoppableTask;
import com.bc.util.XLogger;
import com.scrapper.Resumable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @(#)CaptureSitesService.java   11-Jul-2014 16:31:28
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
public class CaptureSitesService extends AbstractStoppableTask implements Resumable {
    
    private boolean resume;

    private boolean resumable;
    
    private CaptureSitesTask task;

    public CaptureSitesService() throws IOException, ClassNotFoundException {
        this(true, true);
    }
    
    public CaptureSitesService(boolean resume, boolean resumable) 
            throws IOException, ClassNotFoundException {
        this.resume = resume;
        this.resumable = resumable;
        if(resume) {
            task = CaptureSitesService.this.load();
        }
        if(task == null) {
            task = new CaptureSitesTask();
        }
    }

    public CaptureSitesService(boolean resumable, CaptureSitesTask task) {
        this.resumable = resumable;
        this.task = task;
    }
    
    @Override
    protected void doRun() {
        
        if(task != null) {
            
            task.run();
        }
    }

    @Override
    public void stop() {
        
        super.stop();
        
        if(task != null) {
            
            task.stop();
            
            if(this.isResumable()) {
                this.save(task);
            }
        }
    }
    
    public String getFilePath() {
        return this.getClass().getName()+".ser";
    }
    
    public CaptureSitesTask load() throws IOException, ClassNotFoundException {
        
        final String filePath = this.getFilePath();
        
        if(filePath == null) {
            throw new NullPointerException();
        }
        
        File                file = null;
        FileInputStream     fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream   ois = null;
        
        try {

            file = new File(filePath);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);

            return (CaptureSitesTask)ois.readObject();
            
        }finally {
        
            if (ois != null) try { ois.close(); }catch(IOException e) {}
            if (bis != null) try { bis.close(); }catch(IOException e) {}
            if (fis != null) try { fis.close(); }catch(IOException e) {}
        }
    }
    
    public boolean save(CaptureSitesTask task) {
        
        final String filePath = this.getFilePath();
        
        if(filePath == null || task == null) {
            throw new NullPointerException();
        }

        File                 file = null;
        FileOutputStream     fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream   oos = null;
        
        try{
            
            file = new File(filePath);
            fos = new FileOutputStream(file, false);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);

            oos.writeObject(task);
            
            return true;
        
        }catch(IOException e) {
            
            XLogger.getInstance().log(Level.WARNING, 
            "Failed to save "+task+" to "+filePath, this.getClass(), e);

            if(!file.delete()) {
                file.deleteOnExit();
            }
        
        }finally {
        
            if (oos != null) try { oos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
            if (bos != null) try { bos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
            if (fos != null) try { fos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
        }
        
        return false;
    }

    @Override
    public boolean isResumable() {
        return resumable;
    }

    @Override
    public boolean isResume() {
        return resume;
    }

    @Override
    public String getTaskName() {
        return this.getClass().getName();
    }
}

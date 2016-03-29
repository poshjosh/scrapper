package com.scrapper.scheduling;

import com.bc.task.AbstractStoppableTask;
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











public class CaptureSitesService
  extends AbstractStoppableTask
  implements Resumable
{
  private boolean resume;
  private boolean resumable;
  private CaptureSitesTask task;
  
  public CaptureSitesService()
    throws IOException, ClassNotFoundException
  {
    this(true, true);
  }
  
  public CaptureSitesService(boolean resume, boolean resumable) throws IOException, ClassNotFoundException
  {
    this.resume = resume;
    this.resumable = resumable;
    if (resume) {
      this.task = load();
    }
    if (this.task == null) {
      this.task = new CaptureSitesTask();
    }
  }
  
  public CaptureSitesService(boolean resumable, CaptureSitesTask task) {
    this.resumable = resumable;
    this.task = task;
  }
  

  protected void doRun()
  {
    if (this.task != null)
    {
      this.task.run();
    }
  }
  

  public void stop()
  {
    super.stop();
    
    if (this.task != null)
    {
      this.task.stop();
      
      if (isResumable()) {
        save(this.task);
      }
    }
  }
  
  public String getFilePath() {
    return getClass().getName() + ".ser";
  }
  
  public CaptureSitesTask load() throws IOException, ClassNotFoundException
  {
    String filePath = getFilePath();
    
    if (filePath == null) {
      throw new NullPointerException();
    }
    
    File file = null;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    ObjectInputStream ois = null;
    
    try
    {
      file = new File(filePath);
      fis = new FileInputStream(file);
      bis = new BufferedInputStream(fis);
      ois = new ObjectInputStream(bis);
      
      return (CaptureSitesTask)ois.readObject();
    }
    finally
    {
      if (ois != null) try { ois.close(); } catch (IOException e) {}
      if (bis != null) try { bis.close(); } catch (IOException e) {}
      if (fis != null) try { fis.close();
        } catch (IOException e) {}
    }
  }
  
  public boolean save(CaptureSitesTask task) {
    String filePath = getFilePath();
    
    if ((filePath == null) || (task == null)) {
      throw new NullPointerException();
    }
    
    File file = null;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    ObjectOutputStream oos = null;
    
    try
    {
      file = new File(filePath);
      fos = new FileOutputStream(file, false);
      bos = new BufferedOutputStream(fos);
      oos = new ObjectOutputStream(bos);
      
      oos.writeObject(task);
      
      return true;
    }
    catch (IOException e)
    {
      XLogger.getInstance().log(Level.WARNING, "Failed to save " + task + " to " + filePath, getClass(), e);
      

      if (!file.delete()) {
        file.deleteOnExit();
      }
    }
    finally
    {
      if (oos != null) try { oos.close(); } catch (IOException e) { Logger.getLogger(getClass().getName()).log(Level.WARNING, "", e); }
      if (bos != null) try { bos.close(); } catch (IOException e) { Logger.getLogger(getClass().getName()).log(Level.WARNING, "", e); }
      if (fos != null) try { fos.close(); } catch (IOException e) { Logger.getLogger(getClass().getName()).log(Level.WARNING, "", e);
        }
    }
    return false;
  }
  
  public boolean isResumable()
  {
    return this.resumable;
  }
  
  public boolean isResume()
  {
    return this.resume;
  }
  
  public String getTaskName()
  {
    return getClass().getName();
  }
}

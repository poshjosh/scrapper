package com.scrapper;

import com.bc.task.StoppableTask;
import com.scrapper.context.CapturerContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

public abstract interface SiteCapturer
  extends StoppableTask, Resumable
{
  public abstract URLParser getCrawler();
  
  public abstract PageDataConsumer getDataConsumer();
  
  public abstract Scrapper getScrapper();
  
  public abstract CapturerContext getContext();
  
  public abstract Date getStartTime();
  
  public abstract boolean isLogin();
  
  public abstract boolean isRunning();
  
  public abstract void login()
    throws MalformedURLException, IOException;
  
  public abstract void setCrawler(URLParser paramURLParser);
  
  public abstract void setDataConsumer(PageDataConsumer paramPageDataConsumer);
  
  public abstract void setLogin(boolean paramBoolean);
  
  public abstract void setScrapper(Scrapper paramScrapper);
  
  public abstract void setContext(CapturerContext paramCapturerContext);
  
  public abstract void setStartTime(Date paramDate);
}

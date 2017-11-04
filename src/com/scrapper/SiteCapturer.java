package com.scrapper;

import com.bc.webdatex.BaseCrawler;
import com.bc.task.StoppableTask;
import com.scrapper.context.CapturerContext;
import java.io.IOException;
import java.net.MalformedURLException;

public abstract interface SiteCapturer extends StoppableTask<Integer>, Resumable{
    
  public abstract BaseCrawler getUrlParser();
  
  public abstract PageDataConsumer getDataConsumer();
  
  public abstract Scrapper getScrapper();
  
  public abstract CapturerContext getContext();
  
  public abstract boolean isLogin();
  
  public abstract boolean isRunning();
  
  public abstract void login()
    throws MalformedURLException, IOException;
  
  public abstract void setUrlParser(BaseCrawler paramURLParser);
  
  public abstract void setDataConsumer(PageDataConsumer paramPageDataConsumer);
  
  public abstract void setLogin(boolean paramBoolean);
  
  public abstract void setScrapper(Scrapper paramScrapper);
  
  public abstract void setContext(CapturerContext paramCapturerContext);
}

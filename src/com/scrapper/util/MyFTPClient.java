package com.scrapper.util;

import com.bc.util.XLogger;
import com.ftpmanager.DefaultFTPClient;
import com.ftpmanager.Formatter;
import com.scrapper.AppProperties;
import java.util.Properties;
import java.util.logging.Level;

public class MyFTPClient
  extends DefaultFTPClient
{
  public MyFTPClient()
  {
    this(AppProperties.instance());
  }

  public MyFTPClient(Properties props)
  {
    setBufferSize(8192);
    
    setDataTimeout(30000);
    setDefaultTimeout(30000);
    
    int maxRetrials = Integer.parseInt(props.getProperty("ftpMaxretrials"));
    setMaxRetrials(maxRetrials);
    
    int retrialInterval = Integer.parseInt(props.getProperty("ftpRetrialInterval"));
    setRetrialInterval(retrialInterval);
    
    String host = props.getProperty("ftpHost");
    setHost(host);
    
    int port = Integer.parseInt(props.getProperty("ftpPort"));
    setPort(port);
    
    String user = props.getProperty("ftpUser");
    setUser(user);
    
    String password = props.getProperty("ftpPass");
    setPassword(password);
    

    String ftpDir = normalize(props.getProperty("ftpDir"));
    setFtpDir(ftpDir);
    
    Formatter<String> formatter = new Formatter<String>()
    {

      public String format(String path)
      {

        path = MyFTPClient.this.normalize(path);
        
        int len = MyFTPClient.this.getFtpDir().length();
        
        int offset = path.indexOf(MyFTPClient.this.getFtpDir());
        
        if (offset != -1) {
          path = path.substring(offset + len);
        }
        
        if (path.startsWith("/")) {
          path = path.substring(1);
        }
        XLogger.getInstance().log(Level.FINER, "FTP Path: {0}", getClass(), path);
        return path;
      }
    };
    setFormatter(formatter);
  }
  
  private String normalize(String s) {
    return s.replace('\\', '/');
  }
}

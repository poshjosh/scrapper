package com.scrapper.util;

import com.ftpmanager.DefaultFTPClient;
import com.ftpmanager.DefaultPathFormatter;
import java.util.Properties;

public class MyFTPClient extends DefaultFTPClient {
    
  public MyFTPClient() {
    this(com.scrapper.CapturerApp.getInstance().getProperties());
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
    
    final String ftpDir = props.getProperty("ftpDir").replace('\\', '/');
    setFtpDir(ftpDir);
    
    setFormatter(new DefaultPathFormatter(ftpDir));
  }
}

package com.scrapper.config;

import com.bc.json.config.JsonDataIO;
import com.bc.json.config.ConfigSubset;
import com.bc.json.config.JsonConfig;
import com.bc.json.config.SimpleJsonConfig;
import com.bc.util.XLogger;
import com.ftpmanager.DefaultFTPClient;
import com.ftpmanager.FileInterface;
import com.ftpmanager.LocalFile;
import com.ftpmanager.RemoteFile;
import com.ftpmanager.SafeSave;
import com.scrapper.util.MyFTPClient;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.ParseException;

public abstract class JsonConfigFactory
{
  private boolean search;
  private boolean useCache;
  private Map<String, JsonConfig> loadedConfigs;
  private Set<String> remotesites_use_getter_to_access;
  
  protected JsonConfigFactory()
  {
    setUseCache(true);
  }
  
  protected abstract URI getConfigDir();
  
  protected abstract String getDefaultConfigName();
  
  protected DefaultFTPClient getFtpClient() {
    return new MyFTPClient();
  }

  public String getSearchNodeName()
  {
    return "searchresults";
  }
  
  public boolean isRemote() {
    return false;
  }
  
  public File getConfigsDirFile() {
    URI uri = getConfigDir();
    File file = Paths.get(uri).toFile();
    return file;
  }
  
  public FileInterface getFile(String configName) {
    return getFile(configName, isRemote());
  }
  
  protected FileInterface getFile(String configName, boolean remote) {
    File parentFile = getConfigsDirFile();
    FileInterface file;
    if (remote) {
      file = new RemoteFile(getFtpClient(), parentFile.getPath(), configName + ".json", 0);
    }
    else
    {
      file = new LocalFile(parentFile, configName + ".json");
    }
    return file;
  }
  
  public String getPath(JsonConfig config) {
    return getFile(config.getName()).getPath();
  }
  
  public boolean exists(JsonConfig config) throws IOException {
    return getFile(config.getName()).exists();
  }
  
  public boolean rename(String oldName, String newName) throws IOException
  {
    FileInterface from = getFile(oldName);
    FileInterface to = getFile(newName);
    
    boolean success = from.renameTo(to);
    
    if (success)
    {
      replaceConfig(oldName, newName);
    }
    
    return success;
  }
  
  public JsonConfig loadValues(String configName) throws IOException, ParseException {
      
    FileInterface file = getFile(configName);
    
    InputStream in = file.getInputStream();
    
    InputStreamReader reader = null;
    
    try {
        
      reader = new InputStreamReader(in);
      
      return new JsonDataIO().load(reader, this.getContainerFactory());
      
    }finally {
      if (reader != null) {
        try { 
          reader.close();
        } catch (IOException e) { 
          XLogger.getInstance().log(Level.WARNING, "{0}", getClass(), e.toString());
        }
      }
      if (in != null) {
        try { in.close();
        } catch (IOException e) { XLogger.getInstance().log(Level.WARNING, "{0}", getClass(), e.toString());
        }
      }
    }
  }
  
  public ContainerFactory  getContainerFactory() {
    return new SimpleJsonConfig();
  }
  
  public void saveValues(JsonConfig config) throws IOException {
    saveValues(config, isRemote());
  }
  
  public void saveValues(JsonConfig config, boolean remote) throws IOException {
      
    FileInterface file = getFile(config.getName(), remote);
    SafeSave safeSave;
    if (remote) {
      safeSave = new SafeSave(getFtpClient());
    } else {
      safeSave = new SafeSave();
    }
    
    String path = file.getPath();
    String tempDirPath = file.getParentFile().getPath();
    
    safeSave.save(config.toString(), path, tempDirPath, 3);
  }
  
  public boolean delete(JsonConfig config) throws IOException
  {
    FileInterface file = getFile(config.getName());
    
    boolean deleted = file.delete();
    
    if (deleted)
    {
      removeConfig(config.getName());
    }
    
    return deleted;
  }
  
  protected JsonConfig createNew(String name, JsonConfig defaults) {
    JsonConfig config = new SimpleJsonConfig(name, defaults);
    return config;
  }
  
  protected void postCreate(JsonConfig config) throws IOException {}
  
  private JsonConfig initConfig(String configName, boolean create, boolean loadData) throws IOException, ParseException {
      
    if ((configName == null) || (configName.isEmpty())) {
      throw new NullPointerException();
    }
    
    final FileInterface file = getFile(configName);
    
    XLogger.getInstance().log(Level.FINER, "Path: {0}", getClass(), file.getPath());
    
    boolean newlyCreated = false;
    
    if (create) {

      if (!file.exists()) {

        file.createNew();
        
        newlyCreated = true;
      }
    }
    
    XLogger.getInstance().log(Level.FINER, "Properties: {0}", getClass(), this);
    
    final JsonConfig defaultConfig;
    
    if (!getDefaultConfigName().equals(configName)) {
        
      defaultConfig = getConfig(getDefaultConfigName());
      
    }else{
       
      defaultConfig = null;  
    }
    
    JsonConfig config = createNew(configName, defaultConfig);
    
    if (newlyCreated) {
      postCreate(config);
    }else{
      if(loadData) {
          config = this.loadValues(configName);
      }
    }
    
    if (this.search) {
        
      if (config.getObject(getSearchNodeName()) != null) {

//        JsonConfig searchConfig = new ConfigSubset(config.getName(), config, config, getSearchNodeName());
        JsonConfig searchConfig = new ConfigSubset(config.getName(), null, config, getSearchNodeName());
        
        config = searchConfig;
      }
    }
    return config;
  }
  
  public Set<String> deleteOrphanSyncPairs() throws IOException, ParseException {
      
    Set<String> sites = getSitenames();
    
    Set<String> syncSites = getSyncSitenames();
    
    HashMap<String, JsonConfig> toDelete = new HashMap();
    
    for (String syncSite : syncSites) {
        
      if (!sites.contains(syncSite)) {

        JsonConfig syncPair = newSyncPair(syncSite);
        
        if (exists(syncPair))
        {
          toDelete.put(syncPair.getName(), syncPair);
        }
      }
    }
    
    HashSet<String> failed = new HashSet();
    
    for (JsonConfig config : toDelete.values()) {
      try {
        if (!delete(config)) {
          failed.add(config.getName());
        }
      } catch (IOException e) {
        failed.add(config.getName());
      }
    }
    
    return failed;
  }
  
  public void sync(String sitename) throws IOException, ParseException {
      
    JsonConfig config = load(sitename, false);
    
    sync(config);
  }
  
  public void sync(JsonConfig src) throws IOException, ParseException {
      
    if (src == null) {
      throw new NullPointerException();
    }
    
    JsonConfig tgt = newSyncPair(src.getName());
    
    sync(src, tgt);
  }
  
  public void sync(JsonConfig src, JsonConfig tgt) throws IOException {
      
    if ((src == null) || (tgt == null)) {
      throw new NullPointerException();
    }
    
    tgt.update(src);
    
    XLogger.getInstance().log(Level.FINER, "After update Src size: {0}, tgt size: {1}", getClass(), Integer.valueOf(src.getRootContainer().size()), Integer.valueOf(tgt.getRootContainer().size()));
    
    FileInterface srcFile = getFile(src.getName(), isRemote());
    FileInterface tgtFile = getFile(tgt.getName(), !isRemote());
    
    XLogger.getInstance().log(Level.FINE, "Updated {0} with {1}", getClass(), tgtFile, srcFile);
    
    try {
      if (!tgtFile.exists()) {
        tgtFile.createNew();
      }
    }
    catch (IOException e) {}
    
    saveValues(tgt, !isRemote());
  }
  
  public JsonConfig replaceConfig(String oldName, String newName) {
    JsonConfig config = removeConfig(oldName);
    this.loadedConfigs.put(newName, new SimpleJsonConfig(newName, config.getDefaults(), config.getParent(), config.getPath())); 
    if (isRemote()) {
      getSitenames().remove(oldName);
      getSitenames().add(newName);
    }
    return config;
  }
  
  public JsonConfig removeConfig(String configName) {
    if (this.loadedConfigs == null) {
      return null;
    }
    JsonConfig config = (JsonConfig)this.loadedConfigs.remove(configName);
    if (isRemote()) {
      getSitenames().remove(configName);
    }
    return config;
  }
  


  public JsonConfig getConfig(String sitename) {
    return getConfig(sitename, false);
  }
  

  public JsonConfig getConfig(String sitename, boolean refresh) {
    JsonConfig output;
    try {
      output = load(sitename, refresh, false);
    } catch (IOException | ParseException e) {
      output = null;
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    return output;
  }
  
  public JsonConfig load(String sitename, boolean create)
    throws IOException, ParseException {
    return load(sitename, false, create);
  }
  
  public JsonConfig load(String sitename, boolean refresh, boolean create)
    throws IOException, ParseException {
      
    if ((sitename == null) || (sitename.isEmpty())) {
      throw new NullPointerException();
    }
    
    if (this.loadedConfigs == null) {
      if (isUseCache()) {
        this.loadedConfigs = new HashMap()
        {
          public JsonConfig put(String key, JsonConfig value) {
            if ((key == null) || (value == null)) throw new NullPointerException();
            return (JsonConfig)super.put(key, value);
          }
        };
      }
    }
    else if (!isUseCache()) {
      this.loadedConfigs = null;
    }
    

    JsonConfig config = null;
    
    if ((isUseCache()) && (!refresh)) {
      config = (JsonConfig)this.loadedConfigs.get(sitename);
    }
    
    if (config != null)
    {
      XLogger.getInstance().log(Level.FINER, "Loaded from cache: {0}", getClass(), sitename);
    }
    else
    {
      config = newConfig(sitename, create);
      
      if (isUseCache()) {
        this.loadedConfigs.put(sitename, config);
      }
      

      getSitenames().add(sitename);
    }
    
    return config;
  }
  
  protected JsonConfig newConfig(String sitename, boolean create) throws IOException, ParseException {
    JsonConfig config = initConfig(sitename, create, true);
    if (isRemote()) {
      getSitenames().add(sitename);
    }
    return config;
  }
  

  public void reverseSync() {}
  

  public void reverseSync(JsonConfigFilter srcFilter, JsonConfigFilter tgtFilter)
  {
    JsonConfigFactory reverse = newSyncFactory();
    
    ((ScrapperConfigFactory)reverse).syncAll(srcFilter, tgtFilter);
  }
  
  public void syncAll() {
    syncAll(null, null);
  }
  



  public void syncAll(JsonConfigFilter srcFilter, JsonConfigFilter tgtFilter)
  {
    Set<String> names = new HashSet(getSitenames());
    
    names.remove(getDefaultConfigName());
    
    for (String name : names) {
        
      try {
          
        JsonConfig config = load(name, false);
        
        boolean accept = (srcFilter == null) || (srcFilter.accept(config));
        
        if (accept) {
            
          JsonConfig syncPair = newSyncPair(name);
          
          accept = (tgtFilter == null) || (tgtFilter.accept(syncPair));
          
          if (accept) {
            sync(config, syncPair); 
          }
        }
      } catch (IOException | ParseException e) {
        XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
      }
    }
  }
  
  public JsonConfig newSyncPair(String configName) throws IOException, ParseException {
      
    JsonConfigFactory syncFactory = newSyncFactory();
    
    JsonConfig config = syncFactory.initConfig(configName, true, false);
    
    if (isRemote())
    {
      getSitenames().add(configName);
    }
    
    return config;
  }
  
  public JsonConfigFactory newSyncFactory()
  {
    final boolean isRemote = !isRemote();
    
    JsonConfigFactory factory = new JsonConfigFactory()
    {
      public boolean isRemote() {
        return isRemote;
      }
      
      protected URI getConfigDir() {
        return JsonConfigFactory.this.getConfigDir();
      }
      
      protected String getDefaultConfigName() {
        return JsonConfigFactory.this.getDefaultConfigName();
      }
      
      protected DefaultFTPClient getFtpClient()
      {
        return JsonConfigFactory.this.getFtpClient();
      }
    };
    factory.setSearch(isSearch());
    factory.setUseCache(isUseCache());
    return factory;
  }
  
  public Set<String> getSyncSitenames()
  {
    JsonConfigFactory reverse = newSyncFactory();
    
    return reverse.getSitenames();
  }
  
  public Set<String> getSitenames() {
    if (isRemote()) {
      return getRemoteSitenames();
    }
    return getLocalSitenames();
  }
  

  protected Set<String> getRemoteSitenames()
  {
    if (this.remotesites_use_getter_to_access != null) {
      return this.remotesites_use_getter_to_access;
    }
    this.remotesites_use_getter_to_access = new TreeSet();
    

    String[] sites = null;
    
    DefaultFTPClient ftp = getFtpClient();
    
    try
    {
      sites = ftp.listNames(getConfigsDirFile().getPath(), 0, true);
    }
    catch (SocketException e) {
      XLogger.getInstance().log(Level.WARNING, "FTP Connection failed", getClass(), e);
    } catch (IOException e) {
      XLogger.getInstance().log(Level.WARNING, "FTP operation failed", getClass(), e);
    }
    
    XLogger.getInstance().log(Level.FINER, "Files in sites dir: {0}", getClass(), sites == null ? null : Arrays.toString(sites));
    

    if (sites == null) {
      throw new NullPointerException();
    }
    
    for (String site : sites)
    {
      if ((!site.equals(".")) && (!site.equals("..")))
      {

        if (site.endsWith(".json"))
        {

          this.remotesites_use_getter_to_access.add(site.replace(".json", "")); }
      }
    }
    XLogger.getInstance().log(Level.FINER, "Available sitenames: {0}", getClass(), this.remotesites_use_getter_to_access);
    return this.remotesites_use_getter_to_access;
  }
  
  protected Set<String> getLocalSitenames()
  {
    File sitesDir = getConfigsDirFile();
    
    try
    {
      sitesDir = sitesDir.getCanonicalFile();
    }
    catch (IOException e) {
      return null;
    }
    XLogger.getInstance().log(Level.FINER, "Sites dir: {0}", getClass(), sitesDir);
    





    File[] files = sitesDir.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name) {
        return name.endsWith(".json");
      }
      
    });
    XLogger.getInstance().log(Level.FINER, "Files in sites dir: {0}", getClass(), files == null ? null : Arrays.toString(files));
    

    String[] confignames = null;
    if (files != null) {
      confignames = new String[files.length];
      for (int i = 0; i < files.length; i++) {
        confignames[i] = files[i].getName().replace(".json", "");
      }
    }
    
    if (confignames == null) {
      throw new NullPointerException();
    }
    
    TreeSet<String> output = new TreeSet(Arrays.asList(confignames));
    XLogger.getInstance().log(Level.FINER, "Available sitenames: {0}", getClass(), output);
    return output;
  }
  
  public boolean isUseCache() {
    return this.useCache;
  }
  
  public void setUseCache(boolean useCache) {
    this.useCache = useCache;
  }
  
  public boolean isSearch() {
    return this.search;
  }
  
  public void setSearch(boolean search) {
    this.search = search;
  }
  
  public String toString()
  {
    return super.toString() + ". Site names: " + getSitenames();
  }
  
  public static abstract interface JsonConfigFilter
  {
    public abstract boolean accept(JsonConfig paramJsonConfig);
  }
}

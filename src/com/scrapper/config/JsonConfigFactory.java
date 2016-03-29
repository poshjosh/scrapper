package com.scrapper.config;

import com.bc.json.config.ConfigSubset;
import com.bc.json.config.JsonConfig;
import com.bc.json.config.SimpleJsonConfig;
import com.bc.util.XLogger;
import com.ftpmanager.DefaultFTPClient;
import com.ftpmanager.FTPFileTypes;
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


/**
 * @(#)JsonConfigFactory.java   21-Feb-2015 05:23:40
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public abstract class JsonConfigFactory {
    
    /**
     * If true this is a search config. (i.e optimised for extracting search results
     */
    private boolean search;

    private boolean useCache;
    
    private Map<String, JsonConfig> loadedConfigs;

    protected JsonConfigFactory() { 
        JsonConfigFactory.this.setUseCache(true);
    }
    
    protected abstract URI getConfigDir();
    
    protected abstract String getDefaultConfigName();
    
    protected DefaultFTPClient getFtpClient() {
        return new MyFTPClient();
    }
    
    public static interface JsonConfigFilter {
        boolean accept(JsonConfig config);
    }
    
    public String getSearchNodeName() {
        return "searchresults";
    }
    
    public boolean isRemote() {
        return false;
    }
    
    public File getConfigsDirFile() {
        URI uri = this.getConfigDir();
        File file = Paths.get(uri).toFile(); 
        return file; 
    }

    public FileInterface getFile(String configName) {
        return this.getFile(configName, this.isRemote());
    }
    
    protected FileInterface getFile(String configName, boolean remote) {
        File parentFile = this.getConfigsDirFile();
        FileInterface file;
        if(remote) {
            file = new RemoteFile(
                        this.getFtpClient(), parentFile.getPath(), 
                        configName+".json", FTPFileTypes.ASCII_FILE_TYPE);
        }else{
            file = new LocalFile(parentFile, configName+".json");
        }
        return file;
    }
    
    public String getPath(JsonConfig config) {
        return this.getFile(config.getName()).getPath();
    }
    
    public boolean exists(JsonConfig config) throws IOException {
        return getFile(config.getName()).exists();
    }
    
    public boolean rename(String oldName, String newName) throws IOException {
        
        FileInterface from = this.getFile(oldName);
        FileInterface to = this.getFile(newName);
        
        boolean success = from.renameTo(to);
        
        if(success) {
            
            this.replaceConfig(oldName, newName);
        }
        
        return success;
    }

    public void loadValues(JsonConfig config) throws IOException {
        
        FileInterface file = this.getFile(config.getName());
        
        InputStream in = file.getInputStream();
        
        InputStreamReader reader = null;
        try{
            
            reader = new InputStreamReader(in);
            
            config.load(reader);
            
        }finally{
            if(reader != null) {
                try{ reader.close(); }catch(IOException e) {
                    XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
                }
            }
            if(in != null) {
                try{ in.close(); }catch(IOException e) {
                    XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
                }
            }
        }
    }

    public void saveValues(JsonConfig config) throws IOException {
    
        this.saveValues(config, this.isRemote());
    }
    
    public void saveValues(JsonConfig config, boolean remote) throws IOException {
        
        FileInterface file = this.getFile(config.getName(), remote);
        
        SafeSave safeSave;
        if(remote) {
            safeSave = new SafeSave(this.getFtpClient());
        }else{
            safeSave = new SafeSave();
        }
        
        final String path = file.getPath();
        final String tempDirPath = file.getParentFile().getPath();
        
        safeSave.save(config.toString(), path, tempDirPath, 3);
    }
    
    public boolean delete(JsonConfig config) throws IOException {
        
        FileInterface file = this.getFile(config.getName());
        
        boolean deleted = file.delete();

        if(deleted) {
            
            this.removeConfig(config.getName());
        }

        return deleted;
    }
    
    protected JsonConfig createNew() {
        JsonConfig config = new SimpleJsonConfig();
        return config;
    }
    
    protected void postCreate(JsonConfig config) throws IOException { }
    
    private JsonConfig initConfig(final String configName, boolean create, 
            boolean loadData) throws IOException {

        if(configName == null || configName.isEmpty()) {
            throw new NullPointerException();
        }
        
        FileInterface file = this.getFile(configName);
        
        // Order of method call important
        
        JsonConfig config = this.createNew();
        
        config.setName(configName);

XLogger.getInstance().log(Level.FINER, "Path: {0}", this.getClass(), file.getPath());        

        boolean newlyCreated = false;
        
        if(create) {

            // We use this method with care. If an exception is throw it just
            // returns false. Hence a return value if true is reliable but that
            // of false is not necessarily reliable... 
            //
            if(file.exists()) {

//                throw new IOException("File already exists: "+path);

            }else{

                file.createNew();
            }
        }
        
        if(!newlyCreated && loadData) {
            
            this.loadValues(config);
        }

XLogger.getInstance().log(Level.FINER, "Properties: {0}", this.getClass(), this); 
        
        if(!this.getDefaultConfigName().equals(configName)) {
            JsonConfig defaultConfig = this.getConfig(this.getDefaultConfigName());
//System.out.println(this.getClass().getName()+". Setting default values of config: "+configName+" to:\n"+defaultConfig);            
            config.setDefaults(defaultConfig);
        }
        
        if(newlyCreated) {

            this.postCreate(config);
        }    

        if(search && config.getObject(this.getSearchNodeName()) != null) {
            
            // Create a search view
            
            // Doing this ensures that:
            // To search for 'url.path'
            // First searches for 'searchresults.url.path'
            //
            
            // We use the name of the parent config 
            JsonConfig searchConfig = new ConfigSubset(
                    config.getName(), config, this.getSearchNodeName());
            
            // This next line of code ensures that:
            // If 'searchresults.url.path' is not found
            // Searches for 'url.path'
            searchConfig.setDefaults(config);
            
            config = searchConfig;
        }
        
        return config;
    }
    
///////////////////////////////////////////////////////////    
    
    /**
     * If config has properties a, b, and c; and syncConfig has a, b, c and d.
     * Then d is an orphan and is eligible for delete.
     * @return Set of orphan sitenames whose configs could not be deleted
     * @throws IOException 
     */
    public Set<String> deleteOrphanSyncPairs() throws IOException {
        
        Set<String> sites = this.getSitenames();
        
        Set<String> syncSites = this.getSyncSitenames();
        
        final HashMap<String, JsonConfig> toDelete = new HashMap<>();
        
        for(String syncSite:syncSites) {

            if(!sites.contains(syncSite)) {
                
                // This is an orphan
                JsonConfig syncPair = this.newSyncPair(syncSite);
                
                if(this.exists(syncPair)) {
                    
                    toDelete.put(syncPair.getName(), syncPair);
                }
            }
        }

        HashSet<String> failed = new HashSet<>();
        
        for(JsonConfig config:toDelete.values()) {
            try{
                if(!this.delete(config)) {
                    failed.add(config.getName());
                }
            }catch(IOException e) {
                failed.add(config.getName());
            }
        }
        
        return failed;
    }
    
    public void sync(String sitename) throws IOException {
        
        JsonConfig config = this.load(sitename, false);

        this.sync(config);
    }
    
    public void sync(JsonConfig src) throws IOException {

        if(src == null) {
            throw new NullPointerException();
        }

        JsonConfig tgt = this.newSyncPair(src.getName());

        sync(src, tgt);
    }
    
    public void sync(JsonConfig src, JsonConfig tgt) throws IOException {
        
        if(src == null || tgt == null) {
            throw new NullPointerException();
        }

        tgt.update(src);
        
XLogger.getInstance().log(Level.FINER, "After update Src size: {0}, tgt size: {1}", 
        this.getClass(), src.getRootContainer().size(), tgt.getRootContainer().size());        
        
        FileInterface srcFile = this.getFile(src.getName(), this.isRemote());
        FileInterface tgtFile = this.getFile(tgt.getName(), !this.isRemote());

XLogger.getInstance().log(Level.FINE, "Updated {0} with {1}", 
        this.getClass(), tgtFile, srcFile);

        // Create the file, if it doesn't exist. However if it exists
        // and contains any properties don't load them
        //
        try{
            if(!tgtFile.exists()) {
                tgtFile.createNew();
            }
        }catch(IOException e) {
            // ignore
        }
        
        saveValues(tgt, !this.isRemote());
    }
    
    public JsonConfig replaceConfig(String oldName, String newName) {
        JsonConfig config = this.removeConfig(oldName);
        loadedConfigs.put(newName, config);
        config.setName(newName);
        if(this.isRemote()) {
            // Actually already removed by replaceConfig's call to removeConfig
            this.getSitenames().remove(oldName);
            this.getSitenames().add(newName);
        }
        return config;
    }
    
    public JsonConfig removeConfig(String configName) {
        if(this.loadedConfigs == null) {
            return null;
        }
        JsonConfig config = this.loadedConfigs.remove(configName);
        if(this.isRemote()) {
                    // Note this
            this.getSitenames().remove(configName);
        }
        return config;
    }
    
    /**
     * @see #load(java.lang.String, boolean, boolean) 
     */
    public JsonConfig getConfig(String sitename) {
        return getConfig(sitename, false);
    }
    
    /**
     * @see #load(java.lang.String, boolean, boolean) 
     */
    public JsonConfig getConfig(String sitename, boolean refresh) {
        JsonConfig output;
        try{
            output = this.load(sitename, refresh, false);
        }catch(IOException e) {
            output = null;
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
        return output;
    }

    /**
     * @see #load(java.lang.String, boolean, boolean) 
     * @throws IOException 
     */
    public JsonConfig load(String sitename, 
            boolean create) throws IOException {
        
        return load(sitename, false, create);
    }
    
    /**
     * @param sitename String. The name of the site whose config file is to be loaded
     * @param refresh boolean. If true the config is loaded afresh from the file
     * @param create boolean. If true a new config is created
     * @return JsonConfig. 
     * @throws IOException 
     */
    public JsonConfig load(String sitename, boolean refresh, 
            boolean create) throws IOException {

        if(sitename == null || sitename.isEmpty()) {
            throw new NullPointerException();
        }

        if(loadedConfigs == null) {
            if(this.isUseCache()) {
                loadedConfigs = new HashMap<String, JsonConfig>(){
                    @Override
                    public JsonConfig put(String key, JsonConfig value) {
                        if(key == null || value == null) throw new NullPointerException();
                        return super.put(key, value);
                    }
                };
            }
        }else{
            if(!this.isUseCache()) {
                loadedConfigs = null;
            }
        }
        
        JsonConfig config = null;
        
        if(this.isUseCache() && !refresh) {
            config = loadedConfigs.get(sitename);
        }

        if(config != null) {
            
XLogger.getInstance().log(Level.FINER, "Loaded from cache: {0}", 
        this.getClass(), sitename);                
        }else {
            
            config = this.newConfig(sitename, create);

            if(this.isUseCache()) {
                loadedConfigs.put(sitename, config);
            }

            // This is a set so re-adding is a no-op
            getSitenames().add(sitename);
        }
        
        return config;
    }
    
    protected JsonConfig newConfig(String sitename, boolean create) throws IOException {
        JsonConfig config = initConfig(sitename, create, true);
        if(this.isRemote()) {
            // Note this
            this.getSitenames().add(sitename);
        }
        return config;
    }

    public void reverseSync() {
        
    }

    public void reverseSync(JsonConfigFilter srcFilter, JsonConfigFilter tgtFilter) {
        
        JsonConfigFactory reverse = this.newSyncFactory();
        
        ((ScrapperConfigFactory)reverse).syncAll(srcFilter, tgtFilter);
    }
    
    public void syncAll() {
        syncAll(null, null);
    }
            
    public void syncAll(JsonConfigFilter srcFilter, JsonConfigFilter tgtFilter) {
        
        // Without using a copy of the returned set, concurrent modification
        // exception was thrown on iterating through the returned set.
        //
        final Set<String> names = new HashSet<>(this.getSitenames());

        names.remove(this.getDefaultConfigName());
        
//XLogger.getInstance().log(Level.INFO, "Creating backup: before syncing all site configs", this.getClass());        
//        FileInterface file = this.getFile(this.getDefaultConfigName());
//        FileInterface parent = file.getParentFile();
        
        for(String name:names) {

            try{
                
                JsonConfig config = this.load(name, false);
                
                boolean accept = srcFilter == null || srcFilter.accept(config);
                
                if(!accept) {
                    continue;
                }
                
                JsonConfig syncPair = this.newSyncPair(name);
                
                accept =  tgtFilter == null || tgtFilter.accept(syncPair);

                if(!accept) {
                    continue;
                }
                
                this.sync(config, syncPair);

            }catch(IOException e) {
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }
        }
    }
    
    public JsonConfig newSyncPair(String configName) throws IOException {
        
        JsonConfigFactory syncFactory = this.newSyncFactory();
        
        JsonConfig config = syncFactory.initConfig(configName, true, false);
        
        if(this.isRemote()) {
                    // Note this
            this.getSitenames().add(configName);
        }
        
        return config;
    }
    
    public JsonConfigFactory newSyncFactory() {
        
        final boolean isRemote = !this.isRemote();
        
        JsonConfigFactory factory = new JsonConfigFactory(){
            @Override
            public boolean isRemote() {
                return isRemote;
            }
            @Override
            protected URI getConfigDir() {
                return JsonConfigFactory.this.getConfigDir();
            }
            @Override
            protected String getDefaultConfigName() {
                return JsonConfigFactory.this.getDefaultConfigName();
            }

            @Override
            protected DefaultFTPClient getFtpClient() {
                return JsonConfigFactory.this.getFtpClient();
            }
        };
        factory.setSearch(this.isSearch());
        factory.setUseCache(this.isUseCache());
        return factory;
    }
    
    public Set<String> getSyncSitenames() {
        
        JsonConfigFactory reverse = this.newSyncFactory();
        
        return reverse.getSitenames();
    }
    
    public Set<String> getSitenames() {
        if(this.isRemote()) {
            return this.getRemoteSitenames();
        }else{
            return this.getLocalSitenames();
        }
    }

    private Set<String> remotesites_use_getter_to_access;
    protected Set<String> getRemoteSitenames() {
        if(remotesites_use_getter_to_access != null) {
            return remotesites_use_getter_to_access;
        }else{
            this.remotesites_use_getter_to_access = new TreeSet<>();
        }
        
        String [] sites = null;
        
        DefaultFTPClient ftp = this.getFtpClient();
        
        try{
  
            sites = ftp.listNames(this.getConfigsDirFile().getPath(), FTPFileTypes.ASCII_FILE_TYPE, true);
            
        }catch(SocketException e) {
            XLogger.getInstance().log(Level.WARNING, "FTP Connection failed", this.getClass(), e);
        }catch(IOException e) {
            XLogger.getInstance().log(Level.WARNING, "FTP operation failed", this.getClass(), e);
        }
        
XLogger.getInstance().log(Level.FINER, "Files in sites dir: {0}", 
        this.getClass(), sites==null?null:Arrays.toString(sites));                
        
        if(sites == null) {
            throw new NullPointerException();
        }
        
        for(String site:sites) {
            // Default FTP directories
            if(site.equals(".") || site.equals("..")) {
                continue;
            }
            if(!site.endsWith(".json")) {
                continue;
            }
            this.remotesites_use_getter_to_access.add(site.replace(".json", ""));
        }
        
XLogger.getInstance().log(Level.FINER, "Available sitenames: {0}", this.getClass(), remotesites_use_getter_to_access);                
        return remotesites_use_getter_to_access;
    }   
    
    protected Set<String> getLocalSitenames() {    
        
        File sitesDir = this.getConfigsDirFile();

        try{
            
            sitesDir = sitesDir.getCanonicalFile();
            
        }catch(IOException e) {
            return null;
        }
XLogger.getInstance().log(Level.FINER, "Sites dir: {0}", 
        this.getClass(), sitesDir);                
         
        // We use File#listFiles because
        // On some environments File#list returns null
        // If we don't have access (e.g shared server)
        // If hidden file etc
        File [] files = sitesDir.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });
        
XLogger.getInstance().log(Level.FINER, "Files in sites dir: {0}", 
        this.getClass(), files==null?null:Arrays.toString(files));                

        String [] confignames = null;
        if(files != null) {
            confignames = new String[files.length];
            for(int i=0; i<files.length; i++) {
                confignames[i] = files[i].getName().replace(".json", "");     
            }
        }
        
        if(confignames == null) {
            throw new NullPointerException();
        }
        
        TreeSet<String> output = new TreeSet<>(Arrays.asList(confignames));
XLogger.getInstance().log(Level.FINER, "Available sitenames: {0}", this.getClass(), output);                
        return output;
    }
    
    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public boolean isSearch() {
        return search;
    }

    public void setSearch(boolean search) {
        this.search = search;
    }
    
    @Override
    public String toString() {
        return super.toString() + ". Site names: " + getSitenames();
    }
}

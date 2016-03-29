package com.scrapper;

import com.bc.util.XLogger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @(#)AppProperties.java   24-Nov-2013 00:07:30
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class AppProperties {
    
    public static final String TARGETNODES_TO_TRACK = "targetNodesToTrack";
    public static final String ABOUT = "about";
    public static final String LOG_LEVEL = "logLevel";
    public static final String BATCH_SIZE = "batchSize";
    public static final String BATCH_INTERVAL = "batchInterval";
    public static final String MAXCONCURRENT_PROCESSES = "maxConcurrentProcesses";
    public static final String BACKGROUNDPROCESS_MEMORY_FACTOR = "backgroundProcess.memoryFactor";
    public static final String BACKGROUNDPROCESS_MEMORY_MONITOR_INTERVAL = "backgroundProcess.memoryMonitorInterval";
    public static final String BACKGROUNDPROCESS_MINIMUM_MEMORY = "backgroundProcess.minimumMemory";
    public static final String BACKGROUNDPROCESS_CONTINUE_PREVIOUS = "backgroundProcess.continuePrevious";
    public static final String MESSAGE_DISPLAY_INTERVAL = "messageDisplayInterval";
    public static final String INSERT_URL = "insertUrl";
    public static final String CONFIGS_DIR = "configsDir";
    public static final String CONFIGS_DIR_REMOTE = "configsDirRemote";
    /**
     * The default config file name without the extension
     */
    public static final String DEFAULT_CONFIG_NAME = "defaultConfigName";
    public static final String SYNONYMS_PATH = "synonymsPath";
    public static final String LOGFILE_PATTERN = "logFilePattern";
    public static final String LOG_FORMATTER = "logFormatter";
    public static final String FTP_HOST = "ftpHost";
    public static final String FTP_PORT = "ftpPort";
    public static final String FTP_USER = "ftpUser";
    public static final String FTP_PASS = "ftpPass";
    public static final String FTP_DIR = "ftpDir";
    public static final String FTP_MAXRETRIALS = "ftpMaxretrials";
    public static final String FTP_RETRIALINTERVAL = "ftpRetrialInterval";
    public static final String MAX_SITES = "siteSearch.maxSites";
    public static final String SEARCHSITE_MAXCONCURRENTPROCESSES = "siteSearch.maxConcurrentProcesses";
    public static final String SITESEARCH_TIMEOUT = "siteSearch.timeout";
    public static final String MAX_RESULTS = "siteSearch.maxResults";
    public static final String REFRESH_REQUEST_TIMES = "siteSearch.refreshRequestTimes";
    public static final String TABLENAME_KEY = "tablenameKey";
    
    // We use the location of the META-INF folder in the source packages folder 'src'.
    // The default META-INF folder is in the class files package which gets cleaned
    // and built under netbeans development environment
    //
    private static String propertiesFilename = System.getProperty("user.home")+"/Documents/NetBeansProjects/scrapper/src/META-INF/properties/app.properties";

    private static String defaultPropertiesFilename = "META-INF/properties/default.properties";
    
    private static Properties props;
    
    private static final String [] publicStaticFinalFields;
    
    static {
        try{
            publicStaticFinalFields = loadFieldNames();
        }catch(IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }    
    }
    
    public static String [] getPropertynames() {
        return publicStaticFinalFields;
    }

    public static String getProperty(String name) {
        String val = props.getProperty(name);
        return val == null ? null : format(val);
    }

    public static Object setProperty(String name, String val) {
        val = val == null ? null : format(val);
        return props.setProperty(name, val);
    }
    
    public static Properties instance() {
        return props;
    }
    
    private static WeakReference<Pattern> _wr;
    private static Pattern getPattern() {
        if(_wr == null || _wr.get() == null) {
XLogger.getInstance().log(Level.FINER, "Creating pattern for system properties embedded in app properties", AppProperties.class);
            Pattern p = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
            _wr = new WeakReference<>(p);
            return p;
        }else{
XLogger.getInstance().log(Level.FINER, "Reusing pattern for system properties embedded in app properties", AppProperties.class);
            return _wr.get();
        }
    }
    private static String format(String s) {
        int start = nextNameStart(s, 0);
        if(start == -1) {
            return s;
        }
        StringBuffer update = new StringBuffer();
        Pattern pattern = getPattern();
        Matcher m = pattern.matcher(s);
        while(m.find()) {
            String prop_name = m.group(1);
            
            // First check app properties, then check system properties
            String prop_value = props.getProperty(prop_name);
            if(prop_value == null) {
                prop_value = System.getProperty(prop_name);
            }
            
            if(prop_value == null) {
                throw new NullPointerException();
            }
XLogger.getInstance().log(Level.FINER, "Replacing {0} with system property: {1}", 
        AppProperties.class, prop_name, prop_value);
//            prop_value = prop_value.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            prop_value = prop_value.replace('\\', '/');
            m.appendReplacement(update, prop_value);
        }
        m.appendTail(update);
        return update.toString();
    }
    
    private static String nextName(String s, int pos) {
        
        int start = nextNameStart(s, pos);
        
        if(start == -1) {
            return null;
        }
        
        int end = s.indexOf('}', start);
        
        if(end == -1) {
            return null;
        }
        
        String key = s.substring(start, end);
        
        return key;
    }

    private static int nextNameStart(String s, int pos) {
        int n = s.indexOf('$', pos);
        if(n == -1) {
            return -1;
        }
        boolean proceed = s.charAt(n + 1) == '{';
        if(!proceed) {
            return -1;
        }
        int end = s.indexOf('}', n);
        if(end == -1) {
            return -1;
        }
        return n + 2;
    }
    
    private static String [] loadFieldNames() throws IllegalAccessException {
        
        Field [] fields = AppProperties.class.getFields();

        HashSet<String> names = new HashSet<>();

        for(int i=0; i<fields.length; i++) {

            if(accept(fields[i])) {

                Object fieldValue = fields[i].get(null);

XLogger.getInstance().log(Level.FINER, "Field: {0},  Value: {1}", 
    AppProperties.class, (i+1), fieldValue);

                names.add(fieldValue.toString());
            }
        }
        
        return names.toArray(new String[0]);
    }

    private static boolean accept(Field field) {
        final int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
                && Modifier.isFinal(modifiers);
    }

    public static void load(String propertiesFilename) throws IOException {
        setPropertiesFilename(propertiesFilename);
        load();
    }
    
    public static void load() throws IOException {
        
        InputStream in = null;
        
        // create and load default properties
        try{
            
            Properties defaultProps = new Properties();

XLogger.getInstance().log(Level.INFO, "Default properites file: {0}, properties file: {1}", 
        AppProperties.class, defaultPropertiesFilename, propertiesFilename);            
            
            in = getInputStream(defaultPropertiesFilename);
            
            if(in == null) {
                throw new NullPointerException();
            }
            
            defaultProps.load(in);
            
XLogger.getInstance().log(Level.FINE, "Default properties: {0}", AppProperties.class, defaultProps);            

            in.close();

            // create application properties with default
            props = new Properties(defaultProps);

            // now load properties 
            // from last invocation
            in = getInputStream(propertiesFilename);

            if(in == null) {
                throw new NullPointerException();
            }
            
            props.load(in);

XLogger.getInstance().log(Level.FINE, "Properties: {0}", AppProperties.class, props);            
            
        }finally{
            if(in != null) {
                in.close();
            }
        }
    }
    
    public static void store() throws FileNotFoundException, IOException {
  
        // create and load default properties
        try(OutputStream out = getOutputStream(propertiesFilename, false)) {
            
            
            props.store(out, "Application Properties saved on: "+new Date());
            
        }
    }
    
    private static InputStream getInputStream(String path) {
        InputStream in;
        try{
            in = new FileInputStream(path); 
        }catch(FileNotFoundException e) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        }
        return in;
    }
    
    private static OutputStream getOutputStream(String path, boolean append) 
            throws FileNotFoundException {
        OutputStream out;
        try{
            out = new FileOutputStream(path, append);
        }catch(FileNotFoundException e) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            try{
                out = new FileOutputStream(Paths.get(url.toURI()).toFile(), false);
            }catch(URISyntaxException use) {
                XLogger.getInstance().log(Level.WARNING, "For URL: "+url, AppProperties.class, e);
                out = null;
            }
        }
        return out;
    }

    public static String getDefaultPropertiesFilename() {
        return defaultPropertiesFilename;
    }

//    public static void setDefaultPropertiesFilename(String defaultPropertiesFilename) {
//        AppProperties.defaultPropertiesFilename = defaultPropertiesFilename;
//    }

    public static String getPropertiesFilename() {
        return propertiesFilename;
    }

    public static void setPropertiesFilename(String propertiesFilename) {
        AppProperties.propertiesFilename = propertiesFilename;
    }
}

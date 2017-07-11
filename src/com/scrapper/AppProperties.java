package com.scrapper;

public interface AppProperties {
    
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
}

package com.scrapper.config;

import java.util.Map;

public abstract interface Config
{
  public static enum Extractor
  {
    parentNode,  targetNode,  startAtNode,  stopAtNode,  id,  disabled(Boolean.class), 
    crawlLimit(Number.class),  parseLimit(Number.class),  scrappLimit(Number.class), 
    uploadURL,  defaultTitle,  append(Boolean.class),  lineSeparator, 
    partSeparator,  replaceNonBreakingSpace(Boolean.class), 
    maxBreaktagsBetween(Number.class),  nodesToRetainAttributes(String[].class), 
    formatter(String.class),  attributes(Map.class),  attributesRegex(Map.class), 
    columns(String[].class),  maxFiltersPerKey(Number.class),  transverse(String[].class), 
    offset(Number.class),  length(Number.class), 
    nodesToAccept(String[].class),  nodesToReject(String[].class), 
    nodeTypesToAccept(String[].class),  nodeTypesToReject(String[].class), 
    textToReject(String[].class),  textToDisableOn(String[].class), 
    attributesToAccept(String[].class),  attributesToExtract(String[].class), 
    expressionVariableName,  directTagContentsOnly(Boolean.class), 
    captureUrlFilter_required(String[].class),  captureUrlFilter_requiredRegex, 
    captureUrlFilter_unwanted(String[].class),  captureUrlFilter_unwantedRegex, 
    scrappUrlFilter_required(String[].class),  scrappUrlFilter_requiredRegex, 
    scrappUrlFilter_unwanted(String[].class),  scrappUrlFilter_unwantedRegex, 
    replace(Boolean.class),  table(Map.class),  type(Map.class), 
    walk(String[].class),  startAtFilter(String.class),  stopAtFilter(String.class), 
    searchUrlProducerClassName(String.class), 
    minDataToAccept(Number.class), 
    hasExplicitLinks(Boolean.class), 
    isTitleInUrl(Boolean.class),  isTitleGeneric(Boolean.class),  isDescriptionGeneric(Boolean.class);
    
    private Class ftype;
    
    private Extractor() { this.ftype = String.class; }
    
    private Extractor(Class type) {
      this.ftype = type;
    }
    
    public Class getType() { return this.ftype; }
    
    public String getName() {
      return name();
    }
  }
  
  public static enum Formatter
  {
    defaultValues(Map.class),  datePatterns(String[].class),  language, 
    exression,  jobRequestFields(String[].class),  formatter(String.class), 
    update,  accept,  replace,  replaceRegex, 
    set,  maxExpressions(Number.class);
    
    private Class type;
    
    private Formatter() { this.type = String.class; }
    
    private Formatter(Class type) {
      this.type = type;
    }
    
    public Class getType() { return this.type; }
    
    public String getName() {
      return name();
    }
  }
  
  public static enum Site
  {
    url(Map.class),  tables(String[].class),  login,  start(Number.class),  end(Number.class), 
    ascending(Boolean.class),  columns(String[].class), 
    replacements(String[].class);
    
    private Class type;
    
    private Site() { this.type = String.class; }
    
    private Site(Class type) {
      this.type = type;
    }
    
    public Class getType() { return this.type; }
    
    public String getName() {
      return name();
    }
  }
  
  public static enum Login
  {
    loginCredentials(Map.class),  action,  emailAddress,  username,  password,  confirmPassword;
    
    private final Class type;
    
    private Login() { this.type = String.class; }
    
    private Login(Class type) {
      this.type = type;
    }
    
    public Class getType() { return this.type; }
    
    public String getName() {
      return name();
    }
  }
  
  public static enum Keys{
    ;
    private Keys() {}
  }
}

package com.scrapper.context;

public abstract interface CapturerSettings
{
  public abstract String[] getTransverse(String paramString);
  
  public abstract String[] getTextToDisableOn(String paramString);
  
  public abstract String[] getTextToReject(String paramString);
  
  public abstract Boolean isConcatenateMultipleExtracts();
  
  public abstract Boolean isConcatenateMultipleExtracts(String paramString);
  
  public abstract String getLineSeparator();
  
  public abstract String getPartSeparator();
  
  public abstract String getDefaultTitle();
  
  public abstract String[] getColumns(String paramString);
  
  public abstract String[] getAttributesToAccept(String paramString);
  
  public abstract String[] getAttributesToExtract(String paramString);
  
  public abstract String[] getNodeToReject(String paramString);
  
  public abstract String[] getNodeTypesToAccept(String paramString);
  
  public abstract String[] getNodeTypesToReject(String paramString);
  
  public abstract String[] getNodesToAccept(String paramString);
  
  public abstract String[] getNodesToRetainAttributes(String paramString);
  
  public abstract boolean isExtractAttributes(String paramString);
  
  public abstract boolean isReplaceNonBreakingSpace(String paramString);
}

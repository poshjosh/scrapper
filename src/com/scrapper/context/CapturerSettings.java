package com.scrapper.context;

import java.util.Map;

public interface CapturerSettings {
    
    Map getDefaults();
    
    String[] getDatePatterns(); 
    
    String [] getUrlDatePatterns();

    String[] getTransverse(String id);

    String[] getTextToDisableOn(String id);

    String[] getTextToReject(String id);

    boolean isConcatenateMultipleExtracts(String id, boolean defaultValue);

    String getLineSeparator();

    String getPartSeparator();

    String getDefaultTitle();

    String[] getColumns(String id);

    String[] getAttributesToAccept(String id);

    String[] getAttributesToExtract(String id);

    String[] getNodeToReject(String id);

    String[] getNodeTypesToAccept(String id);

    String[] getNodeTypesToReject(String id);

    String[] getNodesToAccept(String id);

    String[] getNodesToRetainAttributes(String id);

    boolean isReplaceNonBreakingSpace(String id, boolean defaultValue);
}

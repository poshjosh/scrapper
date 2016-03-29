package com.scrapper.context;


/**
 * @(#)CapturerSettings.java   08-Oct-2015 22:53:00
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
public interface CapturerSettings {

    String [] getTransverse(String id);
    
    String [] getTextToDisableOn(String id);
    
    String [] getTextToReject(String id);
    
    Boolean isConcatenateMultipleExtracts();
    
    Boolean isConcatenateMultipleExtracts(String id);
    
    String getLineSeparator();
    
    String getPartSeparator();

    String getDefaultTitle();
    
    String [] getColumns(String id);
    
    String[] getAttributesToAccept(String id);

    String[] getAttributesToExtract(String id);

    String[] getNodeToReject(String id);

    String[] getNodeTypesToAccept(String id);

    String[] getNodeTypesToReject(String id);

    String[] getNodesToAccept(String id);

    String[] getNodesToRetainAttributes(String id);

    boolean isExtractAttributes(String id);

    boolean isReplaceNonBreakingSpace(String id);
}

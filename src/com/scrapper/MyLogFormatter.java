package com.scrapper;

import com.scrapper.util.Util;
import java.util.logging.LogRecord;
import org.htmlparser.Tag;

/**
 * @(#)MyLogFormatter.java   11-Jan-2014 01:39:06
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
public class MyLogFormatter extends SimpleLogFormatter {
    
    public MyLogFormatter() {
        this.setAppendMetaData(true);
    }

    @Override
    public synchronized String formatMessage(LogRecord record) {
        
        format(record, 70);

        return super.formatMessage(record);
    }
    
    private void format(LogRecord record, int maxLengthNode) {
        
        Object [] params = record.getParameters();
        
        if(params == null) return;
        
        for(int i=0; i<params.length; i++) {
            
            if(params[i] instanceof org.htmlparser.Tag) {

                String sval = ((Tag)params[i]).toTagHtml();
                int len = maxLengthNode > sval.length() ? sval.length() : maxLengthNode;
                        
                params[i] = sval.substring(0, len);
                
            }else
            if(params[i] instanceof org.htmlparser.Node) {
             
                params[i] = Util.appendTag((org.htmlparser.Node)params[i], maxLengthNode);
            }
        }
    }
}

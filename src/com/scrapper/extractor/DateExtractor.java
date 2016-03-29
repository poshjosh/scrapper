package com.scrapper.extractor;

import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @(#)DateExtractor.java   23-Dec-2011 10:18:13
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class DateExtractor implements Serializable {

    public DateExtractor() { }
    
    public String extractDate(String input) {
        return extractDate(Util.getDatePatterns(), input);
    }

    public String extractDate(Pattern [] datePatterns, String input) {
//Logger.getLogger(this.getClass().getName()).info("Input: " + input);

        String output = null;
        for(int i=0; i<datePatterns.length; i++) {

            Matcher m = datePatterns[i].matcher(input);

            if(m.find()) {
                output = m.group();
                break;
            }
        }

        if(output != null) output.replaceFirst("of\\s", "");

//Logger.getLogger(this.getClass().getName()).info("Output:" + output);
        return output;
    }
}

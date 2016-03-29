package com.scrapper.formatter;

/**
 * @(#)JobTitleFormatter.java   07-Mar-2013 01:27:24
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
public class JobTitleFormatter extends CompanyNameFormatter {
    
    @Override
    public String format(String s) {
        super.format(s);
        return this.getJobTitle();
    }
}

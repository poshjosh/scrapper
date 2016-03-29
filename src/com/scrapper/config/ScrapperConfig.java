package com.scrapper.config;

import com.bc.json.config.JsonData;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @(#)DefaultCapturerConfig.java   11-Jan-2014 00:21:53
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
public class ScrapperConfig 
        extends com.bc.json.config.SimpleJsonConfig {
    
    public ScrapperConfig() { } 

    public ScrapperConfig(String name) {
        super(name);
    }
    
    public ScrapperConfig(String name, Map jsonData) { 
        super(name, jsonData);
    }
    
    public ScrapperConfig(String name, JsonData parent, Object...path) { 
        super(name, parent, path);
    }
    
    public Set getColumns() {
        TreeSet columns = new TreeSet();
        int max = this.getInt(Config.Extractor.maxFiltersPerKey);
        for(int i=0; i<max; i++) {
            List nodeCols = this.getList(
                    Config.Extractor.targetNode+""+i, 
                    Config.Extractor.columns);
            if(nodeCols == null || nodeCols.isEmpty()) {
                continue;
            }
            columns.addAll(nodeCols);
        }
        return columns;
    }
}

package com.scrapper.url;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * @(#)ConfigURLList.java   03-Apr-2014 22:08:19
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class ConfigURLList extends ArrayList<String> implements Serializable {
    
    public static void main(String [] args) {
        
        List letters = new ArrayList();
        letters.addAll(Arrays.asList(new String[]{"A", "B", "C"}));
        List numbers = new ArrayList();
        numbers.addAll(Arrays.asList(new String[]{"1", "2", "3", "4"}));
        List symbols = new ArrayList();
        symbols.addAll(Arrays.asList(new String[]{"*", "+"}));
        
        List source = new ArrayList();
        source.add(letters); source.add(numbers); source.add(symbols);
        
        ConfigURLList list = new ConfigURLList();
        
        list.generatePermutations("http://www.abc.com", source, list);
        
System.out.println(list);        
    }
    
    public ConfigURLList() { }
    
    public void update(JsonConfig config, String type) { 
        
        int max = 20;
        
        List<List> parts = null;        
        
        for(int i=0; i<max; i++) {
            
            if(!ConfigURLPartList.accept(config, type, i)) {
                continue;
            }

            ConfigURLPartList part = new ConfigURLPartList(config, type, i);

            if(parts == null) {

                parts = new ArrayList<List>();
            }

            parts.add(part);
        }
        
        if(parts == null) {
            return;
        }
        
        assert parts.size() <= 10 : this.getClass()+" does not support > 10 parts";
        
        String baseUrl = Util.getBaseURL(config.getString("url", "value"));
        
        this.generatePermutations(baseUrl, parts, this);
       
if(XLogger.getInstance().isLoggable(Level.FINER, this.getClass())) {
    StringBuilder builder = new StringBuilder();
    builder.append("URLs:\n");
    for(String url:this) {
        builder.append(url).append('\n');
    }
    XLogger.getInstance().log(Level.FINER, "{0}", this.getClass(), builder);            
}        
    }
    
    private void generatePermutations(String baseUrl, List<List> source, List<String> result) {

        // Do not use StringBuilder....
        
        this.generatePermutations(baseUrl, source, result, 0, "");
    }
    
    private void generatePermutations(String baseUrl, List<List> source, 
            List<String> result, int depth, String current) {
        
        if(depth == source.size()) {
            
            String url = baseUrl + current;
            
// Spaces have been found in urls due the the combination of various parts            
            url = url.replaceAll("\\s", "");
            
            result.add(url);
            
            return;
        }

        for(int i = 0; i < source.get(depth).size(); ++i) {
            
            // Do not use StringBuilder....
            
            generatePermutations(baseUrl, source, result, depth + 1, current + source.get(depth).get(i));
        }
    }    
}

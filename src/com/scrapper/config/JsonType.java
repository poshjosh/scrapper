package com.scrapper.config;

import com.bc.util.XLogger;
import com.scrapper.config.Config.Extractor;
import com.scrapper.config.Config.Formatter;
import com.scrapper.config.Config.Login;
import com.scrapper.config.Config.Site;
import com.scrapper.filter.FilterFactory;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @(#)JsonType.java   28-Dec-2013 06:37:11
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
public class JsonType {

    private Set<String> nodeTypes;

    private ArrayList<Enum[]> typedEnums = new ArrayList<Enum[]>();
    
    public JsonType() {
        nodeTypes = new HashSet<String>(Arrays.asList(FilterFactory.NODE_TYPES));
        typedEnums.add(Extractor.values());
        typedEnums.add(Formatter.values());
        typedEnums.add(Site.values());
        typedEnums.add(Login.values());
//        typedEnums.add(Keys.values());
    }
    
    /**
     * @param arr The enum class in which to search for type
     * @param propertyLink format abc.def.xyz
     * @return 
     */
    public Class getType(Enum [] arr, String propertyLink) {
        
        Class type = this.getCustomType(propertyLink);

        if(type == null) {

            Enum en = this.getMatchingEnum(arr, propertyLink);

            if(en == null) {
                type = String.class;
            }else{
                type = getType(en);
            }            
        }
        
        return type;
    }

    /**
     * @param arr The enum class in which to search for type
     * @param propertyLink format abc.def.xyz
     * @return 
     */
    public Class getType(Object [] pathToValue, Class defaultType) {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<pathToValue.length; i++) {
            builder.append(pathToValue[i]);
            if(i < pathToValue.length-1) {
                builder.append('.');
            }
        }
        return this.getType(builder.toString(), defaultType);
    }
    
    /**
     * @param arr The enum class in which to search for type
     * @param propertyLink format abc.def.xyz
     * @return 
     */
    public Class getType(String propertyLink, Class defaultType) {
        
        Class type = this.getCustomType(propertyLink);

        if(type == null) {

            Enum en = null;
            for(Enum [] arr:this.typedEnums) {
                en = this.getMatchingEnum(arr, propertyLink);
                if(en != null) {
                    break;
                }
            }

            if(en == null) {
                type = defaultType;
            }else{
                type = getType(en);
            }            
        }
XLogger.getInstance().log(Level.FINER, "Property link: {0}, type: {1}", 
        this.getClass(), propertyLink, type);        
        return type;
    }
    
    private Enum getMatchingEnum(Enum [] arr, String pname) {
    
        int n = pname.indexOf('.');
        String pnamePart = null;
        if(n != -1) {
            // parentNode.append we use last part i.e append to determine type
            String [] parts = pname.split("\\.");
            pnamePart = parts[parts.length-1];
        }
        
        // @bug don't know why datePatterns wasn't matched below
        if(pname.equals("datePatterns")) {
            return Formatter.datePatterns;
        }
        
        for(Enum en:arr) {
            if(this.matches(pnamePart==null?pname:pnamePart, en)) {
                return en;
            }
        }
PrintStream st = n == -1 ? System.out : System.err;
st.println(this.getClass().getName()+". No match found for: "+pname);        
        return null;
    }

    public Class getType(Enum en) {
        Class enClass = en.getClass();
        if(enClass == Config.Formatter.class) {
            return Enum.valueOf(Config.Formatter.class, en.name()).getType();
        }else if(enClass == Config.Extractor.class) {
            return Enum.valueOf(Config.Extractor.class, en.name()).getType();
        }else if(enClass == Config.Site.class) {
            return Enum.valueOf(Config.Site.class, en.name()).getType();
        }else if(enClass == Config.Login.class) {
            return Enum.valueOf(Config.Login.class, en.name()).getType();
        }else{
            throw new IllegalArgumentException("Unexpected PropertyEnum class: "+enClass);
        }        
    }
    
    private Class getCustomType(String pname) {
        if(pname.equals("url.start")) {  
            // resolves conflict between url.start and url.counter.start
            return String.class;
        }else if(pname.contains(".mappings.")) {
            if(pname.endsWith(".replace")) {
                return Boolean.class;
            }else{
                return Map.class;
            }
        }else if(pname.startsWith("expression")) {
            if(pname.contains(".replace.") || pname.contains(".replaceRegex.")) {
                return Map.class;
            }
        }
        return null;
    }
    private boolean matches(String pnamePart, Enum ex) {
        String name = ex.name();
        if(pnamePart.equals(name)) {
            return true;
        }
        if(nodeTypes.contains(name)) {
            for(int i=0; i<20; i++) {
                if(pnamePart.equals(name+i)) {
                    return true;
                }else if(pnamePart.equals(name+i+"Value")) {
                    return true;
                }
            }
        }
        return false;
    }
}

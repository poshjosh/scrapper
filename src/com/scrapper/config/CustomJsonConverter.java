package com.scrapper.config;

import com.bc.util.JsonFormat;
import com.bc.util.XLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * @(#)CustomJsonConverter.java   28-Dec-2013 07:02:19
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
public class CustomJsonConverter {
    
    private boolean escapeOutput;
    
    private Class type;
    
    private final JsonFormat jsonFormat;
    
    public CustomJsonConverter() { 
        jsonFormat = new JsonFormat();
    }
    
    public CustomJsonConverter(Class type) {
        CustomJsonConverter.this.setType(type);
        jsonFormat = new JsonFormat();
    }
    
    public CustomJsonConverter(Object...key) {
        CustomJsonConverter.this.setType(key);
        jsonFormat = new JsonFormat();
    }
    
    public CustomJsonConverter(String propertyLink) {
        CustomJsonConverter.this.setType(propertyLink);
        jsonFormat = new JsonFormat();
    }
    
    public String convert(Object oval) {
        
XLogger.getInstance().log(Level.FINER, "Converting to plain: type: {0}, text: {1}", 
        this.getClass(), type, oval);        

        if(oval == null) {
            throw new NullPointerException();
        }

        String sval;
        
        if(type == String[].class) {
            
            List parts = (List)oval;
            
            StringBuilder builder = new StringBuilder();
            
            for(int i=0; i<parts.size(); i++) {
    
                Object part = parts.get(i);
                
                part = this.removeEdges("\"", part.toString(), "\"");
                
                builder.append(part);
                
                if(i < parts.size()-1) {
                    builder.append(", ");
                }
            }
            
            sval = builder.toString();
            
        }else if(type == String.class) {
            
            sval = this.removeEdges("\"", oval.toString(), "\"");
            
        }else{
            
            sval = oval.toString();
        }
        
XLogger.getInstance().log(Level.FINER, "Converted to plain: {0}", this.getClass(), sval);        
        return sval;
    }
    
    public Object reverse(String sval) {

XLogger.getInstance().log(Level.FINER, "Reversing to json: type: {0}, text: {1}", 
        this.getClass(), type, sval);        
        
        sval = sval.trim();
        
        jsonFormat.setEscapeOutput(this.isEscapeOutput());
        
        String json;
        
        if(type == String[].class) {
            
            List<String> list = toList("[", sval, "]");

            json = jsonFormat.toJSONString(list);

        }else if(type == String.class) {
            
            json = jsonFormat.toJSONString(sval);
            
        }else{
            
            json = sval;
        }
        
        Object oval = null;
        
        try{
            oval = JSONValue.parseWithException(json);
        }catch(ParseException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
        
XLogger.getInstance().log(Level.FINER, "Reversed to json: {0} {1}", 
        this.getClass(), oval.getClass(), oval);        

        return oval;
    }
    
    private List<String> toList(String prefix, String text, String suffix) {
        
        text = this.removeEdges(prefix, text, suffix);
        
        String [] parts = text.split(",\\s{0,2}");
        
        ArrayList<String> list = new ArrayList<String>();
        
        String firstPart = null;
        
        for(String part:parts) {
            
            part = part.trim();
            
            if(part.isEmpty()) {
                continue;
            }

            if(part.endsWith("\\")) {
                
                firstPart = part;
                
            }else{
                
                if(firstPart!=null) {
                    part = firstPart+part;
                }
                
                list.add(this.removeEdges("\"", part, "\""));
            }
        }
        
        return list;
    }
    
    private String removeEdges(String prefix, String tgt, String suffix) {
XLogger.getInstance().log(Level.FINEST, "Prefix: {0}, Main: {1}, Suffix: {2}", 
        this.getClass(), prefix, tgt, suffix);
        int start = 0;
        if(tgt.startsWith(prefix)) {
            start = prefix.length();
        }
        int end = tgt.length();
        if(tgt.endsWith(suffix)) {
            end = tgt.length() - suffix.length();
        }
        String output = tgt.substring(start, end);
XLogger.getInstance().log(Level.FINEST, "Output: {0}", 
        this.getClass(), output);
        return output;
    }

    public boolean isEscapeOutput() {
        return escapeOutput;
    }

    public void setEscapeOutput(boolean escapeOutput) {
        this.escapeOutput = escapeOutput;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public void setType(Object...path) {
        this.type = new JsonType().getType(path, String.class);
    }

    public void setType(String propertyLink) {
        this.type = new JsonType().getType(propertyLink, String.class);
    }
}

package com.scrapper.config;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
//import com.pumanager.fk.ForeignkeySet;
//import com.loosedb.beans.database.tables.MetaDataIx;

/**
 * @(#)MappingsConfig.java   15-Dec-2013 00:05:21
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
public class MappingsConfig {
    
    private JsonConfig config;
    
    public MappingsConfig() { }
    
    public MappingsConfig(JsonConfig config) {
        this.config = config;
    }
    
    public boolean isColumnMapping(Mapping mapping) {
        return mapping.getColumn() != null;
    }
    
    public String getTablePropertyForColumnMapping(
            String prefix, String value, boolean useDefaultIfNoneFound) {
        
        Object [] propertyKey = getPropertyPath(prefix, 
                Config.Extractor.table.name());
        
        Map params = this.getConfig().getMap(propertyKey);
        
        Object output;
        
        if(params != null) {
            
            output = params.get(value);
            
        }else{

            if(useDefaultIfNoneFound) {
                // This is the default tables name
                output = config.getList(Config.Site.tables).get(0).toString();
            }else{
                output = null;
            }
        }
        
        return output==null?null:output.toString();
    }
    
    public Mapping toMapping(
            final String table, final String column, 
            final String category, final String value) {
        return new DefaultMapping(table, column, category, value);
    }    

    private Map<String, String> getCache(String key, Map<String, Map<String, String>> cache) {
        Map<String, String> sub = cache.get(key);
        if(sub == null) {
            sub = new HashMap(){
                @Override
                public Object put(Object key, Object value) {
                    if(key == null || value == null) throw new NullPointerException();
                    return super.put(key, value);
                }
            };
            cache.put(key, sub);
        }
        return sub;
    }
    
    public boolean isTableMapping(Mapping mapping) {
        // If either column or category is null, we can only create a 
        // a tables mapping, we can't create a column mapping
        return mapping.getColumn() == null || mapping.getCategory() == null;
    }
    
    public Map.Entry<String, String> getTableEntry(Mapping mapping) 
            throws IllegalArgumentException {
        
        if(mapping.getTable() == null) {
            throw new IllegalArgumentException("Please specify a table for value: "+mapping.getValue());
        }
        
        // Example format: ${prefix}.mappings.${suffix}=/land/=8,,,/commercial/=2
        // Example format: url.mappings.type=/land/=8,,,/commercial/=2
        // Example format: targetNode0.mappings.offerType=sales=1

        return this.getEntry(mapping.getValue(), mapping.getTable());
    }
    
    
    private Map.Entry<String, String> getEntry(final String key, final String val) {
        return new Map.Entry<String, String>(){
            @Override
            public String getKey() {
                return key;
            }
            @Override
            public String getValue() {
                return val;
            }
            @Override
            public String setValue(String value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
    
    public Object [] getPropertyPath(
            String prefix, String suffix) {
        if(prefix == null) {
            throw new NullPointerException();
        }
        Object [] output;
        if(suffix == null) {
            output = new String[]{prefix, "mappings"};
        }else{
            output = new String[]{prefix, "mappings", suffix};
        }
        return output;
    }
    
    private void setProperty(JsonConfig props, 
            Object[] pathToValue, 
            Map<String, String> value) {
        
        if(value == null || value.isEmpty()) {
            
XLogger.getInstance().log(Level.FINER, "Removing: {0}", 
        this.getClass(), pathToValue);        

            props.remove(pathToValue);
            
        }else{
            
XLogger.getInstance().log(Level.FINER, "Updating: {0}={1}", 
        this.getClass(), pathToValue, value);        

            props.setObject(pathToValue, value);
        }
    }

    public static interface Mapping {
        String getValue();
        String getTable();
        String getColumn();
        String getCategory();
    }
    
    private static class DefaultMapping implements Mapping {
        private String table;
        private String column;
        private String category;
        private String value;
        private DefaultMapping(String table, String column, 
                String category, String value) {
            if(value == null) {
                throw new NullPointerException("Please enter the required input(s) before proceeding");
            }
            if(table == null && column == null && category == null) {
                throw new NullPointerException("Please enter the required input(s) before proceeding");
            }
            this.table = table;
            this.column = column;
            this.category = category;
            this.value = value;
        }
        @Override
        public String getCategory() {
            return category;
        }
        @Override
        public String getColumn() {
            return column;
        }
        @Override
        public String getTable() {
            return table;
        }
        @Override
        public String getValue() {
            return value;
        }
        @Override
        public String toString() {
            // This method is used in the equals method...
            // 
            StringBuilder builder = new StringBuilder();
            if(getColumn() != null) {
                builder.append(getValue()).append('=').append(getTable());
                builder.append('>').append(getColumn()).append('>').append(getCategory());
                return builder.toString();
            }else{
                builder.append(getValue()).append('=').append(getTable());
                return builder.toString();
            }
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if( !(o instanceof Mapping) ) {
                return false;
            }
            if(this.hashCode() != o.hashCode()) {
                return false;
            }
            return this.toString().equals(o.toString());
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (this.table != null ? this.table.hashCode() : 0);
            hash = 59 * hash + (this.column != null ? this.column.hashCode() : 0);
            hash = 59 * hash + (this.category != null ? this.category.hashCode() : 0);
            hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
            return hash;
        }
    }

    public JsonConfig getConfig() {
        return config;
    }

    public void setConfig(JsonConfig config) {
        this.config = config;
    }
}

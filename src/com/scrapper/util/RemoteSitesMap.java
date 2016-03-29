package com.scrapper.util;

import com.bc.manager.Filter;
import com.scrapper.CapturerApp;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.config.Config;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @(#)RemoteSitesMap.java   15-Mar-2014 13:18:49
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * A Map whose keys are table names for all items/products in the database
 * and whose values are list the remote sites offering content for the 
 * corresponding table key.
 * <br/><br/>
 * <b>Format:</b>
 * <tt>table name = List of sites</tt> 
 * <br/><br/>
 * <b>Example:</b>
 * <br/><br/>
 * gadgets = [sitename1, sitename3, sitename17]
 * <b>Note:</b>
 * <br/><br/>
 * Since the table names and corresponding sites don't change readily,
 * The load factor is <tt>1</tt> and the Map is not modifiable.
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class RemoteSitesMap extends HashMap<String, Set<String>> 
        implements Filter<JsonConfig> {
    
    public RemoteSitesMap() {

        this.init();
    }
    
    public RemoteSitesMap(int initialCapacity, float loadfactor) {
        
        super(initialCapacity, loadfactor);
        
        this.init();
    }
    
    private void init() {
        
        ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();

        Set<String> names = factory.getSitenames();

        for(String name:names) {

            // Second method arg = true, indicates 'refresh'. We refresh because 
            // the config file may have been updated since it was last loaded
            //
            JsonConfig config = factory.getConfig(name, true);
            
            if(config == null) {
                continue;
            }
            
            if(!RemoteSitesMap.this.accept(config)) {
                
                // We only accept configs which have search url producers 
                //
                factory.removeConfig(name);
                
                continue;
                
            }
            
            Map map = config.getMap(Config.Site.url, "mappings", Config.Extractor.table.name());

            Collection tables;
            if(map == null) {

                tables = config.getList(Config.Site.tables);
                
            }else{

                tables = map.values();
            }
            
            for(Object table:tables) {

                this.add(table.toString(), name);
            }
        }
    }

    @Override
    public boolean accept(JsonConfig config) {
        return true;
    }
    
    private void add(String table, String sitenameToAdd) {
        
        Set<String> tableSitenames = this.get(table);
        
        if(tableSitenames == null) {
            
            tableSitenames = new TreeSet();
            
            this.put(table, tableSitenames);
        }
        
        tableSitenames.add(sitenameToAdd);
    }
}

package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @(#)MultiResolver.java   27-Mar-2013 10:20:05
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
public class MultiResolver implements Resolver {
    
    private List<Resolver> resolvers;
    
    private Map<String, String> variables;
    
    public MultiResolver() { }
    
    public MultiResolver(Resolver resolver, String variableName, String variableValue) { 
        MultiResolver.this.addResolver(resolver);
        MultiResolver.this.addVariable(variableName, variableValue);
    }
    
    public MultiResolver(List<Resolver> resolvers, Map<String, String> variables) { 
        this.resolvers = resolvers;
        this.variables = variables;
    }    
    
    public void reset() {
        if(variables != null) {
            variables.clear();
        }
    }
    
    @Override
    public boolean isResolvable(String input) {
        if(resolvers == null) throw new NullPointerException();
        for(Resolver resolver:resolvers) {
            if(resolver.isResolvable(input)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String resolve(String input) throws ResolverException {
XLogger.getInstance().log(Level.FINER, "Input: {0}", this.getClass(), input);

        if(variables != null && !variables.isEmpty()) {
            for(String key:variables.keySet()) {
                String val = variables.get(key);
                input = input.replace(key, val);
            }
        }
XLogger.getInstance().log(Level.FINER, "After updating variables, input: {0}", this.getClass(), input);

        int maxCycles = 10;

        for(int i=0; i<maxCycles; i++) {
            
            for(Resolver resolver:resolvers) {

StringBuilder log = new StringBuilder("Resolved: ");
log.append(input).append(", to: ");

                input = resolver.resolve(input);

log.append(input).append(", using: ").append(resolver.getClass().getName());           
XLogger.getInstance().log(Level.FINE, "{0}", this.getClass(), log);
            }
            
            if(!this.isResolvable(input)) {
                break;
            }
        }
        
        return input;
    }

    public Set<String> getVariableNames() {
        return variables == null ? null : new HashSet(variables.keySet());
    }

    public String getVariable(String name) {
        return variables.get(name);
    }
    
    public void addVariable(String name, Object value) {
        if(name == null) throw new NullPointerException();
        if(variables == null) {
            variables = new HashMap<String, String>();
        }
        variables.put(name, value.toString());
    }
    
    public String removeVariable(String name) {
        return variables.remove(name);
    }
    
    public Resolver getResolver(int index) {
        return resolvers.get(index);
    }

    public void addResolver(Resolver resolver) {
        if(resolver == null) throw new NullPointerException();
        if(resolvers == null) {
            resolvers = new ArrayList<Resolver>();
        }
        resolvers.add(resolver);
    }

    public boolean removeResolver(Resolver resolver) {
        if(resolver == null) throw new NullPointerException();
        return resolvers.remove(resolver);
    }

    public Resolver removeResolver(int index) {
        return resolvers.remove(index);
    }
}

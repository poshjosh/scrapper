package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;












public class MultiResolver
  implements Resolver
{
  private List<Resolver> resolvers;
  private Map<String, String> variables;
  
  public MultiResolver() {}
  
  public MultiResolver(Resolver resolver, String variableName, String variableValue)
  {
    addResolver(resolver);
    addVariable(variableName, variableValue);
  }
  
  public MultiResolver(List<Resolver> resolvers, Map<String, String> variables) {
    this.resolvers = resolvers;
    this.variables = variables;
  }
  
  public void reset() {
    if (this.variables != null) {
      this.variables.clear();
    }
  }
  
  public boolean isResolvable(String input)
  {
    if (this.resolvers == null) throw new NullPointerException();
    for (Resolver resolver : this.resolvers) {
      if (resolver.isResolvable(input)) {
        return true;
      }
    }
    return false;
  }
  
  public String resolve(String input) throws ResolverException
  {
    XLogger.getInstance().log(Level.FINER, "Input: {0}", getClass(), input);
    
    if ((this.variables != null) && (!this.variables.isEmpty())) {
      for (String key : this.variables.keySet()) {
        String val = (String)this.variables.get(key);
        input = input.replace(key, val);
      }
    }
    XLogger.getInstance().log(Level.FINER, "After updating variables, input: {0}", getClass(), input);
    
    int maxCycles = 10;
    
    for (int i = 0; i < maxCycles; i++)
    {
      for (Resolver resolver : this.resolvers)
      {
        StringBuilder log = new StringBuilder("Resolved: ");
        log.append(input).append(", to: ");
        
        input = resolver.resolve(input);
        
        log.append(input).append(", using: ").append(resolver.getClass().getName());
        XLogger.getInstance().log(Level.FINE, "{0}", getClass(), log);
      }
      
      if (!isResolvable(input)) {
        break;
      }
    }
    
    return input;
  }
  
  public Set<String> getVariableNames() {
    return this.variables == null ? null : new HashSet(this.variables.keySet());
  }
  
  public String getVariable(String name) {
    return (String)this.variables.get(name);
  }
  
  public void addVariable(String name, Object value) {
    if (name == null) throw new NullPointerException();
    if (this.variables == null) {
      this.variables = new HashMap();
    }
    this.variables.put(name, value.toString());
  }
  
  public String removeVariable(String name) {
    return (String)this.variables.remove(name);
  }
  
  public Resolver getResolver(int index) {
    return (Resolver)this.resolvers.get(index);
  }
  
  public void addResolver(Resolver resolver) {
    if (resolver == null) throw new NullPointerException();
    if (this.resolvers == null) {
      this.resolvers = new ArrayList();
    }
    this.resolvers.add(resolver);
  }
  
  public boolean removeResolver(Resolver resolver) {
    if (resolver == null) throw new NullPointerException();
    return this.resolvers.remove(resolver);
  }
  
  public Resolver removeResolver(int index) {
    return (Resolver)this.resolvers.remove(index);
  }
}

package com.scrapper.expression;

public abstract interface Resolver
{
  public abstract boolean isResolvable(String paramString);
  
  public abstract String resolve(String paramString)
    throws ResolverException;
}

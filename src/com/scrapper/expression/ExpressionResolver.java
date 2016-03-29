package com.scrapper.expression;

public abstract interface ExpressionResolver<E>
  extends Resolver
{
  public abstract String[] getOperators();
  
  public abstract String resolve(String paramString1, String paramString2)
    throws ResolverException;
  
  public abstract String resolve(String paramString1, String paramString2, String paramString3)
    throws ResolverException;
  
  public abstract Object resolve(E paramE1, String paramString, E paramE2)
    throws ResolverException;
}

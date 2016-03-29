package com.scrapper.util;


















public class NumberBase
{
  public static StringBuilder convert(int m, int n)
  {
    return convert(m, n, new StringBuilder());
  }
  





  public static StringBuilder convert(int m, int n, StringBuilder appendTo)
  {
    if (n < 2) {
      throw new UnsupportedOperationException("Base must be > 1");
    }
    
    if (n > 10) {
      throw new UnsupportedOperationException("Base must be <= 10");
    }
    
    if (m < n)
    {
      appendTo.setLength(0);
      
      return appendTo.append(m);
    }
    

    return convert(m / n, n, appendTo).append(m % n);
  }
}

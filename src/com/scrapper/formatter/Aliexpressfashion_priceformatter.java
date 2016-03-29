package com.scrapper.formatter;

import com.scrapper.Formatter;
import java.io.Serializable;
import java.util.Map;















public class Aliexpressfashion_priceformatter
  implements Formatter<Map>, Serializable
{
  private int maxPrice;
  private int margin;
  
  public Aliexpressfashion_priceformatter()
  {
    this.maxPrice = 16;
    this.margin = 10;
  }
  

  public Map format(Map parameters)
  {
    Object oval = parameters.get("price");
    
    if (oval == null) { return parameters;
    }
    String sval = oval.toString().trim();
    
    float f = Float.parseFloat(sval);
    
    if (f > this.maxPrice)
    {


      parameters.clear();

    }
    else
    {

      parameters.put("price", Float.toString(f + this.margin));
    }
    

    return parameters;
  }
  
  public int getMargin() {
    return this.margin;
  }
  
  public void setMargin(int margin) {
    this.margin = margin;
  }
  
  public int getMaxPrice() {
    return this.maxPrice;
  }
  
  public void setMaxPrice(int maxPrice) {
    this.maxPrice = maxPrice;
  }
}

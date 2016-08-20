package com.scrapper;

import java.io.Serializable;

public abstract interface Filter<E> extends Serializable{
    
  boolean accept(E paramE);
}

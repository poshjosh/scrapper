package com.scrapper;

import java.io.Serializable;

public abstract interface Filter<E>
  extends Serializable
{
  public abstract boolean accept(E paramE);
}

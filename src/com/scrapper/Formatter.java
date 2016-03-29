package com.scrapper;

import java.io.Serializable;

public abstract interface Formatter<E>
  extends Serializable
{
  public abstract E format(E paramE);
}

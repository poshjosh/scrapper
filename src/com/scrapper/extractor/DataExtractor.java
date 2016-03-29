package com.scrapper.extractor;

import java.util.Map;

public abstract interface DataExtractor<E>
{
  public abstract Map extractData(E paramE)
    throws Exception;
}

package com.scrapper;

import com.scrapper.util.PageNodes;
import java.util.Map;

public abstract interface PageDataConsumer
{
  public abstract boolean consume(PageNodes paramPageNodes, Map paramMap);
}

package com.scrapper;

import java.util.Map;
import com.bc.webdatex.nodedata.Dom;

public interface PageDataConsumer {
    
  boolean consume(Dom pageDom, Map paramMap);
}

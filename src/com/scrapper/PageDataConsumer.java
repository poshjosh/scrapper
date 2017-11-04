package com.scrapper;

import java.util.Map;
import com.bc.dom.HtmlDocument;

public interface PageDataConsumer {
    
  boolean consume(HtmlDocument pageDom, Map paramMap);
}

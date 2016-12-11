package com.scrapper;

import java.util.Map;
import com.bc.dom.HtmlPageDom;

public interface PageDataConsumer {
    
  boolean consume(HtmlPageDom pageDom, Map paramMap);
}

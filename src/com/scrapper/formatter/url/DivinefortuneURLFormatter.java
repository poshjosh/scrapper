package com.scrapper.formatter.url;

/**
 * @(#)DivinefortuneURLFormatter.java   08-Apr-2014 21:11:39
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Input:<br/>
 * http://www.divinefortune.com/index.php?main_page=product_info&amp;cPath=54_55&amp;products_id=394&amp;zenid=f0f003ff3015ff252da303858a0bba5e (http://www.divinefortune.com/index.php?main_page=product_info&amp;cPath=54_55&amp;products_id=394&amp;zenid=f0f003ff3015ff252da303858a0bba5e)
 * <br/><br/>
 * Output:<br/>
 * http://www.divinefortune.com/index.php?main_page=product_info&cPath=54_55&products_id=394
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class DivinefortuneURLFormatter extends BracketRemovingURLFormatter {

    /**
     * Input:<br/>
     * http://www.divinefortune.com/index.php?main_page=product_info&amp;cPath=54_55&amp;products_id=394&amp;zenid=f0f003ff3015ff252da303858a0bba5e (http://www.divinefortune.com/index.php?main_page=product_info&amp;cPath=54_55&amp;products_id=394&amp;zenid=f0f003ff3015ff252da303858a0bba5e)
     * <br/><br/>
     * Output:<br/>
     * http://www.divinefortune.com/index.php?main_page=product_info&cPath=54_55&products_id=394
     */
    @Override
    public String format(String e) {
        
        int off = e.indexOf("&amp;zendid");
        
        if(off == -1) {
            
            off = e.indexOf("&zenid");
        }
        
        if(off != -1) {
            
            e = e.substring(0, off);
        }
        
        e = super.format(e);
        
        return e.trim();
    }
}

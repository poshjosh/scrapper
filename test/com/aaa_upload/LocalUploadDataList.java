package com.aaa_upload;

import com.bc.io.CharFileInput;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * @(#)LocalUploadDataList.java   09-Dec-2014 21:59:03
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class LocalUploadDataList extends AbstractList<Map> {
    
    private boolean strict = false;

    private File imagesFolder;
    
    private String [] imageNames;
    
    private String dataFile;
    
    private String [] dataLines;
    
    public void init(String imagesFolder, String dataFile) throws IOException { 
    
        this.imagesFolder = new File(imagesFolder);
        
        this.imageNames = this.imagesFolder.list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
//                return name.endsWith("_RESCALED.JPG");
                return true;
            }
        });

        this.dataFile = dataFile;
        
        CharSequence cs = new CharFileInput().readChars(dataFile);
        
        String [] dls = cs.toString().split("\n");
        
        ArrayList<String> lines = new ArrayList<String>();
        int i = 0;
        for(String dl:dls) {
System.out.println((++i)+": "+dl);            
            if(dl == null || dl.trim().isEmpty()) {
                continue;
            }
            lines.add(dl.trim());
        }
        
        if(strict && this.imageNames.length != lines.size()) {
            throw new RuntimeException("Number of images must be equal to number of inputs. Found "+imageNames.length+" images, but "+lines.size()+" inputs");
        }
        
        this.dataLines = lines.toArray(new String[lines.size()]);
    }
    
    @Override
    public int size() {
        return this.imageNames.length;
    }
    
    @Override
    public Map get(int i) {
        
        HashMap map = new HashMap();

        final String dataLine = dataLines[i];

System.out.println(" Raw data: "+dataLine);            

        File imageFile = new File(imagesFolder, imageNames[i]);            

        String [] parts = dataLine.split(","); 

        map.put("keywords", parts[0].trim());
        map.put("type", this.getType(parts[1].trim()));
        int [] priceAndDiscount = this.getPriceAndDiscount(parts[2].trim());
        map.put("price", priceAndDiscount[0]);
        map.put("discount", priceAndDiscount[1]);
        
        StringBuilder desc = new StringBuilder();
        for(int sub=3; sub<parts.length; sub++) {
            if(parts[sub] == null) {
                continue;
            }
            desc.append(parts[sub].trim());
            if(sub < parts.length-1) {
                desc.append(',').append(' ');
            }
        }
        map.put("description", desc.toString());
        
        map.put("image1", imageFile.getPath());
        
        // @related temp data
        //
        map.put("temp", dataLine);
        
System.out.println("Extracted: "+map);  

        return map;
    }

    private int getType(String sval) {
        sval = sval.toLowerCase();
        int type;
        if(sval.contains("baby")) {
            if(sval.contains("cloth")) {
                type = 10;
            }else if(sval.contains("shoe")) {
                type = 11;
            }else if(sval.contains("ceso")) {
                type = 12;
            }else{
                throw new IllegalArgumentException("Cannot determine type: "+sval);
            }
        }else if (sval.contains("kid")) {
            if(sval.contains("cloth")) {
                type = 7;
            }else if(sval.contains("shoe")) {
                type = 8;
            }else if(sval.contains("ceso")) {
                type = 9;
            }else{
                throw new IllegalArgumentException("Cannot determine type: "+sval);
            }
        }else{
            throw new IllegalArgumentException("Cannot determine type: "+sval);
        }
        return type;
    }

// Euro    Markup    
// 1     = x 3      210 * 3    = 630   1000   301    699
// 1.5   = x 2.5    315 * 2.5  = 790   1200   401    799       
// 2     = x 2      420 * 2    = 840   1400   501    899 
// 2.5   = x 2      525 * 2    =1050   1800   701   1099 
// 3.0   = x 2      630 * 2    =1260   2200   901   1299 
// 3.5   = x 2      735 * 2    =1470   2600  1101   1499 
// 4.0   = x 2      840 * 2    =1680   3000  1301   1699 
// 4.5   = x 2      945 * 2    =1890   3400  1501   1899 
// 5.0   = x 2     1050 * 2    =2100   3800  1701   2099    
// 7.0   = x 2     1470 * 2    =2940   4000  1001   2999      
//10.0   = x 2     2100 * 2    =4200   5000  1001   3999      
//12.0   = x 2     2640 * 2            5000  1501   3499
    private int [] getPriceAndDiscount(String sval) {
        float euroPrice = Float.parseFloat(sval);
        if(euroPrice == 1.0f) {
            return new int[]{1000, 301};
        }else if(euroPrice == 1.5f) {
            return new int[]{1200, 401};
        }else if(euroPrice == 2.0f) {
            return new int[]{1400, 501};
        }else if(euroPrice == 2.5f) {
            return new int[]{1800, 701};
        }else if(euroPrice == 3.0f) {
            return new int[]{2200, 901};
        }else if(euroPrice == 3.5f) {
            return new int[]{2600, 1101};
        }else if(euroPrice == 4.0f) {
            return new int[]{3000, 1301};
        }else if(euroPrice == 4.5f) {
            return new int[]{3400, 1501};
        }else if(euroPrice == 5.0f) {
            return new int[]{3800, 1701};
        }else if(euroPrice == 7.0f) {
            return new int[]{4000, 1001};
        }else if(euroPrice == 10.0f) {
            return new int[]{5000, 1001};
        }else if(euroPrice == 12.0f) {
            return new int[]{5000, 1501};
        }else{
            throw new RuntimeException("Unexpected price: "+sval+", computations for markup etc, have not been made for this price");
        }
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}

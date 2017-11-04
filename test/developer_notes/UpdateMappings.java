package developer_notes;

import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import com.scrapper.config.ScrapperConfigFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * @(#)UpdateMappings.java   04-Feb-2014 00:29:24
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class UpdateMappings {
    
    private static String path = System.getProperty("user.home")+"\\Desktop\\DELETE_ME_MappingsUpdate_DoneSitenamesSet.JAVAOBJECT";
    
    private static Set<String> done;
    
    private UpdateMappings() {
        Object oval = this.loadObject(path);
        if(oval == null) {
            done = new HashSet<String>();
        }else{
            done = (Set<String>)oval;
        }
    }
    
    public static void main(String [] args) {
        UpdateMappings x = null;
        try{
            CapturerApp.getInstance().init(true);
            x = new UpdateMappings();
            x.update();
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            if(x != null) {
                x.saveObject(path, done);
            }
        }
    }
    
    private void update() throws IOException {
        
        Object [] tableNames = {"autos", "gadgets", "fashion", "classifieds"};
        Object [] addonOptions = {"exit this", "exit after this", "exit now"};
        Object [] options = new Object[(tableNames.length * 2) + addonOptions.length];
        System.arraycopy(tableNames, 0, options, 0, tableNames.length);
        for(int i=tableNames.length, j=0; i<options.length-addonOptions.length; i++, j++) {
            options[i] = tableNames[j] + " for all"; 
        }
        
        for(int i=tableNames.length*2, j=0; i<options.length; i++, j++) {
            options[i] = addonOptions[j];
        }
        
        ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
        
        Object [] tableMappingsPath = {"url", "mappings", "table"};
        
        Set<String> names = factory.getConfigNames();
        
        for(String name:names) {
            
            if(done.contains(name)) {
                continue;
            }
            
System.out.println("Config: "+name);

            boolean exit = false;
            boolean exitConfig = false;

            JsonConfig config = factory.getConfig(name);
            
            Map<Object, Object> typeMappings = config.getMap("url", "mappings", "type");
            if(typeMappings == null || typeMappings.isEmpty()) {
                done.add(name);
                continue;
            }

            String defaultTableName = null;
            
            Map tableMappings = config.getMap(tableMappingsPath);
            
            HashMap update = new HashMap();
            
            for(Entry<Object, Object> e:typeMappings.entrySet()) {
                
                if(tableMappings != null && tableMappings.containsKey(e.getKey())) {
                    continue;
                }

                Object sel;
                if(defaultTableName == null) {
                    String msg = "For config: "+name+", which table fits best for: "+e.getKey();
                    sel = JOptionPane.showInputDialog(null, msg, "Select Table", 
                            JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    if(sel != null) {
                        String str = sel.toString();
                        int i;
                        if((i = str.indexOf("for all")) != -1) {
                            sel = defaultTableName = str.substring(0, i).trim();
                        }else if(str.equals("exit now")) {
                            exitConfig = true;
                            exit = true;
                        }else if(str.equals("exit after this")) {
                            exitConfig = false;
                            exit = true;
                        }else if(str.equals("exit this")) {
                            exitConfig = true;
                            exit = false;
                        }
                    }
                }else{
System.out.println("= = = Using default table name: "+defaultTableName);                    
                    sel = defaultTableName;
                }
                if(sel != null) {
                    update.put(e.getKey(), sel);
                }else{
                    exitConfig = false;
                    exit = true;
                }
                
                if(exitConfig) {
                    break;
                }
            }
            
            if(update.isEmpty()) {
                done.add(name);
                continue;
            }
            if(tableMappings == null) {
                config.setObject(tableMappingsPath, update);
            }else{
                tableMappings.putAll(update);
            }
            
            factory.saveValues(config);            
            
            done.add(name);
System.out.println("Successfully saved values for: "+config.getName());            

            if(exit) {
                break;
            }
        }
    }

    public boolean saveObject(String path, Object object) {

        boolean result = false;

        try {
//Logger.getLogger(this.getClass().getName()).info("Saving object "+object.getClass().getName()+" to: "+path);
            writeObject(path, object);

            result = true;
            
        }catch(IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e);
        }

        return result;
    }

    public Object loadObject(String path) {

        Object result = null;

        try {
//Logger.getLogger(this.getClass().getName()).info("Loading object from: " + path);
            result = readObject(path);

        }catch(FileNotFoundException e) {
            // Lighter logging, without stack trace
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
            "{0}. {1}", new Object[]{this.getClass().getName(), e});
        }catch(IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e);
        }catch(ClassNotFoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e);
        }

        return result;
    }
    
    public Object readObject(String source) throws ClassNotFoundException, IOException {
        
        Object result = null;
        
        FileInputStream     fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream   ois = null;
        
        try {

            fis = new FileInputStream(source);
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);

            result = ois.readObject();
        
        }catch(IOException e) {
            
            throw e;
        
        }finally {
        
            if (ois != null) try { ois.close(); }catch(IOException e) {}
            if (bis != null) try { bis.close(); }catch(IOException e) {}
            if (fis != null) try { fis.close(); }catch(IOException e) {}
        }
        
        return result;
    }

    public void writeObject(String destination, Object obj) throws FileNotFoundException, IOException {
        
        FileOutputStream     fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream   oos = null;
        
        try{
            
            fos = new FileOutputStream(destination);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);

            oos.writeObject(obj);
        
        }catch(IOException e) {
            
            throw e;
        
        }finally {
        
            if (oos != null) try { oos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
            if (bos != null) try { bos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
            if (fos != null) try { fos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
        }
    }
}

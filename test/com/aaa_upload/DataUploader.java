package com.aaa_upload;

import com.bc.manager.Formatter;
import com.bc.util.XLogger;
import com.scrapper.BasePageDataConsumer;
import com.scrapper.formatter.DefaultFormatter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 * @(#)UploadData.java   08-Dec-2014 18:51:53
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
public class DataUploader {
    
    private final BasePageDataConsumer consumer;
    
    public DataUploader() throws MalformedURLException { 
        
        URL insertURL = new URL("http://www.looseboxes.com/gx?rh=tempadmin");
//        insertURL = new URL("http://localhost:8080/gx?rh=tempadmin");
        
        Map uploadParams = this.getUploadParameters();
        
        Formatter<Map<String, Object>> formatter = this.getFormatter();
        
        consumer = new BasePageDataConsumer(
                insertURL, uploadParams);
        
        consumer.setDefaultTableName("fashion");
        consumer.setMinimumParameters(3);
        consumer.setFormatter(formatter);
    }
    
    public static void main(String [] args) {
        
        try{
            
            DataUploader du = new DataUploader();

            LocalUploadDataList uploadData = new LocalUploadDataList();
            uploadData.init(
                    "E:\\Documents\\Personal\\Coys\\Web Sites\\looseBoxes.com\\directuploads\\images_2",
                    "E:\\Documents\\Personal\\Coys\\Web Sites\\looseBoxes.com\\directuploads\\data_2.txt");

            outer:
            for(Map upload:uploadData) {
                
                final int sel = du.promptUser(upload);

                switch(sel) {
                    case JOptionPane.YES_OPTION: 
                        break;
                    case JOptionPane.NO_OPTION:
                        continue;
                    case JOptionPane.CANCEL_OPTION:
                    default:
                        break outer;
                }
                
System.out.println("Uploading: "+upload);                

                boolean success = du.upload(upload);
                
System.out.println("  Success: "+success);  
            }

        }catch(Throwable t) {
            
            t.printStackTrace();
        }
    }
    
    private int promptUser(Map upload) throws IOException {
        
        // @related temp data
        //
        final String dataLine = (String)upload.remove("temp");
        
        final String imagePath = upload.get("image1").toString();
        
        JTextField textField = new JTextField();
        textField.setText(dataLine);
        JLabel label = new JLabel();
        Image image = ImageIO.read(new File(imagePath));
        ImageIcon icon = new ImageIcon(image);
        label.setIcon(icon);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.NORTH);
        panel.add(label, BorderLayout.CENTER);
        
        JScrollPane scrolls = new JScrollPane(panel);
        
        scrolls.setPreferredSize(new Dimension(400, 500));
        
        final int sel = JOptionPane.showConfirmDialog(null, scrolls,
                "Confirm you want to upload data displayed",
                JOptionPane.YES_NO_CANCEL_OPTION);
        
        return sel;
    }
    
    private boolean upload(Map data) {
        return consumer.consume(null, data);
    }
    
    private Map getUploadParameters() {
        Map params = new HashMap();
        params.put("charset", "UTF-8");
        params.put("checkIfRecordExists", false);
        params.put("corporateCheckbox", true);
        params.put("dbActionType", "INSERT");
        params.put("emailAddress", "coolbuyng@gmail.com");
        params.put("login", true);
        params.put("password", "-123helen");
        params.put("remoteFiles", false);
        params.put("sendMultipart", true);
        return params;
    }
    
    private Formatter<Map<String, Object>> getFormatter() {
        DefaultFormatter formatter = new DefaultFormatter(){
            @Override
            public Map<String, Object> format(Map<String, Object> parameters) {

XLogger.getInstance().log(Level.FINER, "BEFORE Params: {0}", 
                this.getClass(), parameters);

                final String tableName = "fashion";

                // Order of method call important
                //
                Map<String, Object> copy = parameters;

XLogger.getInstance().log(Level.FINER, "BEFORE: {0}", this.getClass(), copy);

//                copy = this.resolveExpressions(copy);

XLogger.getInstance().log(Level.FINER, "AFTER: {0}", this.getClass(), copy);        

//                copy = this.translate(copy);

                // We have to formatCurrencyTypes before we formatIntegerColumns because
                // currency is formatted here and it is an integer columns
                copy = this.formatCurrencyTypes(copy);

                // We have to format type and other integer columns first
                // to avoid some problems 
                //
                copy = this.formatIntegerColumns(tableName, copy);

//                copy = this.formatDateTypes(copy);

//                try{
//                    final int status = this.formatDateinAndExpiryDate(copy);
//                    copy.put("status", status);
//                }catch(RuntimeException e) {
//                    XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
//                }

//                copy = this.formatLinks(copy);

                copy = this.addDefaults(copy);

//                if("jobs".equals(tableName)) {
//                    copy = this.updateHowToApply(copy);
//                }
XLogger.getInstance().log(Level.FINER, "AFTER Params: {0}", 
                this.getClass(), copy);
                return copy;
            }
        };
        
        formatter.setDefaultValues(this.getDefaultValues());
        
        return formatter;
    }
    
    private Map getDefaultValues() {
        HashMap defaultValues = new HashMap();
        defaultValues.put("category", 1);
        defaultValues.put("country", 124);
        defaultValues.put("emailAddress", "coolbuyng@gmail.com");
        defaultValues.put("itemtype", 1);
        defaultValues.put("offerType", 1);
        defaultValues.put("offerscheme", 1);
        defaultValues.put("currency", 4); // 4 = NGN
        return defaultValues;
    }
}
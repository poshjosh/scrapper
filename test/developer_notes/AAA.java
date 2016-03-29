package developer_notes;

import com.bc.util.XLogger;
import com.ftpmanager.FTPFileTypes;
import com.scrapper.util.MyFTPClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * @(#)AAA.java   11-Jan-2014 02:14:49
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
public class AAA {
    
    public void load(String path) throws IOException {
        
        MyFTPClient ftp = new MyFTPClient();
        
        boolean loggedIn = false;
        InputStream in = null;
        InputStreamReader reader = null;
        try{
            
            in = ftp.retrieveFileStream(path, FTPFileTypes.ASCII_FILE_TYPE, true);
            
            reader = new InputStreamReader(in);
            
            loggedIn = true;
            
//            this.load(reader);
            
        }finally{
            
            // Order of method call important
            if(in != null) {
                try{ in.close(); }catch(IOException e) {
                    XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
                }
            }
            if(reader != null) {
                try{ reader.close(); }catch(IOException e) {
                    XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
                }
            }
            
            ftp.completePendingCommand();
            
            ftp.logoutAndDisconnect(loggedIn);
        }
    }
    
    public void save(String path) throws IOException {

        this.save(path, 5);
    }
    
    private void save(String path, int maxTrials) throws IOException {
        
        MyFTPClient ftp = new MyFTPClient();
        
        final String partial = ftp.getFtpDir() + "/" + Long.toHexString(System.currentTimeMillis()) + "_DELETE_LATER.partial";
    
        String forUpload = null;//this.print();

        int totalUpload = 0;
        
        int trials = 0;

        do {
            
            boolean append = totalUpload > 0;
            
            totalUpload = this.save(forUpload.substring(totalUpload), append, partial);
            
            if(++trials == maxTrials) {
                break;
            }
        }while(totalUpload > 0 && totalUpload < forUpload.length());
        
        if(totalUpload == -1) {
            
            throw new IOException("Unable to determine final upload status");
            
        }else if (totalUpload < forUpload.length()) {
            
            ftp.deleteFile(partial, true);
            
            throw new IOException("Incomplete upload");
            
        }else{ // Total upload could actually be a little greater size of initial upload data
            // Success
            
            if(ftp.rename(partial, path, true)) {
                
            }else{
                
                ftp.deleteFile(partial, true);
                
                throw new IOException("Upload failed. Rename of partially uploaded file failed");
            }
        }
    }

    private int save(String uploadText, boolean append, 
            String path) throws IOException {
        
        String downloadText = this.save(uploadText, append, path, true);
        
        return downloadText == null ? -1 : downloadText.length();
    }
    
    private String save(String uploadText, boolean append, 
            String path, boolean downloadAfterUpload) throws IOException {
        
        String downloadText = null;
        
        MyFTPClient ftp = new MyFTPClient();
        
        boolean loggedIn = false;
        OutputStream out = null;
        try{
            
            if(append) {
                out = ftp.appendFileStream(path, FTPFileTypes.ASCII_FILE_TYPE, true);
            }else{
                out = ftp.storeFileStream(path, FTPFileTypes.ASCII_FILE_TYPE, true);
            }

            loggedIn = true;
            
            out.write(uploadText.getBytes());
            
        }finally{
            
            // Order of method call important
            
            if(out != null) {
                try{ out.close(); }catch(IOException e) {
                    XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
                }
            }
            
            ftp.completePendingCommand();
            
            try{
                if(downloadAfterUpload) {
                    downloadText = ftp.download(path, FTPFileTypes.ASCII_FILE_TYPE, false);
XLogger.getInstance().log(Level.FINER, "Upload: {0}, Download: {1}",
this.getClass(), uploadText.length(), downloadText.length());                    
                }
            }catch(IOException e) {
                // Ignore
            }finally{
                ftp.logoutAndDisconnect(loggedIn);
            }
        }

        return downloadText;
    }
}

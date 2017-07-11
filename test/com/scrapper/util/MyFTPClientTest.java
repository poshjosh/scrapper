/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrapper.util;

import com.ftpmanager.FTPFileTypes;
import java.util.Set;
import com.scrapper.CapturerApp;
import java.util.Arrays;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class MyFTPClientTest {
    
    public MyFTPClientTest() {
        
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        CapturerApp.getInstance().init(false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testAll() throws Exception {
System.out.println("General tests");

        MyFTPClient ftp = new MyFTPClient();
        
        boolean loggedIn = false;
        
        try{
            
            String [] fnames = ftp.listNames(ftp.getFtpDir(), -1, true);
System.out.println("BEFORE Creating Dirs. FTP server dir names: "+fnames==null?null:Arrays.toString(fnames));

            final String folder = "kerekuma/aboki_you/xyz";

            boolean exists = ftp.fileExists(folder, true);
            assertFalse(exists);

            boolean created = ftp.mkdirs(folder, true);
            assertTrue(created);

//            fnames = ftp.listNames();
//System.out.println("AFTER Creating Dirs. FTP server dir names: "+fnames==null?null:Arrays.toString(fnames));

            ftp.logoutAndDisconnect();

            String file = folder + "/xyz.properties";
            Properties props = new Properties();
            props.setProperty("nameOne", "value1");
            props.setProperty("nameTwo", "value2");
            
            // Create an empty file first
            ftp.upload("", file, FTPFileTypes.ASCII_FILE_TYPE, true);
            created = ftp.uploadProperties(props, file, true);
            assertTrue(created);

            exists = ftp.fileExists(file, true); 
            assertTrue(exists);

            props.setProperty("name3", "valueThree");
            props.setProperty("name4", "valueFour");
            file = folder + "/xyz1.properties";
            // Create an empty file first
            ftp.upload("", file, FTPFileTypes.ASCII_FILE_TYPE, true);
            created = ftp.uploadProperties(props, file, true);
            assertTrue(created);

            exists = ftp.fileExists(file, true); 
            assertTrue(exists);

            Properties p = new Properties();
            ftp.downloadProperties(p, file, true);
System.out.println("Downloaded: "+p);        

            fnames = ftp.listNames(ftp.getFtpDir(), -1, true);
System.out.println("FTP server dir names: "+fnames==null?null:Arrays.toString(fnames));
        
            boolean deleted = ftp.deleteFile(file, true);
System.out.println("A deleted: "+deleted);        
            deleted = ftp.deleteFile(folder+"/xyz.properties", true);
System.out.println("B deleted: "+deleted);

            Set<String> failed = ftp.removeNonemptyDirectory(folder, true);
if(failed == null || failed.isEmpty()) {
System.out.println("Deleted: "+folder);        
}else{
    fail("Failed to delete: "+failed);
}            
            
        }finally{
            ftp.logoutAndDisconnect(loggedIn);
        }
    }

    /**
     * Test of mkdir method, of class MyFTPClient.
     */
    @Test
    public void testMkdirs() throws Exception {
//System.out.println("mkdir");
    }

    /**
     * Test of fileExists method, of class MyFTPClient.
     */
    @Test
    public void testExists() throws Exception {
//System.out.println("fileExists");
    }

    /**
     * Test of getFileNames method, of class MyFTPClient.
     */
    @Test
    public void testGetFileNames() throws Exception {
//System.out.println("getFileNames");
    }

    /**
     * Test of downloadProperties method, of class MyFTPClient.
     */
    @Test
    public void testDownloadProperties() throws Exception {
//System.out.println("downloadProperties");
    }

    /**
     * Test of uploadProperties method, of class MyFTPClient.
     */
    @Test
    public void testUploadProperties() throws Exception {
//System.out.println("uploadProperties");
    }

    /**
     * Test of uploadFile method, of class MyFTPClient.
     */
    @Test
    public void testUploadFile_3args_1() throws Exception {
//System.out.println("uploadFile");
    }

    /**
     * Test of uploadFile method, of class MyFTPClient.
     */
    @Test
    public void testUploadFile_3args_2() throws Exception {
//System.out.println("uploadFile");
    }

    /**
     * Test of connectAndLogin method, of class MyFTPClient.
     */
    @Test
    public void testConnectAndLogin() throws Exception {
//System.out.println("connectAndLogin");
    }

    /**
     * Test of logoutAndDisconnect method, of class MyFTPClient.
     */
    @Test
    public void testLogoutAndDisconnect_0args() {
//System.out.println("logoutAndDisconnect");
    }

    /**
     * Test of logoutAndDisconnect method, of class MyFTPClient.
     */
    @Test
    public void testLogoutAndDisconnect_boolean() {
//System.out.println("logoutAndDisconnect");
    }
}

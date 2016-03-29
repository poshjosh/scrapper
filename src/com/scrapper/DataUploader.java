package com.scrapper;

import com.bc.io.CharFileIO;
import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import com.scrapper.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author Josh
 */
public abstract class DataUploader implements Serializable {

    private String message;

    private URL url;
    
    private CharFileIO ioMgr;

    private transient ConnectionManager connMgr;

    public DataUploader() {
        init(null);
    }
    public DataUploader(URL aURL) {
        init(aURL);
    }
    private void init(URL aURL) {
        
        this.setUrl(aURL);
XLogger.getInstance().log(Level.FINE, "URL: {0}", this.getClass(), aURL);

        ioMgr = new CharFileIO();
        
        DataUploader.this.initConnectionManager();
    }
    
    public abstract Map getUploadParameters();
    
    protected void initConnectionManager() {
        connMgr = new ConnectionManager(5, 3000);
        connMgr.setAddCookies(true);
    }
    
    private void writeObject(ObjectOutputStream o)
        throws IOException {  
        o.defaultWriteObject();  
    }
  
    private void readObject(ObjectInputStream o)
        throws IOException, ClassNotFoundException {
        o.defaultReadObject();
        this.initConnectionManager();
    }
    
    public void stop(long timeout) {
        this.connMgr.stop(timeout);
XLogger.getInstance().log(Level.FINE, "Stopped", this.getClass());
    }

    public boolean isRunning() {
        return connMgr.isRunning();
    }

    public boolean isStopped() {
        return connMgr.isStopped();
    }

    public void setStopped(boolean stopped) {
        connMgr.setStopped(stopped);
    }

    public void reset(URL aURL) {
        this.url = aURL;
        reset();
    }

    public void reset() {
        this.message = null;
        if(connMgr != null) {
            connMgr.reset();
        }
    }
    
    /**
     * @param parameters
     * @return The HTTP Response Code
     */
    public int uploadRecord(Map parameters) {  
        
        int responseCode = -1;
        
        try{
            
            responseCode = this.doUploadRecord(parameters);
            
        }finally{
            
            if(responseCode == -1) {

                String msg = this.getMessage();

                String extraDetails = (String)parameters.get("extraDetails");
                
                if(extraDetails != null) {
                    String [] parts = extraDetails.split("=");
                    if(parts.length != 2) {
                        XLogger.getInstance().log(Level.WARNING, "Expected format: url=[actualUrlLink]. Found: {0}", this.getClass(), extraDetails);
                    }else{
                        XLogger.getInstance().log(Level.WARNING, "Failed to save: {0}", this.getClass(), parts[1]);
                    }
                }
                // A crude way of knowing if it is an IOException that prevented the
                // paramters from being saved.
                //
                if(msg != null && (msg.contains("java.net.") || msg.contains("java.io."))) {
                    XLogger.getInstance().log(Level.WARNING, "{0}. {1}", this.getClass(), msg);
                }
            }
        }

        return responseCode;
    }
    
    /**
     * @return The Http Response code
     */
    private int doUploadRecord(Map parameters) {
        
        this.reset();
        
        this.log(parameters);
        
        if(parameters == null || parameters.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int responseCode = -1;
        
        URLConnection connection = null;
        OutputStream outputStream = null;

        parameters.putAll(this.getUploadParameters());
        
        try{
            
            HashMap<String, String> files = new HashMap<String, String>();
            HashMap<String, String> nonFiles = new HashMap<String, String>();
            
            for(Object entryObj:parameters.entrySet()) {
                
                Map.Entry entry = (Map.Entry)entryObj;
                
                final String key = entry.getKey().toString();
                
                if(entry.getValue() == null) {
                    throw new NullPointerException("Value is null, for key: "+key);
                }
                
                final String val = entry.getValue().toString();

                if(key.startsWith("image")) {
                    
                    files.put(key, val);    
                    
                }else{
                    
                    nonFiles.put(key, val);
                }
            }

XLogger.getInstance().log(Level.FINE, "After UPDATE Parameter keys: {0}", 
        this.getClass(), parameters.keySet());

XLogger.getInstance().log(Level.FINER, "After UPDATE Parameters: {0}", 
        this.getClass(), parameters);

            boolean multipart = this.isSendMultipart() && !files.isEmpty();
            
XLogger.getInstance().log(Level.FINE, "Send multipart: {0}", this.getClass(), multipart);                                

            try{
                
                final String charset = this.getCharset();
                
                if(multipart) {

                    // Just generate some unique random value.
                    final String BOUNDARY = Long.toHexString(System.currentTimeMillis());

                    connection = connMgr.openConnection(this.url, true, true, charset, "Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

                    outputStream = connMgr.getOutputStream(connection);

                    this.postMultipart(outputStream, nonFiles, files, this.isRemoteFiles(), BOUNDARY);

XLogger.getInstance().log(Level.FINE, "Done posting multipart.", this.getClass());                                

                }else{
                    
                    if(!files.isEmpty()) {
                        parameters.put("images", true);
                    }
XLogger.getInstance().log(Level.FINE, "Charset: {0}, URL: {1}", this.getClass(), charset, this.url);                                

                    connection = connMgr.openConnection(this.url, true, true, charset, "Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

                    outputStream = connMgr.getOutputStream(connection);

                    String queryString = this.getQueryString(parameters, charset);
                    
                    outputStream.write(queryString.getBytes(charset));

XLogger.getInstance().log(Level.FINE, "Done posting query.", this.getClass());                
                }
            }finally{
                
                if (outputStream != null) try { outputStream.close(); } catch (IOException e) {
                    XLogger.getInstance().log(Level.WARNING, "", this.getClass(), e);
                }
            }
            
            responseCode = connMgr.getResponseCode();
XLogger.getInstance().log(Level.INFO, "Server returned response code: {0}", 
        this.getClass(), responseCode);            

            if(responseCode >= 400) {
                
// Not necessary as we can work with the response code above
//            
                this.readResponse();
            }
  
        }catch(IOException e) {
            
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            
        }finally{
            
            connMgr.disconnect();
        }    
        
        return responseCode;
    }
    
    public void postMultipart(OutputStream output, Map<String, String> nonFiles, 
            Map<String, String> files, boolean remoteFiles, final String boundary) throws IOException {

        String charset = this.getCharset();
        
        PrintWriter writer = null;

        try {

            // true = autoFlush, important!
            writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

            if(nonFiles != null) {
                this.postNonFiles(writer, nonFiles, boundary);
            }

            this.postFiles(writer, output, files, remoteFiles, boundary);

        } finally {
            if (writer != null) writer.close();
        }
    }

    protected void postFiles(PrintWriter writer, OutputStream output,
            Map<String, String> files, boolean remote, String boundary) throws IOException {
        for(String name : files.keySet()) {
            this.postFile(writer, output, name, files.get(name), remote, boundary);
        }
    }
    protected void postNonFiles(PrintWriter writer, Map nonFiles, String boundary) {
        for(Object name : nonFiles.keySet()) {
            this.postNonFile(writer, name.toString(), nonFiles.get(name), boundary);
        }
    }
    protected void postNonFile(PrintWriter writer, String name, Object value, String boundary) {
XLogger.getInstance().log(Level.FINER, "{0}. Posting non file: {1}={2}", this.getClass(), name, value);
        final String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        String charset = this.getCharset();
        
        // Send normal param.
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\""+name+"\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
        writer.append(CRLF);
        writer.append(value.toString()).append(CRLF).flush();
    }
    protected void postFile(PrintWriter writer, OutputStream output,
    String name, String filePath, boolean remote, String boundary) throws IOException {
        
XLogger.getInstance().log(Level.FINER, "{0}. Posting file: {1}={2}", this.getClass(), name, filePath);

        File binaryFile = new File(filePath);

        final String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        try {

            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\""+name+"\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            
            InputStream input = null;
            try {
                if(remote) {

                    URL remoteUrl = new URL(filePath);
//Logger.getLogger(this.getClass().getName()).info("Reomte URL: " + remoteUrl);
                    input = connMgr.getInputStream(remoteUrl);

                }else{
                    
                    input = new FileInputStream(binaryFile);
                }
                
                ioMgr.copyStream(input, output);
                
                // Important! Output cannot be closed. Close of writer will close output as well.
                output.flush();
                
            } finally {
                
                if (input != null) try { input.close(); } catch (IOException e) {
                    System.out.println(this.getClass().getName()+". "+e);
                }
                connMgr.disconnect();
            }
            
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF);
            
        } catch(IOException e) {
            XLogger.getInstance().log(Level.WARNING, "", this.getClass(), e);
        }
    }

    private boolean readResponse() throws IOException {
XLogger.getInstance().entering(this.getClass(), "@readResponse()", null);

        List<String> cookies = connMgr.getCookies();
        
        // We get the cookies only if there are none already
        connMgr.setGetCookies(cookies == null);
        
if(cookies != null && !cookies.isEmpty()) {
    XLogger.getInstance().log(Level.FINE, "Cookies: {0}", this.getClass(), cookies);
}
        InputStream in = null;
        try{
            
            in = connMgr.getInputStream();
        
            CharSequence cs = ioMgr.readChars(in);

            this.message = cs.toString();
        
XLogger.getInstance().log(Level.FINER, "Response: {0}", this.getClass(), cs);

        }finally{
            if(in != null) try{ in.close(); }catch(IOException e) { 
                XLogger.getInstance().log(Level.WARNING, "", this.getClass(), e); 
            }
        }
        
        // Expected format: <SUCCESS>success message</SUCCESS>
        // OR <ERROR>error message</ERROR>

        boolean success = (this.message.contains("<SUCCESS>"));

        if(success) {
            XLogger.getInstance().logger(this.getClass()).info(this.message);
        }else{
            StringBuilder logMsg = new StringBuilder();
            try{
                //<div id="myMessage">
                this.appendTag("id", "myMessage", logMsg);
                if(logMsg.length() == 0) {
                    //<div class="handWriting errorPageData">
                    this.appendTag("class", "handWriting errorPageData", logMsg);
                }
            }catch(ParserException e) {
                XLogger.getInstance().log(Level.WARNING, "", this.getClass(), e);
                if(this.message.contains("<ERROR>")) {
                    logMsg.append("Error: ").append(this.message);
                }else{
                    logMsg.append("Message: ").append(this.message);
                }
            }
            String s = logMsg.toString().trim();
            if(!s.isEmpty()) {
                Logger.getLogger(this.getClass().getName()).warning(logMsg.toString());
            }else{
                Logger.getLogger(this.getClass().getName()).warning(message);
            }    
        }

        return success;
    }
    
    private void appendTag(String attrName, String attrValue, 
            StringBuilder appendTo) throws ParserException {
        Parser parser = new Parser(this.message);
        HasAttributeFilter filter = new HasAttributeFilter(attrName, attrValue);
        NodeList list = parser.parse(filter);
        for(int i=0; i<list.size(); i++) {
            String s = list.elementAt(i).toPlainTextString();
            if(s == null || s.trim().isEmpty()) {
                continue;
            }
            appendTo.append(s);
            appendTo.append("\n");
        }
    }
    
    private String getQueryString(Map parameters, String charset) throws IOException {
        
        StringBuilder output = new StringBuilder();

        HashMap map = new HashMap(parameters);

        ArrayList nulls = new ArrayList();
        nulls.add(null);
        map.values().removeAll(nulls);
        
        Iterator iter = map.keySet().iterator();

        while(iter.hasNext()) {

            Object key = iter.next();
            Object val = map.get(key);

            output.append(key);
            output.append('=');
            output.append(URLEncoder.encode(val.toString(), charset));

            if(iter.hasNext()) {
                output.append('&');
            }
        }
XLogger.getInstance().log(Level.FINER, "Query: {0}", this.getClass(), output);        
        return output.toString();
    }
    
    private void log(Map parameters) {
XLogger xlog = XLogger.getInstance();        
Object toLog;
if(xlog.isLoggable(Level.FINER, this.getClass())) {
    toLog = parameters;
}else if(xlog.isLoggable(Level.FINE, this.getClass())) {
    toLog = Util.toString(parameters, 70);
}else if(xlog.isLoggable(Level.INFO, this.getClass())){
    toLog = parameters.keySet();
}else{
    toLog = null;
}
if(toLog != null) {
// This should carry the highest log level from above
xlog.log(Level.INFO, "Parameters: {0}", 
        this.getClass(), toLog);
}
    }

    public String getMessage() {
        return message;
    }

    public boolean isAppendLogo() {
        Object oval = this.getUploadParameters().get("appendLogo");
        return oval == null ? false : Boolean.parseBoolean(oval.toString());
    }

    public String getCharset() {
        return (String)this.getUploadParameters().get("charset");
    }

    public boolean isCheckIfRecordExists() {
        return (Boolean)this.getUploadParameters().get("checkIfRecordExists");
    }

    public String getDbActionType() {
        return (String)this.getUploadParameters().get("dbActionType");
    }

    public boolean isLogin() {
        return (Boolean)this.getUploadParameters().get("login");
    }

    public boolean isRemoteFiles() {
        return (Boolean)this.getUploadParameters().get("remoteFiles");
    }

    public boolean isSendMultipart() {
        return (Boolean)this.getUploadParameters().get("sendMultipart");
    }
    
    // R/W
    //
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}

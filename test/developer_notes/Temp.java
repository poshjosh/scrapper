package developer_notes;

import com.bc.io.CharFileIO;
import com.bc.sql.ResultSetUtils;
import com.scrapper.CapturerApp;
import com.scrapper.config.Sitenames;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.TaafooContext.TaafooFormatter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Josh
 */
public class Temp {
    
    private transient static final Logger logger = Logger.getLogger(Temp.class.getName());
    
    public Temp() throws IOException, IllegalAccessException, InterruptedException, InvocationTargetException {
//        CapturerApp.getInstance().init(true, false);
    }
    
    public static void main(String [] args) {
        try{
            // End of line $
            // Word-boundary \b
            String regex = "/\\w+?(-|,)\\w+?$";
            Pattern p = Pattern.compile(regex);
            String s = "http://www.looseboxes.com/aboki-bcd";
            Matcher m = p.matcher(s);
System.out.println(m.find());            
            s = "http://www.aboki-bcd.com";
            m = p.matcher(s);
System.out.println(m.find());            
            s = "http://www.looseboxes.com/aboki-bcd/";
            m = p.matcher(s);
System.out.println(m.find());            

if(true) return;
            

            File f = new File(System.getProperty("user.home")+"/testProperties.properties");
            f.createNewFile();
            Properties props = new Properties();
            props.put("url", "http://www.looseboxes.com/ux?rh=dp&mobile=true&amp;y=1");
            props.store(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"), null);
            
            if(true) return;
            String [] tables = {"jobs", "gadgets", "autos", "property"};
            String [] text = {"new", "offer", "for sale", "nigeria"};
            String fmtStr = "http://localhost:8080/search?pt=%1s&type=%2s&searchText=%3s";
            for(int i=0; i<tables.length; i++) {
                for(int j=0; j<5; j++) {
                    for(int k=0; k<text.length; k++) {
                        String urlStr = String.format(fmtStr, tables[i], j, text[k]);
                        urlStr = urlStr.replace(" ", "");
                        URI uri = new URI(urlStr);
System.out.println("URI: "+uri);                    
                        Desktop.getDesktop().browse(uri);
                    }
                }
            }
            if(true) return;
            
            String input = "http://ngcareers.com/apply.php?id=12345";
            Matcher matcher = Pattern.compile("http://ngcareers\\.com/apply\\.php\\?id=").matcher(
                    input);
System.out.println("Found: "+matcher.find());            
        if(true) return;
//            String path = System.getProperty("user.home") + "/Desktop/rows.sql";
//            new Temp().convertMultipleInsertsToUpdates(path);
        CapturerContext context = CapturerApp.getInstance().getConfigFactory().getContext(Sitenames.TAAFOO);
        
        final TaafooFormatter fmt = (TaafooFormatter)context.getFormatter();
        
        }catch(Throwable t) {
            logger.log(Level.WARNING, "", t);
        }
    }
    
    private Connection con;
    public Connection initConnection() throws SQLException, ClassNotFoundException {
        if(con == null || con.isClosed()) {
            con = ResultSetUtils.newConnection("com.mysql.jdbc.Driver",
//                    "jdbc:mysql://localhost:3306/loosebox_db1", "loosebox_root", "7345xT-eeSw");
                    "jdbc:mysql://uranus.ignitionserver.net:3306/loosebox_db1", "loosebox_root", "7345xT-eeSw");
System.out.println("CONNECTED");            
        }
        
        return con;
    }

    /**
     * Reads multiple emails from the supplied path (separated by the 
     * second argument) then uploads them to the table loosebox_db2.unofficial_emails
     * @param path The path to the file to readChars the emails from
     * @param separator The separator char(s) between each email to readChars
     * @throws Exception 
     */
    public void insertMultipleEmails(String path, String separator) throws Exception {

        String contents = new CharFileIO().readChars(path).toString();
        String [] parts = contents.split(separator);
        final Set<String> emails = new HashSet<String>();
        for(String email:parts) {
            email = email.trim();
            if(email.isEmpty()) {
                continue;
            }
            emails.add(email);
        }

        try{

            con = this.initConnection();

            String query = "INSERT INTO `loosebox_db2`.`unofficial_emails` VALUES (?, NULL, NULL)";
            PreparedStatement ps = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            for(String email:emails) {
                try{
                    ps.clearParameters();
                    ps.setString(1, email);
System.out.println(email);
                    if(!ps.execute()) {
System.out.println("EXECUTION FAILED!");
                    }
                }catch(SQLException e) {
System.out.println(e);
                }
            }
        }catch(SQLException e) {
            logger.log(Level.WARNING, "", e);
        }finally{
            if(con != null) {
                con.close();
            }
        }
    }
    
    public void changeCharColumnsCharset() throws SQLException, ClassNotFoundException {
        this.initConnection();
        Set<String> tableNames = new HashSet<String>();
        String [] types = {"TABLE"};
        ResultSet rs = null;
        try{
            rs = con.getMetaData().getTables(null, null, null, types);
            while(rs.next()) {
                String tableName = rs.getString(3); // TABLE_NAME
                tableNames.add(tableName);
            }
            for(String tableName:tableNames) {
                try{
                    if(!tableName.equals("property")) continue;
System.out.println("\n======== ATTEMPTING TO MODIFY COLUMN CHARSETS FOR TABLE: "+ tableName+" ========");            
                    changeCharColumnsCharset(tableName);
                }catch(SQLException e) {
                    logger.log(Level.WARNING, "", e);
                }
            }
        }finally{
            if(rs != null) try{rs.close();}catch(SQLException e) {logger.log(Level.WARNING, "", e);}
            if(con != null) try{con.close();}catch(SQLException e) {logger.log(Level.WARNING, "", e);}
        }
    }
    
    public void changeCharColumnsCharset(String tableName) throws SQLException {
        LinkedList<String> columnNames = new LinkedList<String>();
        LinkedList<String> columnTypes = new LinkedList<String>();
        LinkedList<String> columnSizes = new LinkedList<String>();
        LinkedList<Boolean> nonulls = new LinkedList<Boolean>();
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        try{
            // emailAddress is unique across product and user tables
            rs = stmt.executeQuery("SELECT * FROM `"+tableName+"` where `emailAddress` = '1'");
            ResultSetMetaData rsmeta = rs.getMetaData();
            for(int i=0; i<rsmeta.getColumnCount(); i++) {
                columnNames.add(rsmeta.getColumnName(i+1));
                columnTypes.add(rsmeta.getColumnTypeName(i+1));
                columnSizes.add(""+rsmeta.getColumnDisplaySize(i+1));
                nonulls.add(rsmeta.isNullable(i+1) == ResultSetMetaData.columnNoNulls);
            }
        }finally{
            if(rs != null) try{rs.close();}catch(SQLException e) {logger.log(Level.WARNING, "", e);}
            if(stmt != null) try{stmt.close();}catch(SQLException e) {logger.log(Level.WARNING, "", e);}
        }
        changeCharColumnsCharset(tableName, columnNames, columnTypes, columnSizes, nonulls);
    }
    
    public void changeCharColumnsCharset(String tableName, LinkedList<String> columnNames,
            LinkedList<String> columnTypes, LinkedList<String> columnSizes, LinkedList<Boolean> nonulls) {
        Statement stmt = null;
        try{
            con.setAutoCommit(false);
            stmt = con.createStatement();
            // Queries for dropping indices
            // We'll add them back after the modificaton of char set
            Map<String, Collection<String>> indexInfo = getIndexInfo(tableName);
System.out.println("Index info: "+indexInfo);            
            for(String indexName:indexInfo.keySet()) {
                
                if(indexName.equalsIgnoreCase("PRIMARY")) continue;
                
                String query = ("ALTER TABLE `"+tableName+"` DROP INDEX `"+indexName+ "`");
System.out.println(query);                
                stmt.addBatch(query);
            }
            for(int i=0; i<columnNames.size(); i++) {
                String columnType = columnTypes.get(i);
                String columnName = columnNames.get(i);
                String nullablePart = nonulls.get(i) ? " NOT NULL " : " default NULL ";
                if(!columnType.contains("CHAR") || columnType.contains("char")) continue; // Only char types are modified
                String query = ("ALTER TABLE `"+tableName+"` MODIFY `"+columnName+
                "` "+columnTypes.get(i)+"("+columnSizes.get(i) +") character set utf8 "+nullablePart);
System.out.println(query);       
                stmt.addBatch(query);
            }

            stmt.executeBatch();

            Collection<String> textSearchIndex = indexInfo.get("textSearch");
System.out.println("Text search index: "+textSearchIndex);            
            if(textSearchIndex != null) {
                String query = "CREATE FULLTEXT INDEX `textSearch` ON `"+tableName+ "` (";
                for(String indexColumn:textSearchIndex) {
                    query += "`" + indexColumn + "`,";
                }
                // Remove the last comma
                query = query.substring(0, query.length()-1);
                query+= ")";
System.out.println(query);                
                stmt.executeUpdate(query);
            }
            
            con.commit();
            
System.out.println("======== SUCCESSFULLY MODIFIED COLUMN CHARSETS FOR TABLE: "+ tableName+" ========");            
        }catch(SQLException e) {
            logger.log(Level.WARNING, "", e);
            if(con != null) try{ con.rollback(); }catch(SQLException se) { logger.log(Level.WARNING, "", se);}
        }finally{
            if(con != null) try{ con.setAutoCommit(true); }catch(SQLException se) { logger.log(Level.WARNING, "", se);}
            if(stmt != null) try{stmt.close();}catch(SQLException e) {logger.log(Level.WARNING, "", e);}
        }
    }
    
    private Map<String, Collection<String>> getIndexInfo(String tableName) throws SQLException {
        Map<String, Collection<String>> output = new HashMap<String, Collection<String>>();
        ResultSet rs = con.getMetaData().getIndexInfo(null, null, tableName, false, false);
        while(rs.next()) {
            String indexName = rs.getString(6);
            String columnName = rs.getString(9);
            Collection<String> indexColumns = output.get(indexName);
            if(indexColumns == null) {
                indexColumns = new ArrayList<String>();
                output.put(indexName, indexColumns);
            }
            indexColumns.add(columnName);
        }
        return output;
    }

    public void updateUserCurrency() throws Exception {
        
        con = this.initConnection();
        
        updateUserCurrency("personaldetails");
        
        updateUserCurrency("corporatedetails");
    }
    
    public void updateUserCurrency(String table) throws Exception {
        
        con = this.initConnection();
        
        Statement stmt = con.createStatement();
        
        String query = "SELECT emailAddress, currency from loosebox_db1.personaldetails";
        
        ResultSet rs = stmt.executeQuery(query);
        
        while(rs.next()) {
            
            String emailAddress = rs.getString("emailAddress");
            Integer currency = rs.getInt("currency");
            
            this.updateUserCurrency("autos", emailAddress, currency);
            this.updateUserCurrency("gadgets", emailAddress, currency);
            this.updateUserCurrency("property", emailAddress, currency);
        }
    }
    
    private void updateUserCurrency(String table, String email, Integer curr) throws SQLException {
        
        Statement stmt = con.createStatement();
        
        String newCurr = null;
        if(curr == null || curr == 0) {
            newCurr = "NULL";
        }else{
            newCurr = "'" + curr + "'";
        }
        
        float factor = this.getConversionFactor(curr);
        
        StringBuilder query = new StringBuilder("UPDATE `loosebox_db1`.`").append(table);
        query.append("` SET currency = ").append(newCurr).append(", `price` = `price` * '");
        query.append(factor).append("' WHERE `emailAddress` = '").append(email).append("'");
        
        int updateCount = -1;
        
        try{
            updateCount = stmt.executeUpdate(query.toString());
            System.out.println("Table: "+table+", Email: "+email+", Updated: "+updateCount);
        }catch(SQLException e) {
            logger.warning(e+". Table: "+table+", Email: "+email+", Updated: "+updateCount);
        }
    }
    
//EUR=0.754375377
//GBP=0.631392853
//NGN=150.212
    private float getConversionFactor(int curr) {
        switch(curr) {
            case 1: return 1.0f;
            case 2: return 0.754375377f;
            case 3: return 0.631392853f;    
            case 4: 
            default: return 150.212f;
        }
    }

    private void print(String [] cols, String [] vals) {
        for(int i=0; i<vals.length; i++) {
            if(vals[i] == null) continue;
System.out.print(cols[i]+": "+vals[i]+", ");
        }
System.out.println();        
    }
}

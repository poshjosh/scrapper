package developer_notes;

import com.bc.io.CharFileInput;
import com.bc.io.FileIO;
//import com.mysql.jdbc.StringUtils;
import com.scrapper.util.Util;
import com.scrapper.formatter.CompanyNameFormatter;
import com.scrapper.formatter.DefaultFormatter;
import com.scrapper.config.Sitenames;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.htmlparser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Josh
 */
public class Test {
    public static void main(String [] args) {
        try{
            
System.out.println(URLEncoder.encode("http://www.abc.com/x?11@d e", "UTF-8"));            

if(true) return;

            //Artikelnr.: 96303495
            Pattern p = Pattern.compile("Artikelnr\\p{Punct}+\\s*\\d+");            
            Matcher m = p.matcher(" Artikelnr.: 96303495 ma wassss Artikelnr.: 303495abc");
while(m.find()) {
    System.out.println(m.group());
}

if(true) return;
            
            
            DefaultFormatter fmt = new DefaultFormatter();
            
            Map params = new HashMap();
            params.put("datein", "07 29 2013");
            
            int status = fmt.formatDateinAndExpiryDate(params);
            
System.out.println("Status: "+status+", Parameters: "+params);

if(true) return;
            
            String msg = String.format("%1$s. Pages left: %2$s, Processing: %3$s", 
                    Test.class, 23, new URL("http://www.looseboxes.com"));
if(true) {
    System.out.println(msg); return;
}            
            Test test = new Test();
            
            test.getTargetNodeAttributes();
//            test.testSynonyms();
//            test.testThrowSomething();
//            test.a4();
//System.out.println(Pattern.compile("[\\d]+").matcher("f567e").find());            
//            String xmlFile = System.getProperty("user.home") + "/twit88.xml";
        }catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    public Properties getTargetNodeAttributes() {
        Properties output = new Properties();
        String attr = "id=bigDiv,,,onclick='try,,me',,,wait='aboki?a=b'";
        // Expected Format:   id=xxxx,,,width=10,,,height=20
        //@related_43 our separator is ,,,
        String [] parts = attr.split(",,,");
        if(parts == null || parts.length == 0) return null;
        for(String part:parts) {
            String [] subs = part.split("=");
            if(subs == null || subs.length == 0) continue;
            String key = subs[0];
            StringBuilder val = new StringBuilder(subs[1]);
            if(subs.length > 2) {
                for(int i=2; i<subs.length; i++) {
                    val.append("=").append(subs[i]);
                }
            }
System.out.println(key+"   :   "+val);            
            output.setProperty(key, val.toString());
        }
        return output;
    }
    
    private void testThrowSomething() {
        try{
            this.callsThrowSomething();
        }catch(Exception e){
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e);
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
            "@{0}. Caught Exception.\n{1}", new Object[]{this.getClass().getName(), e, e.getStackTrace() != null ? e.getStackTrace()[0] : "No stack trace"});
        }
    }
    
    private void callsThrowSomething() throws Exception {
        this.throwSomething();
    }
    private void throwSomething() throws Exception {
        if(true) {
            throw new Exception("This is the message");
        }
    }

    class Element{
        private int value;
        public Element(int val) {
            value = val;
        }
        public void setValue(int i) {
            value = i;
        }
        public int getValue() {
            return value;
        }
        @Override
        public String toString() {
            return ""+value;
        }
    }
    private void a4() {
        HashMap m = new HashMap();
        m.put("One", new Element(1));
        m.put("Two", new Element(2));
        HashMap x = new HashMap(m);
        x.remove("Two");
System.out.println("X: "+x);        
System.out.println("M: "+m);        
        m.keySet().remove("Two");
System.out.println("X: "+x);        
System.out.println("M: "+m);        
System.out.println("Equals: "+x.get("One").equals(m.get("One")));
        ((Element)x.get("One")).setValue(3);
System.out.println("X: "+x.get("One"));        
System.out.println("M: "+m.get("One"));        
    }
    
    private void a3() {
System.out.println(13/4);
System.out.println(13%4);
    int [][] nums = new int[2][2];
    ++nums[0][0];
    ++nums[0][0];
System.out.println("Row[0]"+Arrays.toString(nums[0])+", Row[1]"+Arrays.toString(nums[1]));    
    }

    private void a2() {
//System.out.println("\u0080");
        UUID frmBytes = UUID.nameUUIDFromBytes("nuroxltd@yahoo.com".getBytes());
System.out.println(frmBytes.toString());
        UUID frmStr = UUID.fromString(frmBytes.toString());
System.out.println(frmStr.toString());
    }
    private void a1() {
        String regex = "<a.+?href=\"/loose/signOut\">.+?</a>\\s*?\\|";
        String input = "Some Text\nSome Text\n<a id=\"toprightlinks.signout\" style=\"color:yellow\" href=\"/loose/signOut\">logout</a> |";
        Matcher m = Pattern.compile(regex).matcher(input);
        String output = m.replaceAll("");
System.out.println(output);
    }

    private void z() {
        try{
            Parser parser = new Parser();
            URL a = new URL("http://www.looseboxes.com/login?emailAddress=looseboxes@gmail.com&password=1kjvdul-");
            a.openConnection();
            for(int i=260; i<267; i++) {

                URL url = new URL("http://www.looseboxes.com/displayProduct?productTable=property&productId="+i);
System.out.println(url);
                url.openConnection();
//                Desktop.getDesktop().browse(url.toURI());
                Thread.sleep(1000);
//                org.htmlparser.http.ConnectionManager.getDefaultRequestProperties().put(
//               "User-Agent", UserAgents.getInsance().get());
//               parser.setURL(url.toString());
            }
        }catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private void y() {
        String x = "it";
        Pattern pattern = Pattern.compile("\b"+x+"\b");
        Matcher matcher = pattern.matcher("Linkserve Limited");
System.out.println("For pattern: "+pattern.pattern()+", output: "+matcher.replaceAll(""));
    }

    private void w() throws IOException {
        String inputFile = System.getProperty("user.home") + "/Desktop/GADGETS.xls";
        String outputFile = inputFile.replace("GADGETS", "GADGETS_1");
        FileIO fileIO = new FileIO();
        fileIO.copy(false, inputFile, outputFile, true);
    }

    private void v() {
        String input = "http://www.dailytrust.dailytrust.com/index.php?option=com_ijoomla_rss&act=xml&sec=1:news&feedtype=RSS2.0&Itemid=184";
System.out.println(UUID.nameUUIDFromBytes(input.getBytes()));
        String input2 = "http://www.looseboxes.com/feeds/rss/news.newsFeed.html";
System.out.println(UUID.nameUUIDFromBytes(input2.getBytes()));
    }

    private void u() throws Exception {
        URL url = new URL("file:/"+System.getProperty("user.home")+"/testConfig.xml");
        InputStream in = null;
        try{
            in = url.openStream();
        }finally{
            if(in != null) in.close();
        }
    }

    private void t(String xmlFile) throws Exception {
        InputStream is = null;
        Document doc = null;
        try{
            is = new FileInputStream(xmlFile);
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            NodeList list = doc.getElementsByTagName("xq-param");
            System.out.println("Number of elements: "+list.getLength());
        }catch (SAXException e) {
            Logger.getLogger(this.getClass().getName()).warning(this.getClass().getName()+"#format. " + e);
        }
        catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).warning(this.getClass().getName()+"#format. " + e);
        }
        catch (ParserConfigurationException e) {
            // Unlikely to happen unless your DOM parsers aren't properly configured.
            Logger.getLogger(this.getClass().getName()).warning(this.getClass().getName()+"#format. " + e);
        }catch(Throwable e) {
            Logger.getLogger(this.getClass().getName()).warning(this.getClass().getName()+"#format. " + e);
        }finally{
            try{ if(is != null) is.close(); }catch(IOException e) { e.printStackTrace(); }
        }
    }

    private void s(String xmlFile) throws Exception {

        // First create a new XMLInputFactory
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        // Setup a new eventReader
        InputStream in = new FileInputStream(xmlFile);

        XMLStreamReader reader = inputFactory.createXMLStreamReader(in);

        // Read the XML document
        while (reader.hasNext()) {

        }
    }

    private void r() throws UnsupportedEncodingException {
System.out.println("\\u0093: " + '\u0093');
System.out.println("\\u0093 as UTF-8: " + new String(Character.toString('\u0093').getBytes("UTF-8")));
System.out.println('\u00A0');
System.out.println(new String(Character.toString('\u00A0').getBytes("UTF-8")));
System.out.println('\u00C2');
System.out.println(new String(Character.toString('\u00C2').getBytes("UTF-8")));
System.out.println('\u0080');
System.out.println(new String(Character.toString('\u0080').getBytes("UTF-8")));
        char ch1 = '\u00C2';
        char ch2 = '\u0080';
        StringBuilder builder = new StringBuilder();
        builder.append(ch1).append(ch2).append(ch1);
        String chStr = builder.toString();
        System.out.println(chStr);
        System.out.println(new String(chStr.getBytes()));
        System.out.println(new String(chStr.getBytes("UTF-8")));
    }

    private void q() {
        String text = "abc";
        System.out.println(Arrays.toString(text.split("\\s")));
    }

    private void p() {
        String input = "Administrative Assistant";
        String regex = "admin";
        Pattern p = Pattern.compile("\b"+regex+"\b", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);
        System.out.println(m.replaceAll("Salsa"));
    }

    private void o() {
        System.out.println('\u00C2');
        System.out.println('\u0080');
        System.out.println('\u00A2');
        System.out.println('\u0099');
    }

    private void n() {
        CompanyNameFormatter ex = new CompanyNameFormatter();
System.out.println(ex.apply("UNDP Nigeria Vacancies: Humanitarian Affairs Officer / Ecowas Liason Officer"));
System.out.println(ex.getJobTitle());
ex.reset();
System.out.println(ex.apply("Program Officer at Transition Monitoring Group"));
System.out.println(ex.getJobTitle());
ex.reset();
System.out.println(ex.apply("Transition Monitoring Group vacancy for National Coordinator"));
System.out.println(ex.getJobTitle());
    }

    private void l() {
        String input = "abc d \"efg\" 124t<!-- what is this; \n  google\"xyz\"; -->\nBaker Hughes";
        String startTag = "<!--";
        String endTag = "-->";
        int a = input.indexOf(startTag);
        int b = input.indexOf(endTag) + endTag.length();
        System.out.println(input.substring(0, a)+input.substring(b));

        System.out.println(Pattern.compile("<!--[.]*?-->").matcher(input).replaceAll(""));
    }

    private void j() {
        final String a = "a\nExxx$- #";
        final String b = "a\nExxx$- #";
System.out.println("Equals        : "+(a.equals(b)));        
System.out.println("Compare To    : "+(a.compareTo(b)));        
System.out.println("Region Matches: "+(a.regionMatches(0, b, 0, a.length())));
System.out.println(a.replaceAll("\\s", ""));
    }

    private void i() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        Date date = sdf.parse("Thu Jun 16 10:01:19 WAT 2011");
System.out.println("Date: "+date);
    }

    private void h() {

        Pattern p = Pattern.compile(".*[apply|application|remuneration|salary|requirements|description|title].*");
System.out.println("How to apply: "+p.matcher("How to apply").find());
    }

    private void g() throws ParseException {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("MMM, dd yyy HH:mm:ss");
//            Object obj = sdf.parseObject("2011-05-15 22:48:05.71");
//System.out.println("Instance of Date: "+(obj instanceof Date));
//System.out.println("Instance of Time: "+(obj instanceof Time));
//System.out.println("Instance of Timestamp: "+(obj instanceof Timestamp));
//            Object obj = sdf.parseObject("2011 05 15 22:48:05");
//System.out.println("Instance of Date: "+(obj instanceof Date));
//System.out.println("Instance of Time: "+(obj instanceof Time));
//System.out.println("Instance of Timestamp: "+(obj instanceof Timestamp));

System.out.println(sdf.format(new java.sql.Timestamp(System.currentTimeMillis())));
    }

    private void f() throws ParseException {

            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("MMM, dd yyy");
            Date date = sdf.parse("Jan, 01 2010");
System.out.println("Date: "+date);

            String str = sdf.format(date);
System.out.println("String: "+str);
    }

    private void e() throws IOException {
        CharFileInput fileInput = new CharFileInput();
        CharSequence input = fileInput.readChars("careersnigeria_downloaded.txt");
        Pattern p = Pattern.compile("\\bhttp://www.careersnigeria.com.*\\b");
        Matcher m = p.matcher(input);
        while(m.find()) {
System.out.println(m.group());
        }
    }

    private void d() {
        System.out.println(Pattern.matches("\\b\\d{1,2}/\\d{1,2}/\\d{4}\\b", "2/9/2010"));
        System.out.println(Pattern.matches("\\b\\d{1,2}/\\d{1,2}/\\d{4}\\b", "02/9/2010"));
        System.out.println(Pattern.matches("\\b\\d{1,2}/\\d{1,2}/\\d{4}\\b", "2/91/2010"));
    }

    private void c() {
//onclick="popup('pop_car.php?img=1299267120DSC09102.JPG');"
//popup('pop_car.php?img=1299267906DSC09110.JPG');

        Pattern p = Pattern.compile("pop_car\\.php\\?img\\=[a-zA-Z0-9]+\\.jpg", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("popup('pop_car.php?img=1299267120DSC09102.JPG');");
        if(m.find()) {
            System.out.println("1. Group: "+m.group());
        }
        m = p.matcher("popup('pop_car.php?img=12992aaa20Photo09sss102.jpg');");
        if(m.find()) {
            System.out.println("2. Group: "+m.group());
        }
        m = p.matcher("popup('pop_car.php?img=1299267906DSC09110.JPG');");
        if(m.find()) {
            System.out.println("3. Group: "+m.group());
        }
    }

    private void b() {
        HashMap m = new HashMap();
        m.put("aboki", "yes mon");
        m.put("wella", "uXuXuX");
        m.put("koifale", " uXuXuX");
        m.put("tula", "kait lute");
        m.put("na", "uXuXuX");
        List unwanted = new ArrayList();
        unwanted.add("uXuXuX");
        m.values().removeAll(unwanted);
System.out.println(m);
    }

    private void a(){
        Map m = new HashMap();
        m.put("apply", "1");
        m.put("how to apply", "2");
System.out.println(Util.findValueWithMatchingKey(m, "app"));
System.out.println(Util.findValueWithMatchingKey(m, "how not to apply"));
    }

    private void patternTest() {

//x?, x*, x+
// once or not at all, zero or more, one or more
// Email pattern
//Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");

        String months = "jan|feb|mar|apr|may|jun|july|aug|sep|oct|nov|dec";

//        Pattern pattern = Pattern.compile("\\d{2}[th]*\\s["+months+"][a-zA-Z]*[\\p{Punct}]?\\s[\\d]{4}", Pattern.CASE_INSENSITIVE);
        Pattern pattern = Pattern.compile("\\d{1,2}[st|nd|rd|th]*\\s[of\\s]*["+months+"][a-zA-Z]*[\\p{Punct}]?\\s[\\d]{4}\\.*", Pattern.CASE_INSENSITIVE);

        String [] inputs = {"abcde <b>new year</b> 12th Jan, 2011<th>thank god</th>12 Jan 300.",
        "abcde <b>new year</b> fghij 12 January 2011 or the likes of it <b>old</b>",
        "abcde <b>new year</b> fghij 1st of January, 2011. or the likes of it <b>old</b>"};

        for(int i=0; i<inputs.length; i++) {
            Matcher m = pattern.matcher(inputs[i]);
            if(m.find()) {
System.out.println("Input["+i+"], Found: "+m.group());
            }
        }

// Put full stops, th and 2 digits in text
//        pattern = Pattern.compile("["+months+"][a-zA-Z]*\\s[\\d]{2}[th]*[\\p{Punct}]?\\s[\\d]{4}", Pattern.CASE_INSENSITIVE);
        pattern = Pattern.compile("["+months+"][a-zA-Z]*\\s[\\d]{1,2}[st|nd|rd|th]*[\\p{Punct}]?\\s[\\d]{4}\\.*", Pattern.CASE_INSENSITIVE);

        inputs = new String[]{"abcde <b>new year</b> Jan 12, 2011<th>thank god</th>12 Jan 300.",
        "jan 12 200 th.<b>new year</b> fghij January 12th 2011 or the likes of it <b>old</b>"};

        for(int i=0; i<inputs.length; i++) {
            Matcher m = pattern.matcher(inputs[i]);
            if(m.find()) {
System.out.println("Input["+i+"], Found: "+m.group());
            }
        }
    }
    private void dateFormatTest() {
        
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("MMM dd'th', yyyy");
        sdf.setLenient(true);
        try{
            Date date = sdf.parse("Apr 12th, 2011");

            System.out.println(date);
        }catch(ParseException e) {
            e.printStackTrace();
        }

    }

    private String getTestUrl(String sitename) {
        if(Sitenames.CAREERSNIGERIA.equals(sitename)) {
            return "http://www.careersnigeria.com/agency-govt-jobs-nigeria/undp-nigeria-coordination-specialist-abuja";
//        return "http://www.careersnigeria.com/construction-jobs-nigeria/adexen-nigeria-project-planner-engineering";
//        return "http://www.careersnigeria.com/pharmaceutical-jobs-in-nigeria/ranbaxy-business-development-manager";
//        return "http://www.careersnigeria.com/jobs/senior-master-planner-oil-gas-power";
//        return "http://www.careersnigeria.com/jobs/oil-gas-project-engineer";
        }else if(Sitenames.NIGERIANDRIVER.equals(sitename)) {
            return "http://www.nigeriandriver.com/index.php?p=car_details&ID=3665";
        }else if(Sitenames.NGCAREERS.equals(sitename)){
//http://www.ngcareers.com/2013, 
//"http://ngcareers.com/2013/02/business-development-officer-at-enabled-business-solutions-ltd"
//http://ngcareers.com/jobs;
//http://ngcareers.com/2013/01/manager-finance-admin-at-reputable-media-agency
//                "http://ngcareers.com/2013/05/crane-operator-at-estymol-oil-service-ltd";
//                "http://ngcareers.com/2013/05/head-of-primary-school-at-a-nursery-primary-school";
//                "http://ngcareers.com/2013/05/crane-operator-at-estymol-oil-service-ltd";
//                "http://ngcareers.com/2013/05/glaxosmithkline-graduate-recruitment-2013-commercial-graduate";
//                "http://ngcareers.com/2013/04/assistant-head-haulage-operations-at-a-reputable-logistics-company";
//                "http://ngcareers.com/2013/04/vacancies-at-a-group-of-companies-sales-executives";
//                "http://ngcareers.com/2011/04/pricewaterhousecoopers-graduate-recruitment-2011";
            return "http://ngcareers.com/2013/06/fleet-officers-at-a-reputable-logistics-company";
        }else if("jumia".equals(sitename)){
//            return "http://www.jumia.com.ng/Bottle-Starter-Kit-28973.html";
//            return "http://www.jumia.com.ng/12PCE-Classique-Cookware-Set-T21179-31178.html";            
//            return "http://www.jumia.com.ng/Pour-Homme-Red-Label-EDP---100ml-33180.html";            
//            return "http://www.jumia.com.ng/Curve-7-9320---Black-17814.html";
            return "http://www.jumia.com.ng/Galaxy-S3-Bundle---Black-34622.html";
        }else if(Sitenames.JOBSNIGERIANA.equals(sitename)){
            return "http://jobsnigeriana.com/united-bank-for-africa-recruits-graduate-trainee-and-experienced-candidates-nationwide.html";            
        }else if("undp".equals(sitename)){
            return "http://jobs.undp.org/cj_view_job.cfm?cur_job_id=35893";            
        }else if("aliexpress_fashion".equals(sitename)) {
            return "http://www.aliexpress.com/item/Without-Belt-Korean-Women-Summer-New-Fashion-Chiffon-Dress-Short-sleeve-Dots-Polka-Waist-Mini-Beige/629131530.html";
        }else{
            //file:///+System.getProperty("user.home")+/scrapper_temp/local.html            
            throw new IllegalArgumentException();
        }
    }
}
/**
 *
        InputStream in = new FileInputStream(inputFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "US-ASCII"));
        OutputStream out = new FileOutputStream(outputFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "US-ASCII"));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        } finally {
            in.close();
            br.close();
            out.close();
            bw.close();
        }
 */
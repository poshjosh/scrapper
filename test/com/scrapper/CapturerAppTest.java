package com.scrapper;

import com.bc.webdatex.URLParser;
import com.bc.webdatex.locator.impl.TagLocatorImpl;
import com.scrapper.context.CapturerContext;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author USER
 */
public class CapturerAppTest {
    
    public CapturerAppTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    @Test
    public void testAll() {
        try{
            
            final URI configDir =  Paths.get(System.getProperty("user.home"), "/Documents/NetbeansProjects/idisccore/src/META-INF/configs").toUri();
            
            CapturerApp app = new CapturerApp(){
                @Override
                public URI getConfigDir() {
                    return configDir;
                }
            };
            
            app.init(true);
            
            Set<String> siteNames = app.getConfigFactory().getSitenames();
            
            int sitesExtracted = 0;
            final int sitesToExtract = 2;
            
            for(String siteName:siteNames) {
            
                if(++sitesExtracted == sitesToExtract) {
                    break;
                }
                
                extract(app, siteName, "targetNode0", true);
            }
        }catch(IOException | IllegalAccessException | InterruptedException | 
                InvocationTargetException | ParserException e) {
            log(e);
        }
    }
    private void extract(CapturerApp app, String site, String key, boolean preLocateTarget) throws ParserException {
        
        MultipleNodesExtractorIx pageExtractor = getPageExtractor(app, site);

        com.bc.webdatex.extractor.node.NodeExtractor nodeExtractor = 
                ((MultipleNodesExtractorIx)pageExtractor).getExtractor(key);

        NodeList nodes = CapturerAppTest.this.parse(site);

        if(preLocateTarget) {
            
            NodeList targetNodes = parse(nodes, nodeExtractor, key);

            if(targetNodes != null) {
                nodes = targetNodes;
            }
        }
        
        nodes.visitAllNodesWith(nodeExtractor);
            
System.out.println(this.getClass().getName()+". EXTRACT:\n"+nodeExtractor.getExtract());
    }

    private static MultipleNodesExtractorIx getPageExtractor(CapturerApp app, String site) {
        CapturerContext ctx;
        ctx = app.getConfigFactory().getContext(site);
        MultipleNodesExtractorIx pageExtractor = ctx.getExtractor();
        return pageExtractor;
    }
    
    private String getUrl(String site) {
        String [] urls;
        switch(site) {
            case "bellanaija":
                urls = new String[]{
                    "http://www.bellanaija.com/2016/03/ty-bello-is-bimpe-onakoyas-biggest-fan-read-her-inspiring-story-on-the-makeup-maestro/",
                    "http://www.bellanaija.com/2015/06/09/designer-deola-sagoe-is-a-vision-in-gold-in-her-own-piece/"
                };
                break;
            case "lindaikeji.blogspot":
                urls = new String []{
                    "http://www.lindaikejisblog.com/2015/06/dear-lib-readers-my-wife-complains-that.html",
                    "http://www.lindaikejisblog.com/2015/06/photos-femi-otedolas-daughter-graduates.html",
                    "http://www.lindaikejisblog.com/2015/06/former-miss-mississippis-boobs-rots.html"
                };
                break;
            case "naij":
                urls = new String[]{
                    "http://www.naij.com/460524-read-happened-men-trekked-atiku.html",
                    "http://www.naij.com/460495-live-ngr-vs-chad-afcon-qualifier.html",
                    "http://www.naij.com/460491-photos-dprince-is-now-a-father.html"
                };
                break;
            case "ngrguardiannews":
                urls = new String[]{
                    "http://www.ngrguardiannews.com/2015/06/malaysian-villagers-beg-spirits-to-end-quake-aftershocks/",
                    "http://www.ngrguardiannews.com/2015/06/singapore-gay-rights-rally-draws-record-crowd-organisers/",
                    "http://www.ngrguardiannews.com/2015/06/new-york-rally-launches-clintons-bid-for-white-house/"
                };
                break;
            case "punchng":
                urls = new String[]{
                    "http://www.punchng.com/news/30-hospitalised-for-suspected-food-poisoning-in-china/",
                    "http://www.punchng.com/news/el-rufai-appoints-dada-onazi-as-kaduna-head-of-service/",
                    "http://www.punchng.com/news/honda-reports-another-death-in-exploding-airbag-crisis/"
                };
                break;
            case "saharareporters":
                urls = new String[]{
                    "http://saharareporters.com/2015/06/15/exclusive-garba-shehu-speaks-saharatv-explains-delayed-ministerial-appointments",
                    "http://saharareporters.com/2015/06/15/serap-icc-should-refer-south-africa-un-security-council-refusing-arrest-al-bashir",
                    "http://saharareporters.com/2015/06/15/us-commits-5-billion-military-assistance-against-boko-haram"
                };
                break;
            case "sunnewsonline_breaking":
                urls = new String[]{
                    "http://sunnewsonline.com/new/?p=123586",
                    "http://sunnewsonline.com/new/?p=123571",
                    "http://sunnewsonline.com/new/?p=123565"
                };
                break;
            case "sunnewsonline_national":
                urls = new String[]{
                    "http://sunnewsonline.com/new/?p=123586",
                    "http://sunnewsonline.com/new/?p=123571",
                    "http://sunnewsonline.com/new/?p=123565"
                };
                break;
            case "thenationonlineng":    
                urls = new String[]{
                    "http://thenationonlineng.net/new/first-lady-dont-pay-any-money-to-see-president/",
                    "http://thenationonlineng.net/new/bpe-in-n1-45b-scam/",
                    "http://thenationonlineng.net/new/buhari-considers-balance-in-sgf-choice/"
                };
                break;
            case "thisday":
                urls = new String[]{
                    "http://www.thisdaylive.com/articles/nerc-to-revoke-inoperative-power-generation-licences/212109/",
                    "http://www.thisdaylive.com/articles/ssanu-ask-buhari-to-sack-nuc-executive-secretary/212108/",
                    "http://www.thisdaylive.com/articles/i-have-no-interest-in-sgf-position-says-oyegun/212106/"
                };
                break;
            case "channelstv_headlines":
                urls = new String[]{
                    "http://www.channelstv.com/2015/10/21/tribunal-adjourns-trial-of-bukola-saraki-till-november-5/",
                    "http://www.channelstv.com/2015/10/21/reps-to-investigate-nnpc-joint-venture-operations/",
                    "http://www.channelstv.com/2015/10/19/buhari-meets-with-service-chiefs-gets-assurance-of-peace-in-north-east/"
                };
                break;
            case "aitonline_news":
                urls = new String[]{
                    "http://www.aitonline.tv/post-council_of_state_approves_president___s_nomination_of_new_inec_chairman__5_national_commissioners",
                    "http://www.aitonline.tv/post-lagos_state_govt_donates_n150m_to_adamawa__yobe_and_borno",
                    "http://www.aitonline.tv/post-don_t_compare_me_with_ronaldo_and_messi___lewandowski"
                };
                break;
            default:    
                throw new UnsupportedOperationException("Unexpected site: "+site);
        }        

        int random = com.bc.util.Util.randomInt(urls.length);
        
        return urls[random];
    }
    
    private NodeList parse(String site) 
            throws ParserException {
        URLParser p = new URLParser();
        String url = getUrl(site);
        NodeList nodes = p.parse(url);
        return nodes;
    }
    
    private NodeList parse(
            NodeList nodes, com.bc.webdatex.extractor.node.NodeExtractor nodeExtractor, String key) 
            throws ParserException {

        TagLocatorImpl locator = (TagLocatorImpl)nodeExtractor.getFilter().getTagLocator();

        List<String> [] transverse = locator.getTransverse();
        
log("Transverse:\n"+(transverse==null?null:Arrays.toString(transverse)));

        nodes.visitAllNodesWith(locator);

        Tag target = locator.getTarget();

log("Target:\n"+(target==null?null:target.toHtml(false)));

        if(target == null) {
            return null;
        }
        
        nodes = new NodeList();
        nodes.add(target);

        return nodes;
    }
    
    private void log(Object msg) {
System.out.println(this.getClass().getName()+". "+msg);        
    }
    private void log(Throwable t) {
        t.printStackTrace();
    }
}

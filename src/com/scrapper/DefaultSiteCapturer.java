package com.scrapper;

import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import com.scrapper.url.ConfigURLList;
import com.scrapper.url.ConfigURLPartList;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)DefaultSiteCapturer.java   28-Aug-2013 23:43:16
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
public class DefaultSiteCapturer extends BaseSiteCapturer {
    
    private ResumeHandler parseResumeHandler;
    
    private ResumeHandler scrappResumeHandler;
    
    public DefaultSiteCapturer() { }
    
    public DefaultSiteCapturer(final CapturerContext context) { 
        
        super(context);

        this.init(context, null, false, true);
    }
    
    public DefaultSiteCapturer(final CapturerContext context, List<String> urlList) {
        
        super(context);
        
        this.init(context, urlList, false, true);
    }

    public DefaultSiteCapturer(final CapturerContext context, 
            List<String> urlList, boolean resume, boolean resumable) {
        
        super(context);
        
        this.init(context, urlList, resume, resumable);
    }
    
    public DefaultSiteCapturer(String sitename) {
        
        this(sitename, null, -1, -1, false, true);
    }
    
    public DefaultSiteCapturer(String sitename, List<String> urls) {
        
        this(sitename, urls, -1, -1, false, true);
    }

    public DefaultSiteCapturer(String sitename, List<String> urls, 
            boolean resume, boolean resumable) {
        
        this(sitename, urls, -1, -1, resume, resumable);
    }
    
    public DefaultSiteCapturer(String sitename, List<String> urls, 
            int batchSize, int batchInterval, boolean resume, boolean resumable) {
        
        ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();

        JsonConfig config = factory.getConfig(sitename);

        if(config == null) {
            throw new NullPointerException("Failed to load site config for: "+sitename);
        }
        
        this.init(factory.getContext(config), urls, resume, resumable);
        
        if(urls == null) {
            this.setScrappLimit(
                config.getInt(Config.Extractor.scrappLimit.name())
            );
        }
        
        if(batchSize > 0) {
            this.setBatchSize(batchSize);
        }
        
        if(batchInterval > 0) {
            this.setBatchInterval(batchInterval);
        }
    }

    private void init(
            final CapturerContext context, 
            List<String> urlList,
            boolean resume,
            boolean resumable) {
    
        this.setLogin(true);
        this.setContext(context);
        
        URLParser urlParser = this.createCrawler(context, urlList, resume, resumable);
        this.setCrawler(urlParser);
        
        Scrapper scrapper = this.createScrapper(context, urlList, resume, resumable);
        this.setScrapper(scrapper);
        
        PageDataConsumer dataConsumer = this.createDataConsumer(context, urlList);
        this.setDataConsumer(dataConsumer);
        
XLogger.getInstance().log(Level.FINE, "Created:: {0}", this.getClass(), this);        
    }
    
    protected URLParser createCrawler(
            CapturerContext context, 
            List<String> urlList,
            final boolean resume,
            final boolean resumable) {

        ResumableUrlParser urlParser;
            
        if(urlList != null) {
            
            urlParser = new ResumableUrlParser(
                    context.getConfig().getName(), urlList){
                @Override
                public boolean isResumable() {
                    return resumable;
                }
                @Override
                public boolean isResume() {
                    return resume;
                }
            };
        }else{
            
            JsonConfig config = context.getConfig();

            ConfigURLList urllist = new ConfigURLList();

            urllist.update(config, "counter");

            if(!urllist.isEmpty()) {

                ConfigURLPartList serialPart = ConfigURLPartList.getSerialPart(config, "counter");

                if(serialPart == null) {
                    urlParser = new MultipleSourcesCrawler(context, urllist){
                        @Override
                        public boolean isResumable() {
                            return resumable;
                        }
                        @Override
                        public boolean isResume() {
                            return resume;
                        }
                    };
                }else{        
                    urlParser = new DirectSourcesParser(context, urllist){
                        @Override
                        public boolean isResumable() {
                            return resumable;
                        }
                        @Override
                        public boolean isResume() {
                            return resume;
                        }
                    };
                }        
            }else{

                urlParser = new Crawler(context){
                    @Override
                    public boolean isResumable() {
                        return resumable;
                    }
                    @Override
                    public boolean isResume() {
                        return resume;
                    }
                };
            }
        }
        
        if(parseResumeHandler != null) {
            urlParser.setResumeHandler(parseResumeHandler);
        }
        
        return urlParser;
    }

    protected Scrapper createScrapper(
            CapturerContext context, 
            List<String> urlList,
            final boolean resume,
            final boolean resumable) {
        ResumableScrapper scrapper = new ResumableScrapper(context){
            @Override
            public boolean isResumable() {
                return resumable;
            }
            @Override
            public boolean isResume() {
                return resume;
            }
        };
        if(scrappResumeHandler != null) {
            scrapper.setResumeHandler(scrappResumeHandler);
        }
        return scrapper;
    }

    protected PageDataConsumer createDataConsumer(CapturerContext context, List<String> urlList) {
        if(this.hasUploaderSettings(context)) {
            PageDataConsumer uploader = new ScrappUploader(context);
            return uploader;
        }else{
            return null;
        }
    }
    
    private boolean hasUploaderSettings(CapturerContext context) {
        
        try{
            String urlStr = AppProperties.getProperty(AppProperties.INSERT_URL);
            if(urlStr == null || urlStr.isEmpty()) {
                return false;
            }
            URL url = new URL(AppProperties.getProperty(AppProperties.INSERT_URL));
        }catch(MalformedURLException e) {
            return false;
        }
        
        JsonConfig config = context.getConfig();
        
        final Map m = config.getMap("uploadParameters");
        
        return !(m == null || m.isEmpty());
    }
    

    public ResumeHandler getParseResumeHandler() {
        return parseResumeHandler;
    }

    public void setParseResumeHandler(ResumeHandler parseResumeHandler) {
        this.parseResumeHandler = parseResumeHandler;
    }

    public ResumeHandler getScrappResumeHandler() {
        return scrappResumeHandler;
    }

    public void setScrappResumeHandler(ResumeHandler scrappResumeHandler) {
        this.scrappResumeHandler = scrappResumeHandler;
    }
}

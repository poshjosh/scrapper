package com.scrapper;

import com.bc.process.StoppableTask;
import com.scrapper.context.CapturerContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

/**
 * @(#)SiteCapturer.java   09-Nov-2013 16:03:21
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
public interface SiteCapturer 
        extends StoppableTask, Resumable {

    URLParser getCrawler();

    PageDataConsumer getDataConsumer();

    Scrapper getScrapper();

    CapturerContext getContext();

    Date getStartTime();

    boolean isLogin();

    boolean isRunning();

    void login() throws MalformedURLException, IOException;

    void setCrawler(URLParser crawler);

    void setDataConsumer(PageDataConsumer dataConsumer);
    
    void setLogin(boolean login);

    void setScrapper(Scrapper scrapper);

    void setContext(CapturerContext context);

    void setStartTime(Date startTime);
}

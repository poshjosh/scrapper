package com.scrapper.extractor;

import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.CapturerSettings;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;


/**
 * @(#)PageExtractorIx.java   09-Oct-2015 00:18:38
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
public interface PageExtractorIx extends NodeListExtractorIx {

    JsonConfig getCapturerConfig();

    CapturerContext getCapturerContext();

    CapturerSettings getCapturerSettings();    

    String getPageTitle();

    @Override
    String getTaskName();

    Tag getTitleTag();

    boolean isDone();

    boolean isTitleExtracted();

    @Override
    void reset();

    void setPageTitle(String pageTitle);

    @Override
    void visitEndTag(Tag tag);

    @Override
    void visitRemarkNode(Remark remark);

    @Override
    void visitStringNode(Text node);

    @Override
    void visitTag(Tag tag);
}

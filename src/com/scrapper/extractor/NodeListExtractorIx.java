package com.scrapper.extractor;

import com.bc.process.StoppableTask;
import java.io.Serializable;
import java.util.Map;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


/**
 * @(#)NodeListExtractorIx.java   09-Oct-2015 00:25:07
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
public interface NodeListExtractorIx extends DataExtractor<NodeList>, Serializable, StoppableTask {

    @Override
    Map extractData(NodeList nodeList) throws ParserException;

    NodeList getSource();

    @Override
    String getTaskName();

    @Override
    boolean isCompleted();

    @Override
    boolean isStarted();

    @Override
    boolean isStopInitiated();

    @Override
    boolean isStopped();

    void reset();

    // Stoppable Task interface
    //
    @Override
    void run();

    void setSource(NodeList source);

    @Override
    void stop();

    @Override
    String toString();

    // NodeList methods
    //
    void visitEndTag(Tag tag);

    void visitRemarkNode(Remark remark);

    void visitStringNode(Text string);

    void visitTag(Tag tag);

}

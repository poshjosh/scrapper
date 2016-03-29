package com.scrapper.filter;

import com.bc.webdatex.filter.NodeVisitingFilterIx;
import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;

/**
 * @(#)FilterFactory.java   04-Oct-2013 03:15:39
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
public class FilterFactory {
    
    public static enum FilterType{ tagNameOnly, tagName, textToDisableOn,
    textToReject, rejectNode, hasIdAttribute, attributes, attributesRegex,
    nodesToAccept, nodesToReject, nodeTypesToAccept, nodeTypesToReject}
    
    public static final String TARGET = "targetNode";
    public static final String PARENT = "parentNode";
    public static final String START_AT = "startAtNode";
    public static final String STOP_AT = "stopAtNode";
    public static final String [] NODE_TYPES = {PARENT, TARGET, START_AT, STOP_AT};
    
    public static FilterType [] getTagNameAndAttributesFilter(
            JsonConfig config, Object [] propertyKey) {
        
        propertyKey = config.getPath(propertyKey, 
                Config.Extractor.attributes, Config.Extractor.id);
        
        final String idValue = config.getString(propertyKey);
        
        FilterType [] arr;
        
        if(idValue == null) {
            
            arr = new FilterType[]{FilterType.tagName, 
                FilterType.attributes, FilterType.attributesRegex};
            
        }else{
            
            arr = new FilterType[]{FilterType.tagName, 
                FilterType.hasIdAttribute};
        }
        
//        arr = new FilterType[]{FilterType.tagName, FilterType.hasIdAttribute, 
//            FilterType.attributes, FilterType.attributesRegex};
        
        return arr;
    }
    
    public static NodeFilter get(JsonConfig config, 
            String propertyKey, FilterType [] types) {
    
        return get(config, new Object[]{propertyKey}, types);
    }
    
    public static NodeFilter get(JsonConfig config, 
            Object [] propertyKey, FilterType [] types) {
    
        return get(config, propertyKey, types, false);
    }
    
    public static NodeFilter get(JsonConfig props, 
            String propertyKey, FilterType [] types, boolean or) {
    
        return get(props, new Object[]{propertyKey}, types, or);
    }
    
    public static NodeFilter get(JsonConfig props, 
            Object [] propertyKey, FilterType [] types, boolean or) {
        
        ArrayList<NodeFilter> filters = new ArrayList<NodeFilter>();
        
        for(FilterType type:types) {
        
            NodeFilter filter = get(props, propertyKey, type);
            
            if(filter == null) continue;
            
            filters.add(filter);
        }
        
        return get(filters, or);
    }
    
    public static NodeFilter get(List<NodeFilter> filters, boolean or) {
        
        NodeFilter output = null;
        
        if(filters != null && !filters.isEmpty()) {

            if(filters.size() == 1) {
                output = filters.get(0);
            }else{
                if(or) {
                    OrFilter orFilter = new OrFilter();
                    orFilter.setPredicates(filters.toArray(new NodeFilter[0]));
                    output = orFilter;
                }else{
                    AndFilter andFilter = new AndFilter();
                    andFilter.setPredicates(filters.toArray(new NodeFilter[0]));
                    output = andFilter;
                }
            }
        }
        
        return output;
    }

    public static NodeFilter get(JsonConfig props, String propertyKey, FilterType type) {
        return get(props, new Object[]{propertyKey}, type);
    }
    
    public static NodeFilter get(JsonConfig props, 
            Object [] propertyKey, FilterType type) {
        
        NodeFilter output = null;
        
        switch(type) {
            case tagNameOnly: output = FilterFactory.newTagNameOnlyFilter(props, propertyKey);
                break;
            case tagName: output = FilterFactory.newTagNameFilter(props, propertyKey);
                break;
            case attributes : output = FilterFactory.newHasAttributesFilter(props, propertyKey);
                break;
            case attributesRegex: output = FilterFactory.newHasAttributesRegexFilter(props, propertyKey);    
                break;
            case hasIdAttribute: output = FilterFactory.newHasIdAttributeFilter(props, propertyKey);
                break;
            case textToReject: output = FilterFactory.newUnwantedTextFilter(props, propertyKey, type);
                break;
            case textToDisableOn: output = FilterFactory.newUnwantedTextFilter(props, propertyKey, type);
                break;
            case rejectNode: output = FilterFactory.newRejectNodeFilter(props, propertyKey);    
                break;
            case nodesToReject: 
            case nodesToAccept:
            case nodeTypesToReject:
            case nodeTypesToAccept:
                output = FilterFactory.newNodesFilter(props, propertyKey, type);
                break;
            default:
                throw new IllegalArgumentException(FilterType.class.getName()+": "+type);
        }
        
        return output;
    }
    
    public static NodeFilter newDefaultFilter(JsonConfig config, Object[] pathToValue) {
        
        return FilterFactory.get(config, pathToValue, getTagNameAndAttributesFilter(config, pathToValue));
    }
    
    private static NodeFilter newTagNameFilter(
            JsonConfig config, Object [] propertyKey) {

        propertyKey = config.getPath(propertyKey, "value");
        
        final String nodeName = config.getString(propertyKey);

        final String ID = propertyKey == null ? null : Arrays.toString(propertyKey);
        
XLogger.getInstance().log(Level.FINER, "PropertyKey: {0}, Node to create filter for: {1}", 
FilterFactory.class, ID, nodeName);        

        if(nodeName == null) return null;

final Level level = CapturerApp.getInstance().isTrackLog(propertyKey[0].toString()) ? Level.FINE : Level.FINER;                
        
        return new TagNameFilter(nodeName){

            @Override
            public boolean accept(Node node) {
                
                boolean accept = super.accept(node);
              
XLogger.getInstance().log(level, "{0}-TagnameFilter, Accept: {1}, Node: {2}", 
this.getClass(), ID, accept, node);
                return accept;
            }
            @Override
            public String toString() {
                return ID+"-TagNameFilter. Node name: "+nodeName;
            }
        };
    }

    private static NodeFilter newTagNameOnlyFilter(JsonConfig config, 
            Object [] propertyKey) {

        propertyKey = config.getPath(propertyKey, "value");
        
        final String nodeName = config.getString(propertyKey);

        final String ID = propertyKey == null ? null : Arrays.toString(propertyKey);
        
XLogger.getInstance().log(Level.FINER, "PropertyKey: {0}, Node to create filter for: {1}", 
FilterFactory.class, ID, nodeName);        

        if(nodeName == null) return null;

final Level level = CapturerApp.getInstance().isTrackLog(propertyKey[0].toString()) ? Level.FINE : Level.FINER;                
        
        // Note we use TagNameOnlyFilter here because we want
        // to accept Tags which do not have any attributes
        //
        return new TagNameOnlyFilter(nodeName){
            @Override
            public boolean accept(Node node) {
                boolean accept = super.accept(node);
XLogger.getInstance().log(level, "TagNameOnlyFilter, Accept: {0}, Node: {1}", 
this.getClass(), accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return ID+"-TagNameOnlyFilter. Node name: "+nodeName;
            }
        };
    }
    
    private static NodeFilter newHasAttributesRegexFilter(
            JsonConfig config, Object[] propertyKey) {

        propertyKey = config.getPath(propertyKey, Config.Extractor.attributesRegex);
        
        Map map = config.getMap(propertyKey);
        
        if(map == null) return null;
        
        return newHasAttributesRegexFilter(propertyKey, map); 
    }
    
    public static NodeFilter newHasAttributesRegexFilter(
            final Object [] propertyKey, final Map<String, String> attributes) {

        final String ID = propertyKey == null ? null : Arrays.toString(propertyKey);
        
XLogger.getInstance().log(Level.FINER, "Attributes to create filter for: {0}={1}", 
FilterFactory.class, ID, attributes);        

        NodeFilter [] predicates = new NodeFilter[attributes.size()];

        int i = 0;
        for(String key:attributes.keySet()) {
            Object value = attributes.get(key);
XLogger.getInstance().log(Level.FINEST, "@newAttributesRegexInstance. Key: {0}, Value: {1}", 
FilterFactory.class, key, value);
            predicates[i++] = new HasAttributesRegexFilter(key, value.toString());
        }

final Level level = CapturerApp.getInstance().isTrackLog(propertyKey[0].toString()) ? Level.FINE : Level.FINER;                
        
        AndFilter andFilter = new AndFilter(predicates){
            @Override
            public boolean accept(Node node) {
                
                boolean accept = super.accept(node);
                
XLogger.getInstance().log(level, "{0}-HasAttributesRegexFilter, Accept: {1}, Node: {2}", 
this.getClass(), ID, accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return ID+"-HasAttributesRegexFilter. attributes: "+attributes;
            }
        };
        return andFilter;
    }

    private static NodeFilter newHasAttributesFilter(
            JsonConfig config, Object[] propertyKey) {

        propertyKey = config.getPath(propertyKey, Config.Extractor.attributes);
        
        Map map = config.getMap(propertyKey);
        
        if(map == null) return null;

        return newHasAttributesFilter(propertyKey, map);
    }
    
    public static NodeFilter newHasAttributesFilter(
            final Object [] propertyKey, final Map<String, String> attributes) {

        final String ID = propertyKey == null ? null : Arrays.toString(propertyKey);
        
XLogger.getInstance().log(Level.FINER, "Attributes to create filter for: {0}={1}", 
FilterFactory.class, ID, attributes);        

        NodeFilter [] predicates = new NodeFilter[attributes.size()];

        int i = 0;
        for(String key:attributes.keySet()) {
            Object value = attributes.get(key);
XLogger.getInstance().log(Level.FINEST, "@newAttributesInstance. Key: {0}, Value: {1}", 
FilterFactory.class, key, value);
            predicates[i++] = new HasAttributeFilter(key, value.toString());
        }

final Level level = CapturerApp.getInstance().isTrackLog(propertyKey[0].toString()) ? Level.FINE : Level.FINER;                
        
        return new AndFilter(predicates){
            @Override
            public boolean accept(Node node) {
                
                boolean accept = super.accept(node);

XLogger.getInstance().log(level, "{0}-HasAttributesFilter, Accept: {1}, Node: {2}", 
this.getClass(), ID, accept, node);                

                return accept;
            }
            @Override
            public String toString() {
                return ID+"-HasAttributesFilter. attributes: "+attributes;
            }
        };
    }

    private static NodeFilter newHasIdAttributeFilter(
            JsonConfig config, Object[] propertyKey) {
        
        propertyKey = config.getPath(propertyKey, 
                Config.Extractor.attributes, Config.Extractor.id);
        
        final String idValue = config.getString(propertyKey);
        
        if(idValue == null) {
            return null;
        }

        final String ID = Arrays.toString(propertyKey);
        
XLogger.getInstance().log(Level.FINER, "{0}-HasIdAttribute. id value: {1}", 
FilterFactory.class, ID, idValue);        

final Level level = CapturerApp.getInstance().isTrackLog(propertyKey[0].toString()) ? Level.FINE : Level.FINER;                

        return new HasAttributeFilter("id", idValue){
            @Override
            public boolean accept(Node node) {
                boolean accept = super.accept(node);
XLogger.getInstance().log(level, "HasIdAttributeFilter. Accept: {0}, Node: {1}", 
this.getClass(), accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return ID + "-HasIdAttributeFilter. id="+idValue;
            }
        };
    }

    private static NodeFilter newUnwantedTextFilter(
            JsonConfig props, Object [] propertyKey, FilterType type) {
        
        propertyKey = props.getPath(propertyKey, type);
        
        final String [] toReject = getLowerCaseArray(props, propertyKey);

        final String ID = Arrays.toString(propertyKey);
        
XLogger.getInstance().log(Level.FINER, "{0}={1}", 
FilterFactory.class, ID,toReject==null?null:Arrays.asList(toReject));        

        if(toReject == null) return null;
        
final Level level = CapturerApp.getInstance().isTrackLog(propertyKey[0].toString()) ? Level.FINE : Level.FINER;                

        return new NodeFilter() {
            
            @Override
            public boolean accept(Node node) {
                
                String nodeValue = null;
                
                if (node instanceof Text) {
                    nodeValue = node.getText();
                }else if (node instanceof Remark) {
                    nodeValue = node.getText();
                } 
                
                boolean accept = true;
                if(nodeValue != null) {
                    accept = !contains(toReject, nodeValue.toLowerCase(), false);
                }
XLogger.getInstance().log(level, "UnwantedTextFilter, Accept: {0}, Node: {1}", 
this.getClass(), accept, node);                
                
                return accept;
            }    
            @Override
            public String toString() {
                return ID + "-UnwantedTextFilter. To reject: "+Arrays.toString(toReject);
            }
        };        
    }
    
    private static NodeFilter newRejectNodeFilter(
            JsonConfig props, final Object [] propertyKey) {

        int maxFiltersPerKey = props.getInt(Config.Extractor.maxFiltersPerKey);
        
        ArrayList<NodeFilter> filters = new ArrayList<NodeFilter>();
        
        final Object [] pathToValue = Arrays.copyOf(propertyKey, propertyKey.length + 1);
        
        final String ID = pathToValue == null ? null : Arrays.toString(pathToValue);

final Level level = CapturerApp.getInstance().isTrackLog(pathToValue[0].toString()) ? Level.FINE : Level.FINER;                
        
        for(int i=0; i<maxFiltersPerKey; i++) {

            pathToValue[pathToValue.length-1] = FilterType.rejectNode + "" + i;
            
            if(props.getString(pathToValue) == null) {
                continue;
            }
            
            NodeFilter filter = FilterFactory.newDefaultFilter(props, pathToValue);
            
XLogger.getInstance().log(Level.FINER, 
"Id: {0}, Filter: {1}", FilterFactory.class, ID, filter);            

            if(filter == null) {
                continue;
            }
            
            NotFilter notFilter = new NotFilter(filter){
                @Override
                public boolean accept(Node node) {
                    boolean b = super.accept(node);
XLogger.getInstance().log(level, "{0}-RejectNodeFilter, Accept: {1}, Node: {2}", 
        this.getClass(), ID, b, node);                    
                    return b;
                }
                @Override
                public String toString() {
                    return ID + "-RejectNodeFilter";
                }
            };
            
            filters.add(notFilter);
        }

        return FilterFactory.get(filters, false);
    }

    private static NodeFilter newNodesFilter(JsonConfig props, 
            Object [] propertyKey, final FilterType filterType) {
        
        propertyKey = props.getPath(propertyKey, filterType);
        
        final String [] arr = getLowerCaseArray(props, propertyKey);
        
        if(arr == null) return null;
        
        return newNodesFilter(arr, propertyKey, filterType);
    }
    
    public static NodeFilter newNodesFilter(final String [] nodeIds, 
            final Object[] propertyKey, final FilterType filterType) {

        final String ID = propertyKey == null ? null : Arrays.toString(propertyKey);

final Level level = CapturerApp.getInstance().isTrackLog(propertyKey[0].toString()) ? Level.FINE : Level.FINER;                

//XLogger.getInstance().log(Level.INFO, "= = = = = = = = = =  Level: {0}", FilterFactory.class, level.getName());

XLogger.getInstance().log(Level.FINER, "{0}={1}",
FilterFactory.class, ID, nodeIds==null?null:Arrays.toString(nodeIds));        
        
        return new NodeFilter() {
            
            @Override
            public boolean accept(Node node) {
                
                String nodeID = null;

                switch(filterType) {
                    case nodesToReject:
                    case nodesToAccept:    
                        if(node instanceof Tag) {
                            nodeID = ((Tag)node).getTagName();
                        }else if (node instanceof Text) {
                            nodeID = node.getText();
                        }else if (node instanceof Remark) {
                            nodeID = node.getText();
                        }else{
                            StringBuilder nodeStr = Util.appendTag(node, 50);
                            throw new IllegalArgumentException(nodeStr.toString());
                        }               
                        break;
                    case nodeTypesToReject:
                    case nodeTypesToAccept:   
                        if(node instanceof Tag) {
                            nodeID = "tag";
                        }else if (node instanceof Text) {
                            nodeID = "text";
                        }else if (node instanceof Remark) {
                            nodeID = "remark";
                        }else{
                            StringBuilder nodeStr = Util.appendTag(node, new StringBuilder(), false, -1);
                            throw new IllegalArgumentException(nodeStr.toString());
                        }                
                        break;
                    default:    
                        throw new IllegalArgumentException(Config.Extractor.class.getName()+": "+filterType);
                }
                
                boolean accept = true;
                switch(filterType) {
                    case nodesToReject:
                    case nodeTypesToReject:    
                        accept = !contains(nodeIds, nodeID.toLowerCase());
                        break;
                    case nodesToAccept:
                    case nodeTypesToAccept:    
                        accept = contains(nodeIds, nodeID.toLowerCase());
                        break;
                    default:
                        throw new IllegalArgumentException(Config.Extractor.class.getName()+": "+filterType);
                }
XLogger.getInstance().log(level, "{0}-{1}Filter, accept: {2}, Node: {3}", 
this.getClass(), ID, filterType, accept, node);                
                
                return accept;
            }    
            @Override
            public String toString() {
                return ID + "-"+filterType+"Filter. Nodes: " + Arrays.toString(nodeIds);  
            }
        };        
    }

    public static NodeFilter newOffsetFilter(final NodeVisitingFilterIx filter, 
            JsonConfig props, final String propertyKey) {

        final Integer offset = props.getInt(propertyKey, Config.Extractor.offset);
        if(offset == null) {
            return null;
        }
        
final Level level = CapturerApp.getInstance().isTrackLog(propertyKey) ? Level.FINE : Level.FINER;                
        
        NodeFilter offsetFilter = new NodeFilter() {
            @Override
            public boolean accept(Node node) {
                boolean accept = filter.getVisitedStartTags() >= offset;

XLogger.getInstance().log(level, "{0}-OffsetFilter, Accept: {1}, Index: {2}, Node: {3}", 
this.getClass(), propertyKey, accept, filter.getVisitedStartTags(), node);                
                return accept;
            }
            @Override
            public String toString() {
                return propertyKey+"-OffsetFilter. offset: "+offset;
            }
        };
        
        return offsetFilter;
    }
    
    public static NodeFilter newLengthFilter(final NodeVisitingFilterIx filter, 
            JsonConfig props, final String propertyKey) {
        
        final Integer offset = props.getInt(propertyKey, Config.Extractor.offset);
        
        final Integer length = props.getInt(propertyKey, Config.Extractor.length);
        if(length == null) {
            return null;
        }

final Level level = CapturerApp.getInstance().isTrackLog(propertyKey) ? Level.FINE : Level.FINER;                
        
        NodeFilter lengthFilter = new NodeFilter() {
            @Override
            public boolean accept(Node node) {
                int mOffset = offset == null ? 0 : offset;
                boolean accept = filter.getVisitedStartTags()- mOffset <= length;
XLogger.getInstance().log(level, "LengthFilter, Accept: {0}, Node: {1}", 
this.getClass(), accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return propertyKey+"-LengthFilter. offset: "+offset+", length: "+length;
            }
        };
        
        return lengthFilter;
    }
    
    private static String [] getLowerCaseArray(
            JsonConfig props, Object[] propertyKey) {

        List list = props.getList(propertyKey);
        
        if(list == null) {
            // Check if a global is available
            list = props.getList(propertyKey[propertyKey.length-1]);
        }
        
        String [] output = null;
        
        if(list != null) {
            
            output = new String[list.size()];
            
            for(int i=0; i<output.length; i++) {
                
                output[i] = list.get(i).toString().toLowerCase();
            }
        }
        
        return output;
    }
    
    private static boolean contains(String [] arr, String elem) {
        return contains(arr, elem, true);
    }
    private static boolean contains(String [] arr, String elem, boolean equals) {
        return indexOf(arr, elem, equals) != -1;
    }
    private static int indexOf(String [] arr, String elem, boolean equals) {
        if(arr == null || elem == null) throw new NullPointerException();
        synchronized(arr) {
            for(int i=0; i<arr.length; i++) {
                boolean found = false;
                if(equals) {
                    found = arr[i].equals(elem);
                }else{
                    found = elem.contains(arr[i]);
                }
XLogger.getInstance().log(Level.FINEST, "Found {0}, {1} in: {2}", 
        FilterFactory.class, found, arr[i], elem);
                if(found) {
                    return i;
                }
            }
        }
        return -1;
    }
}

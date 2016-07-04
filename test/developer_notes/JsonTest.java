package developer_notes;

import com.bc.json.config.DefaultJsonConfig;
import com.bc.json.config.DefaultJsonData;
import com.bc.json.config.JsonConfig;
import com.bc.json.config.JsonConfigIO;
import com.bc.json.config.JsonData;
import com.bc.manager.Filter;
import com.bc.manager.util.PropertiesExt;
import com.bc.ui.treebuilder.MapTreeBuilder;
import com.bc.ui.treebuilder.TreeBuilderFactoryImpl;
import com.bc.util.QueryParametersConverter;
import com.bc.util.JsonFormat;
import com.scrapper.AppProperties;
import com.scrapper.CapturerApp;
import com.scrapper.config.JsonType;
import com.scrapper.config.Config;
import com.scrapper.filter.FilterFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.json.simple.JSONValue;

/**
 * @(#)JsonTest.java   21-Dec-2013 16:55:54
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
public class JsonTest {

    private Set<String> nodeTypes;
    
    public JsonTest() {
        nodeTypes = new HashSet<String>(Arrays.asList(FilterFactory.NODE_TYPES));
    }
    
    private static URI getConfigDir(boolean remote) {
        String propName = remote ? 
                AppProperties.CONFIGS_DIR_REMOTE : 
                AppProperties.CONFIGS_DIR;
        
        // We trim the output, because we have encountered properties 
        // with leading or trailings paces
        String configsDir = AppProperties.getProperty(propName).trim();
        try{
            return new URI(configsDir);
        }catch(URISyntaxException e) {
            throw new RuntimeException("Failed to load: "+configsDir, e);
        }
    }
    
    public static void main(String [] args) {
        
        try{
            
if(true) {
System.out.println(new File(System.getProperty("user.home")+"/Documents/NetbeansProjects/scrapper/local").toURI());
return;
}            
            JsonTest test = new JsonTest();
            
            test.testSampleJson();
            
if(true)            return;

            
            final String configName = "nigeriacar24";
            final String fname = "/"+configName+".json";
            File file = new File(Paths.get(getConfigDir(false)).toFile(), fname);
            JsonConfigIO configIO = new JsonConfigIO();
            FileReader reader = new FileReader(file);
            JsonConfig config = configIO.load(configName, reader, new DefaultJsonConfig()); 
            
            String url = config.getString("url", "value");
System.out.println("URL: "+url);            
            
            FileWriter writer = new FileWriter(file);
            try{
                configIO.store(config, writer);
            }finally{
                writer.close();
            }
if(true) return;

//            test.createJsonConfigs();
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createJsonConfigs() throws Exception {
        
        Set<String> sitenames = CapturerApp.getInstance().getConfigFactory().getSitenames();

        for(String site:sitenames) {

            this.createJsonConfig(site);
        }
    }
    
    private void testSampleJson() throws Exception {

        JsonData jsonData = new DefaultJsonData();

        Map childMap = this.getSampleJson();
        JsonData childData = new DefaultJsonData(childMap);
        
        boolean testParent = false;
        if(testParent) {
            
            ArrayList listElement = new ArrayList();
            listElement.addAll(Arrays.asList(new String[]{"list_element_0", "list_element_1"}));

            Map parentMap = this.createObjectContainer();

            parentMap.put("map_element", childMap);
            parentMap.put("list_element", listElement);
            
            DefaultJsonData parentData = new DefaultJsonData(parentMap);
            jsonData = new DefaultJsonData(parentData, "map_element");
        }else{
            jsonData = childData;
        }

        jsonData.setObject(new Object[]{"level_0", "level_1", "level_1_child_a"}, "Sample Data A");
        
        Map map = jsonData.getMap(new Object[]{"level_0", "level_1"});
        
        map.put("level_1_child_b", "Sample Data B");
        
        jsonData.setObject(new Object[]{"level_0", "level_1", "level_1_child_c"}, "Sample Data C");
        
        jsonData.remove(new Object[]{"level_0", "level_1", "level_1_child_b"});

        jsonData.remove(new Object[]{"NotAlone", "List", "2"});
        
this.print(jsonData);

        Boolean b = jsonData.getBoolean(new Object[]{"NotAlone", "boolean"});
        // This doesn't exist
        Boolean b1 = jsonData.getBoolean(new Object[]{"NotAlone", "boolean1"});
        Integer i = jsonData.getInt(new Object[]{"NotAlone", "number"});
        // This doesn't exist
        Integer i1 = jsonData.getInt(new Object[]{"NotAlone", "number1"});
        List l = jsonData.getList(new Object[]{"NotAlone", "List"});
        Object [] arr = jsonData.getArray(new Object[]{"NotAlone", "List"});
        
        Object [] key = new Object[]{"NotAlone", "List", "2"};
        Object obj = (List)jsonData.getContainerForElementAt(2, key);
System.out.println(Arrays.toString(key)+".containerForElement(2): "+obj);
        key = new Object[]{"NotAlone", "number", 100};
        obj = jsonData.getContainerForElementAt(2, key);
System.out.println(Arrays.toString(key)+".containerForElement(2): "+obj);
        key = new Object[]{"NotAlone", "List", "1"};
        Set s = jsonData.getSiblingPaths(key);
        this.print(key, "siblingPaths", s);
        
        key = new Object[]{"NotAlone", "HasChildren", "GrandChild"};
        s = jsonData.getSiblingPaths(key);
        this.print(key, "siblingPaths", s);
        
        this.testJsonTree(jsonData.getRootContainer());
        
//        this.testJsonTree1(jsonData.getRootContainer());

        MapTreeBuilder treeBuilder = new TreeBuilderFactoryImpl().getMapInstance();

        Map.Entry jsonEntry = treeBuilder.getRootNode("ROOT", jsonData.getRootContainer());

        TreeNode node = treeBuilder.build(jsonEntry, null);

        this.testJSON(jsonData, node);
    }
    
    private JsonData createJsonConfig(String sitename) throws Exception {

        final String configsPath = this.getConfigDir(false).toString();
        
        final String oldDir = configsPath + "Old";
        
        final String newDir = configsPath;

        File fromDir = new File(oldDir + File.separatorChar + sitename);

        File toDir = new File(newDir);
        
        toDir.mkdirs();

        File [] fromFiles = fromDir.listFiles();
        
        if(fromFiles == null) {
System.err.println("= = = = = = = = = No files found in: "+fromDir);  
            return null;
        }
       
        Map outputMap = this.createObjectContainer();
        
        JsonData jsonData = new DefaultJsonData(outputMap);
        
        for(File from:fromFiles) {

            PropertiesExt src = this.getSource(from);
            
            Enum [] arr = this.getEnum(sitename, from.getPath());

            if(from.getPath().endsWith("keys.properties")) {
                
                this.updateKeys(src, outputMap);
                
            }else{
            
                if(arr == null) {
                    continue;
                }

                this.toJson(arr, src, outputMap);
            }
            
            this.testEquality(arr, src, jsonData);
            
//System.out.println("Success: .."+fromDir.getName()+"\\"+from.getName());            
        }
        
//        this.testJSON(jsonData);
            
        File tgt = new File(toDir.getPath()+"\\"+sitename+".json");
            
        String jsonText = JSONValue.toJSONString(outputMap);  

        FileWriter writer = null;
        try{
            writer = new FileWriter(tgt, false);
            writer.write(jsonText);
        }finally{
            if(writer != null) writer.close();
        }
        
        return jsonData;
    }
    
    private void testEquality(Enum [] arr, PropertiesExt props,
            JsonData jsonObject) throws Exception{

//System.out.println("Testing: "+props.getPath());        
        
        JsonType jt = new JsonType();
        
        for(String name:props.stringPropertyNames()) {
            
            Class type = jt.getType(arr, name);
            
            Object obj0 = this.toJsonStandard(props, name, type);
            
            // Note this
            //
            Object [] pathToValue = this.getPathToValue(props, name);
            
            Object obj1 = jsonObject.getObject(pathToValue);
            
boolean success;            
if(obj0 == null) {             
    success = obj1 == null;
}else{
    if(obj1 == null) {
        success = false;
    }else{
        success = obj0.toString().equals(obj1.toString());
    }
}
if(!success) {
System.out.println("Equals: "+success+", Property: "+name+", Values: "+obj0+" AND "+obj1);    
}
        }
    }
    
    private Object [] getPathToValue(PropertiesExt props, String pname) {
        String [] path = pname.split("\\.");
        boolean addValue;
        if(this.isSingle(path)) {
            addValue = this.singleHasNested(props, pname);
        }else {
            String pnamePart = path[path.length-1];
            addValue = this.partHasNested(props, pname, pnamePart);
        }
        if(addValue) {
            Object [] arr = Arrays.copyOf(path, path.length + 1);
            arr[arr.length-1] = "value";
            return arr;
        }else{
            return path;
        }
    }
    
    private void updateKeys(PropertiesExt src, Map tgt) throws Exception {

        int selection = this.showWarning();
        
        if(selection != JOptionPane.YES_OPTION) {
            return;
        }
        
        Map m = (Map)tgt.get("keys");
        if(m == null) {
            m = this.createObjectContainer();
            tgt.put("keys", m);
        }
        
        for(String pname:src.stringPropertyNames()) {

            this.doAdd(m, pname, src.getProperty(pname));
        }
    }
    
    private Enum [] getEnum(String sitename, String path) {
        if(path.endsWith("extractor.properties")) {
            return Config.Extractor.values();
        }else if(path.endsWith(sitename+".properties")) {
            return Config.Site.values();
        }else if(path.endsWith("formatter.properties")) {
            return Config.Formatter.values();
        }else if(path.endsWith("login.properties")) {
            return Config.Login.values();
        }else if(path.endsWith("keys.properties")) {
            return Config.Keys.values();
        }else{
System.err.println("Unexpected file: "+path);
            return null;
        }
    }
    
    private void toJson(Enum [] arr, PropertiesExt src, Map tgt) throws Exception{
        
        Set<String> pnames = src.stringPropertyNames();
        
        JsonType jt = new JsonType();
        
        for(String pname:pnames) {
            
            Class type = jt.getType(arr, pname);

            Object value = this.toJsonStandard(src, pname, type);
            
            this.update(src, tgt, pname, value);
        }
    }
    
    private PropertiesExt getSource(File from) throws Exception {
        PropertiesExt src = new PropertiesExt();
        
        FileReader reader = null;
        try{
            reader = new FileReader(from);
            src.load(reader);
        }finally{
            if(reader != null) reader.close();
        }
//System.out.println("Loaded: "+src);        
        return src;
    }
    
    private Object toJsonStandard(PropertiesExt src, 
            String pname, Class type) throws Exception {
        
        Class [] parameterTypes; 
        if(type == String[].class) {
            parameterTypes = new Class[]{String.class, String.class};
        }else if(type == Number.class) {
            parameterTypes = new Class[]{String.class, int.class};
        }else if(type == Boolean.class) {
            parameterTypes = new Class[]{String.class, boolean.class};
        }else{
            parameterTypes = new Class[]{String.class};
        }
        
        Object [] parameters; 
        if(type == String[].class) {
            parameters = new Object[]{pname, ",,,"};
        }else if(type == Number.class) {
            parameters = new Object[]{pname, Integer.MIN_VALUE};
        }else if(type == Boolean.class) {
            parameters = new Object[]{pname, false};
        }else{
            parameters = new Object[]{pname};
        }
        
        Method method = src.getClass().getMethod(this.getMethodName(type), parameterTypes);
        
        Object result = null;
        try{
            result = method.invoke(src, parameters);
        }catch(Exception e) {
System.err.println(method.getName()+(parameters==null?"null":Arrays.toString(parameters)));            
            throw e;
        }
        
        if(result == Integer.valueOf(Integer.MIN_VALUE)) {
            result = null;
        }
        
        if(result instanceof String[]) {
            String[] arr = (String[])result;
            result = new ArrayList<String>(Arrays.asList(arr));    
        }

        if(result != null && type == Map.class) {
            QueryParametersConverter c = new QueryParametersConverter(
                    true, Integer.MAX_VALUE, ",,,");
            String s = result.toString();
            result = c.reverse(s);
        }

        return result;
    }

    private Object toPreviousStandard(Map json, 
            Object [] pathToValue) throws Exception {
        
        JsonData data =  new DefaultJsonData(json);
        
        Object result = data.getObject(pathToValue);
        
        if(result instanceof List) {
            result = ((List)result).toArray(new String[0]);
        }

        if(result instanceof Map) {
            QueryParametersConverter c = new QueryParametersConverter(
                    true, Integer.MAX_VALUE, ",,,");
            result = c.convert((Map)result);
        }

        return result;
    }
    
    private int showWarning() {
        StringBuilder builder = new StringBuilder("<html><b>Dude!!! This is dangerous.</b><br/>");
        builder.append("You are trying to edit the json configs with property values<br/>");
        builder.append("from the old files. VERY DANGEROUS. This action could reverse<br/>");
        builder.append("updates made to the json file. DUDE !!! You've been warned</html>");
        JLabel message = new JLabel(builder.toString());
        return JOptionPane.showConfirmDialog(null, message, "Continue?", JOptionPane.YES_NO_OPTION);
    }
    
    private void update(PropertiesExt src, Map tgt, 
            String pname, Object result) throws Exception{
        
//System.out.println("To add: "+pname+"="+result);        

        int selection = this.showWarning();
        
        if(selection  != JOptionPane.YES_OPTION) {
            return;
        }
        
        if(result == null) {
            return;
        }
        
        // url.mappings.table is broken down to 
        // url{ mappings{ table: } }
        String [] parts = pname.split("\\.");
        
//System.out.println("Property parts: "+parts==null?null:Arrays.toString(parts));

        if(this.isSingle(parts)) {
            this.addSingle(src, tgt, pname, result);
            return;
        }
        
        Map val = tgt;
        
        for(int i=0; i<parts.length; i++) {
            
            String key = parts[i];
                
            if(i < parts.length-1) {

                try{
                    Map m = (Map)val.get(key);
                    
                    if(m == null) {
                        m = new TreeMap();
//System.out.println("Added new map for: "+key);
                        val.put(key, m);
                    }
                
                    val = m;
                
                }catch(Exception e) {
System.err.println("Index: "+i+ ", Key: "+key+", current Map: "+val);
                    throw e;
                }
                
            }else{
                
                this.addPart(src, val, pname, key, result);
            }
        }
//System.out.println("After adding, size: "+tgt.size());        
    }
    
    private boolean isSingle(String [] parts) {
        return parts == null || parts.length < 2;
    }
    
    private boolean matches(String pnamePart) {
        for(String nodeType:nodeTypes) {
            for(int i=0; i<20; i++) {
                if(pnamePart.equals(nodeType)) {
                    return true;
                }
                if(pnamePart.equals(nodeType+i)){
                    return true;
                }
                if(pnamePart.equals(nodeType+i+"Value")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * For: url.mappings.type=abc=2 property name has length = 3
     * This method is for only property names with length 1 e.g targetNode0=<br/>
     */
    private void addSingle(PropertiesExt src, Map tgt, String pname, Object result) {
        if(this.singleHasNested(src, pname)) {
            this.addNested(tgt, pname, result);
        }else{
            this.doAdd(tgt, pname, result);
        }
    }

    private void addPart(PropertiesExt src, Map tgt, 
            String pnameOriginal, String pnamePart, Object result) {
        if(this.partHasNested(src, pnameOriginal, pnamePart)) {
            this.addNested(tgt, pnamePart, result);
        }else{
            this.doAdd(tgt, pnamePart, result);
        }
    }
    
    /**
     * For: url.mappings.type=abc=2 property name has length = 3
     * This method is for only property names with length 1 e.g targetNode0=<br/>
     */
    private boolean singleHasNested(PropertiesExt props, String pname) {
        if(pname.equals("url") || this.matches(pname)) {
            return true;
        }
        Set<String> names = props.stringPropertyNames();
        String s = pname+".";
        for(String name:names) {
            if(name.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean partHasNested(PropertiesExt props, String pnameOriginal, String pnamePart) {
        if(pnamePart.startsWith(FilterFactory.START_AT) || 
                pnamePart.startsWith(FilterFactory.STOP_AT) ||
                pnamePart.equals("prefix") || pnamePart.equals("suffix")) {
            return true;
        }
        Set<String> names = props.stringPropertyNames();
        String s = "."+pnamePart+".";
        for(String name:names) {
            if(name.startsWith(pnameOriginal+".") && name.contains(s)) {
                return true;
            }
        }
        return false;
    }
    
    private void addNested(Map tgt, String pname, Object result) {
        //targetNode=DIV becomes targetNode.value=DIV
        // Later we can do this: 
        //targetNode{
        //  "value":"DIV",
        //  "columns":[1, 2, 3]
        //}
        Map m = (Map)tgt.get(pname);
        if(m == null) {
            m = this.createObjectContainer();
        }
        this.doAdd(m, "value", result);

        tgt.put(pname, m);
    }
    
    private void doAdd(Map m, String key, Object val) {
//System.out.println("Adding: "+key+"="+val+" to: "+m);        
        if(m.get(key) != null) {
            throw new UnsupportedOperationException("Key: "+key+" is already in Map: "+m);
        }
        m.put(key, val);
    }
    
    private String getMethodName(Class type) {
        if(type == String.class) {
            return "getProperty";
        }else if(type == Boolean.class) {
            return "getBoolean";
        }else if(type == String[].class) {
            return "getArray";
        }else if(type == Number.class) {
            return "getInt";
        }else if(type == Map.class){
            // Note this... will have to be converted later
            return "getProperty";
        }else{    
            throw new IllegalArgumentException("Unexpected type: "+type);
        }
    }
    
    

    private void testJsonTree(final Map jsonObject) {
        
        final MapTreeBuilder treeBuilder = new TreeBuilderFactoryImpl().getMapInstance();
        
        TreeNode treeNode = treeBuilder.build(jsonObject);
        
        JTree tree = new JTree(treeNode);

        tree.setRootVisible(false);
        
        JScrollPane treeUI = new javax.swing.JScrollPane(tree);
        treeUI.setSize(300, 300);
        
        JOptionPane.showMessageDialog(null, treeUI);
    }

    private boolean equals(Object [] path0, Object [] path1) {
        if(path0 ==  path1) {
            return true;
        }
        if(path0.length != path1.length) {
            return false;
        }
        for(int i=0; i<path0.length; i++) {
            if(path0[i] == path1[i]) {
                continue;
            }
            if(!path0[i].toString().equals(path1[i].toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param root
     * @param path Must be a path to a leaf
     * @return 
     */
    private DefaultMutableTreeNode getTreeNode(TreeNode root, Object [] path) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)root;
        Enumeration en = treeNode.breadthFirstEnumeration();
        while(en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
            Object [] id = this.getId(node);
            if(id == null) {
                continue;
            }
            if(this.equals(id, path)) {
                return node;
            }
        }
        return null;
    }
    
    public Object [] getId(DefaultMutableTreeNode node) {
        Object [] path = node.getPath();
        Object [] id;
        if(path.length < 2) {
            id = null;
        }else if (path.length == 2) {
            // ["", "level_0"] becomes ["level_0"]
            id = new Object[1];
            id[0] = path[1].toString();
        }else{
            // ["", "level_0", "level_1"] becomes ["level_0", "level_1"] (non leaf)
            // ["", "level_0", "level_1"] becomes ["level_0"] (leaf)
            int len = node.isLeaf() ? path.length-2 : path.length-1;
            id = new Object[len];
            for(int i=0; i<id.length; i++) {
                id[i] = path[i+1].toString();
            }
        }
        return id;
    }

    private Collection<Object[]> getFullPaths(TreeNode root, Object [] path) {
        HashSet<Object[]> paths = new HashSet<Object[]>();
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)root;
        Enumeration en = treeNode.breadthFirstEnumeration();
        DefaultJsonData jd = new DefaultJsonData();
        while(en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
            Object [] id = this.getId(node);
            if(id == null) {
                continue;
            }
            boolean isSub = jd.isSubPath(id, path);
//System.out.println("Is sub: "+isSub+", Parent: "+Arrays.toString(id));            
            if(isSub) {
                paths.add(id);
            }
        }
        return paths;
    }

    private Collection<Object []> getPaths(TreeNode root, Filter<TreeNode> f) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)root;
        Enumeration en = treeNode.breadthFirstEnumeration();
        ArrayList<Object[]> paths = new ArrayList<Object[]>();
        while(en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
            if(f == null || f.accept(node)) {
                paths.add(node.getPath());
            }
        }
        return paths;
    }

    private Collection<Object []> getFullPaths(TreeNode root) {
        return getPaths(root, new Filter<TreeNode>(){
            @Override
            public boolean accept(TreeNode e) {
                return e.isLeaf();
            }
        });
    }
    
    private void testJSON(JsonData jsonData, TreeNode jsonNode) throws Exception{

//print("Full paths: ", jsonData.getFullPaths());        

//print("Full paths2: ", this.getFullPaths(jsonNode));        

//print("Paths: ", jsonData.getPaths());        

//print("Paths2: ", this.getPaths(jsonNode));        
                
        Object [] samplePath = new Object[2];
        
        Map root = jsonData.getRootContainer();

        for(Object key:root.keySet()) {
            
            Object val = root.get(key);
            
            if(val instanceof Map) {
                Iterator iter = ((Map)val).keySet().iterator();
                if(iter.hasNext()) {
                    samplePath[0] = key;
                    samplePath[1] = iter.next();
                    break;
                }
            }
        }
System.out.print("\nSample path: "+Arrays.toString(samplePath));        

//        DefaultMutableTreeNode sampleNode = this.getTreeNode(jsonNode, samplePath);
//System.out.print("\nSample path2: "+Arrays.toString(sampleNode.getPath()));        
        
print(samplePath, "fullPaths", jsonData.getFullPaths(samplePath));        

System.out.println();
print(samplePath, "fullPaths2", this.getFullPaths(jsonNode, samplePath));        

print(samplePath, "paths", jsonData.getPaths(samplePath));        

print(samplePath, "siblingPaths", jsonData.getSiblingPaths(samplePath));        
    }
    
    private void print(JsonData jsonData) {
        Map root = jsonData.getRootContainer();
        Set rootKeys = root.keySet();
System.out.println("JSONData.root.keys: "+rootKeys);        
        JsonFormat string = new JsonFormat(true);
System.out.println("JSONData.root: "+string.toJSONString(root));        
    }
    
    private void print(Object [] key, String id, Collection<Object []> paths) {
        print(Arrays.toString(key)+"."+id, paths);
    }

    private void print(String id, Collection<Object []> paths) {
System.out.println("\n"+id+": "+paths.size());
        for(Object [] path:paths) {
System.out.println(Arrays.toString(path));            
        }
    }
    
//    @Override
    public List creatArrayContainer() {
        return new ArrayList();
    }

//    @Override
    public Map createObjectContainer() {
        return new TreeMap();
    }

    private Map getSampleJson() {
        Map root = this.createObjectContainer();
        Map children = this.createObjectContainer();
        List list = this.creatArrayContainer();
        Map grandChildren = this.createObjectContainer();
        root.put("Alone", "I don't have any children");
        root.put("NotAlone", children);
        children.put("boolean", Boolean.TRUE);
        children.put("number", 100);
        children.put("List", list);
        children.put("HasChildren", grandChildren);
        list.add("1"); list.add("2"); list.add("3");
        grandChildren.put("GrandChild", "I am a grand child");
        return root;
    }
}

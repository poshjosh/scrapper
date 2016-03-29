package com.scrapper.config;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.DefaultCapturerContext;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.logging.Level;

/**
 *
 * @author Josh
 */
public abstract class ScrapperConfigFactory extends JsonConfigFactory {
    
    protected ScrapperConfigFactory() { }
    
    @Override
    protected JsonConfig createNew() {
        JsonConfig config = new ScrapperConfig();
        return config;
    }
    
    @Override
    public JsonConfigFactory newSyncFactory() {
        
        final boolean isRemote = !this.isRemote();
        
        JsonConfigFactory factory = new ScrapperConfigFactory(){
            @Override
            public boolean isRemote() {
                return isRemote;
            }
            @Override
            protected URI getConfigDir() {
                return ScrapperConfigFactory.this.getConfigDir();
            }
            @Override
            protected String getDefaultConfigName() {
                return ScrapperConfigFactory.this.getDefaultConfigName();
            }
        };
        factory.setSearch(this.isSearch());
        factory.setUseCache(this.isUseCache());
        return factory;
    }
    
    public CapturerContext getContext(String name) {
        
        return this.getContext(this.getConfig(name));
    }

    public CapturerContext getContext(JsonConfig config) {
        
        if(config == null) {
            throw new NullPointerException();
        }
        
        final String className = getClassName(config.getName());
        
XLogger.getInstance().log(Level.FINE, "Class name: {0}", this.getClass(), className);

        CapturerContext output = null;
        try{
            
            Class aClass = Class.forName(className);
            
            output = (CapturerContext)aClass.getConstructor(JsonConfig.class).newInstance(config);
            
        }catch(ClassNotFoundException e) {
            
            output = newDefaultContext(config);
            
        }catch(NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            
            XLogger.getInstance().log(Level.WARNING, "Failed to create: "+className, this.getClass(), e);
        }
        
        return output;
    }
    
    protected CapturerContext newDefaultContext(JsonConfig config) {
        return new DefaultCapturerContext(config);
    }
    
    private String getClassName(String sitename) {
        // Any class in the package will do
        // Format: {packageName}.{siteName}Context e.g. com.abc.def.GoogleContext
        //
        String packageName = DefaultCapturerContext.class.getPackage().getName();
        StringBuilder builder = new StringBuilder(packageName);
        builder.append('.').append(toTitleCase(sitename)).append("Context");
        return builder.toString();
    }
    
    private Object toTitleCase(String arg0) {
        char ch0 = arg0.charAt(0);
        if(Character.isUpperCase(ch0)) return arg0;
        return Character.toTitleCase(ch0) + arg0.substring(1);
    }
}

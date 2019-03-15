package com.dawninfotek.logplus.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

public class LogPlusProperties {

	/**
	 * LogPlusproperties 
	 * customized java properties loading methodology
	 */
	private final LinkedHashMap<Object, Object> map = new LinkedHashMap<Object, Object>();
	
	public LogPlusProperties() {
		
	}

    public Object get(Object key) {
    	return map.get(key);
    }
 
    public Object get( Object key, Object defaultValue ) {
        Object s = map.get( key );
        if( s != null )
            return s;
        return defaultValue;
    }
 
    /**
     * If the key does not exist then add new entry to the end of the
     * linked list. If the key does exist then replace the entry at
     * its current location in the list.
     * @param key
     * @param value
     * @return previous value of the key (which may be null)
     */
    
    public Object set( Object key, Object value ) {
        Object s = map.get( key );
        map.put( key, value );
        return s;
    }
 
    public void load( String fname ) throws IOException {
        File file = new File( fname );
        if( file.exists() && file.isFile() )
        {
            InputStream is = null;
            try
            {
                load(is = new FileInputStream(file));
            }
            catch(IOException e)
            {
                throw e;
            }
            finally
            {
                if( is != null )
                    { try{ is.close(); }catch( IOException ex ){ ex.printStackTrace(); }
                }
            }
        }
    }
 
    public void load( InputStream is ) throws IOException {
        load( new InputStreamReader( is ) );
    }
 
    /**
     * load from reader
     * @param reader
     * @throws IOException
     */
	public void load( Reader reader ) throws IOException {
        String s;
        String sectionName = "";
        int lineNumber = 0;
        BufferedReader in = new BufferedReader(reader);
        while((s = in.readLine()) != null ) {
        	if(s.isEmpty() || s.startsWith("#")) {
        		continue;
        	}
        	if(s.startsWith("[")) {
        		sectionName = StringUtils.trim(s.substring(s.indexOf("[") + 1, s.indexOf("]")));
                LinkedHashMap<String, String> sectionMap = new LinkedHashMap<String, String>();
                map.put(sectionName, sectionMap);
        	}else {
        		if(s.indexOf("=") > 0) {
            		String kv[] = s.split("=", 2);
            		if(!sectionName.isEmpty()) {
            			Object result = get(sectionName);
            			if(result instanceof Map) {
                			@SuppressWarnings("unchecked")
							LinkedHashMap<String, String> sMap = (LinkedHashMap<String, String>) result;
                			sMap.put(StringUtils.trim(kv[0]), StringUtils.trim(kv[1]));
            			}
            		} else {
            			set(StringUtils.trim(kv[0]), StringUtils.trim(kv[1]));
            		}
        		}else {
        			System.out.println("invalid format line: " + lineNumber);
        		}
        	}
        	lineNumber++;
        }
        reader.close();
    }

    public String toString() {
    	String result = "";
    	for(Map.Entry<Object, Object> entry: map.entrySet()) {
    		Object value = entry.getValue();
    		if(value instanceof Map) {
    			@SuppressWarnings("unchecked")
				LinkedHashMap<String, String> sMap = (LinkedHashMap<String, String>) value;
    			for(Map.Entry<String, String> sEntry: sMap.entrySet()) {
    				result += sEntry.getKey() + "=" + sEntry.getValue() + ", ";
    			}
    		}else {
        		result += entry.getKey() + "=" + value + ", ";
    		}
    	}
        return result;
    }
    
    public boolean isEmpty() {
    	return map.isEmpty();
    }
    
    public Iterable<Object> keySet() {
    	return map.keySet();
    }
    
    public Iterable<Entry<Object,Object>> entrySet(){
    	return map.entrySet();
    }
}

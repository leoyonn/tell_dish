/**
 * @(#)RableDefine.java, 2012-11-01.
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.rable;

import java.util.HashMap;
import java.util.Map;

import sanji.rable.RedisConf.RableDB;

/**
 * @author leo
 */
public class RableDefine {
    private RableDB db;
    
    private String[] colNames;

    private Class<?>[] colClasses;

    private Map<String, Class<?>> colClassMap;

    /**
     * 鏋勯�鍑芥暟
     * 
     * @param name
     *            Rable鍚�     * @param colNames
     *            鍒楀悕鏁扮粍
     * @param colClasses
     *            鍒楃被鍨嬫暟缁�     */
    public RableDefine(RableDB db, String[] colNames, Class<?>[] colClasses) {
        if (db == null || colNames.length < 1 || colNames.length != colClasses.length) {
            throw new IllegalArgumentException("Rable[" + db + "]'s columns define error");
        }
        this.colNames = colNames;
        this.colClasses = colClasses;
        this.colClassMap = new HashMap<String, Class<?>>();
        for (int i = 0; i < colNames.length; i++) {
            this.colClassMap.put(colNames[i], colClasses[i]);
        }
    }

    public String[] getColNames() {
        return colNames;
    }

    public Class<?>[] getColClasses() {
        return colClasses;
    }
    
    public Class<?> getColClass(String col){
        return colClassMap.get(col);
    }
    
    public boolean containsCol(String col){
        return colClassMap.containsKey(col);
    }

    public String getName() {
        return db.name();
    }

    public int getColumnNum() {
        return colNames.length;
    }
    
    public int getDB() {
        return db.db();
    }
}

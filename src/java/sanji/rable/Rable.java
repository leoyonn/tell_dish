/**
 * @(#)Rable.java, 2011-11-12.
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.rable;

import java.util.Set;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import sanji.utils.Logger;

/**
 * Table on Redis <br>
 * 瀹為檯瀛樺偍鐨剅ealkey涓�name.key"鐨勫舰寮忥紝鍏朵腑name鍙负Rable鐨勮〃鍚嶆垨TableSpace+琛ㄥ悕 <br>
 * 
 * @author leo
 */
public class Rable {
    /** 瀛樺偍鏁版嵁鐨刢lient */
    private BinaryJedis jedis;

    /** 瀵硅〃鐨勬弿杩�*/
    protected RableDefine define;

    private static final String DATA_PREFIX_SPLITTER = "#鍗�";

    /**
     * 鏋勯�鍑芥暟<br>
     * 鍒濆鍖栧垪鍙婄被鍨嬬殑瀵瑰簲鍏崇郴锛屽垪绫诲瀷鍦ㄥ彇鍊兼椂decode鐢�     * 
     * @param name
     *            鐢ㄤ互浣滀负key鐨勫墠缂�紝鍙互鏄疶ableName鎴朤ableSpace+TableName
     * @param colNames
     *            鍒楀悕
     * @param colClasses
     *            鍒椾腑value鐨勭被鍨嬶紝<b>鍙互涓簀ava.util.ArrayList/鏁扮粍</b>
     * @param jedis
     *            杩炴帴Redis鐨刢lient
     */
    public Rable(RableDefine define) {
        this.define = define;
        this.jedis = new Jedis(RedisConf.host(), RedisConf.port());
        this.jedis.select(define.getDB());
    }

    public RableDefine getDefine() {
        return define;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * 璁剧疆鍗曚釜column鐨剉alue
     * 
     * @param key
     * @param column
     * @param value
     */
    public void setColumnValue(Object key, String column, Object value) {
        jedisSetValue(key, column, value);
    }

    /**
     * 鑾峰彇鍗曚釜column鐨剉alue
     * 
     * @param key
     * @param column
     * @return
     */
    public Object getColumnValue(Object key, String column) {
        return jedisGetValue(key, column, define.getColClass(column));
    }

    /**
     * 璁剧疆澶氫釜column鐨剉alue
     * 
     * @param key
     * @param columns
     * @param values
     */
    public void setColumnValues(Object key, String[] columns, Object[] values) {
        for (int i = 0; i < columns.length; i++) {
            jedisSetValue(key, columns[i], values[i]);
        }
    }

    /**
     * 璁剧疆澶氫釜column鐨剉alue<br>
     * 鍙傛暟鏍煎紡锛�columnName1, columnValue1, columnName2, columnValue2 ...
     * 
     * @param key
     * @param columnsAndValues
     */
    public void setColumnValues(Object key, Object... columnsAndValues) {
        if ((columnsAndValues.length & 1) != 0) {
            throw new IllegalArgumentException(
                    "usage: column1,value2,column2,value2 ...");
        }
        for (int i = 0; i < columnsAndValues.length; i += 2) {
            Object column = columnsAndValues[i];
            jedisSetValue(key, (String) column, columnsAndValues[i + 1]);
        }
    }

    /**
     * 鑾峰彇澶氫釜column鐨剉alue
     * 
     * @param key
     * @param columns
     * @return
     */
    public Object[] getColumnValues(Object key, String... columns) {
        Object[] r = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            r[i] = jedisGetValue(key, columns[i], define.getColClass(columns[i]));
        }
        return r;
    }

    /**
     * 璁剧疆涓�釜column鐨勫�
     * 
     * @param key
     * @param column
     * @param value
     */
    private void jedisSetValue(Object key, String column, Object value) {
        if (!define.containsCol(column)) {
            throw new IllegalArgumentException("column not exsist in this table.");
        }
        if (!value.getClass().equals(define.getColClass(column))) {
            throw new IllegalArgumentException("column value's type is not what was set.");
        }
        jedis.hset(groundKey(key), Serializer.encode(column), Serializer.encode(value));
    }

    /**
     * 璇诲彇涓�釜key涓竴涓猚olumn鐨勫�
     * 
     * @param key
     * @param column
     * @param clazz
     * @return
     */
    private Object jedisGetValue(Object key, String column, Class<?> clazz) {
        if (!define.containsCol(column)) {
            throw new IllegalArgumentException(
                    "column not exsist in this table.");
        }
        byte[] result = jedis.hget(groundKey(key), Serializer.encode(column));
        if (result == null) {
            return null;
        }
        return Serializer.decode(result, clazz);
    }

    /**
     * 鎷兼垚鐪熸鐢ㄦ埛璇诲啓Redis鐨刱ey锛屽嵆鍓嶉潰鎷间笂琛ㄥ悕
     * 
     * @param key
     * @return
     */
    private byte[] groundKey(Object key) {
        return Serializer.mEncode(define.getName() + DATA_PREFIX_SPLITTER, key);
    }

    /**
     * 涓�釜key瀵瑰簲鐨勬暟鎹槸鍚﹀瓨鍦�     * 
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return jedis.exists(groundKey(key));
    }

    /**
     * 娓呯┖琛ㄤ腑鐨勬暟鎹紝澶嶆潅搴︿綆O(n)锛屽敖閲忓彧鍦╠ebug鏃舵厧閲嶆墽琛�     */
    public int clear() {
        byte[][] keys = getAllGroundKeys();
        if (keys == null) {
            return 0;
        }
        jedis.del(keys);
        Logger.severe("{Rache.clear} deleting rable [" + define.getName() + "]...done!");
        return keys.length;
    }

    /**
     * 鑾峰彇鎵�湁鐨刱ey锛屽鏉傚害O(n)锛屽敖閲忓彧鍦╠ebug鏃舵厧閲嶆墽琛�     * 
     * @return
     */
    public byte[][] getAllGroundKeys() {
        Set<byte[]> keys = jedis.keys(Serializer.encode(define.getName() + DATA_PREFIX_SPLITTER + "*"));
        Logger.info("{Rache.getAll} In rable [" + define.getName() + "], there all "
                + keys.size() + " keys.");
        if (keys.size() == 0) {
            return null;
        }
        return keys.toArray(new byte[keys.size()][]);
    }

    /**
     * 鍒犻櫎鎵�湁db锛屼粎娴嬭瘯鏃朵娇鐢紒锛�     */
    public void deleteAllRables() {
        Logger.severe("{Rache.delall} deleteing all keys from current DB!");
        jedis.flushDB();
    }

    public void synchronize2Snapshot() {
        jedis.save();
    }
}

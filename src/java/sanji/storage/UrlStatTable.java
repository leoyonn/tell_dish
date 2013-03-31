/**
 * @(#)UrlStatTable.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.storage;

import redis.clients.jedis.BinaryJedis;
import sanji.crawler.UrlStat;
import sanji.rable.AbstractTable;
import sanji.rable.RedisConf.RableDB;

/**
 * @author leo
 */
public class UrlStatTable extends AbstractTable {
    BinaryJedis jedis = RableDB.URL_STAT.getJedis();

    public void add(UrlStat stat) {
        jedis.set(enc(stat.url()), enc(stat));
    }
    
    public UrlStat get(String url) {
        byte[]bytes = jedis.get(enc(url));
        if (bytes == null) {
            return null;
        }
        return dec(bytes, UrlStat.class);
    }
}

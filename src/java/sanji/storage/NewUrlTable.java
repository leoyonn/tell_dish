/**
 * @(#)NewUrlTable.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.storage;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.BinaryJedis;
import sanji.base.Conf;
import sanji.rable.AbstractTable;
import sanji.rable.RedisConf.RableDB;

/**
 * @author leo
 */
public class NewUrlTable extends AbstractTable {
    BinaryJedis jedis = RableDB.NEW_URL.getJedis();

    public void add(String dish, String url) {
        jedis.sadd(enc(dish), enc(url));
    }

    public String popUrl(String dish) {
        byte[] dishBytes = enc(dish);
        byte[] urlBytes = jedis.spop(dishBytes);
        if (urlBytes == null) {
            jedis.del(dishBytes);
            return null;
        }
        return dec(urlBytes, String.class);
    }

    public Set<String> allUrls(String dish) {
        Set<byte[]> res = jedis.smembers(enc(dish));
        if (res == null) {
            return null;
        }
        Set<String> urls = new HashSet<String>((int) (res.size() / 0.75 + 1));
        for (byte[] bytes: res) {
            urls.add(dec(bytes, String.class));
        }
        return urls;
    }
    
    public static void main(String[]args) {
        Conf.init(".");
        NewUrlTable table = new NewUrlTable();
        table.add("111", "111");
        table.add("111", "222");
        table.add("111", "333");
        System.out.println(table.allUrls("111"));
    }
}

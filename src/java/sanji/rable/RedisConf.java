/**
 * @(#)RedisConf.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.rable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import redis.clients.jedis.BinaryJedis;
import sanji.utils.Logger;

/**
 * configure for redis
 * @author leo
 */
public class RedisConf {
    private static String HOST = "localhost";

    private static int PORT = 6379;
    
    public static int port() {
        return PORT;
    }
    
    public static String host () {
        return HOST;
    }
    
    public static enum RableDB {
        NEW_URL(1),
        URL_STAT(2),
        TEST(3);
        
        RableDB(int db) {
            this.db = db;
        }
        
        private int db = 0;
        
        public int db() {
            return db;
        }
        
        public BinaryJedis getJedis() {
            BinaryJedis j = new BinaryJedis(HOST, PORT);
            j.select(db);
            Logger.info("{RableDB.getJedis} got " + HOST + ":" + PORT + "/db-" + db);
            return j;
        }
    }
    
    public static void init(String confFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(confFile));
            String line = null;
            while (null != (line = br.readLine())) {
                if (line.charAt(0) == '#') {
                    continue;
                }
                String[] prop = line.split("=");
                if (prop.length != 2) {
                    continue;
                }
                setProp(prop[0], prop[1]);
            }
        } catch (Exception ex) {
            Logger.severe("{RedisConf.init} got exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param k
     * @param v
     */
    private static void setProp(String k, String v) {
        if (k.equals("redis.host")) {
            HOST = v;
            Logger.info("{RedisConf} got host: " + HOST);
        } else if (k.equals("redis.port")) {
            PORT = Integer.valueOf(v);
            Logger.info("{RedisConf} got port: " + PORT);
        }
    }
}

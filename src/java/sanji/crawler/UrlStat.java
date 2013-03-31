/**
 * @(#)UrlStat.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.crawler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sanji.base.Conf;
import sanji.utils.Utils;
import toolbox.misc.MD5;

/**
 * @author leo
 */
public class UrlStat implements Serializable {
    private static final long serialVersionUID = 1L;
    private String url;
    private String dish;
    private long code;
    private Stat stat;
    
    public UrlStat(String url, String dish) {
        this.url = url;
        this.dish = dish;
        this.stat = Stat.NEW;
        this.code = Math.abs(MD5.longDigest(url, Conf.DEFAULT_CHARSET));
    }
    
    public String url() {
        return url;
    }
    
    public String dish() {
        return dish;
    }
    
    public long code() {
        return code;
    }
    
    public Stat stat() {
        return stat;
    }
    
    public String localPath () {
        return localPath (dish, url, this.code);
    }
    
    public static String localPath(String dish, String url, long code) {
        String ext = Utils.getFileExtFromPath(url);
        if (ext == null) {
            ext = ".jpg";
        }
        return Conf.CRAWL_PATH + dish + "/" + dish + "-" + code + ext;
    }
    
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        url = is.readUTF();
        dish = is.readUTF();
        code = is.readLong();
        stat = Stat.fromCode(is.readInt());
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        os.writeUTF(url);
        os.writeUTF(dish);
        os.writeLong(code);
        os.writeInt(stat.code);
    }

    /**
     * @param stat
     */
    public void setStat(Stat stat) {
        this.stat = stat;
    }
    
    public String toString() {
        return "{dish:" + dish + ", url:" + url + ", stat: " + stat + ", code: " + code + "}";
    }
    public static enum Stat {
        NEW(0),
        CRAWLING(1),
        CRAWLED(2),
        FAILED(3);

        int code;

        private static Map<Integer, Stat> map = new HashMap<Integer, Stat>();
        static {
            for (Stat s: values()) {
                map.put(s.code, s);
            }
        }

        Stat(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
        
        public static Stat fromCode(int code) {
            return map.get(code);
        }
    }

}

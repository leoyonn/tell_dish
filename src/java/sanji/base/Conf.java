/**
 * @(#)Conf.java, 2012-10-18. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.base;

import sanji.rable.RedisConf;
import sanji.utils.SysUtils;

/**
 * configures of sanji.
 * @author leo
 */
public class Conf {
	public static String APP_HOME = System.getProperty("user.dir");
    /** define the weka attributes */
    public static String WEKA_DEFINE_FILE = APP_HOME + "/conf/weka_def.arff";
    
    public static String DISH_LIST_FILE = APP_HOME + "/conf/dishes.list";

    public static String DISH_ALL_LIST_FILE = APP_HOME + "/conf/dishes.all.list";

    public static String DATA_PATH = APP_HOME + "/data/";

    public static String CRAWL_PATH = APP_HOME + "/crawl/";

    /** splitter in file-name to file dish-name */
    public static final char DISH_NAME_SPLITTER = '-';

    /** if the file contain's no dish, set this */
    public static final String NULL_DISH_NAME = "啊哦不是菜吧";
    
    public static final String IMAGE_CACHE_PATH = "/disk1/liuyang/imgcache/";
    
    public static final String DEFAULT_CHARSET = "UTF-8";
    
    public static void init(String appHome) {
        SysUtils.addUserPath(path("so"));
    	APP_HOME = appHome;
        SysUtils.addUserPath(appHome + "/so"); // for jni load *.so
    	WEKA_DEFINE_FILE = APP_HOME + "/conf/weka_def.arff";
        DISH_LIST_FILE = APP_HOME + "/conf/dishes.list";
        DISH_ALL_LIST_FILE = APP_HOME + "/conf/dishes.all.list";
        DATA_PATH = APP_HOME + "/data/";
        CRAWL_PATH = APP_HOME + "/crawl/";
    	RedisConf.init(APP_HOME + "/conf/redis.conf");
    }

	public static String path(String file) {
		return APP_HOME + "/" + file;
	}
}

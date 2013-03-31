/**
 * @(#)AbstractTest.java, 2012-10-16. 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.base;

import org.junit.After;
import org.junit.Before;

import sanji.flow.Featurer;
import sanji.flow.Modeler;

/**
 * set user dir to ./unittest.data
 * @author leo
 */
public abstract class AbstractTest {
    protected static final String ROOT_DIR;
    protected static final String REDIS_HOST_UNITTEST = "localhost";
    protected static final int REDIS_PORT_UNITTEST = 60606;

    static {
        Featurer.init();
        Modeler.init();
        String userDir = System.getProperty("user.dir");
        if (!userDir.contains("unittest.data")) {
            userDir += "/unittest.data";
        }
        ROOT_DIR = userDir + "/";
        Conf.init(ROOT_DIR + "../");
    }

    @Before
    public void superInit() throws Exception {
        init();
    }

    public abstract void init() throws Exception;

    public abstract void after() throws Exception;

    @After
    public void superAfter() throws Exception {
        after();
    }
    
    public static String path(String file) {
        return ROOT_DIR + file;
    }
}

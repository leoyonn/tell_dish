/**
 * @(#)TestRable.java, 2012-11-8. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.rable;

import junit.framework.Assert;

import org.junit.Test;

import redis.clients.jedis.BinaryJedis;
import sanji.base.AbstractTest;

/**
 * @author leo
 */
public class TestRable extends AbstractTest {
    @Test
    public void testJodis() {
        BinaryJedis j = new BinaryJedis(REDIS_HOST_UNITTEST, REDIS_PORT_UNITTEST);
        Assert.assertEquals("OK", j.set("1".getBytes(), "2".getBytes()));
        Assert.assertEquals("2", new String(j.get("1".getBytes())));
        Assert.assertEquals("OK", j.flushAll());
        Assert.assertEquals("OK", j.quit());
    }

    @Override
    public void init() throws Exception {}

    @Override
    public void after() throws Exception {}
}

/**
 * @(#)Crawler.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.crawler;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import sanji.base.SanjiException;

/**
 * crawler
 * 
 * @author leo
 */
public abstract class Crawler {
    public static final String FAKE_UA = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.1"
            + " (KHTML, like Gecko) Chrome/13.0.782.218 Safari/535.1";

    protected long period = 1000;

    private ScheduledExecutorService sheluler = Executors.newSingleThreadScheduledExecutor();

    protected int cid = 0;
    
    /**
     * configure the crawler
     * @param cid       crawler id
     * @param period    period to next crawl
     * @param input     inputs to be crawled
     */
    public void config(int cid, long period, List<?> input) {
        this.cid = cid;
        this.period = period;
        config(input);
    }

    protected abstract boolean crawl();

    protected abstract void config(List<?> input);

    public void start() {
        sheluler.scheduleAtFixedRate(new Worker(), 0, period, TimeUnit.MILLISECONDS);
    }

    private class Worker implements Runnable {
        boolean stop = false;
        @Override
        public void run() {
            try {
                stop = !crawl();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // to stop
            if (stop) {
                throw new SanjiException("{Crawler." + cid + "} mission accomplished! congratulations!");
            }
        }
    }
}

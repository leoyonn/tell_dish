/**
 * @(#)CrawlerMaster.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.crawler;

import sanji.tools.ITool;

/**
 * @author leo
 */
public abstract class CrawlerMaster implements ITool {
    protected int workerNum = 1;
    protected long period = 10;
    protected Crawler[] crawlers = null;

    public boolean dispatch(int workerNum, long period) {
        this.workerNum = workerNum;
        this.period = period;
        crawlers = new Crawler[workerNum];
        return dispatch();
    }

    public void start() {
        for (Crawler crawler: crawlers) {
            crawler.start();
        }
    }

    public abstract boolean dispatch();
}

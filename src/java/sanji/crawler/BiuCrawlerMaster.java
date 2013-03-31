/**
 * @(#)BiuCrawlerMaster.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sanji.base.Conf;
import sanji.base.SanjiException;
import sanji.utils.Logger;
import toolbox.misc.MD5;

/**
 * baidu image url crawler maseter.
 * @author leo
 */
public class BiuCrawlerMaster extends CrawlerMaster {
    @Override
    public boolean dispatch() {
        File dishListFile = new File(Conf.DISH_ALL_LIST_FILE);
        if (!dishListFile.exists()) {
            dishListFile = new File (Conf.DISH_LIST_FILE);
        }
        BufferedReader reader = null;
        Map<Integer, List<String>> DISHES = new HashMap<Integer, List<String>>();
        try {
            reader = new BufferedReader(new FileReader(dishListFile));
            String dish = null;
            while (null != (dish = reader.readLine())) {
                int crawler = (int) (Math.abs(MD5.longDigest(dish, Conf.DEFAULT_CHARSET)) % workerNum);
                List<String> cdishes = DISHES.get(crawler);
                if (cdishes == null) {
                    cdishes = new ArrayList<String>();
                    DISHES.put(crawler, cdishes);
                }
                cdishes.add(dish);
            }
        } catch (IOException e) {
            Logger.severe("{BiuCrawlerMaster.dispatch} read [" + Conf.DISH_ALL_LIST_FILE + "] faield");
            e.printStackTrace();
            return false;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry<Integer, List<String>> entry: DISHES.entrySet()) {
            BiuCrawler c = new BiuCrawler();
            c.config(entry.getKey(), period, entry.getValue());
            crawlers[entry.getKey()] = c;
        }
        return true;
    }
    
    @Override
    public boolean exec(String[] args, PrintWriter out) throws SanjiException {
        if (args.length != 2) {
            usage(out);
            return false;
        }
        int nworker = Integer.valueOf(args[0]);
        long period = Long.valueOf(args[1]);
        dispatch(nworker, period);
        start();
        return true;
    }

    @Override
    public void usage(PrintWriter out) {
        out.println("Usage:   biucrawler <worker-number> <period>");
        out.println("      (run in sanji/, and put 'dishes.all.list' in sanji/conf");
    }
}

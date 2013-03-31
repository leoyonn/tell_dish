/**
 * @(#)ImageCrawlerMaster.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sanji.base.Conf;
import sanji.base.SanjiException;
import sanji.crawler.ImageCrawler.InputType;
import sanji.storage.NewUrlTable;
import sanji.utils.Logger;
import toolbox.misc.MD5;

/**
 * image crawler maseter.
 * @author leo
 */
public class ImageCrawlerMaster extends CrawlerMaster {
    public CrawlType crawlType = CrawlType.ALL;
    private String urlsPath;
    
    enum CrawlType {
        ALL,
        ONE,
    };
    
    private boolean dispatchAll() {
        File dishListFile = new File(Conf.DISH_ALL_LIST_FILE);
        if (!dishListFile.exists()) {
            dishListFile = new File(Conf.DISH_LIST_FILE);
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
            Logger.severe("{ImageCrawlerMaster.dispatch} read [" + Conf.DISH_ALL_LIST_FILE + "] faield");
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
            ImageCrawler c = new ImageCrawler();
            c.setInputType(InputType.REDIS);
            c.config(entry.getKey(), period, entry.getValue());
            crawlers[entry.getKey()] = c;
        }
        return true;
    }
    
    private boolean dispatchOne() {
        File urlFile = new File(urlsPath);
        BufferedReader reader = null;
        List<String> dishUrls = new ArrayList<String>();
        Map<Integer, List<String>> crawlerDishUrls = new HashMap<Integer, List<String>>();
        try {
            reader = new BufferedReader(new FileReader(urlFile));
            String dishUrl = null;
            while (null != (dishUrl = reader.readLine())) {
                dishUrls.add(dishUrl);
            }
            int size = (int) Math.ceil(dishUrls.size() / (double)workerNum);
            for (int i = 0; i < dishUrls.size(); i ++) {
                int crawler = i / size;
                List<String> cdishes = crawlerDishUrls.get(crawler);
                if (cdishes == null) {
                    cdishes = new ArrayList<String>();
                    crawlerDishUrls.put(crawler, cdishes);
                }
                cdishes.add(dishUrls.get(i));
            }
        } catch (IOException e) {
            Logger.severe("{ImageCrawlerMaster.dispatch} read [" + urlsPath + "] faield");
            e.printStackTrace();
            return false;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry<Integer, List<String>> entry: crawlerDishUrls.entrySet()) {
            ImageCrawler c = new ImageCrawler();
            c.setInputType(InputType.FILE);
            c.config(entry.getKey(), period, entry.getValue());
            crawlers[entry.getKey()] = c;
        }
        return true;
    }
    
    @Override
    public boolean dispatch() {
        switch (crawlType) {
            case ALL:
                return dispatchAll();
            case ONE:
                return dispatchOne();
        }
        return false;
    }

    private boolean resetUrls(String dishUrlsRootPath) {
        NewUrlTable urlTable = new NewUrlTable();
        File root = new File(dishUrlsRootPath);
        File[] dishUrls = root.listFiles();
        for (File f: dishUrls) {
            if (f.getName().startsWith("dish-urls-") && !f.getName().contains("raw")) {
                try {
                    addUrls(urlTable, f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
    
    private void addUrls(NewUrlTable urlTable, File dishUrlFile) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(dishUrlFile), Conf.DEFAULT_CHARSET));
        String line = null;
        while (null != (line = reader.readLine())) {
            String[] f = line.trim().split("\\s+");
            if (f.length != 3) {
                Logger.info("{ImageCrawlerMaster.setUrls} bad format: " + line);
                continue;
            }
            urlTable.add(f[0], f[2]);
            Logger.info("{ImageCrawlerMaster.setUrls} add: " + line);
        }
    }
    
    @Override
    public boolean exec(String[] args, PrintWriter out) throws SanjiException {
        if (args.length == 1) {
            return resetUrls(args[0]);
        };
        if (args.length == 3 && args[0].equals("-all")) {
            crawlType = CrawlType.ALL;
            int nworker = Integer.valueOf(args[1]);
            long period = Long.valueOf(args[2]);
            dispatch(nworker, period);
            start();
            return true;
        }
        if (args.length == 4 && args[0].equals("-one")) {
            crawlType = CrawlType.ONE;
            int nworker = Integer.valueOf(args[2]);
            long period = Long.valueOf(args[3]);
            urlsPath = args[1];
            dispatch(nworker, period);
            start();
            return true;
        }
        usage(out);
        return false;
    }

    @Override
    public void usage(PrintWriter out) {
        out.println("Usage: 1. imagecrawler -all <worker-number> <period>");
        out.println("      (run in sanji/, and put 'dishes.all.list' in sanji/conf");
        out.println(" 2. imagecrawler -one <dish-urls-file-path> <worker-number> <period>");
        out.println("      (crawl images in <dish-urls-file-path> to <dish-images-root-path>");
        out.println(" 3. reseturls <dish-urls-root-path>");
        out.println("      (reset dishs' urls for crawl");
    }
}

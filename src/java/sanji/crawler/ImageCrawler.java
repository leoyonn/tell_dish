/**
 * @(#)ImgeCrawler.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import sanji.base.Conf;
import sanji.base.SanjiException;
import sanji.crawler.UrlStat.Stat;
import sanji.image.utils.ImageUtils;
import sanji.storage.NewUrlTable;
import sanji.storage.UrlStatTable;
import sanji.utils.Logger;

/**
 * image crawler
 * @author leo
 */
public class ImageCrawler extends Crawler {
    private InputType inputType = InputType.REDIS;
    
    private List<?> input;

    private Iterator<?> urlIterator = null;

    private NewUrlTable urlTable = new NewUrlTable();

    private UrlStatTable statTable = new UrlStatTable();
    
    public void setInputType(InputType type) {
        this.inputType = type;
    }
    
    @Override
    public boolean crawl() {
        if (!urlIterator.hasNext()) {
            Logger.info("{ImageCrawler.crawl." + cid + "} mission accomplished! no images to save any more...");
            return false;
        }

        String url = (String) urlIterator.next();
        UrlStat stat = null;
        switch (inputType) {
            case REDIS:
                stat = new UrlStat(url, ((IteratorR) urlIterator).dish());
                break;
            case FILE:
                stat = ((IteratorF) urlIterator).stat();
                break;
        }
        stat.setStat(Stat.CRAWLING);
        boolean success = false;
        try {
            success = ImageUtils.loadAndSave(url, stat.localPath(), 5);
        } catch (IOException ex) {
            Logger.warning("{ImageCrawler.crawl." + cid + "} save [" + stat + "] failed: " + ex.getMessage());
        }
        if (success) {
            stat.setStat(Stat.CRAWLED);
        } else {
            stat.setStat(Stat.FAILED);
        }
        statTable.add(stat);
        Logger.info("{ImageCrawler.crawl." + cid + "} got " + stat);
        return true;
    }

    /**
     * should set {@link #inputType} before call config.
     * if {@link #inputType} is {@link InputType#FILE}:
     *   input should be line of: dish   idx   url,
     *   pair will be: <url, dish>
     */
    @Override
    protected void config(List<?> input) {
        if (inputType == InputType.REDIS) {
            this.input = input;
            urlIterator = new IteratorR();
        } else if (inputType == InputType.FILE) {
            List<UrlStat> urlDishes = new ArrayList<UrlStat>();
            for (Object line: input) {
                String[]pair = ((String)line).split("\\s+");
                if (pair.length != 3) {
                    Logger.warning("{ImageCrawler} invalid line: " + line);
                    continue;
                }
                UrlStat urlDish = new UrlStat(pair[2], pair[0]);
                urlDishes.add(urlDish);
            }
            this.input = urlDishes;
            urlIterator = new IteratorF();
        }
    }

    private class IteratorR implements java.util.Iterator<String> {
        private int dishIdx = 0;
        private String url = null;

        public IteratorR() {
            new File(Conf.CRAWL_PATH + input.get(dishIdx)).mkdirs();
        }
        
        @Override
        public String next() {
            return url;
        }

        @Override
        public boolean hasNext() {
            url = urlTable.popUrl((String) input.get(dishIdx));
            if (url == null) {
                dishIdx ++;
                if (dishIdx >= input.size()) {
                    return false;
                }
                File file = new File(Conf.CRAWL_PATH + input.get(dishIdx));
                file.mkdirs();
                return hasNext();
            }
            UrlStat stat = statTable.get(url);
            if (stat != null && stat.stat() != Stat.NEW) {
                return hasNext();
            }
            if (stat == null) {
                stat = new UrlStat(url, (String) input.get(dishIdx));
            }
            if (new File(stat.localPath()).exists()) {
                return hasNext();
            }
            return true;
        }

        @Override
        public void remove() {
            throw new SanjiException("Not implemented.");
        }

        public String dish() {
            return (String) input.get(dishIdx);
        }
    };
    
    private class IteratorF implements java.util.Iterator<String> {
        private int urlIdx = -1;
        private String url = null;

        public IteratorF() {}
        
        public UrlStat stat() {
            return ((UrlStat) input.get(urlIdx));
        }

        @Override
        public String next() {
            return url;
        }

        @Override
        public boolean hasNext() {
            urlIdx ++;
            if (urlIdx >= input.size()) {
                return false;
            }
            UrlStat stat = (UrlStat) input.get(urlIdx);
            url = stat.url();
            File file = new File(Conf.CRAWL_PATH + stat.dish());
            if (!file.exists()) {
                file.mkdirs();
            }
            if (new File(stat.localPath()).exists()) {
                Logger.info("{ImageCrawler." + cid + "} already crawled, ignore: " + stat);
                return hasNext();
            }
            return true;
        }

        @Override
        public void remove() {
            throw new SanjiException("Not implemented.");
        }
    };
    
    public static enum InputType {
        FILE,
        REDIS,
    };

    @SuppressWarnings("serial")
    public static void main(String[]args) throws JSONException, IOException {
        ImageCrawler c = new ImageCrawler();
        c.config(new ArrayList<String>(){{add("麻辣香锅");}});
        while(c.crawl() == true) ;
        Conf.init(".");
        new File(Conf.CRAWL_PATH + "麻辣").mkdirs();
    }
}

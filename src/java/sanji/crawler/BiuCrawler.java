/**
 * @(#)BiuCrawler.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.crawler;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sanji.base.Conf;
import sanji.base.SanjiException;
import sanji.utils.Logger;

/**
 * baidu image url crawler.
 * 
 * @author leo
 */
public class BiuCrawler extends Crawler {
    private static final String QUERY_URL = "http://image.baidu.com/i?tn=baiduimagejson"
            + "&ct=201326592&cl=2&lm=-1&st=-1&fm=&fr=&sf=1&fmq=1352199039094_R"
            + "&pv=&ic=0&nc=1&z=&se=1&showtab=0&fb=0&width=&height=&face=0&istype=2"
            + "&ie=utf-8&oe=utf-8&rn=%d&171501879773.19495&1287771110186.968"
            + "&word=%s&pn=%d"; // %(PER_PAGE, query, start)

    private static URL url(String query, int start) throws IOException {
        return new URL(String.format(QUERY_URL, PER_PAGE, query, start));
    }
    
    private static final int PER_PAGE = 60;

    private static final int PER_DISH = PER_PAGE * 20;

    private List<String> dishes;

    private StringBuilder buffer = new StringBuilder();

    private Iterator urlIterator = new Iterator();
    
    private OutputStreamWriter urlWriter;

    private OutputStreamWriter rawWriter;

    private boolean done = false;
    
    @Override
    public boolean crawl() {
        if (done) {
            Logger.info("{BiuCrawler.crawl." + cid + "} no results any more...");
            return false;
        } else if (!urlIterator.hasNext()) {
            Logger.info("{BiuCrawler.crawl." + cid + "} no results any more! now save results...");
            try {
                urlWriter.write("==============all done==============\n");
                rawWriter.write("==============all done==============\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    urlWriter.close();
                    rawWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            done = true;
            return false;
        }
                
        URL url = urlIterator.next();
        if (url == null) {
            urlIterator.setLastCount(0);
            return true;
        }
        int count = get(url);
        urlIterator.setLastCount(count);
        Logger.info("{BiuCrawler.crawl." + cid + "} got [" + count + "] urls of[" + urlIterator.state() + "]...");
        return true;
    }
    
    private int get(URL url) {
        BufferedReader reader = null;
        try {
            url.openConnection().addRequestProperty("User-Agent", FAKE_UA);
            InputStream is = url.openStream();
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            buffer.setLength(0);
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.warning("{BiuCrawler.crawl." + cid + "} got [" + e.getMessage() + "] when crawling [" + url + "]...");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject json = null;
        try {
            json = new JSONObject(buffer.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parse(json);
    }

    private int parse(JSONObject json) {
        if (json == null) {
            return 0;
        }
        JSONArray datas = null;
        try {
            datas = json.getJSONArray("data");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        if (datas == null) {
            return 0;
        }
        for (int i = 0; i < datas.length(); i++) {
            try {
                JSONObject data = datas.getJSONObject(i);
                String url = data.optString("objURL");
                if (url != null && (url = url.trim()).length() > 0) {
                    try {
                        urlWriter.write(urlIterator.dish() + "\t" + (urlIterator.pageIdx + i) + "\t" + url.trim() + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return i;
            }
        }
        try {
            urlWriter.flush();
            rawWriter.write(json.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datas.length() - 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void config(List<?> input) {
        dishes = (List<String>) input;
        try {
            urlWriter = new OutputStreamWriter(new FileOutputStream(
                    Conf.CRAWL_PATH + "dish-urls-" + cid + ".dat", true), Conf.DEFAULT_CHARSET);
            rawWriter = new OutputStreamWriter(new FileOutputStream(
                    Conf.CRAWL_PATH + "dish-urls-raw-" + cid + ".dat", true), Conf.DEFAULT_CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Iterator implements java.util.Iterator<URL> {
        private int dishIdx = -1;
        private int pageIdx = 0;
        private int lastCount = -1;

        @Override
        public URL next() {
            try {
                return url(dishes.get(dishIdx), pageIdx);
            } catch (IOException e) {
                Logger.warning("{BiuCrawler.crawl." + cid + "} got [" + e.getMessage()
                        + "] when crawling [" + dishes.get(dishIdx) + "] page:" + pageIdx);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public boolean hasNext() {
            if (lastCount == -1) {
                lastCount = 0;
                dishIdx = 0;
                pageIdx = 0;
                return true;
            } else if (lastCount == 0 || pageIdx + lastCount > PER_DISH) {
                dishIdx++;
                pageIdx = 0;
                if (dishIdx >= dishes.size()) {
                    return false;
                }
            } else {
                pageIdx += lastCount;
            }
            return true;
        }

        @Override
        public void remove() {
            throw new SanjiException("Not implemented.");
        }

        public void setLastCount(int count) {
            this.lastCount = count;
        }
        
        public String state() {
            return "{dish:" + dishes.get(dishIdx) + ", pageIdx:" + pageIdx
                    + ", lastCount:" + lastCount + "}";
        }
        
        public String dish() {
            return dishes.get(dishIdx);
        }
    };

    @SuppressWarnings("serial")
    public static void main(String[]args) throws JSONException, IOException {
        BiuCrawler c = new BiuCrawler();
        c.config(new ArrayList<String>(){{add("麻辣香锅");}});
        while(c.crawl() == true) ;
    }
}

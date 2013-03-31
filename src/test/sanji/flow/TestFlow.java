/**
 * @(#)TestFlow.java, 2012-10-16. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.flow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import sanji.base.AbstractTest;
import sanji.base.TestResult;
import sanji.image.base.Image;
import sanji.image.feature.Gist;
import sanji.image.feature.GistJ;
import sanji.image.feature.Hist;
import sanji.image.utils.Display;
import sanji.image.utils.ImageUtils;
import sanji.utils.SysUtils;
import weka.classifiers.trees.J48;
import weka.core.Instance;

/**
 * @author leo
 */
public class TestFlow extends AbstractTest {
    private static final String TRAIN_IMAGE_PATH = path("train");

    private static final String TRAIN_WEKA_PATH = path("train.arff");

    private static final String TEST_IMAGE_PATH = path("test.jpg");

    @Test
    public void testFeaturer() throws Exception {
        // test extract
        Featurer.extract(TRAIN_IMAGE_PATH, TRAIN_WEKA_PATH);
        BufferedReader br = new BufferedReader(new FileReader(TRAIN_WEKA_PATH));
        String line = null;
        String aline = null;
        while (null != (line = br.readLine())) {
            if (!line.startsWith("%") && !line.startsWith("@") && line.contains(",")) {
                String[] f = line.split(",");
                double[] features = new double[f.length - 1];
                for (int i = 0; i < features.length; i++) {
                    features[i] = Double.valueOf(f[i]);
                }
                String name = f[f.length - 1].split(" % ")[1];
                Display.showHist(features, null, null, name, 200);
                if (aline == null) {
                    aline = line;
                }
            }
        }
        br.close();

        // test use attributes to new weka instance.
        String[] features = aline.split(" % ")[0].split(",");
        Instance instance = Featurer.newWekaInstance((Object[]) features);
        Assert.assertEquals(features.length, instance.numAttributes());
        Object[] features2 = new Object[features.length];
        for (int i = 0; i < features.length; i++) {
            try {
                features2[i] = Double.valueOf((String) features[i]).doubleValue();
            } catch (NumberFormatException e) {
                features2[i] = features[i];
            }
        }
        instance = Featurer.newWekaInstance((Object[]) features2);
        Assert.assertEquals(features.length, instance.numAttributes());
        System.out.println(instance);
        
        // test use relation to new weka instance.
        instance = Featurer.newWekaInstance(aline);
        Assert.assertEquals(features.length, instance.numAttributes());
        Assert.assertEquals(instance.classIndex(), Featurer.RELATION.classIndex());
        Assert.assertEquals(instance.classIndex(), Featurer.RELATION.numAttributes() - 1);
        instance.setClassMissing();
        Assert.assertTrue(instance.classIsMissing());
    }
    
    @Test
    public void testModel() throws Exception {
        Modeler.train(TRAIN_WEKA_PATH, path("j48.model"));
        J48 j48 = (J48) Modeler.loadModel(path("j48.model"));

        File imageFile = new File(TEST_IMAGE_PATH);
        String feature = Featurer.extract(imageFile);
        System.out.println(feature);
        Instance instance = Featurer.newWekaInstance(feature);
        System.out.println(instance);
        // test sanji.flow.Model
        for (String l:Featurer.LABELS) {
            System.out.print(l + ",");
        }
        TestResult res = Modeler.test(instance, j48);
        System.out.println("\n" + res);
    }
    
    @Test
    public void testTestResult() {
        TestResult res = new TestResult();
        String[] classes = {
            "扒鸡", "道口烧鸡", "功夫鱼", "宫保鸡丁", "龙井虾仁", "清蒸武昌鱼", "酥油茶", "竹筒饭"
        };
        double[] values = {0.1, 0.2, 0.03, 0.04, 0.05, 0.4, 0.08, 0.25};
        res.build(classes, values);
        System.out.println(res);
        System.out.println(res.echo());
    }

    @Test
    public void testGist() throws IOException {
        // test cgist
        SysUtils.addUserPath(System.getProperty("user.dir") + "../so");
        double[] gists = Gist.extract(new int[]{1,2,3,4,5,6,7,8,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,}, 8, 8);
        System.out.println(gists.length);
        for (int i = 0; i < gists.length; i ++) {
            System.out.print(String.format("%6.4f%s", gists[i], ((i + 1) % 10 == 0) ? "\n" : " "));
        }
        System.out.println();
        // checked, same as matlab version.
        Image image = new Image(path("train/啊哦这不是菜吧3.bmp"));
        gists = Gist.extract(image.pixels(), image.width(), image.height());
        System.out.println(gists.length);
        for (int i = 0; i < gists.length; i ++) {
            System.out.print(String.format("%6.4f%s", gists[i], ((i + 1) % 10 == 0) ? "\n" : " "));
        }

        // test jgist
        Image image1 = new Image(path("train/啊哦这不是菜吧3.bmp"));
        Image image2 = image1.scale(8, 8);
        ImageUtils.save(image2.data(), path("tmp.bmp"), 1.0f);
        Display.showImage(path("tmp.bmp"), 100);
        long time = System.currentTimeMillis();
        double[] gabor = GistJ.gist(new Image(path("tmp.bmp")), null);
        System.out.println("gabors: all " + gabor.length + " gabor features");
        for(int i = 0; i < gabor.length; i++) {
            System.out.print(gabor[i] + ",");
        }
        System.out.println("\n" + (System.currentTimeMillis() - time) / 1000.0 + " seconds elapsed.");
    }
    
    @Test
    public void testHist() throws IOException {
      // create an image
      int[] pixels = new int[] {
              0xFF0000, 0xFF0000, 0x000000,
              0xFF0080, 0xFF0080, 0x000080,
              0xFF00FF, 0xFF00FF, 0x0000FF,
      };
      ImageUtils.create(pixels, 3, 3, path("tmp.bmp"));
      Display.showImage("../tmp.bmp", 1000);
      Image image = new Image(path("tmp.bmp"));
      // get it's rgb-hist
      Display.showHist(Hist.rgbHist(image, 9), null, null, null, 100);
      Display.showHist(Hist.rgbHist(image, 100), null, null, null, 100);
      // get it's hsv-hist
      for (int i = 0; i < pixels.length; i ++) {
          float [] hsv = ImageUtils.rgb2hsv(pixels[i]);
          System.out.println(String.format("h:%.2f, s:%.2f, v:%.2f; ", hsv[0], hsv[1], hsv[2]));
      }
      Display.showHist(Hist.hsvHist(image, 30, 5, 5), null, null, null, 100);
      System.out.println();
      image = new Image(path("train/啊哦这不是菜吧3.bmp"));
      // get it's rgb-hist
      Display.showHist(Hist.rgbHist(image, 9), null, null, null, 100);
      Display.showHist(Hist.rgbHist(image, 100), null, null, null, 100);
      Display.showHist(Hist.hsvHist(image, 10, 10, 10), null, null, null, 100);
    }
    
    @Override
    public void init() throws Exception {}

    @Override
    public void after() throws Exception {}
}

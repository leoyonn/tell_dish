/**
 * @(#)TestUtils.java, 2012-10-16. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.utils;

import junit.framework.Assert;

import org.junit.Test;

import sanji.base.AbstractTest;
import sanji.image.base.Image;
import sanji.image.utils.Display;
import sanji.image.utils.ImageUtils;

/**
 * @author leo
 */
public class TestUtils extends AbstractTest {
    @Test
    public void testUtils() {
        Assert.assertEquals(Utils.getDishFromFileName("扒鸡-p0.1.jpg"), "扒鸡");
        Assert.assertEquals(Utils.getFileNameFromPath("./扒鸡-p0.1.jpg"), "扒鸡-p0.1.jpg");
        Assert.assertEquals(Utils.getFileNameFromPath(".\\扒鸡-p0.1.jpg"), "扒鸡-p0.1.jpg");
        Assert.assertEquals(Utils.getFileNameFromPath("./.\\./扒鸡-p0.1.jpg"), "扒鸡-p0.1.jpg");
        Assert.assertEquals(Utils.getFileNameFromPath("/home/leo/扒鸡-p0.1.jpg"), "扒鸡-p0.1.jpg");
        Assert.assertEquals(Utils.getFileNameFromPath("C:\\home\\leo\\扒鸡-p0.1.jpg"), "扒鸡-p0.1.jpg");
    }
    
    @Test
    public void testImageUtils() {
        Assert.assertTrue(ImageUtils.seemsLikeAnImage("扒鸡-p0.1.jpg"));
        Assert.assertTrue(ImageUtils.seemsLikeAnImage("扒鸡-p0.1.jpeg"));
        Assert.assertTrue(ImageUtils.seemsLikeAnImage("扒鸡-p0.1.png.png"));
        Assert.assertFalse(ImageUtils.seemsLikeAnImage("扒鸡-p0.1"));
        Assert.assertFalse(ImageUtils.seemsLikeAnImage("扒鸡-p0.1.png.png.1"));
        
        Display.showImage("http://oimagea1.ydstatic.com/image?product=silmaril&id=8071939097325179589", 1000);
        int rgb = 0xFF1188;
        System.out.println(ImageUtils.rgbString(rgb));
        int r = Image.getR(rgb), g = Image.getG(rgb), b = Image.getB(rgb);
        Assert.assertEquals(0xFF, r);
        Assert.assertEquals(0x11, g);
        Assert.assertEquals(0x88, b);
        Assert.assertEquals(rgb, Image.rgb(r, g, b));

        float[] hsv = ImageUtils.rgb2hsv(0xFF0000);
        System.out.println(String.format("h: %.2f, s: %.2f, v: %.2f", hsv[0], hsv[1],hsv[2]));
        hsv = ImageUtils.rgb2hsv(0x000000);
        System.out.println(ImageUtils.rgbString(ImageUtils.hsv2rgb(hsv)));
        System.out.println(String.format("h: %.2f, s: %.2f, v: %.2f", hsv[0], hsv[1],hsv[2]));
        hsv = ImageUtils.rgb2hsv(0x800000);
        System.out.println(ImageUtils.rgbString(ImageUtils.hsv2rgb(hsv)));
        System.out.println(String.format("h: %.2f, s: %.2f, v: %.2f", hsv[0], hsv[1],hsv[2]));
        hsv = ImageUtils.rgb2hsv(0x00FF00);
        System.out.println(ImageUtils.rgbString(ImageUtils.hsv2rgb(hsv)));
        System.out.println(String.format("h: %.2f, s: %.2f, v: %.2f", hsv[0], hsv[1],hsv[2]));
        hsv = ImageUtils.rgb2hsv(0x404080);
        System.out.println(ImageUtils.rgbString(ImageUtils.hsv2rgb(hsv)));
        System.out.println(String.format("h: %.2f, s: %.2f, v: %.2f", hsv[0], hsv[1],hsv[2]));
        int size = 256;
        int []pixels = new int[size * size];
        for (int y = 0; y < size; y ++) {
            for (int x = 0; x < size; x ++) {
                pixels[y * size + x] = (((y / 20) * 20) << 16) + (x / 20) * 20;
            }
        }
        ImageUtils.create(pixels, size, size, path("tmp.bmp"));
        Display.showImage(path("tmp.bmp"), 1000);
    }

    @Test
    public void testDisplay() {
        Display.showHist(new double[] { 100.0, 200.0, 300.0}, null, null, "直方图", 300);
        Display.showHist(new double[] { 1.0, 20.0, 30.0}, null, null, "直方图", 300);
        Display.showHist(new double[] { .1, .02, .003}, null, null, "直方图", 400);
        Display.showHist(new double[] {
            0.0769, 0.0725, 0.2289, 0.0943, 0.0330, 0.0514, 0.0175, 0.0159, 0.0159, 0.0078, 0.0063, 0.0158, 0.0246,
            0.0248, 0.0084, 0.0460, 0.0164, 0.0261, 0.0440, 0.0221, 0.0214, 0.0120, 0.0054, 0.0061, 0.0036, 0.0099,
            0.0117, 0.0157, 0.0201, 0.0455, 0.5149, 0.1616, 0.1413, 0.0900, 0.0923, 0.1561, 0.1334, 0.2183, 0.2679,
            0.2243, 0.1925, 0.1992, 0.3404, 0.2679, 0.2771, 0.2390, 0.2514, 0.2324, 0.3723, 0.2028, 0.2010, 0.2238,
        }, null, null, "直方图", 400);
    }
    
    @Test
    public void test() {
        System.out.println(System.getProperty("user.dir"));
    }

    @Override
    public void init() throws Exception {}

    @Override
    public void after() throws Exception {}
}

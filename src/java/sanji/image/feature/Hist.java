/**
 * @(#)Hist.java, 2012-10-15. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.image.feature;

import java.io.IOException;

import sanji.image.base.Image;
import sanji.image.utils.ImageUtils;
import sanji.math.MathUtils;

/**
 * hist of Image, such as rgb-hist, hsv-hist
 * 
 * @author leo
 */
public class Hist {
    /**
     * compute the rgb-hist of an image
     * 
     * @param image
     *            the {@link sanji.image.base#Image} to be compute on.
     * @param binNum
     *            the number of hist's bin, will be round to 3's integral
     *            multiple, such as 30, 36, etc.
     * @return
     */
    public static double[] rgbHist(Image image, int binNum) {
        binNum = (int)Math.ceil(binNum / 3.0) * 3;
        double[] hist = new double[binNum];
        int step = (int) Math.ceil(256.0 / (binNum / 3.0));
        int rOffset = 0, gOffset = binNum / 3, bOffset = binNum / 3 * 2;
        for (int y = 0; y < image.height(); y++) {
            for (int x = 0; x < image.width(); x++) {
                int rgb = image.pixel(x, y);
                hist[Image.getR(rgb) / step + rOffset]++;
                hist[Image.getG(rgb) / step + gOffset]++;
                hist[Image.getB(rgb) / step + bOffset]++;
            }
        }
        double norm = 1 / (image.width() * (double)image.height());
        for (int i = 0; i < hist.length; i ++) {
            hist[i] *= norm; 
        }
        return hist;
    }

    /**
     * compute the hsv-hist of an image.
     * 
     * @param image
     *            the {@link sanji.image.base#Image} to be compute on.
     * @param hNum
     *            the number of hue-hist's bin
     * @param sNum
     *            the number of saturation-hist's bin
     * @param vNum
     *            the number of value(lightness)-hist's bin
     * @return
     */
    public static double[] hsvHist(Image image, int hNum, int sNum, int vNum) {
        double[] hist = new double[hNum + sNum + vNum];
        int hOffset = 0, sOffset = hNum, vOffset = hNum + sNum;
        for (int y = 0; y < image.height(); y++) {
            for (int x = 0; x < image.width(); x++) {
                int rgb = image.pixel(x, y);
                float[] hsv = ImageUtils.rgb2hsv(rgb);
                int bh = (int) (hsv[0] * hNum) + hOffset;
                int bs = (int) (hsv[1] * sNum) + sOffset;
                int bv = (int) (hsv[2] * vNum) + vOffset;
                hist[MathUtils.clamp(bh, hOffset, hOffset + hNum - 1)]++;
                hist[MathUtils.clamp(bs, sOffset, sOffset + sNum - 1)]++;
                hist[MathUtils.clamp(bv, vOffset, vOffset + vNum - 1)]++;
            }
        }
        double norm = 1 / (image.width() * (double)image.height());
        for (int i = 0; i < hist.length; i ++) {
            hist[i] *= norm; 
        }
        return hist;
    }

    public static void main(String[] args) throws IOException {
    }
}

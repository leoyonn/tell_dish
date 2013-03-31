/**
 * @(#)Gist.java, 2012-10-29. 
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.image.feature;

import sanji.image.base.Image;
import sanji.math.Complex;
import sanji.math.FFT;
import sanji.math.Gabor;

/**
 * gist of image, java version
 * @see http://people.csail.mit.edu/torralba/code/spatialenvelope/
 * (Modeling the shape of the scene: a holistic representation of the spatial envelope锛�
 * International Journal of Computer Vision, Vol. 42(3): 145-175, 2001.)
 * 
 * @author leo
 */
public class GistJ {
    public static class GistParam {
        /** image's width */
        private int width = DEFAULT_WIDTH;
        /** image's height */
        private int height = DEFAULT_HEIGHT;
        /** boundaryExtension, number of pixels to pad */
        private int bound = DEFAULT_BOUND; 
        /** orientationsPerScale */
        private int []orient = DEFAULT_ORIENT;
        /** fc prefilt */
        private int fcPrefilt = 4;
        /** nunber of blocks */
        private int nblock = 4;
        /** the gabor filters */
        private double[][] gabor = null;

        protected static final int DEFAULT_BOUND = 4;
        protected static final int DEFAULT_WIDTH = 8;
        protected static final int DEFAULT_HEIGHT = 8;

        protected static final int[] DEFAULT_ORIENT = new int[] { 4, 4, 4, 4};

        public static final GistParam DEFAULT = new GistParam(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_ORIENT, 4, 4);

        /**
         * constructor
         * 
         * @param width
         * @param height
         * @param orient
         * @param nblock
         * @param fcPrefilt
         */
        public GistParam(int width, int height, int orient[], int nblock, int fcPrefilt) {
            this.width = width;
            this.height = height;
            this.orient = orient == null ? DEFAULT_ORIENT : orient;
            this.nblock = nblock;
            this.fcPrefilt = fcPrefilt;
            this.gabor = Gabor.createFilters(orient, width + 2 * DEFAULT_BOUND, height + 2 * DEFAULT_BOUND);
        }
    }
    
    public static double[] gist(Image image, GistParam param) {
        // 1. check param
        if (param == null) {
            param = GistParam.DEFAULT;
        } else {
            if (param.width <= 0 || param.height <= 0) {
                param.width = image.width();
                param.height = image.height();
            }
            if (param.gabor == null) {
                param.gabor = Gabor.createFilters(param.orient,
                        param.width + 2 * param.bound, param.height + 2 * param.bound);
            }
        }
        if (image.height() != param.height || image.width() != param.width) {
            // throw new SanjiException("image's width and height not match with param...");
        }

        prefilter(image, param.fcPrefilt);
        

        return Gabor.gabor(image.pixels(), image.width(), image.height(), param.nblock, param.gabor);  
    }

    /**
     * prefiltering: local contrast scaling
     * @param image
     * @param fc cycles per image
     */
    protected static void prefilter(Image image, int fc) {
        if (fc <= 0) {
            fc = 4;
        }
        int x, y;
        // 1. log image:
        for(y = 0; y < image.height(); y++) {
            for(x = 0; x < image.width(); x++) {
                int pixel = image.pixel(x, y);
                int r = Image.getR(pixel), g = Image.getG(pixel), b = Image.getB(pixel);
                pixel = Image.rgb(  (int) Math.log(r + 1.0),
                                    (int) Math.log(g + 1.0),
                                    (int) Math.log(b + 1.0));
                image.setPixel(pixel, x, y);
            }
        }
        // 2. Pad images to reduce boundary artifacts
        // final int PAD_SIZE = 5;
        // int []pad = MathUtils.padding(image.pixels(), image.width(),
        // image.height(), PAD_SIZE);
        int[] pad = image.pixels(); 
        int width = image.width();// + 2 * PAD_SIZE;
        int height = image.height();// + 2 * PAD_SIZE;
        int size = width * height;

        // 3. Build whitening filter
        double[] gfc = new double[size];
        Complex[][] in = new Complex[3][size];
        Complex[][] out = new Complex[3][];
        Complex[][] iout = new Complex[3][];
        double s1 = fc / Math.sqrt(Math.log(2)), s1p = s1 * s1;
        for(y = 0; y < height; y++) {
            for (x = 0; x < width; x ++) {
                int idx = y * width + x;
                in[0][idx] = new Complex(pad[idx], .0);
                in[1][idx] = new Complex(pad[idx], .0);
                in[2][idx] = new Complex(pad[idx], .0);
                double fx = x - width / 2.0;
                double fy = y - height / 2.0;
                gfc[idx] = Math.exp(-(fx * fx + fy * fy) / s1p);
            }
        }


        // 4. FFT
        FFT.fftShift(gfc, width, height);
        // TODO use fftw
        out[0] = FFT.Slow.fft(in[0]);
        out[1] = FFT.Slow.fft(in[1]);
        out[2] = FFT.Slow.fft(in[2]);

        // 5. Apply whitening filter
        for(y = 0; y < height; y++) {
            for (x = 0; x < width; x ++) {
                int idx = y * width + x;
                out[0][idx] = out[0][idx].times(gfc[idx]);
                out[1][idx] = out[1][idx].times(gfc[idx]);
                out[2][idx] = out[2][idx].times(gfc[idx]);
                System.out.println(String.format("(%.2f, %.2f), (%.2f, %.2f), (%.2f, %.2f),",
                        out[0][idx].re(), out[0][idx].im(),
                        out[1][idx].re(), out[1][idx].im(),
                        out[2][idx].re(), out[2][idx].im()));
            }
        }

        // 6. IFFT
        iout[0] = FFT.Slow.ifft(out[0]);
        iout[1] = FFT.Slow.fft(out[1]);
        iout[2] = FFT.Slow.fft(out[2]);

        // 7. Local contrast normalisation
        for(y = 0; y < height; y++) {
            for (x = 0; x < width; x ++) {
                int idx = y * width + x;
                int pixel = pad[idx];
                int r = Image.getR(pixel), g = Image.getG(pixel), b = Image.getB(pixel);
                r -= iout[0][idx].re() / size;
                g -= iout[1][idx].re() / size;
                b -= iout[2][idx].re() / size;
                double mean = (r + g + b) / 3.0;
                in[0][idx] = new Complex(mean * mean, .0);
            }
        }

        // FFT
        out[0] = FFT.Slow.fft(in[0]);

        // Apply contrast normalisation filter
        for(y = 0; y < height; y++) {
            for (x = 0; x < width; x ++) {
                int idx = y * width + x;
                out[0][idx] = out[0][idx].times(gfc[idx]);
            }
        }

        // IFFT
        iout[0] = FFT.Slow.ifft(out[0]);

        // Get result from contrast normalisation filter
        for(y = 0; y < height; y++) {
            for (x = 0; x < width; x ++) {
                int idx = y * width + x;
                double val = Math.sqrt(iout[0][idx].abs() / size);
                int pixel = pad[idx];
                int r = Image.getR(pixel), g = Image.getG(pixel), b = Image.getB(pixel);
                r /= (0.2 + val);
                g /= (0.2 + val);
                b /= (0.2 + val);
                pad[idx] = Image.rgb(r, g, b);
            }
        }
        // MathUtils.removePadding(pad, image.pixels(), image.width(), image.height(), PAD_SIZE);
    }
}
